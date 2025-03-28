package com.liukx.expression.engine.core.api.model.api;

import com.liukx.expression.engine.core.api.model.FunctionRequestDocumentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 可见变量定义模型
 */
public class FunctionApiModel {
    /**
     * 注册类型
     */
    private String registerType = "local";
    /**
     * 分组名称
     */
    private String groupName;
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
     * 参考案例值
     */
    private String example;

    /**
     * 参数列表
     */
    private List<String> args = new ArrayList<>();
    /**
     * 请求参数描述
     */
    private List<FunctionRequestDocumentModel> documentModel;


    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public List<String> getArgs() {
        return args;
    }

    public void setArgs(List<String> args) {
        this.args = args;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
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
