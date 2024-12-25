package com.liukx.expression.engine.core.model;

import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;

import java.util.HashMap;
import java.util.Map;

public class ContextTemplateRequest {

    /**
     * 请求参数
     */
    private ExpressionBaseRequest request;

    /**
     * 上下文参数
     */
    private Map<String, Object> envContext = new HashMap<>();

    /**
     * 缓存参数
     */
    private Map<String, Object> cache = new HashMap<>();


    public Map<String, Object> getEnvContext() {
        return envContext;
    }

    public void setEnvContext(Map<String, Object> envContext) {
        this.envContext = envContext;
    }

    public Map<String, Object> getCache() {
        return cache;
    }

    public void setCache(Map<String, Object> cache) {
        this.cache = cache;
    }

    public ExpressionBaseRequest getRequest() {
        return request;
    }

    public void setRequest(ExpressionBaseRequest request) {
        this.request = request;
    }
}
