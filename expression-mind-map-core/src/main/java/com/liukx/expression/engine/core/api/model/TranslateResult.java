package com.liukx.expression.engine.core.api.model;

import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.List;

/**
 * 翻译表达式
 */
@ApiModel
public class TranslateResult {

    @ApiModelProperty(value = "相关的变量api列表")
    public List<VariableApiModel> variableApiList;
    @ApiModelProperty(value = "简单的翻译文本")
    private String simpleTranslateText;
    @ApiModelProperty(value = "相关的函数api列表")
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
