package com.liukx.expression.engine.core.api.model;

import lombok.Data;

/**
 * 基础参数模型
 */
@Data
public class ExpressionBaseRequest {

    /**
     * 服务名称
     */
    private String serviceName;

    /**
     * 确定组名称,比如活动code
     */
    private String businessCode;

    /**
     * 执行器名称
     */
    private String executorCode;

    /**
     * 代表事件名称,确定业务事件
     */
    private String eventName;

    /**
     * 用户编号
     */
    private Long userId;
    /**
     * 唯一编号
     */
    private String unionId;
    /**
     * 链路编号
     */
    private String traceId;

}
