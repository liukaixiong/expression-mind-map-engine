package com.liukx.expression.engine.core.enums;

public enum ExpressionTypeEnum {
    CONDITION("condition", "条件表达式"),
    CALLBACK("callback", "回调型表达式"),
    TRIGGER("trigger", "触发型表达式"),
    ACTION("action", "行为表达式"),
    ;
    private final String code;
    private final String description;

    ExpressionTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
