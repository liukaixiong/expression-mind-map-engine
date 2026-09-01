package com.liukx.expression.engine.client.filter.expression;

import com.liukx.expression.engine.client.api.ExpressionExecutorFilter;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.ExpressionFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.ExpressionContextResult;

/**
 * 分支获取注入上下文对象
 * 可以使用函数获取:
 * {@code fn_env_get_branch_value('_branchContext')}
 * {@code fn_env_put_value('xxx',fn_env_get_branch_value('_branchContext')) && xxx.expressionId}
 *
 * @author liukaixiong
 * @date 2026/1/8 - 16:43
 */
public class BranchContextExpressionExecutorFilter implements ExpressionExecutorFilter {

    @Override
    public ExpressionContextResult doExpressionFilter(ExpressionEnvContext env, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, ExpressionFilterChain chain) {
        env.recordBranchResult(configTreeModel.getExpressionId(), "_branchContext", configTreeModel);
        return chain.doFilter(env, configInfo, configTreeModel, request);
    }
}
