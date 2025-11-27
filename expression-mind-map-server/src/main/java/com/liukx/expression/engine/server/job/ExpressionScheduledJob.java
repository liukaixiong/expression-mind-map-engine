package com.liukx.expression.engine.server.job;

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

import java.util.Set;

/**
 * 处理统一的定时任务逻辑
 * 测试: @Scheduled(cron = "0/5 * * * * ? ")
 */
@Slf4j
@Component
@EnableScheduling
public class ExpressionScheduledJob {
    private Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private MysqlTableManager manager;

    /**
     * 每月1日凌晨0点执行表归档任务
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void executeMonthlyTableArchive() {
        logger.info("开始执行表归档任务...");
//        // todo 如果存在集群部署的话，需要考虑分布式锁，同一时刻只能有一个执行
        final Set<ExpressionServerProperties.TableRule> tableNameList = manager.getTableNameList(TableSplitRule.month);

        for (ExpressionServerProperties.TableRule tableInfo : tableNameList) {
            // 执行原子性表轮转操作
            final String tableName = tableInfo.getTableName();
            final String tableArchiveName = manager.tableArchiveByMonth(tableName);
            if (StringUtils.isNotEmpty(tableArchiveName)) {
                logger.info("归档表成功:{}", tableArchiveName);
            }
            // 同时清理掉过期的归档表
            manager.clearExpiredTableName(tableName);
//        }
        }
    }
}
