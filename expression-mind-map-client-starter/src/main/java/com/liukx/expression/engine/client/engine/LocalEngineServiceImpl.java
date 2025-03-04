package com.liukx.expression.engine.client.engine;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.liukx.expression.engine.client.api.*;
import com.liukx.expression.engine.client.api.config.ExpressionConfigCallManager;
import com.liukx.expression.engine.client.enums.EngineCallType;
import com.liukx.expression.engine.client.enums.ExpressionCoxnfigurabilitySwitchEnum;
import com.liukx.expression.engine.client.factory.ExpressionExecutorFactory;
import com.liukx.expression.engine.client.helper.ConfigurabilityHelper;
import com.liukx.expression.engine.client.log.LogEventEnum;
import com.liukx.expression.engine.client.log.LogHelper;
import com.liukx.expression.engine.client.process.ExpressionFilterChain;
import com.liukx.expression.engine.core.api.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 本地执行方式
 *
 * @author liukaixiong
 * @date 2023/12/12
 */
public class LocalEngineServiceImpl implements ClientEngineInvokeService, ConfigRefreshService {
    private final Logger LOG = getLogger(LocalEngineServiceImpl.class);

    @Autowired
    private ExpressionConfigCallManager configCallManager;

    @Value("${spring.application.name:unknown}")
    private String serviceName;

    @Autowired
    private ExpressionExecutorFactory executorFactory;

    @Autowired
    private ExpressionAsyncThreadExecutor executor;

    @Autowired(required = false)
    private List<ExpressionConfigExecutorIntercept> executionCallbackList = new ArrayList<>();

    @Autowired(required = false)
    private List<ExpressionExecutorFilter> expressionExecutorFilters = new ArrayList<>();

    @Autowired(required = false)
    private List<ExpressionExecutorPostProcessor> executorPostProcessors = new ArrayList<>();

    @Override
    public EngineCallType type() {
        return EngineCallType.LOCAL;
    }

    @Override
    public void trigger(String businessCode) {

    }

    @Override
    public Object invoke(ClientExpressionSubmitRequest request, ExpressionEnvContext expressionEnvContext) {
        final String businessCode = request.getBusinessCode();
        final String eventName = request.getEventName();
        final String executorCode = request.getExecutorCode();
        final Long userId = request.getUserId();
        final String unionId = request.getUnionId();
        ExpressionBaseRequest baseRequest = new ExpressionBaseRequest();
        baseRequest.setServiceName(serviceName);
        baseRequest.setBusinessCode(businessCode);
        baseRequest.setEventName(eventName);
        baseRequest.setExecutorCode(executorCode);
        baseRequest.setUnionId(unionId);
        baseRequest.setUserId(userId);
        Supplier<String> defaultValueSupplier = () -> UUID.fastUUID().toString();
        baseRequest.setTraceId(ObjectUtil.defaultIfEmpty(request.getTraceId(), defaultValueSupplier));

        // 上游存在循环调用，会导致结果集混乱
        expressionEnvContext.clearAllExpressionFunctionCache();

        expressionEnvContext.setEventName(eventName);
        // 将请求入参注入到上下文中
        expressionEnvContext.setRequest(request.getRequest());
        expressionEnvContext.addEnvClassInfo(baseRequest);
        expressionEnvContext.addEnvClassInfo(request);

        // 仅支持单事件匹配，如果存在多事件请循环处理。
        expressionEnvContext.addEnvContext("event", eventName);
        expressionEnvContext.addEnvContext("userId", userId);
        expressionEnvContext.addEnvContext("unionId", unionId);

        List<ExpressionConfigTreeModel> configTreeModelList;
        ExpressionConfigInfo configInfo = null;
        try {

            // 向引擎获取对应的业务编码相关的配置
            configInfo = getExpressionConfigInfo(expressionEnvContext.getSourceMap(), businessCode, executorCode);

            ExpressionConfigInfo finalConfigInfo = configInfo;

            executorPostProcessors.forEach(var -> var.beforeExecutor(expressionEnvContext, baseRequest, finalConfigInfo));

            Assert.notNull(configInfo, "业务编码：" + businessCode + ",远端配置获取失败");

            configTreeModelList = configInfo.getConfigTreeModelList();

            executorExpression(baseRequest, expressionEnvContext, configInfo, configTreeModelList);
        } finally {
            ExpressionConfigInfo finalConfigInfo = configInfo;
            executorPostProcessors.forEach(var -> var.afterExecutor(expressionEnvContext, baseRequest, finalConfigInfo));
        }
        return true;
    }

    @Override
    public Object invoke(ClientExpressionSubmitRequest request, Map<String, Object> envContext) {
        ExpressionEnvContext expressionEnvContext = new ExpressionEnvContext(envContext);
        return invoke(request, expressionEnvContext);
    }

