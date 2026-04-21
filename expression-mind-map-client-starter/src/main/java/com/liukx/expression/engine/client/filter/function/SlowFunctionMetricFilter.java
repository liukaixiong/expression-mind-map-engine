package com.liukx.expression.engine.client.filter.function;

import com.liukx.expression.engine.client.api.ExpressionFunctionFilter;
import com.liukx.expression.engine.client.config.props.ExpressionProperties;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.FunctionFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.enums.MetricKeyEnum;
import com.liukx.expression.engine.core.utils.MetricHelper;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 慢函数指标过滤器，基于阈值记录单个函数的慢调用指标。
 * <p>
 * 通过 {@code spring.plugin.express.slow-function-threshold-ms} 配置阈值（默认 500ms），
 * 超过阈值的函数调用会被记录到 {@code function_slow_execution} Summary 指标中，
 * 便于在 Grafana 中按慢调用次数排行和分析。设为 0 或负数则禁用。
 *
 * @author liukaixiong
 * @date 2026/4/20
 */
public class SlowFunctionMetricFilter implements ExpressionFunctionFilter {

    private final ExpressionProperties properties;

    public SlowFunctionMetricFilter(ExpressionProperties properties) {
        this.properties = properties;
    }

    @Override
    public Object doFunctionFilter(ExpressionEnvContext env,
                                   ExpressionConfigTreeModel configTreeModel,
                                   ExpressionBaseRequest request,
                                   FunctionApiModel functionInfo,
                                   List<Object> funcArgs,
                                   FunctionFilterChain chain) {
        final long slowFunctionThresholdMs = properties.getSlowFunctionThresholdMs();
        if (slowFunctionThresholdMs <= 0) {
            return chain.doFilter(env, configTreeModel, request, functionInfo, funcArgs);
        }

        long startNanos = System.nanoTime();
        Object result = chain.doFilter(env, configTreeModel, request, functionInfo, funcArgs);
        long durationMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);

        if (durationMs >= slowFunctionThresholdMs) {
            MetricHelper.record(MetricKeyEnum.function_slow_execution, durationMs,
                    "serviceName", nullSafe(request.getServiceName()),
                    "businessCode", nullSafe(request.getBusinessCode()),
                    "executorCode", nullSafe(request.getExecutorCode()),
                    "functionName", nullSafe(functionInfo.getName()),
                    "groupName", nullSafe(functionInfo.getGroupName())
            );
        }

        return result;
    }

    private static String nullSafe(String value) {
        return value != null ? value : "";
    }
}
