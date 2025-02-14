package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.ExpressionFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;

/**
 * 表达式过滤链条
 *
 * @author liukaixiong
 * @date 2024/8/23 - 10:10
 */
public interface ExpressionExecutorFilter {

    Object doExpressionFilter(ExpressionEnvContext env, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, ExpressionFilterChain chain);

}
