package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("查询服务节点请求类")
public class QueryExpressionNodeRequest implements Serializable {
    /**
     * 调用方式
     */
    @ApiModelProperty("调用方式")
    private String callMethod;

    /**
     * 服务名称
     */
    @ApiModelProperty("服务名称")
    private String serviceName;

    /**
     * 路由地址
     */
    @ApiModelProperty("路由地址")
    private String domain;

    /**
     * 状态:0.启用1.禁用
     */
    @ApiModelProperty("状态")
    private Integer status;
}
