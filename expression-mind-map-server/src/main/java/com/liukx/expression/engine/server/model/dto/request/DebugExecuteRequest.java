package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.Map;

/**
 * 服务端调试请求
 *
 * @author liukaixiong
 */
@ApiModel("服务端调试请求")
public class DebugExecuteRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "服务名", required = true)
    private String serviceName;

    @ApiModelProperty("服务地址(可选,手动输入时使用)")
    private String serviceAddress;

    @ApiModelProperty(value = "Token", required = true)
    private String token;

    @ApiModelProperty(value = "表达式", required = true)
    private String expression;

    @ApiModelProperty(value = "上下文参数", required = true)
    private Map<String, Object> context;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceAddress() {
        return serviceAddress;
    }

    public void setServiceAddress(String serviceAddress) {
        this.serviceAddress = serviceAddress;
    }

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
