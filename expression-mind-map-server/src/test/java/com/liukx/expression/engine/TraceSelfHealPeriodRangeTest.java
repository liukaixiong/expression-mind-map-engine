package com.liukx.expression.engine;

import cn.hutool.core.date.DateUtil;
import com.liukx.expression.engine.server.enums.TableSplitRule;
import com.liukx.expression.engine.server.manager.MysqlTableManager;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 启动自愈（G8）纯逻辑单测：滞留周期后缀 → 时间区间还原、
 * MySQL DATE_FORMAT 后缀与 Java 侧表名后缀一致性。
 *
 * @author liukx
 */
class TraceSelfHealPeriodRangeTest {

    // ============ periodRange：后缀 → [起点, 终点) ============

    @Test
    void daySuffix_toDayRange() {
        Date[] range = MysqlTableManager.periodRange("260901", TableSplitRule.day);
        assertEquals(DateUtil.parseDateTime("2026-09-01 00:00:00"), range[0]);
        assertEquals(DateUtil.parseDateTime("2026-09-02 00:00:00"), range[1]);
    }

    @Test
    void daySuffix_crossMonthBoundary() {
        Date[] range = MysqlTableManager.periodRange("260831", TableSplitRule.day);
        assertEquals(DateUtil.parseDateTime("2026-08-31 00:00:00"), range[0]);
        assertEquals(DateUtil.parseDateTime("2026-09-01 00:00:00"), range[1]);
    }

    @Test
    void monthSuffix_toMonthRange() {
        Date[] range = MysqlTableManager.periodRange("2609", TableSplitRule.month);
        assertEquals(DateUtil.parseDateTime("2026-09-01 00:00:00"), range[0]);
        assertEquals(DateUtil.parseDateTime("2026-10-01 00:00:00"), range[1]);
    }

    @Test
    void monthSuffix_crossYearBoundary() {
        Date[] range = MysqlTableManager.periodRange("2512", TableSplitRule.month);
        assertEquals(DateUtil.parseDateTime("2025-12-01 00:00:00"), range[0]);
        assertEquals(DateUtil.parseDateTime("2026-01-01 00:00:00"), range[1]);
    }

    @Test
    void invalidSuffix_returnsNull() {
        assertNull(MysqlTableManager.periodRange("garbage", TableSplitRule.day));
        assertNull(MysqlTableManager.periodRange(null, TableSplitRule.day));
        assertNull(MysqlTableManager.periodRange("2609", null));
    }

    // ============ MySQL 后缀格式与 Java 表名后缀一致性 ============

    @Test
    void mysqlDateFormat_day_matchesJavaSuffix() {
        // Java 侧 yyMMdd 与 SQL 侧 %y%m%d 输出必须逐字符一致，否则自愈会算错分表名
        Date date = DateUtil.parseDateTime("2026-09-01 10:00:00");
        String javaSuffix = DateUtil.format(date, "yyMMdd");
        String tableName = TableSplitRule.day.getFullTableNameByDate("expression_trace_log_info", date);
        assertEquals("expression_trace_log_info_" + javaSuffix, tableName);
        assertEquals("%y%m%d", TableSplitRule.day.getMysqlDateFormat());
        assertEquals("%y%m", TableSplitRule.month.getMysqlDateFormat());
        assertEquals("expression_trace_log_index_" + DateUtil.format(date, "yyMM"),
                TableSplitRule.month.getFullTableNameByDate("expression_trace_log_index", date));
    }

    @Test
    void beginOfCurrentPeriod_isStartOfPeriod() {
        Date dayStart = TableSplitRule.day.beginOfCurrentPeriod();
        assertNotNull(dayStart);
        assertTrue(!dayStart.after(new Date()), "周期起点不应晚于当前时间");

        Date monthStart = TableSplitRule.month.beginOfCurrentPeriod();
        assertEquals(DateUtil.parseDateTime(
                DateUtil.format(new Date(), "yyyy-MM") + "-01 00:00:00"), monthStart);
    }
}
