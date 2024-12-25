package com.liukx.expression.engine.server.model.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;


//@ApiModel("编辑表达式引擎执行器请求类")
@Data
public class EditExpressionExecutorRequest {
    @JsonSerialize(using = ToStringSerializer.class)
//    @Schema(description="主键id")
    private Long id;

    /**
     * 服务名称
     */
//    @Schema(description="服务名称")
    private String serviceName;

    /**
     * 业务编码
     */
//    @Schema(description="业务编码")
    private String businessCode;

    /**
     * 执行器名称
     */
//    @Schema(description="执行器编码",required = true)
    private String executorCode;
    /**
     * 执行器描述
     */
//    @Schema(description="执行器描述")
    private String executorDescription;

    //    @Schema(description="配置能力")
    private String configurabilityJson;
    /**
     * 变量定义,方便索引
     */
//    @ApiModelProperty("变量定义")
    private String varDefinition;

    /**
     * 执行器状态:0.创建，1.启用，2.禁用
     */
//    @Schema(description="执行器状态")
    private Integer status;

    /**
     * 更新人
     */
//    @Schema(description="更新人")
    private String updateBy;
}
