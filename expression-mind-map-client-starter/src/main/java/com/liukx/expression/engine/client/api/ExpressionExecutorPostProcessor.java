package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;

/**
 * 执行器拦截处理器
 *
 * @author liukaixiong
 * @date 2024/7/18 - 13:13
 */
public interface ExpressionExecutorPostProcessor {


    /**
     * 前置执行器
     *
     * @param envContext
     * @param baseRequest
     * @param configInfo
     */
    void beforeExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configInfo);


    /**
     * 后置执行器
     *
     * @param envContext
     * @param baseRequest
     * @param configTreeModelList
     */
    void afterExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configTreeModelList);

}
