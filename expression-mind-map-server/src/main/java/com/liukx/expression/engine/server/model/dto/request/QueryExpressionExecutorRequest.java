package com.liukx.expression.engine.server.model.dto.request;

//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.tags.Tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;

//@Tag(name="查询表达式")
@EqualsAndHashCode(callSuper = true)
@Data
public class QueryExpressionExecutorRequest extends PageQuery implements Serializable {
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
//    @Schema(description="执行器名称")
    private String executorCode;
    /**
     * 执行器状态:0.创建，1.启用，2.禁用
     */
//    @Schema(description="执行器状态")
    private Integer status;

    /**
     * 回调结果
     */
//    @Schema(description="回调结果")
    private String callbackResult;

    /**
     * 表达式搜索
     */
    private String expressionContent;

    private Date changeDate;
}
