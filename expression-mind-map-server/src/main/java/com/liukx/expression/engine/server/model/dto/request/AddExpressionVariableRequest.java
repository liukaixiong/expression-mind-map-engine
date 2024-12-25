package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("添加注册变量请求类")
public class AddExpressionVariableRequest implements Serializable {

    /**
     * 服务名称
     */
    @ApiModelProperty("服务名称")
    private String serviceName;

    /**
     * 变量编码
     */
    @ApiModelProperty(value = "变量编码", required = true)
    private String varCode;

    /**
     * 变量描述
     */
    @ApiModelProperty("变量描述")
    private String varDescription;

    /**
     * 变量来源:local本地,remote远程
     */
    @ApiModelProperty("变量来源")
    private String varSource;

    /**
     * 变量数据类型
     */
    @ApiModelProperty("变量数据类型")
    private String varDataType;

    /**
     * 状态:0.启用,1.禁用
     */
    @ApiModelProperty("状态")
    private Integer status;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

}
