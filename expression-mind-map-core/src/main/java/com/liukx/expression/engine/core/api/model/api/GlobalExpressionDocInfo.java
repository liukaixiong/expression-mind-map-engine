package com.liukx.expression.engine.core.api.model.api;

import java.util.List;

/**
 * 全局表达式信息
 * @author liukaixiong
 * @date : 2022/6/9 - 17:19
 */
public class GlobalExpressionDocInfo {

    private String serviceName;
    private List<FunctionApiModel> functionApi;
    private List<VariableApiModel> variableApi;
    private List<VariableApiModel> variableTypeApi;

    public List<VariableApiModel> getVariableTypeApi() {
        return variableTypeApi;
    }

    public void setVariableTypeApi(List<VariableApiModel> variableTypeApi) {
        this.variableTypeApi = variableTypeApi;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public List<FunctionApiModel> getFunctionApi() {
        return functionApi;
    }

    public void setFunctionApi(List<FunctionApiModel> functionApi) {
        this.functionApi = functionApi;
    }

    public List<VariableApiModel> getVariableApi() {
        return variableApi;
    }

    public void setVariableApi(List<VariableApiModel> variableApi) {
        this.variableApi = variableApi;
    }
}
