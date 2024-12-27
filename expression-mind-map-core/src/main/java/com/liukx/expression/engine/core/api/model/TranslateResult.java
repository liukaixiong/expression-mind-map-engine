package com.liukx.expression.engine.core.api.model;

import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;

import java.util.List;

/**
 * 翻译表达式
 */
public class TranslateResult {

    /**
     * 变量类型
     */
    public List<VariableApiModel> variableApiList;
    /**
     * 简单的翻译
     */
    private String simpleTranslateText;
    /**
     * 函数API
     */
    private List<FunctionApiModel> functionApiList;

    public String getSimpleTranslateText() {
        return simpleTranslateText;
    }

    public void setSimpleTranslateText(String simpleTranslateText) {
        this.simpleTranslateText = simpleTranslateText;
    }

    public List<VariableApiModel> getVariableApiList() {
        return variableApiList;
    }

    public void setVariableApiList(List<VariableApiModel> variableApiList) {
        this.variableApiList = variableApiList;
    }

    public List<FunctionApiModel> getFunctionApiList() {
        return functionApiList;
    }

    public void setFunctionApiList(List<FunctionApiModel> functionApiList) {
        this.functionApiList = functionApiList;
    }
}
