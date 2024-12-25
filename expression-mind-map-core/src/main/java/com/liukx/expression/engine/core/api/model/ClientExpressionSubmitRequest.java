package com.liukx.expression.engine.core.api.model;

import lombok.Data;

/**
 * 客户端表达式提交
 *
 * @author liukaixiong
 * @date 2024/7/19 - 11:44
 */
@Data
public class ClientExpressionSubmitRequest {
    /**
     * 确定组名称,比如活动code
     */
    private String businessCode;

    /**
     * 确定业务编码
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

    /**
     * 请求入参
     */
    private Object request;
}
