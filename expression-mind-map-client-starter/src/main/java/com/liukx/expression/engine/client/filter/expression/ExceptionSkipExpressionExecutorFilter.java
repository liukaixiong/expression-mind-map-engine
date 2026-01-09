package com.liukx.expression.engine.client.filter.expression;

import com.liukx.expression.engine.client.api.ExpressionConfigExecutorIntercept;
import com.liukx.expression.engine.client.api.ExpressionExecutorFilter;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.enums.ExpressionCoxnfigurabilitySwitchEnum;
import com.liukx.expression.engine.client.helper.ConfigurabilityHelper;
import com.liukx.expression.engine.client.process.ExpressionFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.ExpressionContextResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * 如果当前表达式遇到异常的情况下，仅仅打印异常，并返回null
 *
 * @author liukaixiong
 * @date 2026/1/8 - 18:10
 */
public class ExceptionSkipExpressionExecutorFilter implements ExpressionExecutorFilter {
    private final Logger LOG = LoggerFactory.getLogger(ExceptionSkipExpressionExecutorFilter.class);
    private final List<ExpressionConfigExecutorIntercept> executionCallbackList;

    public ExceptionSkipExpressionExecutorFilter(List<ExpressionConfigExecutorIntercept> executionCallbackList) {
        this.executionCallbackList = executionCallbackList;
    }

    @Override
    public ExpressionContextResult doExpressionFilter(ExpressionEnvContext env, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, ExpressionFilterChain chain) {
        if (ConfigurabilityHelper.isEnableConfigurability(configTreeModel, ExpressionCoxnfigurabilitySwitchEnum.enableExceptionSkip)) {
            try {
                return chain.doFilter(env, configInfo, configTreeModel, request);
            } catch (Exception e) {
//                LogHelper.trace(request, LogEventEnum.EXPRESSION_CALL, "表达式执行异常，表达式：{}，异常信息：", configTreeModel.getExpression());
                LOG.error("表达式跳过异常，表达式：{}，异常信息：{}", configTreeModel.getExpression(), e.getMessage());
                executionCallbackList.forEach(var -> var.error(configTreeModel, request, env, e));
                return new ExpressionContextResult(configTreeModel.getExpression(), Collections.emptyList(), Collections.emptyList(), e.getMessage());
            }
        }
        return chain.doFilter(env, configInfo, configTreeModel, request);
    }
}
