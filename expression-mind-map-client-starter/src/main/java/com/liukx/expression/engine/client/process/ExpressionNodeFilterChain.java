package com.liukx.expression.engine.client.process;

import com.liukx.expression.engine.client.api.ExpressionNodeExecutorFilter;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.function.Supplier;

/**
 * 表达式子分支执行过滤器链
 *
 * @author liukaixiong
 * @date 2024/8/23 - 10:09
 */
@Deprecated
public class ExpressionNodeFilterChain {
    /**
     * 拦截器
     */
    private final List<ExpressionNodeExecutorFilter> expressionNodeExecutorFilters;
    /**
     * 具体的执行方法
     */
    private final Supplier<Object> supplier;

    private int index = 0;

    public ExpressionNodeFilterChain(List<ExpressionNodeExecutorFilter> expressionExecutorFilters, Supplier<Object> supplier) {
        this.expressionNodeExecutorFilters = expressionExecutorFilters;
        this.supplier = supplier;
    }


    public void doFilter(ExpressionBaseRequest request, ExpressionEnvContext envContext, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel configTreeModelList, Object execute) {
        if (CollectionUtils.isEmpty(expressionNodeExecutorFilters) || expressionNodeExecutorFilters.size() == index) {
            supplier.get();
            return;
        }
        expressionNodeExecutorFilters.get(index++).doExpressionNodeFilter(request, envContext, configInfo, configTreeModelList, execute, this);
    }

}
