package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.FunctionFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;

import java.util.List;

/**
 * 表达式函数过滤
 *
 * @author liukaixiong
 * @date 2024/8/23 - 10:10
 */
public interface ExpressionFunctionFilter {

    Object doFunctionFilter(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, FunctionApiModel functionInfo, List<Object> funcArgs, FunctionFilterChain chain);

}
