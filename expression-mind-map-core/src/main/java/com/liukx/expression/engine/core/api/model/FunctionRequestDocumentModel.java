package com.liukx.expression.engine.core.api.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel("函数请求文档模型")
public class FunctionRequestDocumentModel {
    @ApiModelProperty(value = "函数名称")
    private String name;
    @ApiModelProperty(value = "函数类型")
    private String type;
    @ApiModelProperty(value = "函数描述")
    private String describe;
    @ApiModelProperty(value = "是否必填项")
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
