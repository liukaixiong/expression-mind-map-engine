package com.liukx.expression.engine.client.debug.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * 客户端调试响应
 *
 * @author liukaixiong
 */
@ApiModel("客户端调试响应")
public class DebugResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("执行结果")
    private Object result;

    @ApiModelProperty("使用的函数列表")
    private List<String> functionNameList;

    @ApiModelProperty("使用的变量列表")
    private List<String> variableNameList;

    @ApiModelProperty("执行时间(毫秒)")
    private Long executionTime;

    @ApiModelProperty("错误信息")
    private String errorMessage;

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
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

    public Long getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Long executionTime) {
        this.executionTime = executionTime;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
