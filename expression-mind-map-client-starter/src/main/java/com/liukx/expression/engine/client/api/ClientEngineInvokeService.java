package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.enums.EngineCallType;
import com.liukx.expression.engine.core.api.model.ClientExpressionSubmitRequest;

import java.util.Map;

public interface ClientEngineInvokeService {

    EngineCallType type();

    /**
     * 引擎执行方法
     * {@link ClientEngineInvokeService#invoke(ClientExpressionSubmitRequest, ExpressionEnvContext)}
     *
     * @param request    请求参数
     * @param envContext 上下文事件
     * @return
     */
    @Deprecated
    default Object invoke(ClientExpressionSubmitRequest request, Map<String, Object> envContext) {
        return invoke(request, new ExpressionEnvContext(envContext));
    }

    /**
     * 执行引擎的入口
     * @param request
     * @param envContext
     * @return
     */
    Object invoke(ClientExpressionSubmitRequest request, ExpressionEnvContext envContext);
}
