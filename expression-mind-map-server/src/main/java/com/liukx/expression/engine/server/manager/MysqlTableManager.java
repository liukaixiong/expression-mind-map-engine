package com.liukx.expression.engine.server.manager;

import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
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

    private Map<String, TableSplitRule> tableRuleMap;

    private final Set<String> currentTableNameCache = new HashSet<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        this.tableRuleMap = expressionServerProperties.getTableRuleList().stream().collect(Collectors.toMap(ExpressionServerProperties.TableRule::getTableName, ExpressionServerProperties.TableRule::getTableSplitRule));
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
