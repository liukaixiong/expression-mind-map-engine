package com.liukx.expression.engine.client.process;


import com.liukx.expression.engine.client.api.ExpressionVariableRegister;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.model.ContextTemplateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionVariableManager implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired(required = false)
    private List<ExpressionVariableRegister> expressionVariableRegisterList;

    @Override
    public void afterPropertiesSet() throws Exception {
    }

    /**
     * 执行器 , 可以传递全局变量,防止多次执行内部出现重复调用
     *
     * @param variableName
     * @param contextTemplateCache
     * @return
     * @throws Exception
     */
    public Object process(String variableName, ContextTemplateRequest contextTemplateCache) {
        ExpressionVariableRegister expressionVariableFinder = getExpressionVariableRegister(variableName);
        if (expressionVariableFinder == null) {
            final ExpressionBaseRequest request = contextTemplateCache.getRequest();
            logger.warn("[{}-{}]变量名:{} 没有匹配到对应的执行器!", request.getBusinessCode(), request.getExecutorCode(), variableName);
            return null;
        }
        return expressionVariableFinder.invoke(variableName, contextTemplateCache);
    }

    /**
     * 批量执行器
     *
     * @param variableNameList
     * @param contextTemplateCache
     * @return
     */
    public Map<String, Object> processList(Collection<String> variableNameList, ContextTemplateRequest contextTemplateCache) {
        Map<String, Object> resultMap = new HashMap<>();
        for (String variableName : variableNameList) {
            final Object processResult = process(variableName, contextTemplateCache);
            if (!ObjectUtils.isEmpty(processResult)) {
                logger.debug("该变量:{} = {} 被加入到上下文中", variableName, processResult);
                resultMap.put(variableName, processResult);
            }
        }
        return resultMap;
    }

    public ExpressionVariableRegister getExpressionVariableRegister(String variableName) {
        return this.findVariableRegister("default", variableName);
    }

    /**
     * 从实现中获取
     *
     * @param groupName
     * @param variableName
     * @return
     */
    protected ExpressionVariableRegister findVariableRegister(String groupName, String variableName) {
        if (!CollectionUtils.isEmpty(expressionVariableRegisterList)) {
            for (int i = 0; i < expressionVariableRegisterList.size(); i++) {
                ExpressionVariableRegister expressionVariableRegister = expressionVariableRegisterList.get(i);
                if (expressionVariableRegister.isMatch(groupName) && expressionVariableRegister.finderVariable(variableName)) {
                    return expressionVariableRegister;
                }
            }
        }
        return null;
    }

}
