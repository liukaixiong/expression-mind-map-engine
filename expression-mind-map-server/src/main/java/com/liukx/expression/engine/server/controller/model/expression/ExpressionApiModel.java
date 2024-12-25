package com.liukx.expression.engine.server.controller.model.expression;

/**
 * @author liukaixiong
 * @date : 2022/6/27 - 13:21
 */
//@Tag(name = "表达式api相关")
//@Data
public class ExpressionApiModel {

    //    @Schema(description = "表达式内容", requiredMode = Schema.RequiredMode.REQUIRED)
//    @NotEmpty
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
