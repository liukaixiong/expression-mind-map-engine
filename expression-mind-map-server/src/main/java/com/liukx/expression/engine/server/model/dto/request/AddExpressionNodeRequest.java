package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@ApiModel("添加节点请求类")
@Data
@Builder
public class AddExpressionNodeRequest implements Serializable {

    /**
     * 服务名称
     */
    @ApiModelProperty("服务名称")
    private String serviceName;

    /**
     * 调用方式
     */
    @ApiModelProperty("调用方式")
    private String callMethod;
    /**
     * 服务描述
     */
    @ApiModelProperty("服务描述")
    private String serviceDescription;

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

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;
}
