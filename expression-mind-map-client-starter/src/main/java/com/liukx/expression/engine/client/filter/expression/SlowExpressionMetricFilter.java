package com.liukx.expression.engine.client.filter.expression;

import com.liukx.expression.engine.client.api.ExpressionExecutorFilter;
import com.liukx.expression.engine.client.config.props.ExpressionProperties;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.ExpressionFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.ExpressionContextResult;
import com.liukx.expression.engine.core.enums.MetricKeyEnum;
import com.liukx.expression.engine.core.utils.MetricHelper;

import java.util.concurrent.TimeUnit;

/**
 * 慢表达式指标过滤器，基于阈值记录单个表达式的慢调用指标。
 * <p>
 * 通过 {@code spring.plugin.express.slow-expression-threshold-ms} 配置阈值（默认 100ms），
 * 超过阈值的表达式调用会被记录到 {@code expression_slow_execution} Summary 指标中，
 * 便于在 Grafana 中按慢调用次数排行和分析。设为 0 或负数则禁用。
 *
 * @author liukaixiong
 * @date 2026/4/20
 */
public class SlowExpressionMetricFilter implements ExpressionExecutorFilter {

    private final ExpressionProperties properties;

    public SlowExpressionMetricFilter(ExpressionProperties properties) {
        this.properties = properties;
    }

    @Override
    public ExpressionContextResult doExpressionFilter(ExpressionEnvContext env,
                                                      ExpressionConfigInfo configInfo,
                                                      ExpressionConfigTreeModel configTreeModel,
                                                      ExpressionBaseRequest request,
                                                      ExpressionFilterChain chain) {
        final long slowExpressionThresholdMs = properties.getSlowExpressionThresholdMs();
        if (slowExpressionThresholdMs <= 0) {
            return chain.doFilter(env, configInfo, configTreeModel, request);
        }

        long startNanos = System.nanoTime();
        ExpressionContextResult result = chain.doFilter(env, configInfo, configTreeModel, request);
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

        if (durationMs >= slowExpressionThresholdMs) {
            MetricHelper.record(MetricKeyEnum.expression_slow_execution, durationMs,
                    "serviceName", nullSafe(request.getServiceName()),
                    "businessCode", nullSafe(request.getBusinessCode()),
                    "executorCode", nullSafe(request.getExecutorCode()),
                    "expressionCode", nullSafe(configTreeModel.getExpressionCode()),
                    "expressionType", nullSafe(configTreeModel.getExpressionType()),
                    "expressionId", String.valueOf(configTreeModel.getExpressionId())
            );
        }

        return result;
    }

    private static String nullSafe(String value) {
        return value != null ? value : "";
    }
}
