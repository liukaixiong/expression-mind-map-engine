package com.liukx.expression.engine.server.mapper.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *
 * </p>
 *
 * @author liukx
 * @since 2024-07-18
 */
@Getter
@Setter
@TableName("expression_trace_log_info")
//@Schema(name = "ExpressionTraceLogInfo", description = "")
public class ExpressionTraceLogInfo extends BaseTableEntity {

    private static final long serialVersionUID = 1L;

    //    @Schema(description = "追踪编号主键")
    @TableField("trace_log_id")
    private Long traceLogId;

    //    @Schema(description = "执行器编号")
    @TableField("executor_id")
    private Long executorId;

    //    @Schema(description = "表达式配置编号")
    @TableField("expression_config_id")
    private Long expressionConfigId;

    //    @Schema(description = "模块类型: expression:表达式,function:函数")
    @TableField("module_type")
    private String moduleType;

    //    @Schema(description = "表达式内容")
    @TableField("expression_content")
    private String expressionContent;

    //    @Schema(description = "表达式结果 0 失败 1成功")
    @TableField("expression_result")
    private Integer expressionResult;

    //    @Schema(description = "调试追踪上下文")
    @TableField("debug_trace_content")
    private String debugTraceContent;

    //    @Schema(description = "表达式描述")
    @TableField("expression_description")
    private String expressionDescription;
}
