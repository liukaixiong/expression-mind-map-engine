package com.liukx.expression.engine.server.enums;

import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.function.Function;

/**
 * @author liukaixiong
 * @date 2025/11/26 - 15:44
 */
public enum TableSplitRule {

    /**
     * 月纬度
     */
    month(
            num -> DateUtil.format(DateUtil.offsetMonth(DateUtil.date(), num), "yyMM"),
            date -> DateUtil.format(date, "yyMM")
    ),

    /**
     * 天纬度
     */
    day(
            num -> DateUtil.format(DateUtil.offsetDay(DateUtil.date(), num), "yyMMdd"),
            date -> DateUtil.format(date, "yyMMdd")
    ),

    ;

    /** 按今日偏移算后缀（写入/归档轮转用） */
    private final Function<Integer, String> offsetRule;

    /** 按绝对日期算后缀（按 created 路由分表用） */
    private final Function<Date, String> dateRule;

    TableSplitRule(Function<Integer, String> offsetRule, Function<Date, String> dateRule) {
        this.offsetRule = offsetRule;
        this.dateRule = dateRule;
    }

    /**
     * 按今日偏移算完整表名（写入/归档轮转用）。
     */
    public String getFullTableName(String tableName, Integer offsetNumber) {
        return tableName + "_" + this.offsetRule.apply(offsetNumber);
    }

    /**
     * 按绝对日期算完整表名（按 created 路由分表用）。
     *
     * @param tableName 基准表名
     * @param date      记录日期（如 created）
     * @return 带日期后缀的物理表名（month → tableName_yyMM；day → tableName_yyMMdd）
     */
    public String getFullTableNameByDate(String tableName, Date date) {
        return tableName + "_" + this.dateRule.apply(date);
    }

    /**
     * 对应的 MySQL DATE_FORMAT 格式（SQL 侧按周期分组用，输出后缀与
     * {@link #getFullTableNameByDate} 的 Java 格式保持一致）。
     */
    public String getMysqlDateFormat() {
        return this == month ? "%y%m" : "%y%m%d";
    }

    /**
     * 当前活跃周期的起点（day → 今日 00:00；month → 本月 1 号 00:00）。
     * 归档语义下基准表只应存放当前周期的数据。
     */
    public Date beginOfCurrentPeriod() {
        return this == month ? DateUtil.beginOfMonth(DateUtil.date()) : DateUtil.beginOfDay(DateUtil.date());
    }
}
