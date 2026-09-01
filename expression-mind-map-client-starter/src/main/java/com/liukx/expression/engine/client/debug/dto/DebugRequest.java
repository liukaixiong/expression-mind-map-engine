package com.liukx.expression.engine.client.debug.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Map;

/**
 * 客户端调试请求
 *
 * @author liukaixiong
 */
@ApiModel("客户端调试请求")
public class DebugRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "调试Token", required = true)
    private String token;

    @ApiModelProperty(value = "表达式", required = true)
    private String expression;

    @ApiModelProperty(value = "上下文参数", required = true)
    private Map<String, Object> context;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public Map<String, Object> getContext() {
        return context;
    }

    public void setContext(Map<String, Object> context) {
        this.context = context;
    }
}
