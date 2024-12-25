package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;

import java.util.Collections;
import java.util.List;

public interface ExpressFunctionDocumentLoader {

    /**
     * 是否需要动态刷新
     *
     * @return
     */
    default boolean isDynamicRefresh() {
        return false;
    }

    /**
     * 加载函数表
     *
     * @return
     */
    default List<FunctionApiModel> loadFunctionList() {
        FunctionApiModel functionApiModel = loadFunctionInfo();
        if (functionApiModel != null) {
            return Collections.singletonList(functionApiModel);
        }
        return Collections.emptyList();
    }

    default FunctionApiModel loadFunctionInfo() {
        return null;
    }


}
