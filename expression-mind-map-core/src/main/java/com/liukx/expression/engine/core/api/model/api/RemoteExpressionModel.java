package com.liukx.expression.engine.core.api.model.api;

import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.enums.ExpressionKeyTypeEnums;

import java.util.List;

public class RemoteExpressionModel {

    /**
     * 请求参数
     */
    private ExpressionBaseRequest request;

    /**
     * 执行类型
     */
    private ExpressionKeyTypeEnums keyType;

    /**
     * 执行变量或者函数的请求参数
     */
    private List<RemoteExecutorRequest> executorRequests;

    public ExpressionBaseRequest getRequest() {
        return request;
    }

    public void setRequest(ExpressionBaseRequest request) {
        this.request = request;
    }

    public ExpressionKeyTypeEnums getKeyType() {
        return keyType;
    }

    public void setKeyType(ExpressionKeyTypeEnums keyType) {
        this.keyType = keyType;
    }

    public List<RemoteExecutorRequest> getExecutorRequests() {
        return executorRequests;
    }

    public void setExecutorRequests(List<RemoteExecutorRequest> executorRequests) {
        this.executorRequests = executorRequests;
    }
}
