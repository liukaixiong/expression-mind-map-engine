package com.liukx.expression.engine.client.api;


import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;

import java.util.List;

public interface DocumentApiExecutor {

    public VariableApiModel getVariableApi(String groupName, String name);

    public FunctionApiModel getFunctionApi(String functionName);

    public List<VariableApiModel> getGroupVariableApi(String group);

    public List<VariableApiModel> getAllVariableApi();

    public List<FunctionApiModel> getAllFunctionApi();

}
