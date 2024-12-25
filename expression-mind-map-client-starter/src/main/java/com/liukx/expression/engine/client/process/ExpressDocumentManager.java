package com.liukx.expression.engine.client.process;


import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.liukx.expression.engine.client.api.DocumentApiExecutor;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.api.ExpressVariableDocumentLoader;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressDocumentManager implements DocumentApiExecutor, InitializingBean {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * 基本上不会发生变化的缓存
     */
    private final Table<String, String, VariableApiModel> variableCache = HashBasedTable.create();

    private final Map<String, FunctionApiModel> functionApiCache = new HashMap<>();

    @Autowired(required = false)
    private List<ExpressVariableDocumentLoader> variableDocumentLoaders = new ArrayList<>();

    @Autowired(required = false)
    private List<ExpressFunctionDocumentLoader> functionDocumentLoaders = new ArrayList<>();

    @Override
    public VariableApiModel getVariableApi(String groupName, String name) {
        VariableApiModel variableApiModel = variableCache.get(groupName, name);

        if (variableApiModel == null) {
            variableApiModel = getDynamicVariableApi(groupName, name);
        }

        return variableApiModel;
    }

    @Override
    public FunctionApiModel getFunctionApi(String functionName) {
        return functionApiCache.getOrDefault(functionName, this.getDynamicFunction(functionName));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        registerStaticVariable();
        registerStaticFunction();
    }

    @Override
    public List<VariableApiModel> getAllVariableApi() {
        return getGroupVariableApi(null);
    }

    @Override
    public List<VariableApiModel> getGroupVariableApi(String group) {
        List<VariableApiModel> allApi = new ArrayList<>();
        for (ExpressVariableDocumentLoader variableDocumentLoader : variableDocumentLoaders) {
            List<VariableApiModel> variableApiModels = variableDocumentLoader.loadVariableList();
            if (variableApiModels != null) {
                allApi.addAll(variableApiModels);
            }
        }
        return allApi;
    }

    @Override
    public List<FunctionApiModel> getAllFunctionApi() {
        List<FunctionApiModel> allApi = new ArrayList<>();
        for (ExpressFunctionDocumentLoader functionDocumentLoader : functionDocumentLoaders) {
            List<FunctionApiModel> functionApiModelList = functionDocumentLoader.loadFunctionList();
            allApi.addAll(functionApiModelList);
        }
        return allApi;
    }

    private FunctionApiModel getDynamicFunction(Object functionName) {
        for (ExpressFunctionDocumentLoader functionDocumentLoader : functionDocumentLoaders) {
            if (functionDocumentLoader.isDynamicRefresh()) {
                List<FunctionApiModel> functionApiModelList = functionDocumentLoader.loadFunctionList();
                for (FunctionApiModel functionApiModel : functionApiModelList) {
                    if (functionApiModel.getName().equals(functionName)) {
                        return functionApiModel;
                    }
                }
            }
        }
        return null;
    }

    private VariableApiModel getDynamicVariableApi(String groupName, String name) {
        for (ExpressVariableDocumentLoader variableDocumentLoader : variableDocumentLoaders) {
            String group = variableDocumentLoader.groupName();
            if (groupName.equals(group)) {
                VariableApiModel variableInfo = variableDocumentLoader.getVariableInfo(groupName, name);
                if (variableInfo != null) {
                    return variableInfo;
                }
            }
        }
        return null;
    }

    /**
     * 注册不会发生变化的函数方法
     */
    private void registerStaticFunction() {
        for (ExpressFunctionDocumentLoader functionDocumentLoader : functionDocumentLoaders) {
            if (functionDocumentLoader.isDynamicRefresh()) {
                continue;
            }
            List<FunctionApiModel> functionApiModelList = functionDocumentLoader.loadFunctionList();
            for (FunctionApiModel apiModel : functionApiModelList) {
                FunctionApiModel functionApi = functionApiCache.put(apiModel.getName(), apiModel);
                if (functionApi != null) {
                    logger.warn("function api 重复存在 :{}", functionApi.getName());
                }
            }
        }
    }

    /**
     * 注册不会发生变化的变量
     */
    private void registerStaticVariable() {
        variableDocumentLoaders.forEach(variable -> {
            if (!variable.isDynamicRefresh()) {
                List<VariableApiModel> variableApiModelList = variable.loadVariableList();
                if (CollectionUtil.isNotEmpty(variableApiModelList)) {
                    for (VariableApiModel variableApiModel : variableApiModelList) {
                        VariableApiModel apiModel = variableCache
                                .put(variableApiModel.getGroupName(), variableApiModel.getName(), variableApiModel);
                        if (apiModel != null) {
                            logger.warn("variable api 重复存在 :{},{}", variableApiModel.getGroupName(),
                                    variableApiModel.getName());
                        }
                    }
                }
            }
        });
    }
}
