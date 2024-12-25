package com.liukx.expression.engine.client.log;

import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;

/**
 * @author liukaixiong
 * @date 2023/12/22
 */
public interface LogTraceService {


    /**
     * 日志规范追踪
     *
     * @param request      请求入参
     * @param logEventType 关键事件
     * @param text         日志描述
     * @param logParam     日志参数
     */
    void trace(ExpressionBaseRequest request, LogEventEnum logEventType, String text, Object... logParam);


}
