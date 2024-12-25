package com.liukx.expression.engine.server.service.impl;

import com.liukx.expression.engine.core.api.model.api.*;
import com.liukx.expression.engine.core.enums.ExpressionKeyTypeEnums;
import com.liukx.expression.engine.core.model.ContextTemplateRequest;
import com.liukx.expression.engine.server.executor.RemoteNodeExecutor;
import com.liukx.expression.engine.server.service.*;
import com.liukx.expression.engine.server.service.model.doc.ExpressionDocDto;
import com.liukx.expression.engine.server.service.model.function.FunctionInfoDto;
import com.liukx.expression.engine.server.service.model.variable.VariableInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 表达式关键字处理器
 * @author liukaixiong
 * @date : 2022/6/10 - 11:59
 */
@Service
@Slf4j
public class DefaultExpressionKeyService implements ExpressionKeyService {

    @Autowired
    private FunctionService functionService;
    @Autowired
    private RemoteNodeExecutor nodeExecutor;
    @Autowired
    private VariableService variableService;
    @Autowired
    private ExpressionDocService documentService;

    @Autowired
    private ExpressionVarTypeService varTypeService;

    @Override
    public boolean isVariableDefined(String key) {
        return variableService.getKeyInfo(key) != null;
    }

    @Override
    public boolean isFunctionDefined(String key) {
        return functionService.getKeyInfo(key) != null;
    }

    @Override
    public VariableInfoDto getVariableDefined(String key) {
        return variableService.getKeyInfo(key);
    }

    @Override
    public List<VariableInfoDto> loadAllVariableDefined() {
        return variableService.loadAllVariableDefined();
    }

    @Override
    public List<FunctionInfoDto> loadAllFunctionDefined() {
        return functionService.loadAllFunctionDefined();
    }

    @Override
    public boolean refreshDocument(GlobalExpressionDocInfo docInfo) {

        try {
            final String serviceName = docInfo.getServiceName();

            final List<VariableApiModel> variableApi = docInfo.getVariableApi();
            final List<FunctionApiModel> functionApi = docInfo.getFunctionApi();
            final List<VariableApiModel> typeApi = docInfo.getVariableTypeApi();

            List<ExpressionDocDto> expressionDocList = new ArrayList<>();
            List<ExpressionDocDto> expressionTypeList = new ArrayList<>();

            variableApi.stream().map(var -> {
                ExpressionDocDto expressionDocDto = new ExpressionDocDto();
                expressionDocDto.setServiceName(serviceName);
                expressionDocDto.setName(var.getName());
                expressionDocDto.setType("var");
                expressionDocDto.setDescribe(var.getDescribe());
                expressionDocDto.setGroupName(var.getGroupName());
                return expressionDocDto;
            }).forEach(expressionDocList::add);


            functionApi.stream().map(var -> {
                ExpressionDocDto expressionDocDto = new ExpressionDocDto();
                expressionDocDto.setServiceName(serviceName);
                expressionDocDto.setName(var.getName());
                expressionDocDto.setType("fn");
                expressionDocDto.setParams(var.getArgs());
                expressionDocDto.setDescribe(var.getDescribe());
                expressionDocDto.setExample(var.getExample());
                expressionDocDto.setGroupName(var.getGroupName());
                return expressionDocDto;
            }).forEach(expressionDocList::add);

            final boolean refresh = documentService.refresh(serviceName, expressionDocList);
            log.info("刷新服务:{} , 变量:{}个,函数:{}个,共:{}个", serviceName, variableApi.size(), functionApi.size(), expressionDocList.size());


            typeApi.stream().map(var -> {
                ExpressionDocDto expressionDocDto = new ExpressionDocDto();
                expressionDocDto.setServiceName(serviceName);
                expressionDocDto.setName(var.getName());
                expressionDocDto.setType("type");
                expressionDocDto.setDescribe(var.getDescribe());
                expressionDocDto.setGroupName(var.getGroupName());
                return expressionDocDto;
            }).forEach(expressionTypeList::add);
            final boolean refreshTypeResult = varTypeService.refresh(serviceName, typeApi);
            log.info("刷新服务:{} ,数据类型:{}个", serviceName, typeApi.size());
        } catch (Exception e) {
            log.error("节点数据同步失败", e);
        }
        return false;
    }

    @Override
    public Object invokeFunction(String key, ContextTemplateRequest request, Map<String, Object> param) throws Exception {
        FunctionInfoDto keyInfo = functionService.getKeyInfo(key);
        String serviceName = keyInfo.getServiceName();
        RemoteExpressionModel expressionModel = new RemoteExpressionModel();
        expressionModel.setKeyType(ExpressionKeyTypeEnums.FUNCTION);
        expressionModel.setRequest(request.getRequest());
        RemoteExecutorRequest remoteExecutorRequest = new RemoteExecutorRequest(key);
        remoteExecutorRequest.setParams(param);
        expressionModel.setExecutorRequests(Collections.singletonList(remoteExecutorRequest));
        final Map<String, Object> executor = nodeExecutor.executor(serviceName, expressionModel);
        return executor.get(key);
    }

    @Override
    public Object invokeVariable(String key, ContextTemplateRequest request) throws Exception {
        RemoteExpressionModel expressionModel = new RemoteExpressionModel();
        expressionModel.setKeyType(ExpressionKeyTypeEnums.VARIABLE);
        expressionModel.setRequest(request.getRequest());
        VariableInfoDto keyInfo = variableService.getKeyInfo(key);
        String serviceName = keyInfo.getServiceName();
        Map<String, Object> executor = nodeExecutor.executor(serviceName, expressionModel);
        return executor.get(key);
    }

    @Override
    public Map<String, Object> invokeVariable(Collection<String> keys, ContextTemplateRequest request) throws Exception {
        Map<String, Object> resultValueMap = new LinkedHashMap<>();
        RemoteExpressionModel expressionModel = new RemoteExpressionModel();
        expressionModel.setKeyType(ExpressionKeyTypeEnums.VARIABLE);
        expressionModel.setRequest(request.getRequest());
        Map<String, Set<VariableInfoDto>> serviceGroupMap = keys.stream().map(key -> variableService.getKeyInfo(key)).collect(Collectors.groupingBy(VariableInfoDto::getServiceName, Collectors.toSet()));

        // 按服务进行分组
        for (String serviceName : serviceGroupMap.keySet()) {
            List<RemoteExecutorRequest> remoteExecutorRequestList = new ArrayList<>();
            Set<VariableInfoDto> variableInfoList = serviceGroupMap.get(serviceName);
            variableInfoList.forEach(key -> {
                RemoteExecutorRequest remoteExecutorRequest = new RemoteExecutorRequest(key.getName());
                remoteExecutorRequestList.add(remoteExecutorRequest);
            });
            expressionModel.setExecutorRequests(remoteExecutorRequestList);
            Map<String, Object> result = nodeExecutor.executor(serviceName, expressionModel);
            resultValueMap.putAll(result);
        }
        return resultValueMap;
    }
}
