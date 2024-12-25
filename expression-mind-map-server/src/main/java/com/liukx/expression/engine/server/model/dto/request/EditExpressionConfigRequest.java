package com.liukx.expression.engine.server.model.dto.request;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serializable;

//@Tag(name = "编辑引擎表达式请求类")
@Data
public class EditExpressionConfigRequest implements Serializable {
    /**
     * 主键id
     */
    @JsonSerialize(using = ToStringSerializer.class)
//    @Schema(description = "表达式id")
    private Long id;

    //    @Schema(description = "表达式编码，需要唯一")
    private String expressionCode;

    //    @Schema(description = "表达式上级编号")
    private Long parentId;

    /**
     * 表达式类型:condition 条件表达式;rule 规则表达式
     */
//    @Schema(description = "表达式类型", allowableValues = "condition,rule")
    private String expressionType;
    /**
     * 表达式描述
     */
//    @Schema(description = "表达式描述")
    private String expressionDescription;
    //    @Schema(description = "表达式标题")
    private String expressionTitle;
    /**
     * 表达式内容
     */
//    @Schema(description = "表达式内容")
    private String expressionContent;

    /**
     * 表达式状态:0.启用,1.禁用
     */
//    @Schema(description = "表达式状态")
    private Integer expressionStatus;

    //    @ApiModelProperty("优先级顺序")
    private Integer priorityOrder;
    /**
     * 更新人
     */
//    @Schema(description = "更新人")
    private String updateBy;
}
