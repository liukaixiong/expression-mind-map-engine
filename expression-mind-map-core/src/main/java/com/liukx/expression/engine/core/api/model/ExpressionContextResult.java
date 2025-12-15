package com.liukx.expression.engine.core.api.model;

import java.util.List;

/**
 * 表达式关键字信息
 *
 * @author liukaixiong
 * @date 2025/11/7 - 14:45
 */
public class ExpressionContextResult {
    private String expression;

    private List<String> functionNameList;

    private List<String> variableNameList;

    private Object result;

    public ExpressionContextResult() {
    }

    public ExpressionContextResult(String expression, List<String> functionNameList, List<String> variableNameList, Object result) {
        this.expression = expression;
        this.functionNameList = functionNameList;
        this.variableNameList = variableNameList;
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public List<String> getFunctionNameList() {
        return functionNameList;
    }

    public void setFunctionNameList(List<String> functionNameList) {
        this.functionNameList = functionNameList;
    }

    public List<String> getVariableNameList() {
        return variableNameList;
    }

    public void setVariableNameList(List<String> variableNameList) {
        this.variableNameList = variableNameList;
    }
}
