package com.liukx.expression.engine.core.api.model.api;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
/**
 * 可见变量定义模型
 */
@ApiModel("变量模型")
public class VariableApiModel {
    /**
     * 注册类型
     */
    @ApiModelProperty(value = "注册类型", example = "local || remote")
    private String registerType = "local";
    /**
     * 分组名称
     */
    @ApiModelProperty(value = "分组名称")
    private String groupName;
    /**
     * 变量名称
     */
    @ApiModelProperty(value = "变量名称")
    private String name;
    /**
     * 描述
     */
    @ApiModelProperty(value = "变量描述")
    private String describe;
    /**
     * 变量类型
     */
    @ApiModelProperty(value = "变量类型")
    private String type;

    public String getRegisterType() {
        return registerType;
    }

    public void setRegisterType(String registerType) {
        this.registerType = registerType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
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
