package com.liukx.expression.engine.client.variable;

import com.liukx.expression.engine.client.api.ExpressionVariableRegister;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.log.LogEventEnum;
import com.liukx.expression.engine.client.log.LogHelper;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;
import com.liukx.expression.engine.core.model.ContextTemplateRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 抽象的变量实现其
 *
 * @author liukaixiong
 * @date 2025/2/27 - 15:29
 */
public abstract class AbstractExpressionVariableContextProcessor implements ExpressionVariableRegister {

    @Override
    public List<VariableApiModel> loadVariableList() {
        final Enum<? extends VariableDefinitionalService> variableName = variableName();
        VariableApiModel variables = new VariableApiModel();
        variables.setName(variableName.name());
        variables.setGroupName(groupName());

        if (variableName instanceof VariableDefinitionalService) {
            VariableDefinitionalService variableDefinitionalService = (VariableDefinitionalService) variableName;
            variables.setDescribe(variableDefinitionalService.getVariableDescription());
            variables.setType(variableDefinitionalService.getVariableReturnType().getSimpleName());
        }

        return Collections.singletonList(variables);
    }

    @Override
    public boolean finderVariable(String name) {
        return variableName().name().equals(name);
    }

    @Override
    public Object invoke(String name, ContextTemplateRequest cache) {
        final Object processor = processor(name, cache.getRequest(), cache.getEnvContext());
        final ExpressionEnvContext expressionEnvContext = new ExpressionEnvContext(cache.getEnvContext());
        LogHelper.trace(expressionEnvContext, cache.getRequest(), LogEventEnum.VARIABLE_CALL, "add var put context {} = {}", name, processor);
        return processor;
    }

    @Override
    public String groupName() {
        return "var";
    }

    /**
     * 定义变量信息
     * @return
     */
    public abstract Enum<? extends VariableDefinitionalService> variableName();


    /**
     * 变量执行规则
     * @param name
     * @param request
     * @param envContext
     * @return
     */
    public abstract Object processor(String name, ExpressionBaseRequest request, Map<String, Object> envContext);

}
