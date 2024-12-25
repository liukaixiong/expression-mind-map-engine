package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.core.api.model.api.VariableApiModel;

import java.util.List;

/**
 * 变量类型加载
 */
public interface ExpressVariableTypeDocumentLoader {

    /**
     * 加载函数表
     *
     * @return
     */
    List<VariableApiModel> loadList();

}
