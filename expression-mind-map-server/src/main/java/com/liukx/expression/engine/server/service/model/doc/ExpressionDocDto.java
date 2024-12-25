package com.liukx.expression.engine.server.service.model.doc;

import lombok.Data;

import java.util.List;

/**
 * 可见变量定义模型
 */
@Data
public class ExpressionDocDto {
    /**
     * 分组
     */
    private String groupName;
    /**
     * 服务名称
     */
    private String serviceName;
    /**
     * 变量名称
     */
    private String name;

    /**
     * var | fn
     */
    private String type;

    /**
     * 参数
     */
    private List<String> params;

    /**
     * 描述
     */
    private String describe;

    /**
     * 使用方式
     */
    private String example;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getParams() {
        return params;
    }

    public void setParams(List<String> params) {
        this.params = params;
    }

    public String getDescribe() {
        return describe;
    }

    public void setDescribe(String describe) {
        this.describe = describe;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }
}
