package com.liukx.expression.engine.client.filter.executor;

import com.liukx.expression.engine.client.api.ExecutorFilter;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.ExecutorFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.enums.MetricKeyEnum;
import com.liukx.expression.engine.core.utils.MetricHelper;

import java.util.List;

/**
 * 指标埋点
 *
 * @author liukaixiong
 * @date 2026/4/16 - 18:59
 */
public class MetricExecutorFilter implements ExecutorFilter {

    @Override
    public void doExecutorFilter(ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, ExpressionConfigInfo configInfo, List<ExpressionConfigTreeModel> expressionConfigTreelList, ExecutorFilterChain chain) {
        final String serviceName = baseRequest.getServiceName();
        final String businessCode = baseRequest.getBusinessCode();
        final String executorCode = baseRequest.getExecutorCode();
        MetricHelper.timed(MetricKeyEnum.expression_executor_local_call, () -> {
            chain.doFilter(baseRequest, envContext, configInfo, expressionConfigTreelList);
        }, "serviceName", serviceName, "businessCode", businessCode, "executorCode", executorCode);
    }
}
