package com.liukx.expression.engine.client.process;

import com.liukx.expression.engine.client.api.ExpressionFunctionFilter;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Supplier;

/**
 * 函数过滤器责任链
 *
 * @author liukaixiong
 * @date 2024/8/23 - 10:09
 */
public class FunctionFilterChain {
    /**
     * 拦截器
     */
    private final List<ExpressionFunctionFilter> functionFilters;
    /**
     * 具体的执行方法
     */
    private final Supplier<Object> supplier;

    private int index = 0;

    public FunctionFilterChain(List<ExpressionFunctionFilter> functionFilters, Supplier<Object> supplier) {
        this.functionFilters = functionFilters;
        this.supplier = supplier;
    }


    public Object doFilter(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, FunctionApiModel functionInfo, List<Object> funcArgs) {

        if (CollectionUtils.isEmpty(functionFilters) || functionFilters.size() == index) {
            return supplier.get();
        }

        return functionFilters.get(index++).doFunctionFilter(env, configTreeModel, request, functionInfo, funcArgs, this);
    }

}
