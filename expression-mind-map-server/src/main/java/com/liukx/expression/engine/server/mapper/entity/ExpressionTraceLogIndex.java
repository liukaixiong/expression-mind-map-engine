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
@TableName("expression_trace_log_index")
//@Schema(name = "ExpressionTraceLogIndex", description = "")
public class ExpressionTraceLogIndex extends BaseTableEntity {

    private static final long serialVersionUID = 1L;

    //    @Schema(description = "执行器编号")
    @TableField("executor_id")
    private Long executorId;

    //    @Schema(description = "服务名称")
    @TableField("service_name")
    private String serviceName;

    //    @Schema(description = "业务编码")
    @TableField("business_code")
    private String businessCode;

    //    @Schema(description = "执行器编码")
    @TableField("executor_code")
    private String executorCode;

    //    @Schema(description = "执行器名称")
    @TableField("executor_name")
    private String executorName;

    //    @Schema(description = "事件名称")
    @TableField("event_name")
    private String eventName;

    //    @Schema(description = "用户编号")
    @TableField("user_id")
    private Long userId;

    //    @Schema(description = "唯一编号")
    @TableField("union_id")
    private String unionId;

    //    @Schema(description = "追踪编号")
    @TableField("trace_id")
    private String traceId;

    //    @Schema(description = "上下文请求体")
    @TableField("env_body")
    private String envBody;
}
