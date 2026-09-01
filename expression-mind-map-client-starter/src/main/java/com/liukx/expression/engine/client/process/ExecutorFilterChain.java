package com.liukx.expression.engine.client.process;

import com.liukx.expression.engine.client.api.ExecutorFilter;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Supplier;

/**
 * 表达式过滤器责任链
 *
 * @author liukaixiong
 * @date 2026/4/17 - 10:09
 */
public class ExecutorFilterChain {
    /**
     * 拦截器
     */
    private final List<ExecutorFilter> filters;
    /**
     * 具体的执行方法
     */
    private final Supplier<Void> supplier;

    private int index = 0;

    public ExecutorFilterChain(List<ExecutorFilter> filters, Supplier<Void> supplier) {
        this.filters = filters;
        this.supplier = supplier;
    }


    public void doFilter(ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, ExpressionConfigInfo configInfo, List<ExpressionConfigTreeModel> expressionConfigTreelList) {

        if (CollectionUtils.isEmpty(filters) || filters.size() == index) {
            supplier.get();
            return;
        }

        filters.get(index++).doExecutorFilter(baseRequest, envContext, configInfo, expressionConfigTreelList, this);
    }

}
