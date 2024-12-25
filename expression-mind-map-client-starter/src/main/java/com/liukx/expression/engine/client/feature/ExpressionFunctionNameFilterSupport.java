package com.liukx.expression.engine.client.feature;

import com.liukx.expression.engine.client.api.ExpressionFunctionFilter;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.FunctionFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * 表达式函数名称过滤能力支持
 * 开启能力可通过: ExpressionEnvContext#enableExpressionFunctionNameSkipFilter(Set)
 *
 * @author liukaixiong
 * @date 2024/8/23 - 10:59
 */
@Component
public class ExpressionFunctionNameFilterSupport implements ExpressionFunctionFilter {

    @Override
    public Object doFunctionFilter(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, FunctionApiModel functionInfo, List<Object> funcArgs, FunctionFilterChain chain) {
        final Set<String> expressionFunctionNameSkipFilterList = env.getExpressionFunctionNameSkipFilterList();
        if (!CollectionUtils.isEmpty(expressionFunctionNameSkipFilterList)) {
            if (expressionFunctionNameSkipFilterList.contains(functionInfo.getName())) {
                env.recordTraceDebugContent(functionInfo.getName(), "function_name_skip", "触发函数名称跳过能力,默认true!");
                return true;
            }
        }
        return chain.doFilter(env, configTreeModel, request, functionInfo, funcArgs);
    }
}
