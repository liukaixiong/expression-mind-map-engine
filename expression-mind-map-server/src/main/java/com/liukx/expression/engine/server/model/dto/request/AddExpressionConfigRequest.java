package com.liukx.expression.engine.server.model.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

//@ApiModel
@Data
public class AddExpressionConfigRequest implements Serializable {
    /**
     * 表达式类型:condition 条件表达式;rule 规则表达式
     */
//    @Schema(description = "表达式类型", allowableValues = "condition,rule")
    private String expressionType;

    /**
     * 执行器id
     */
//    @Schema(description = "执行器id", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long executorId;

    /**
     * 上级编号
     */
//    @Schema(description = "上级编号", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    /**
     * 表达式编码
     */
//    @Schema(description = "表达式编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expressionCode;
    /**
     * 表达式标题
     */
//    @Schema(description = "表达式标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expressionTitle;
    /**
     * 表达式内容
     */
//    @Schema(description = "表达式内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String expressionContent;
    /**
     * 表达式描述
     */
//    @ApiModelProperty("表达式描述")
    private String expressionDescription;

    private String configurabilityJson;
    /**
     * 表达式状态:0.启用,1.禁用
     */
//    @ApiModelProperty("表达式状态")
    private Integer expressionStatus;

    //    @ApiModelProperty("优先级顺序")
    private Integer priorityOrder;

    /**
     * 创建人
     */
//    @ApiModelProperty("创建人")
    private String createBy;

}
