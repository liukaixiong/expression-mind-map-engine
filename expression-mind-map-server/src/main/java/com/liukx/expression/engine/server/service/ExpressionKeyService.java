package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.core.api.model.api.GlobalExpressionDocInfo;
import com.liukx.expression.engine.core.model.ContextTemplateRequest;
import com.liukx.expression.engine.server.service.model.function.FunctionInfoDto;
import com.liukx.expression.engine.server.service.model.variable.VariableInfoDto;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 表达式关键字管理
 * @author liukaixiong
 * @date : 2022/6/10 - 11:31
 */
public interface ExpressionKeyService {
    /**
     * 变量是否定义
     * @param key
     * @return
     */
    public boolean isVariableDefined(String key);

    /**
     * 函数是否定义
     * @param key
     * @return
     */
    public boolean isFunctionDefined(String key);

    /**
     * 获取变量定义
     * @param key
     * @return
     */
    public VariableInfoDto getVariableDefined(String key);

    /**
     * 加载所有变量定义
     * @return
     */
    public List<VariableInfoDto> loadAllVariableDefined();

    /**
     * 加载所有函数定义
     * @return
     */
    public List<FunctionInfoDto> loadAllFunctionDefined();

    /**
     * 获取函数的值
     * @param key
     * @param request
     * @param param
     * @return
     * @throws Exception
     */
    public Object invokeFunction(String key, ContextTemplateRequest request, Map<String, Object> param) throws Exception;

    /**
     * 变量执行
     * @param key
     * @param request
     * @return
     * @throws Exception
     */
    public Object invokeVariable(String key, ContextTemplateRequest request) throws Exception;

    /**
     * 多个变量执行
     * @param keys
     * @param request
     * @return
     * @throws Exception
     */
    public Map<String, Object> invokeVariable(Collection<String> keys, ContextTemplateRequest request) throws Exception;

    /**
     * 刷新远端变量
     * @param serviceName
     * @return
     */
    public boolean refreshDocument(GlobalExpressionDocInfo serviceName);


}
