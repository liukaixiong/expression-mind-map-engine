package com.liukx.expression.engine.client.collect;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.googlecode.aviator.utils.Reflector;
import com.liukx.expression.engine.client.api.ExpressionConfigExecutorIntercept;
import com.liukx.expression.engine.client.api.ExpressionExecutorPostProcessor;
import com.liukx.expression.engine.client.api.ExpressionFunctionPostProcessor;
import com.liukx.expression.engine.client.api.RemoteHttpService;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.factory.ExpressionExecutorFactory;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.ExpressionContextResult;
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
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * 执行器数据收集
 *
 * @author liukaixiong
 * @date 2024/7/17 - 16:58
 */
@Slf4j
public class ExecutorTraceCollectIntercept implements ExpressionConfigExecutorIntercept, ExpressionFunctionPostProcessor, ExpressionExecutorPostProcessor, InitializingBean {

    // 将 ThreadLocal 改造为 Stack 结构，支持嵌套调用
    private final ThreadLocal<Stack<ExpressionExecutorResultDTO>> resultLogThreadLocal = new TransmittableThreadLocal<Stack<ExpressionExecutorResultDTO>>() {
        @Override
        protected Stack<ExpressionExecutorResultDTO> initialValue() {
            return new Stack<>();
        }
    };
    @Autowired
    private RemoteHttpService remoteHttpService;
    @Value("${spring.application.name:unknown}")
    private String serviceName;
    @Autowired
    private ExpressionExecutorFactory executorFactory;

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
            log.warn("配置异常,请检查表达式配置相关链路!");
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

        // 使用栈结构压入当前执行器结果
        resultLogThreadLocal.get().push(dto);
    }

    @Override
    public void afterExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configTreeModelList) {
        if (!envContext.isEnableTrace()) {
            return;
        }

        try {
            // 从栈中弹出当前执行器结果
            Stack<ExpressionExecutorResultDTO> stack = resultLogThreadLocal.get();
            if (!stack.isEmpty()) {
                ExpressionExecutorResultDTO expressionExecutorResultDTO = stack.pop();
                ExpressionResultLogCollect.getInstance().add(expressionExecutorResultDTO);
            } else {
                log.warn("追踪日志数据构建异常!");
            }
        } catch (Exception e) {
            log.warn("追踪日志设置失败:{}", e.getMessage());
        } finally {
            // 安全清理：如果栈为空，则清理 ThreadLocal
            Stack<ExpressionExecutorResultDTO> stack = resultLogThreadLocal.get();
            if (stack.isEmpty()) {
                resultLogThreadLocal.remove();
            }
        }
    }

    @Override
    public void after(ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, ExpressionContextResult execute) {
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

    private void triggerResultLogCollect(ExpressionEnvContext envContext, ExpressionLogTypeEnum resultType, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, FunctionApiModel functionInfo, List<Object> funcArgs, Object execute) {
        try {
            if (!envContext.isEnableTrace()) {
                return;
            }

            // 从栈中获取当前执行器结果
            Stack<ExpressionExecutorResultDTO> stack = resultLogThreadLocal.get();
            if (stack.isEmpty()) {
                log.warn(">>> 执行器结果栈为空，无法记录日志! <<<");
                return;
            }
            Object result = execute;
            if (execute instanceof ExpressionContextResult) {
                ExpressionContextResult contextResult = ((ExpressionContextResult) execute);
                result = contextResult.getResult();
            }

            ExpressionResultLogDTO dto = new ExpressionResultLogDTO();
            dto.setExecutorId(configTreeModel.getExecutorId());
            dto.setResultType(resultType.name());
            dto.setExpressionConfigId(configTreeModel.getExpressionId());
            dto.setFunctionApiModel(functionInfo);
            // 这里需要过滤一些参数,aviator的某些函数类型会导致死循环
            if (!CollectionUtils.isEmpty(funcArgs)) {
                final List<Object> functionArgs = funcArgs.stream().filter(var -> !(var instanceof AviatorFunction)).collect(Collectors.toList());
                dto.setFuncArgs(functionArgs);
            }
            dto.setResult(result);

            if (resultType == ExpressionLogTypeEnum.expression) {
                final String expression = configTreeModel.getExpression();
                // 获取变量值
                if (execute instanceof ExpressionContextResult) {
                    dto.setExpression(expression);
                    ExpressionContextResult contextResult = ((ExpressionContextResult) execute);
                    final List<String> variableFullNames = contextResult.getVariableNameList();
                    if (CollectionUtil.isNotEmpty(variableFullNames)) {
                        final Map<String, Object> variableMap = variableFullNames.stream().collect(Collectors.toMap(var -> var, var -> getContextValue(envContext, var)));
                        dto.setDebugTraceContent(variableMap);
                    }
                }
                dto.setDescription(configTreeModel.getTitle());
            } else {
                final String functionInfoName = functionInfo.getName();
                dto.setExpression(functionInfoName);
                Map<String, Object> debugTraceInfo = envContext.getTraceDebugContent(functionInfoName);
                dto.setDebugTraceContent(debugTraceInfo);
            }

            if (execute instanceof Exception) {
                Exception e = (Exception) execute;
                dto.setResult(-1);
                Map<String, Object> debugTraceContent = dto.getDebugTraceContent();
                if (debugTraceContent == null) {
                    debugTraceContent = new HashMap<>();
                }
                debugTraceContent.put("errorMessage", e.getMessage());
                dto.setDebugTraceContent(debugTraceContent);
            }

            // 获取当前执行器结果并添加日志
            stack.peek().getResultLogList().add(dto);
        } catch (Exception e) {
            log.warn("追踪结果日志构建失败! -> {}", e.getMessage());
        }
    }

    private String getContextValue(ExpressionEnvContext envContext, String var) {
        try {
            final Object property = Reflector.getProperty(envContext.getSourceMap(), var);
            return property == null ? "null" : property.toString();
        } catch (Exception e) {
            log.warn("获取变量值异常:{}", e.getMessage());
        }
        return "null";
    }
}
