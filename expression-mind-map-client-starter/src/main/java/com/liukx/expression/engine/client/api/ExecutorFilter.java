package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.ExecutorFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;

import java.util.List;

/**
 * 执行器过滤链条
 * <p>
 * 在规则执行时，会触发执行器过滤器链
 * </p>
 *
 * @author liukaixiong
 * @date 2024/8/23 - 10:10
 */
public interface ExecutorFilter {

    void doExecutorFilter(ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, ExpressionConfigInfo configInfo, List<ExpressionConfigTreeModel> expressionConfigTreelList, ExecutorFilterChain chain);

}
