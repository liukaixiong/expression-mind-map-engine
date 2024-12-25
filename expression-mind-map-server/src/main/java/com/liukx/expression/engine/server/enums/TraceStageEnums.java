package com.liukx.expression.engine.server.enums;

public enum TraceStageEnums {
    START("开始阶段"),
    ACTION("行为阶段"),
    CONDITION("条件比较"),
    TRIGGER("执行阶段"),
    CALLBACK("回调阶段"),
    VARIABLE_PARSE("变量解析"),
    FUNCTION_PARSE("函数解析"),
    START_MATCH("开始匹配"),
    END_MATCH("结束阶段"),
    ERROR("异常阶段"),
    SUCCESS("执行成功");

    private String describe;

    TraceStageEnums(String describe) {
        this.describe = describe;
    }
}
