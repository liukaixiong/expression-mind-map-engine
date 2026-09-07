package com.liukx.expression.engine;

import cn.hutool.core.date.DateUtil;
import com.liukx.expression.engine.core.utils.LocalDistributedLock;
import com.liukx.expression.engine.server.config.props.ExpressionServerProperties;
import com.liukx.expression.engine.server.enums.TableSplitRule;
import com.liukx.expression.engine.server.manager.MysqlTableManager;
import com.liukx.expression.engine.server.mapper.TableArchiveMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 自愈恢复的行为门控测试：
 * 正常重启（上一周期归档分表存在）必须零数据扫描；
 * 判定跨周期宕机（分表缺失）才执行补建 + 区间迁移。
 *
 * @author liukx
 */
class MysqlTableManagerSelfHealGateTest {

    private static final String BASE = "expression_trace_log_info";

    private MysqlTableManager manager;
    private TableArchiveMapper mapper;

    @BeforeEach
    void setUp() throws Exception {
        manager = new MysqlTableManager();
        mapper = mock(TableArchiveMapper.class);

        ExpressionServerProperties properties = new ExpressionServerProperties();
        ExpressionServerProperties.TableRule rule = new ExpressionServerProperties.TableRule();
        rule.setTableName(BASE);
        rule.setTableSplitRule(TableSplitRule.day);
        properties.setTableRuleList(Collections.singletonList(rule));

        // 本地锁直接执行动作,等价单机部署路径
        setField(manager, "distributedLock", new LocalDistributedLock());
        setField(manager, "tableArchiveMapper", mapper);
        setField(manager, "expressionServerProperties", properties);
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
                // 继续向父类找
            }
        }
        throw new NoSuchFieldException(name);
    }

    /** 上一周期归档分表存在（零点轮转执行过）→ 不得触碰任何数据行 */
    @Test
    void previousShardExists_noDataScan() {
        final String previousShard = TableSplitRule.day.getFullTableName(BASE, -1);
        when(mapper.showTables(previousShard)).thenReturn(Collections.singletonList(previousShard));

        manager.selfHealMissedArchive();

        verify(mapper, never()).selectDistinctPeriodSuffixes(anyString(), anyString(), any(Date.class));
        verify(mapper, never()).insertIgnoreRange(anyString(), anyString(), any(Date.class), any(Date.class));
        verify(mapper, never()).deleteRange(anyString(), any(Date.class), any(Date.class));
    }

    /** 上一周期归档分表缺失（跨周期宕机）→ 补建分表并按周期区间迁移滞留数据 */
    @Test
    void previousShardMissing_recoversStrandedPeriods() {
        when(mapper.showTables(anyString())).thenReturn(Collections.emptyList());
        when(mapper.selectDistinctPeriodSuffixes(anyString(), anyString(), any(Date.class)))
                .thenReturn(Collections.singletonList("260831"));

        manager.selfHealMissedArchive();

        // 260831 滞留行迁回 info_260831，区间为 [08-31 00:00, 09-01 00:00)
        ArgumentCaptor<Date> startCaptor = ArgumentCaptor.forClass(Date.class);
        ArgumentCaptor<Date> endCaptor = ArgumentCaptor.forClass(Date.class);
        verify(mapper, times(1)).createTableLike(BASE + "_260831", BASE);
        verify(mapper, times(1)).insertIgnoreRange(eq(BASE + "_260831"), eq(BASE), startCaptor.capture(), endCaptor.capture());
        verify(mapper, times(1)).deleteRange(eq(BASE), startCaptor.capture(), endCaptor.capture());

        assertEquals(DateUtil.parseDateTime("2026-08-31 00:00:00"), startCaptor.getAllValues().get(0));
        assertEquals(DateUtil.parseDateTime("2026-09-01 00:00:00"), endCaptor.getAllValues().get(0));
        assertEquals(DateUtil.parseDateTime("2026-08-31 00:00:00"), startCaptor.getAllValues().get(1));
        assertEquals(DateUtil.parseDateTime("2026-09-01 00:00:00"), endCaptor.getAllValues().get(1));
    }

    /** 判定需恢复但基准表无滞留数据（如首次部署）→ 只做判定扫描，不建表不迁移 */
    @Test
    void noStrandedRows_createsNothing() {
        when(mapper.showTables(anyString())).thenReturn(Collections.emptyList());
        when(mapper.selectDistinctPeriodSuffixes(anyString(), anyString(), any(Date.class)))
                .thenReturn(Collections.<String>emptyList());

        manager.selfHealMissedArchive();

        verify(mapper, never()).createTableLike(anyString(), anyString());
        verify(mapper, never()).deleteRange(anyString(), any(Date.class), any(Date.class));
    }

    private static String eq(String value) {
        return org.mockito.ArgumentMatchers.eq(value);
    }
}
