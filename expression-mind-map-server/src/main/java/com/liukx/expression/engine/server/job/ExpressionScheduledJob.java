package com.liukx.expression.engine.server.job;

import com.liukx.expression.engine.core.utils.DistributedLock;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.enums.TableSplitRule;
import com.liukx.expression.engine.server.manager.MysqlTableManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Set;

/**
 * 处理统一的定时任务逻辑
 * 测试: @Scheduled(cron = "0/5 * * * * ? ")
 * <p>
 * 集群部署下由 {@link DistributedLock}（逻辑键
 * {@link MysqlTableManager#TABLE_ROTATION_LOCK_KEY}）保证同一时刻只有一个实例执行轮转，
 * 未获取到锁的实例直接跳过（抢到锁的实例会完成同样的工作）。
 */
@Slf4j
@Component
@EnableScheduling
public class ExpressionScheduledJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private MysqlTableManager manager;
    @Autowired
    private DistributedLock distributedLock;

    /**
     * 每月1日凌晨0点执行表归档任务
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void executeMonthlyTableArchive() {
        distributedLock.runWithLock(MysqlTableManager.TABLE_ROTATION_LOCK_KEY, "每月表归档",
                Duration.ofMinutes(5), Duration.ZERO, () -> archiveTables(TableSplitRule.month, "每月"));
    }

    /**
     * 每天凌晨 0 点执行表归档任务（处理按天拆分的表，如 expression_trace_log_info）
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void executeDailyTableArchive() {
        distributedLock.runWithLock(MysqlTableManager.TABLE_ROTATION_LOCK_KEY, "每日表归档",
                Duration.ofMinutes(5), Duration.ZERO, () -> archiveTables(TableSplitRule.day, "每日"));
    }

    /**
     * 逐表执行轮转与过期清理；单表失败不影响其余表（集群并发下的败者会抛
     * RENAME 冲突异常，属于预期噪音，不应中断整轮）。
     */
    private void archiveTables(TableSplitRule splitRule, String label) {
        logger.info("开始执行{}表归档任务...", label);
        final Set<ExpressionServerProperties.TableRule> tableNameList = manager.getTableNameList(splitRule);

        for (ExpressionServerProperties.TableRule tableInfo : tableNameList) {
            final String tableName = tableInfo.getTableName();
            try {
                // 执行原子性表轮转操作
                final String tableArchiveName = manager.tableArchive(tableName);
                if (StringUtils.isNotEmpty(tableArchiveName)) {
                    logger.info("{}归档表成功:{}", label, tableArchiveName);
                }
                // 同时清理掉过期的归档表
                manager.clearExpiredTableName(tableName);
            } catch (Exception e) {
                logger.error("表{}{}归档失败,继续处理其余表", tableName, label, e);
            }
        }
    }
}
