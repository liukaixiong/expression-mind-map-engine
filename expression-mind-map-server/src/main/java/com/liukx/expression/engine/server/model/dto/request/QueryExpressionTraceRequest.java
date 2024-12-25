package com.liukx.expression.engine.server.model.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
//@Tag(name = "查询表达式")
@Data
public class QueryExpressionTraceRequest extends PageQuery implements Serializable {

    //    @Schema(description = "执行器编号")
    private Long executorId;

    //    @Schema(description = "服务名称")
    private String serviceName;

    /**
     * 执行器编码
     */
//    @Schema(description = "执行器编码")
    private String executorCode;

    //    @Schema(description = "业务编码")
    private String businessCode;

    //    @Schema(description = "事件名称")
    private String eventName;

    //    @Schema(description = "用户编号")
    private Long userId;

    //    @Schema(description = "唯一编号")
    private String unionId;

    //    @Schema(description = "追踪编号")
    private String traceId;

}
