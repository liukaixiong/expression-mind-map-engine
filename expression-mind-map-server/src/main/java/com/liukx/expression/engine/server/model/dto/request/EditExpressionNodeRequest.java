package com.liukx.expression.engine.server.model.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@ApiModel("修改服务节点请求类")
@Data
@Builder
public class EditExpressionNodeRequest implements Serializable {
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("节点id")
    private Long id;

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
     * 服务描述
     */
    @ApiModelProperty("服务描述")
    private String serviceDescription;

    /**
     * 路由地址
     */
    @ApiModelProperty("服务地址")
    private String domain;

    /**
     * 状态:0.启用1.禁用
     */
    @ApiModelProperty("状态")
    private Integer status;


    /**
     * 更新人
     */
    @ApiModelProperty("更新人")
    private String updateBy;
}