    private ExpressionConfigInfo getExpressionConfigInfo(Map<String, Object> envContext, String businessGroupCode, String executorCode) {
        //允许同一个上下文获取时，进行结果缓存，这里其实可以适当抽象，后期再优化吧
        String key = "env_config_info:" + businessGroupCode;

        Object configObject = envContext.get(key);

        if (configObject != null) {
            LOG.debug("从本地缓存中获取引擎配置： {} , {} ", key, configObject);
            return (ExpressionConfigInfo) configObject;
        }

        ConfigDiscoverRequest baseRequest = new ConfigDiscoverRequest();
        baseRequest.setServiceName(serviceName);
        baseRequest.setBusinessCode(businessGroupCode);
        baseRequest.setExecutorCode(executorCode);

        return configCallManager.getConfigInfo(serviceName, businessGroupCode, executorCode);
    }


    /**
     * 执行表达式
     *
     * @param baseRequest
     * @param envContext
     * @param configInfo
     * @param configTreeModelList
     */
    private void executorExpression(ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, ExpressionConfigInfo configInfo, List<ExpressionConfigTreeModel> configTreeModelList) {
        if (configTreeModelList == null) {
            return;
        }

        ExpressionService expressionService = executorFactory.getExpressionService();
        for (ExpressionConfigTreeModel treeModel : configTreeModelList) {
            // 后续可以考虑做一些拓展，针对流程控制，允许跳过一些表达式,当然你可以自己设置一些流程分支的表达式去控制,那样也方便

            boolean isSkip = expressionProcessor(baseRequest, envContext, configInfo, treeModel, expressionService);

            // 进行流程控制
            if (!isSkip) {
                LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, String.format("[%s] 触发in_end全流程流程终止标记! ", treeModel.getTitle()));
                // 执行到当前分支结束
                envContext.forceEnd();
                break;
            }

            if (envContext.isForceEnd()) {
                LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, String.format("[%s] 触发force_end全流程终止标记! ", treeModel.getTitle()));
                break;
            }

            if (envContext.isReturnEnd()) {
                envContext.restReturnEnd();
                LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, String.format("[%s] 触发return_end,同级分支不在执行! ", treeModel.getTitle()));
                break;
            }


        }
    }

    private boolean expressionProcessor(ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel treeModel, ExpressionService expressionService) {
        Object execute;
        String expressionType = treeModel.getExpressionType();
        String expressionCode = treeModel.getExpressionCode();
        String expression = treeModel.getExpression();
        String title = treeModel.getTitle();

        try {
            // 将表达式配置对象注入到上下文中
            envContext.addEnvClassInfo(treeModel);

            executionCallbackList.forEach(var -> var.before(treeModel, baseRequest, envContext));

            // 如果表达式是空的,那么默认认为是可执行的
            ExpressionFilterChain filterChain = new ExpressionFilterChain(expressionExecutorFilters, () -> expressionService.execute(expression, envContext.getSourceMap()));
            execute = StringUtils.isNotEmpty(expression) ? filterChain.doFilter(envContext, configInfo, treeModel, baseRequest) : true;

            executionCallbackList.forEach(var -> var.after(treeModel, baseRequest, envContext, execute));
        } catch (Exception e) {
            LogHelper.trace(envContext, baseRequest, LogEventEnum.CALL_ERROR, "[{}] error - [{}] [title:{}],[表达式:{}]", expressionType, expressionCode, title, expression);
            executionCallbackList.forEach(var -> var.error(treeModel, baseRequest, envContext, e));
            throw e;
        }

        LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, " [{}] [{}] [title:{}],[表达式:{}] -> 结果:[{}]", expressionType, expressionCode, title, expression, execute);

        // 是否终止下一个同级别分支
        boolean isBreakNextBranch = !(envContext.isTopEnd() && envContext.restTopEnd());

        if (execute instanceof Boolean && (Boolean) execute) {
            // 启用子分支的异步能力，这里需要注意的是：1、当前一级子分支的逻辑需要互不干扰启用才会有意义,如果你的逻辑是有依赖的，那么请不要启用异步能力。
            // 如果使用了fn_in_end,类似 break \ continue 等等, 会导致并发出错.请慎重!
            if (ConfigurabilityHelper.isEnableExpressionConfigurability(treeModel.getConfigurabilityMap(), ExpressionCoxnfigurabilitySwitchEnum.enableNodeAsync)) {
                if (!(envContext.getSourceMap() instanceof ConcurrentHashMap<String, Object>)) {
                    LOG.warn("[异步能力开启警告]当前环境上下文不是线程安全[非ConcurrentHashMap]的，可能会导致并发操作上下文出现异常，请注意检查！可以在源头上进行处理，比如使用ConcurrentHashMap!");
                }
                LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, " [{}] [{}] [title:{}],[表达式:{}] -> 启用子分支异步能力", expressionType, expressionCode, title, expression);
                final List<ExpressionConfigTreeModel> nodeExpressionList = treeModel.getNodeExpression();
                final List<CompletableFuture<Void>> taskList = nodeExpressionList.stream().map(nodeExpression -> CompletableFuture.runAsync(() -> expressionProcessor(baseRequest, envContext, configInfo, nodeExpression, expressionService), executor.executorService())).toList();
                CompletableFuture.allOf(taskList.toArray(new CompletableFuture[0])).join();
            } else {
                executorExpression(baseRequest, envContext, configInfo, treeModel.getNodeExpression());
            }
        }

        return isBreakNextBranch;
    }

}
