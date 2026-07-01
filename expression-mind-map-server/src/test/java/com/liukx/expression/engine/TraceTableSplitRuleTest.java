package com.liukx.expression.engine;

import cn.hutool.core.date.DateUtil;
import com.liukx.expression.engine.server.enums.TableSplitRule;
import com.liukx.expression.engine.server.manager.MysqlTableManager;
import org.junit.Assert;
import org.junit.Test;

import java.util.Date;

/**
 * 拆表规则按日期建表名的纯逻辑测试（不依赖 Spring 容器）。
 *
 * @author liukx
 */
public class TraceTableSplitRuleTest {

    @Test
    public void monthRule_byDate_returns_yyMM_suffix() {
        Date date = DateUtil.parseDateTime("2026-07-01 10:00:00");
        String name = TableSplitRule.month.getFullTableNameByDate("expression_trace_log_index", date);
        Assert.assertEquals("expression_trace_log_index_2607", name);
    }

    @Test
    public void dayRule_byDate_returns_yyMMdd_suffix() {
        Date date = DateUtil.parseDateTime("2026-07-01 10:00:00");
        String name = TableSplitRule.day.getFullTableNameByDate("expression_trace_log_info", date);
        Assert.assertEquals("expression_trace_log_info_260701", name);
    }

    @Test
    public void dayRule_byDate_crossYear() {
        Date date = DateUtil.parseDateTime("2025-12-31 23:59:59");
        Assert.assertEquals("expression_trace_log_info_251231",
                TableSplitRule.day.getFullTableNameByDate("expression_trace_log_info", date));
    }

    @Test
    public void monthRule_byDate_crossYear() {
        Date date = DateUtil.parseDateTime("2025-12-15 00:00:00");
        Assert.assertEquals("expression_trace_log_index_2512",
                TableSplitRule.month.getFullTableNameByDate("expression_trace_log_index", date));
    }

    // ============ 当前活跃周期 vs 历史周期 表名决策（resolveTableName）============

    private static final String INFO_TABLE = "expression_trace_log_info";
    private static final String INDEX_TABLE = "expression_trace_log_index";

    /** 当天数据尚未归档，应查源表（基准表名），而不是尚未生成的 info_yyMMdd。 */
    @Test
    public void resolveTableName_day_currentPeriod_returnsBaseTable() {
        Date now = DateUtil.parseDateTime("2026-07-01 10:00:00");
        Date today = DateUtil.parseDateTime("2026-07-01 23:59:59");
        Assert.assertEquals(INFO_TABLE,
                MysqlTableManager.resolveTableName(INFO_TABLE, TableSplitRule.day, today, now));
    }

    /** 昨天数据已被归档，应查 info_yyMMdd 分表。 */
    @Test
    public void resolveTableName_day_pastPeriod_returnsSplitTable() {
        Date now = DateUtil.parseDateTime("2026-07-01 10:00:00");
        Date yesterday = DateUtil.parseDateTime("2026-06-30 23:59:59");
        Assert.assertEquals("expression_trace_log_info_260630",
                MysqlTableManager.resolveTableName(INFO_TABLE, TableSplitRule.day, yesterday, now));
    }

    /** 本月数据尚未归档，应查源表。 */
    @Test
    public void resolveTableName_month_currentPeriod_returnsBaseTable() {
        Date now = DateUtil.parseDateTime("2026-07-15 10:00:00");
        Date thisMonth = DateUtil.parseDateTime("2026-07-01 00:00:00");
        Assert.assertEquals(INDEX_TABLE,
                MysqlTableManager.resolveTableName(INDEX_TABLE, TableSplitRule.month, thisMonth, now));
    }

    /** 上月数据已被归档，应查 index_yyMM 分表。 */
    @Test
    public void resolveTableName_month_pastPeriod_returnsSplitTable() {
        Date now = DateUtil.parseDateTime("2026-07-01 00:00:01");
        Date lastMonth = DateUtil.parseDateTime("2026-06-30 23:59:59");
        Assert.assertEquals("expression_trace_log_index_2606",
                MysqlTableManager.resolveTableName(INDEX_TABLE, TableSplitRule.month, lastMonth, now));
    }

    /** day 规则跨天边界：now 为次日 00:00:01，昨天 23:59:59 应判定为历史周期。 */
    @Test
    public void resolveTableName_day_midnightBoundary() {
        Date now = DateUtil.parseDateTime("2026-07-01 00:00:01");
        Date yesterday = DateUtil.parseDateTime("2026-06-30 23:59:59");
        Assert.assertEquals("expression_trace_log_info_260630",
                MysqlTableManager.resolveTableName(INFO_TABLE, TableSplitRule.day, yesterday, now));
    }
}
