package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.ExpressionNodeFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;

/**
 * 表达式子节点执行过滤器
 *
 * @author liukaixiong
 * @date 2024/8/23 - 10:10
 */
@Deprecated
public interface ExpressionNodeExecutorFilter {

    void doExpressionNodeFilter(ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel configTreeModelList, Object execute, ExpressionNodeFilterChain chain);

}
