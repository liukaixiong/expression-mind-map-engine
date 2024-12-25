package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;

/**
 * 表达式类型回调
 *
 * @author liukaixiong
 * @date 2024/6/24 - 17:38
 */
public interface ExpressionConfigExecutorIntercept {

    default void before(ExpressionConfigTreeModel expressionType, ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext) {

    }

    /**
     * 执行器回调
     *
     * @param expressionType 表达式对象
     * @param baseRequest    请求参数
     * @param envContext     上下文参数
     * @param execute        执行结果
     */
    default void after(ExpressionConfigTreeModel expressionType, ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, Object execute) {

    }


    /**
     * 执行器出现异常的情况
     *
     * @param expressionType
     * @param baseRequest
     * @param envContext
     * @param e
     */
    default void error(ExpressionConfigTreeModel expressionType, ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, Exception e) {

    }

}
