package com.liukx.expression.engine.server.service.model.variable;

import lombok.Builder;

/**
 * 可见变量定义模型
 */
@Builder
public class VariableInfoDto {
    /**
     * 注册类型
     */
    private String registerType = "local";

    /**
     * 项目名称
     */
    private String serviceName;
    /**
     * 变量名称
     */
    private String name;
    /**
     * 描述
     */
    private String describe;
    /**
     * 变量类型
     */
    private String type;

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
