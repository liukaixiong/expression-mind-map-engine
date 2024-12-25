package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@ApiModel("")
@Data
public class QueryExpressionVariableRequest implements Serializable {
    /**
     * 服务名称
     */
    @ApiModelProperty("服务名称")
    private String serviceName;

    /**
     * 变量编码
     */
    @ApiModelProperty("变量编码")
    private String varCode;

    /**
     * 变量英文名
     */
    @ApiModelProperty("变量英文名")
    private String varName;

    /**
     * 变量值
     */
    @ApiModelProperty("变量值")
    private String varValue;

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
     * 状态:1.启用,0.禁用
     */
    @ApiModelProperty("状态")
    private Integer status;
}
