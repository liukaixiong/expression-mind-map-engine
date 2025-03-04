package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.core.api.model.api.VariableApiModel;

import java.util.List;

public interface ExpressVariableDocumentLoader extends GroupNameService {

    /**
     * 是否需要动态刷新
     *
     * @return
     */
    default boolean isDynamicRefresh() {
        return false;
    }

    /**
     * 加载变量列表
     *
     * @return
     */
    public List<VariableApiModel> loadVariableList();

    /**
     * 查看变量信息
     *
     * @param name
     * @return
     */
    default VariableApiModel getVariableInfo(String group, String name) {
        return null;
    }

}
