package com.liukx.expression.engine.client.feature;

import com.liukx.expression.engine.client.api.ExpressionFunctionFilter;
import com.liukx.expression.engine.client.api.ExpressionFunctionPostProcessor;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.FunctionFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * 函数上下文执行器管理
 *
 * @author liukaixiong
 * @date 2025/12/9 - 18:01
 */
public class FunctionContextManager {

    @Autowired(required = false)
    private List<ExpressionFunctionPostProcessor> functionPostProcessorList = new ArrayList<>();

    @Autowired(required = false)
    private List<ExpressionFunctionFilter> functionFilters = new ArrayList<>();

    public Object executor(FunctionApiModel functionApiModel, Map<String, Object> env, List<Object> funcArgs, Supplier<Object> supplier) {
        final ExpressionEnvContext expressionEnvContext = ExpressionEnvContext.of(env);
        // 提取通用参数
        ExpressionBaseRequest request = expressionEnvContext.getEnvClassInfo(ExpressionBaseRequest.class);
        ExpressionConfigTreeModel configTreeModel = expressionEnvContext.getConfigTreeModel();
        try {
            functionPostProcessorList.forEach(var -> var.functionBefore(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs));

            FunctionFilterChain functionFilterChain = new FunctionFilterChain(functionFilters, supplier);

            final Object processor = functionFilterChain.doFilter(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs);

            functionPostProcessorList.forEach(var -> var.afterFunction(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs, processor));
            return processor;
        } catch (Exception e) {
            functionPostProcessorList.forEach(var -> var.functionError(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs, e));
            throw e;
        }
    }

}
