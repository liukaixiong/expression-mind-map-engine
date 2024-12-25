package com.liukx.expression.engine.server.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@ApiModel("执行器的表达式配置响应类")
@Data
public class ExpressionExecutorDetailConfigDTO implements Serializable {
    /**
     * 主键id
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @ApiModelProperty("表达式id")
    private Long id;

    /**
     * 表达式类型:condition 条件表达式;rule 规则表达式
     */
    @ApiModelProperty(value = "表达式类型", allowableValues = "condition,rule")
    private String expressionType;
    /**
     * 执行器id
     */
    @ApiModelProperty(value = "执行器id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long executorId;

    @ApiModelProperty(value = "上级编号")
    private Long parentId;
    /**
     * 表达式编码
     */
    @ApiModelProperty(value = "表达式编码")
    private String expressionCode;
    /**
     * 表达式标题
     */
    @ApiModelProperty(value = "表达式标题")
    private String expressionTitle;
    /**
     * 表达式描述
     */
    @ApiModelProperty("表达式描述")
    private String expressionDescription;
    /**
     * 表达式内容
     */
    @ApiModelProperty("表达式内容")
    private String expressionContent;

    /**
     * 表达式状态:1 启用,0禁用
     */
    @ApiModelProperty("表达式状态")
    private Integer expressionStatus;

    @ApiModelProperty("优先级顺序")
    private Integer priorityOrder;

    /**
     * 创建人
     */
    @ApiModelProperty("创建人")
    private String createBy;

    /**
     * 更新人
     */
    @ApiModelProperty("更新人")
    private String updateBy;

    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy/MM/dd HH:mm:ss")
    private LocalDateTime updateTime;

    private List<ExpressionTraceLogInfo> traceLogInfos;
}
