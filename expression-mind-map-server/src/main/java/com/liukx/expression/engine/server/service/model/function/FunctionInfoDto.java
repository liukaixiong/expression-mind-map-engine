package com.liukx.expression.engine.server.service.model.function;

import com.liukx.expression.engine.core.api.model.FunctionRequestDocumentModel;
import lombok.Builder;

import java.util.List;

/**
 * 可见变量定义模型
 */
@Builder
public class FunctionInfoDto {
    /**
     * 注册类型
     */
    private String registerType = "local";
    /**
     * 服务名称
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
     * 结果类型
     */
    private String resultClassType;
    /**
     * 请求参数描述
     */
    private List<FunctionRequestDocumentModel> documentModel;

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

    public List<FunctionRequestDocumentModel> getDocumentModel() {
        return documentModel;
    }

    public void setDocumentModel(List<FunctionRequestDocumentModel> documentModel) {
        this.documentModel = documentModel;
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

    public String getResultClassType() {
        return resultClassType;
    }

    public void setResultClassType(String resultClassType) {
        this.resultClassType = resultClassType;
    }
}
