package com.liukx.expression.engine.server.manager;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import cn.hutool.core.date.DateUtil;
import com.liukx.expression.engine.core.utils.DistributedLock;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.enums.TableSplitRule;
import com.liukx.expression.engine.server.mapper.TableArchiveMapper;
import com.liukx.expression.engine.server.mapper.entity.BaseTableEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 表管理
 *
 * @author liukaixiong
 * @date 2025/11/26 - 11:46
 */
@Component
public class MysqlTableManager implements InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private TableArchiveMapper tableArchiveMapper;

    @Autowired
    private ExpressionServerProperties expressionServerProperties;

    @Autowired
    private DistributedLock distributedLock;

    /** 表轮转/自愈共用的分布式锁逻辑名（两者操作同一批表，必须串行） */
    public static final String TABLE_ROTATION_LOCK_KEY = "table-rotation";
    private Map<String, TableSplitRule> tableRuleMap;

    private final Set<String> currentTableNameCache = new HashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        this.tableRuleMap = expressionServerProperties.getTableRuleList().stream().collect(Collectors.toMap(ExpressionServerProperties.TableRule::getTableName, ExpressionServerProperties.TableRule::getTableSplitRule));
        // 启动自愈：补上宕机期间错过的归档轮转（失败不影响启动）
        try {
            selfHealMissedArchive();
        } catch (Exception e) {
            logger.error("分表自愈迁移执行失败,不影响服务启动", e);
        }
    }

    /**
     * 启动自愈：进程若在周期切换点（每日/每月 0 点的归档定时任务时刻）宕机，
     * 该次轮转不会补跑，基准表会滞留上一周期已写入的数据；下一次轮转再把整个
     * 基准表 RENAME 走，滞留数据会进错分表、按 created 将永远查不到。
     * <p>
     * 集群部署下由 {@link DistributedLock}（有 Redis 时为跨进程实现）保证同一时刻只有
     * 一个实例执行；等锁 3 秒未获取则视为其它实例正在恢复，跳过（迁移本身幂等）。
     * <p>
     * 是否需要恢复先用元数据级判定（见 {@link #healMissedArchive}），正常重启
     * 不产生任何数据页扫描；仅判定发生过跨周期宕机时，才把基准表中当前周期
     * 之前的数据按周期迁回各自的归档分表：
     * <ul>
     *   <li>分表缺失则先 CREATE TABLE LIKE 补建；</li>
     *   <li>INSERT IGNORE + DELETE 区间迁移，中断重跑幂等（主键保持不变）；</li>
     *   <li>只处理当前周期之前的区间，与在线写入（永远写当前周期）互不冲突。</li>
     * </ul>
     */
    public void selfHealMissedArchive() {
        distributedLock.runWithLock(TABLE_ROTATION_LOCK_KEY, "分表自愈迁移", Duration.ofMinutes(10), Duration.ofSeconds(3),
                this::doSelfHealMissedArchive);
    }

    private void doSelfHealMissedArchive() {
        for (ExpressionServerProperties.TableRule rule : expressionServerProperties.getTableRuleList()) {
            if (rule == null || rule.getTableName() == null || rule.getTableSplitRule() == null) {
                continue;
            }
            try {
                healMissedArchive(rule);
            } catch (Exception e) {
                logger.error("表{}自愈迁移失败", rule.getTableName(), e);
            }
        }
    }

    private void healMissedArchive(ExpressionServerProperties.TableRule rule) {
        final String baseTableName = rule.getTableName();
        final TableSplitRule splitRule = rule.getTableSplitRule();
        // 元数据级快速判定，正常重启零数据扫描：上一周期的归档分表由周期切换点的
        // 轮转任务 RENAME 产生。它存在 ⟹ 切换点进程在线、轮转已执行，基准表不可能
        // 滞留历史周期数据；不存在 ⟹ 切换点进程不在线（如宕机跨天），才值得做数据迁移。
        final String expectedArchiveTable = splitRule.getFullTableName(baseTableName, -1);
        if (checkTableExist(expectedArchiveTable)) {
            return;
        }
        logger.info("自愈:表{}缺失上一周期归档分表{},判定发生过跨周期宕机,开始恢复", baseTableName, expectedArchiveTable);

        final Date periodStart = splitRule.beginOfCurrentPeriod();
        final List<String> suffixes = tableArchiveMapper.selectDistinctPeriodSuffixes(
                baseTableName, splitRule.getMysqlDateFormat(), periodStart);
        if (suffixes == null || suffixes.isEmpty()) {
            return;
        }
        logger.info("自愈:表{}基准表检测到{}个滞留历史周期,开始迁移", baseTableName, suffixes.size());
        for (String suffix : suffixes) {
            final Date[] range = periodRange(suffix, splitRule);
            if (range == null) {
                logger.warn("自愈:表{}无法解析滞留周期后缀{}，跳过", baseTableName, suffix);
                continue;
            }
            final String shardTable = baseTableName + "_" + suffix;
            if (!checkTableExist(shardTable)) {
                tableArchiveMapper.createTableLike(shardTable, baseTableName);
                logger.info("自愈:补建缺失分表{}", shardTable);
            }
            tableArchiveMapper.insertIgnoreRange(shardTable, baseTableName, range[0], range[1]);
            final int deleted = tableArchiveMapper.deleteRange(baseTableName, range[0], range[1]);
            logger.info("自愈:表{}周期{}滞留数据已迁移至{}共{}条", baseTableName, suffix, shardTable, deleted);
        }
    }

    /**
     * 纯逻辑（不依赖 Spring/DB，便于单测）：把分表后缀还原成该周期的
     * [起点, 终点) 时间区间。day → yyMMdd；month → yyMM。
     *
     * @return 区间数组；后缀非法返回 null
     */
    public static Date[] periodRange(String suffix, TableSplitRule splitRule) {
        if (suffix == null || splitRule == null) {
            return null;
        }
        try {
            if (splitRule == TableSplitRule.day) {
                final Date start = DateUtil.parse(suffix, "yyMMdd");
                return new Date[]{start, DateUtil.offsetDay(start, 1)};
            }
            final Date start = DateUtil.parse(suffix, "yyMM");
            return new Date[]{start, DateUtil.offsetMonth(start, 1)};
        } catch (Exception e) {
            return null;
        }
    }

    public Set<ExpressionServerProperties.TableRule> getTableNameList(TableSplitRule tableSplitRule) {
        return expressionServerProperties.getTableRuleList().stream().filter(var -> Objects.equals(var.getTableSplitRule(), tableSplitRule)).collect(Collectors.toSet());
    }

    public ExpressionServerProperties.TableRule getTableConfigInfo(String tableName) {
        return expressionServerProperties.getTableRuleList().stream().filter(var -> Objects.equals(var.getTableName(), tableName)).findFirst().orElse(null);
    }

    /**
     * 原子性表轮转操作（规则无关：按各表配置的 tableSplitRule 计算归档表名）。
     *
     * @param tableName 源表名
     * @return 归档表名
     */
    @Transactional
    public String tableArchive(String tableName) {
        String newTable = getTableName(tableName, -1);

        if (checkTableExist(newTable)) {
            logger.warn("数据库表名已存在:{} , 不做归档处理!", newTable);
            return null;
        }

        String tmpTableName = newTable + "_tmp";
        // 创建表: CREATE TABLE IF NOT EXISTS tmpTableName LIKE tableName;
        tableArchiveMapper.createTableLike(tmpTableName, tableName);
        // 截断表: RENAME TABLE tableName TO newTable;
        tableArchiveMapper.renameTable(tableName, newTable);
        // 新表替代原表: RENAME TABLE tmpTableName TO tableName;
        tableArchiveMapper.renameTable(tmpTableName, tableName);
        return newTable;
    }

    /**
     * 清除指定表名中已过期的数据的方法
     *
     * @param tableName 表名
     */
    public void clearExpiredTableName(String tableName) {
        final TableSplitRule tableSplitRule = tableRuleMap.get(tableName);
        if (tableSplitRule != null) {

            final ExpressionServerProperties.TableRule tableConfigInfo = getTableConfigInfo(tableName);
            final Integer maxSaveNumber = tableConfigInfo.getMaxSaveNumber();
            if (maxSaveNumber != null) {
                final String fullTableName = tableSplitRule.getFullTableName(tableName, -maxSaveNumber - 1);

                if (checkTableExist(fullTableName)) {
                    tableArchiveMapper.dropTable(fullTableName);
                    logger.info("已删除过期表:{}", fullTableName);
                } else {
                    logger.info("过期的表不存在:{} , 不做处理!", fullTableName);
                }
            }
        }
    }

    public String getLastTableNameList(Class<? extends BaseTableEntity> entityClass, Integer offsetNumber) {
        final String entityTableName = TableInfoHelper.getTableInfo(entityClass).getTableName();
        final String tableName = getTableName(entityTableName, offsetNumber);

        if (!checkTableExist(tableName)) {
            logger.debug("数据库表名不存在:{}", tableName);
            return null;
        }

        return tableName;
    }

    /**
     * 按绝对日期计算某实体对应配置的分表物理表名，并校验表是否真实存在。
     * 用于按 created 精确路由查询（详情查询）。
     * <p>
     * 归档语义：写入永远落到源表（基准表名），定时任务在周期切换时
     * （day 规则每天 0 点、month 规则每月 1 号）才把源表 RENAME 为上一周期的归档表，
     * 再新建空源表。因此：
     * <ul>
     *   <li>date 落在当前活跃周期（今天 / 本月）→ 数据仍在源表，直接返回基准表名；</li>
     *   <li>date 落在历史周期 → 数据已被归档到 {@code baseTableName_yyMMdd(yyMM)} 分表，
     *       按日期定位并校验存在性，不存在（被清理）返回 null。</li>
     * </ul>
     * 若不区分活跃周期，当前周期的数据会因归档表尚未生成（如今天的 info_260701 要到
     * 明天 0 点归档后才出现）而查不到。
     *
     * @param entityClass MyBatis-Plus 实体类（@TableName 解析基准表名）
     * @param date        记录的 created 时间；为空返回 null
     * @return 物理表名；表不存在（已被清理或尚未建）返回 null
     */
    public String getTableNameByDate(Class<? extends BaseTableEntity> entityClass, Date date) {
        if (date == null) {
            return null;
        }
        final String baseTableName = TableInfoHelper.getTableInfo(entityClass).getTableName();
        final ExpressionServerProperties.TableRule rule = getTableConfigInfo(baseTableName);
        if (rule == null) {
            return baseTableName;
        }
        final String resolved = resolveTableName(baseTableName, rule.getTableSplitRule(), date, new Date());
        if (resolved == null) {
            return null;
        }
        return checkTableExist(resolved) ? resolved : null;
    }

    /**
     * 纯逻辑决策（不依赖 Spring/DB，便于单测）：
     * <ul>
     *   <li>date 落在当前活跃周期（与 now 同一周期）→ 返回基准表名（数据尚未归档，仍在源表）；</li>
     *   <li>date 落在历史周期 → 返回按 date 计算的归档分表名。</li>
     * </ul>
     * 仅返回"应当查的表名"，不校验表是否存在；存在性由调用方负责。
     */
    public static String resolveTableName(String baseTableName, TableSplitRule splitRule, Date date, Date now) {
        final String dateTableName = splitRule.getFullTableNameByDate(baseTableName, date);
        final String currentPeriodTableName = splitRule.getFullTableNameByDate(baseTableName, now);
        // date 在当前活跃周期，数据尚未归档，仍在源表
        if (dateTableName.equals(currentPeriodTableName)) {
            return baseTableName;
        }
        return dateTableName;
    }

    public boolean checkTableExist(String tableName) {
        if (currentTableNameCache.contains(tableName)) {
            return true;
        }

        final List<String> tableList = tableArchiveMapper.showTables(tableName);

        if (!tableList.isEmpty()) {
            currentTableNameCache.add(tableName);
            return true;
        }

        return false;
    }

    private String getTableName(String entityTableName, Integer offsetNumber) {
        final TableSplitRule tableSplitRule = tableRuleMap.get(entityTableName);
        return tableSplitRule.getFullTableName(entityTableName, offsetNumber);
    }

}
