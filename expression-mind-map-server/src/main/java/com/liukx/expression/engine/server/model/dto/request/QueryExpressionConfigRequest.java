package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;

@ApiModel
@Data
public class QueryExpressionConfigRequest implements Serializable {

    //    @Schema(description = "执行器编号")
    private Integer executorId;
    //    @Schema(description = "上级编号")
    private Long parentId;
    //    @Schema(description = "表达式类型", allowableValues = "action,condition,trigger,callback")
    private String expressionType;

    //    @Schema(description = "追踪日志编号")
    private Long traceLogId;
    /**
     * 表达式描述
     */
//    @Schema(description = "表达式描述")
    private String expressionDescription;
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

}
