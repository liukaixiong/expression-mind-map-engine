package com.liukx.expression.engine.server.enums;

import cn.hutool.core.date.DateUtil;

import java.util.function.Function;

/**
 * @author liukaixiong
 * @date 2025/11/26 - 15:44
 */
public enum TableSplitRule {

    /**
     * 月纬度
     */
    month(num -> DateUtil.format(DateUtil.offsetMonth(DateUtil.date(), num), "yyMM"));


    private final Function<Integer, String> rule;

    TableSplitRule(Function<Integer, String> rule) {
        this.rule = rule;
    }


    public String getFullTableName(String tableName, Integer offsetNumber) {
        return tableName + "_" + this.rule.apply(offsetNumber);
    }
}
