package com.liukx.expression.engine.server.event;

import org.springframework.context.ApplicationEvent;

/**
 * 执行配置配置刷新
 *
 * @author liukaixiong
 * @date 2024/2/20
 */
public class ExecutorConfigRefreshEvent extends ApplicationEvent {

    /**
     * Create a new {@code ApplicationEvent}.
     *
     * @param source the object on which the event initially occurred or with
     *               which the event is associated (never {@code null})
     */
    public ExecutorConfigRefreshEvent(Object source) {
        super(source);
    }


}
