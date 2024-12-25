package com.liukx.expression.engine.core.api.model.api;

import java.util.HashMap;
import java.util.Map;

/**
 * 远程执行器参数 涵盖函数、变量执行
 */
public class RemoteExecutorRequest {

    private String name;

    private Map<String, Object> params = new HashMap<>();

    public RemoteExecutorRequest(String name) {
        this.name = name;
    }

    public RemoteExecutorRequest(String name, Map<String, Object> params) {
        this.name = name;
        this.params = params;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
