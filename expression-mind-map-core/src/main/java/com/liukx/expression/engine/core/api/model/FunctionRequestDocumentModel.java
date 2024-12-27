package com.liukx.expression.engine.core.api.model;

/**
 * 函数请求文档模型
 */
public class FunctionRequestDocumentModel {
    /**
     * 函数名称
     */
    private String name;
    /**
     * 函数类型
     */
    private String type;
    /**
     * 函数描述
     */
    private String describe;
    /**
     * 是否必填项
     */
    private boolean require;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public boolean isRequire() {
        return require;
    }

    public void setRequire(boolean require) {
        this.require = require;
    }
}
