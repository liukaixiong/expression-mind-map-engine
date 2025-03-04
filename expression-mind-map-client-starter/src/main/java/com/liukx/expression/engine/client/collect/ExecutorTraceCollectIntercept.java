package com.liukx.expression.engine.client.collect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.liukx.expression.engine.client.api.ExpressionConfigExecutorIntercept;
import com.liukx.expression.engine.client.api.ExpressionExecutorPostProcessor;
import com.liukx.expression.engine.client.api.ExpressionFunctionPostProcessor;
import com.liukx.expression.engine.client.api.RemoteHttpService;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.api.ExpressionExecutorResultDTO;
import com.liukx.expression.engine.core.api.model.api.ExpressionResultLogCollect;
import com.liukx.expression.engine.core.api.model.api.ExpressionResultLogDTO;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.consts.ExpressionConstants;
import com.liukx.expression.engine.core.enums.ExpressionLogTypeEnum;
import com.liukx.expression.engine.core.utils.Jsons;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 执行器数据收集
 *
 * @author liukaixiong
 * @date 2024/7/17 - 16:58
 */
@Slf4j
public class ExecutorTraceCollectIntercept implements ExpressionConfigExecutorIntercept, ExpressionFunctionPostProcessor, ExpressionExecutorPostProcessor, InitializingBean {

    private final ThreadLocal<ExpressionExecutorResultDTO> resultLogThreadLocal = new InheritableThreadLocal<>();
    @Autowired
    private RemoteHttpService remoteHttpService;
    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @Override
    public void afterPropertiesSet() throws Exception {

        if (ExpressionConstants.ENGINE_SERVER_ID.equals(serviceName)) {
            return;
        }

        ThreadUtil.newSingleExecutor().execute(() -> {
            while (true) {
                try {
                    final List<ExpressionExecutorResultDTO> resultList = ExpressionResultLogCollect.getInstance().pollBatch(10);
                    if (!CollectionUtil.isEmpty(resultList)) {
                        remoteHttpService.call(ExpressionConstants.ENGINE_SERVER_ID, ExpressionConstants.SERVER_EXECUTOR_TRACE_SUBMIT_PATH, resultList, Object.class);
                    } else {
                        ThreadUtil.sleep(50);
                    }
                } catch (Exception e) {
                    log.error("执行器日志提交失败", e);
                } finally {
                    ThreadUtil.sleep(5);
                }
            }
        });
    }

    @Override
    public void beforeExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configInfo) {

        if (!envContext.isEnableTrace()) {
            return;
        }

        if (configInfo == null) {
            log.debug("配置异常,请检查表达式配置相关链路!");
            return;
        }
        ExpressionExecutorResultDTO dto = new ExpressionExecutorResultDTO();
        dto.setExecutorId(configInfo.getExecutorId());
        dto.setServiceName(configInfo.getServiceName());
        dto.setBusinessCode(configInfo.getBusinessCode());
        dto.setExecutorCode(configInfo.getExecutorCode());
        dto.setExecutorName(configInfo.getExecutorName());
        dto.setEventName(baseRequest.getEventName());
        dto.setUserId(baseRequest.getUserId());
        dto.setUnionId(baseRequest.getUnionId());
        dto.setTraceId(baseRequest.getTraceId());
        dto.setEnvBody(Jsons.toJsonString(envContext.getBusinessEnvContext()));
        resultLogThreadLocal.set(dto);
    }

    @Override
    public void afterExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configTreeModelList) {
        if (!envContext.isEnableTrace()) {
            return;
        }

        try {
            final ExpressionExecutorResultDTO expressionExecutorResultDTO = resultLogThreadLocal.get();
            if (expressionExecutorResultDTO != null) {
                ExpressionResultLogCollect.getInstance().add(expressionExecutorResultDTO);
            } else {
                log.warn("追踪日志数据构建异常!");
            }
        } catch (Exception e) {
            log.warn("追踪日志设置失败:{}", e.getMessage());
        } finally {
            resultLogThreadLocal.remove();
        }
    }

    @Override
    public void after(ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, Object execute) {
        triggerResultLogCollect(envContext, ExpressionLogTypeEnum.expression, configTreeModel, baseRequest, null, null, execute);
    }

    @Override
    public void error(ExpressionConfigTreeModel expressionType, ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, Exception e) {
        triggerResultLogCollect(envContext, ExpressionLogTypeEnum.expression, expressionType, baseRequest, null, null, e);
    }

    @Override
    public void afterFunction(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, FunctionApiModel functionInfo, List<Object> funcArgs, Object result) {
        triggerResultLogCollect(env, ExpressionLogTypeEnum.function, configTreeModel, request, functionInfo, funcArgs, result);
    }

    @Override
    public void functionError(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, FunctionApiModel functionInfo, List<Object> funcArgs, Exception e) {
        triggerResultLogCollect(env, ExpressionLogTypeEnum.function, configTreeModel, request, functionInfo, funcArgs, e);

    }

    private void triggerResultLogCollect(ExpressionEnvContext envContext, ExpressionLogTypeEnum resultType, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, FunctionApiModel functionInfo, List<Object> funcArgs, Object result) {
        try {
            if (!envContext.isEnableTrace()) {
                return;
            }
            ExpressionResultLogDTO dto = new ExpressionResultLogDTO();
            dto.setExecutorId(configTreeModel.getExecutorId());
            dto.setResultType(resultType.name());
            dto.setExpressionConfigId(configTreeModel.getExpressionId());
            dto.setFunctionApiModel(functionInfo);
            dto.setFuncArgs(funcArgs);
            dto.setResult(result);

            if (resultType == ExpressionLogTypeEnum.expression) {
                final String expression = configTreeModel.getExpression();
                final List<String> variableFullNames = AviatorEvaluator.compile(expression, true).getVariableNames();
                if (CollectionUtil.isNotEmpty(variableFullNames)) {
                    final Map<String, Object> variableMap = variableFullNames.stream().collect(Collectors.toMap(var -> var, var -> envContext.getValue(var) == null ? "null" : envContext.getValue(var)));
                    dto.setExpression(Jsons.toJsonString(variableMap));
                } else {
                    dto.setExpression(expression);
                }
                dto.setDescription(configTreeModel.getTitle());
            } else {
                final String functionInfoName = functionInfo.getName();
                dto.setExpression(functionInfoName);
                Map<String, Object> debugTraceInfo = envContext.getTraceDebugContent(functionInfoName);
                dto.setDebugTraceContent(debugTraceInfo);
            }

            if (result instanceof Exception) {
                Exception e = (Exception) result;
                dto.setResult(-1);
                Map<String, Object> debugTraceContent = dto.getDebugTraceContent();
                if (debugTraceContent == null) {
                    debugTraceContent = new HashMap<>();
                }
                debugTraceContent.put("errorMessage", e.getMessage());
                dto.setDebugTraceContent(debugTraceContent);
            }

            resultLogThreadLocal.get().getResultLogList().add(dto);
        } catch (Exception e) {
            log.debug("追踪结果日志构建失败! -> {}", e.getMessage());
        }
    }
}
