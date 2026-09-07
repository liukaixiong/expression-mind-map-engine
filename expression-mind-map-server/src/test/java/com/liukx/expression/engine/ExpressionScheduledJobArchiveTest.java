package com.liukx.expression.engine;

import com.liukx.expression.engine.core.utils.DistributedLock;
import com.liukx.expression.engine.core.utils.LocalDistributedLock;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.enums.TableSplitRule;
import com.liukx.expression.engine.server.job.ExpressionScheduledJob;
import com.liukx.expression.engine.server.manager.MysqlTableManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 归档定时任务测试：集群锁接线 + 单表失败不中断其余表的处理。
 *
 * @author liukx
 */
class ExpressionScheduledJobArchiveTest {

    private ExpressionScheduledJob job;
    private MysqlTableManager manager;

    @BeforeEach
    void setUp() throws Exception {
        job = new ExpressionScheduledJob();
        manager = mock(MysqlTableManager.class);
        setField(job, "manager", manager);
        // 本地锁直跑动作,聚焦任务本身的行为
        setField(job, "distributedLock", new LocalDistributedLock());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = findField(target.getClass(), name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Field findField(Class<?> type, String name) throws NoSuchFieldException {
        for (Class<?> c = type; c != null; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignore) {
                // 向父类继续
            }
        }
        throw new NoSuchFieldException(name);
    }

    private static ExpressionServerProperties.TableRule rule(String tableName) {
        ExpressionServerProperties.TableRule rule = new ExpressionServerProperties.TableRule();
        rule.setTableName(tableName);
        rule.setTableSplitRule(TableSplitRule.day);
        return rule;
    }

    /** 集群下败者实例的典型场景：第一张表 RENAME 冲突抛异常，其余表必须继续处理 */
    @Test
    void singleTableFailure_doesNotAbortRemainingTables() {
        Set<ExpressionServerProperties.TableRule> rules = new HashSet<>();
        rules.add(rule("expression_trace_log_info"));
        rules.add(rule("expression_trace_log_index_daily"));
        when(manager.getTableNameList(TableSplitRule.day)).thenReturn(rules);
        when(manager.tableArchive("expression_trace_log_info"))
                .thenThrow(new RuntimeException("Duplicate key name 'expression_trace_log_info_260902'"));

        job.executeDailyTableArchive();

        // 其余表的轮转与过期清理必须继续；失败表自身的清理随之跳过（同表维护动作，次日再试）
        verify(manager).tableArchive("expression_trace_log_index_daily");
        verify(manager).clearExpiredTableName("expression_trace_log_index_daily");
        verify(manager, org.mockito.Mockito.never()).clearExpiredTableName("expression_trace_log_info");
    }

    /** 空规则列表安全通过 */
    @Test
    void emptyRuleList_noop() {
        when(manager.getTableNameList(TableSplitRule.month)).thenReturn(Collections.emptySet());
        job.executeMonthlyTableArchive();
        verify(manager, org.mockito.Mockito.never()).tableArchive(anyString());
    }

    /** 定时任务必须经 DistributedLock 以共享逻辑键执行（集群互斥接线） */
    @Test
    void archiveJob_runsUnderDistributedLockWithSharedKey() throws Exception {
        final DistributedLock lock = mock(DistributedLock.class);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(4)).run();
            return null;
        }).when(lock).runWithLock(anyString(), anyString(), any(Duration.class), any(Duration.class), any(Runnable.class));
        setField(job, "distributedLock", lock);
        when(manager.getTableNameList(TableSplitRule.day)).thenReturn(Collections.emptySet());

        job.executeDailyTableArchive();

        verify(lock).runWithLock(eq(MysqlTableManager.TABLE_ROTATION_LOCK_KEY), eq("每日表归档"),
                any(Duration.class), any(Duration.class), any(Runnable.class));
    }
}
