package com.liukx.expression.engine.server.model.dto.request;

import lombok.Data;

import java.io.Serializable;

@Data
//@Tag(name = "添加表达式引擎执行器请求类")
public class AddExpressionExecutorRequest implements Serializable {
    /**
     * 服务名称
     */
//    @Schema(description = "服务名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serviceName;

    /**
     * 业务编码
     */
//    @Schema(description = "业务编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String businessCode;

    /**
     * 执行器名称
     */
//    @Schema(description = "执行器编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String executorCode;
    /**
     * 执行器描述
     */
//    @ApiModelProperty("执行器描述")
    private String executorDescription;

    //    @ApiModelProperty("配置能力")
    private String configurabilityJson;
    /**
     * 变量定义,方便索引
     */
//    @ApiModelProperty("变量定义")
    private String varDefinition;

    /**
     * 执行器状态:0.创建，1.启用，2.禁用
     */
//    @ApiModelProperty("执行器状态")
    private Integer status;

    /**
     * 创建人
     */
//    @ApiModelProperty("创建人")
    private String createBy;
}
