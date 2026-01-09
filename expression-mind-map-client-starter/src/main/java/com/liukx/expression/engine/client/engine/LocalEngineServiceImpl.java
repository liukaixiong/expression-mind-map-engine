package com.liukx.expression.engine.client.engine;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.ttl.threadpool.TtlExecutors;
import com.liukx.expression.engine.client.api.*;
import com.liukx.expression.engine.client.api.config.ExpressionConfigCallManager;
import com.liukx.expression.engine.client.enums.ContextKeyConstant;
import com.liukx.expression.engine.client.enums.EngineCallType;
import com.liukx.expression.engine.client.enums.ExpressionCoxnfigurabilitySwitchEnum;
import com.liukx.expression.engine.client.enums.FlowControlEnum;
import com.liukx.expression.engine.client.factory.ExpressionExecutorFactory;
import com.liukx.expression.engine.client.helper.ConfigurabilityHelper;
import com.liukx.expression.engine.client.log.LogEventEnum;
import com.liukx.expression.engine.client.log.LogHelper;
import com.liukx.expression.engine.client.process.ExpressionFilterChain;
import com.liukx.expression.engine.client.process.ExpressionNodeFilterChain;
import com.liukx.expression.engine.core.api.model.*;
import com.liukx.expression.engine.core.utils.AssertUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    @Autowired(required = false)
    private List<ExpressionNodeExecutorFilter> expressionNodeExecutorFilters = new ArrayList<>();

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

            AssertUtils.Executor.isTrue(configInfo != null, "业务编码：" + businessCode + ",远端配置获取失败");

            configTreeModelList = configInfo.getConfigTreeModelList();

            configInfo.setConfigExpressionCodeMap(toCodeMap(configTreeModelList));

            expressionEnvContext.addEnvClassInfo(configInfo);

            executorExpression(baseRequest, expressionEnvContext, configInfo, configTreeModelList);
        } finally {
            ExpressionConfigInfo finalConfigInfo = configInfo;
            executorPostProcessors.forEach(var -> var.afterExecutor(expressionEnvContext, baseRequest, finalConfigInfo));
        }
        return true;
    }

    private Map<String, ExpressionConfigTreeModel> toCodeMap(List<ExpressionConfigTreeModel> treeModels) {
        Map<String, ExpressionConfigTreeModel> codeMap = new HashMap<>(64);
        toCodeMap(treeModels, codeMap);
        return codeMap;
    }

    /**
     * 递归获取所有节点（包括当前节点及其所有子节点）
     */
    private void toCodeMap(List<ExpressionConfigTreeModel> treeModels, Map<String, ExpressionConfigTreeModel> codeMap) {
        if (!CollectionUtils.isEmpty(treeModels)) {
            for (ExpressionConfigTreeModel treeModel : treeModels) {
                codeMap.put(treeModel.getExpressionCode(), treeModel);
                toCodeMap(treeModel.getNodeExpression(), codeMap);
            }
        }
    }


    @Override
    public Object invoke(ClientExpressionSubmitRequest request, Map<String, Object> envContext) {
        ExpressionEnvContext expressionEnvContext = ExpressionEnvContext.of(envContext);
        return invoke(request, expressionEnvContext);
    }

    private ExpressionConfigInfo getExpressionConfigInfo(Map<String, Object> envContext, String businessGroupCode, String executorCode) {
        //允许同一个上下文获取时，进行结果缓存，这里其实可以适当抽象，后期再优化吧
        String key = "env_config_info:" + businessGroupCode + ":" + executorCode;

        Object configObject = envContext.get(key);

        if (configObject != null) {
            LOG.debug("从本地缓存中获取引擎配置： {} , {} ", key, configObject);
            return (ExpressionConfigInfo) configObject;
        }

        ConfigDiscoverRequest baseRequest = new ConfigDiscoverRequest();
        baseRequest.setServiceName(serviceName);
        baseRequest.setBusinessCode(businessGroupCode);
        baseRequest.setExecutorCode(executorCode);

        final ExpressionConfigInfo configInfo = configCallManager.getConfigInfo(serviceName, businessGroupCode, executorCode);

        if (configInfo != null) {
            LOG.debug("加入本地缓存中获取引擎配置： {} , {} ", key, configInfo);
            envContext.put(key, configInfo);
        }

        return configInfo;
    }


    /**
     * 执行表达式
     *
     * @param baseRequest
     * @param envContext
     * @param configInfo
     * @param expressionConfigTreelList
     */
    private void executorExpression(ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, ExpressionConfigInfo configInfo, List<ExpressionConfigTreeModel> expressionConfigTreelList) {
        if (CollectionUtils.isEmpty(expressionConfigTreelList)) {
            return;
        }

        ExpressionService expressionService = executorFactory.getExpressionService();
        for (ExpressionConfigTreeModel treeModel : expressionConfigTreelList) {
            // 后续可以考虑做一些拓展，针对流程控制，允许跳过一些表达式,当然你可以自己设置一些流程分支的表达式去控制,那样也方便
            final FlowControlEnum flowControlEnum = expressionProcessor(baseRequest, envContext, configInfo, treeModel, expressionService);

            // 进行流程控制
            if (FlowControlEnum.IN_END == flowControlEnum) {
                LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, String.format("[%s] 触发in_end全流程流程终止标记! ", treeModel.getTitle()));
                // 执行到当前分支终止所有流程
                envContext.forceEnd();
                break;
            }

            if (envContext.isForceEnd() || FlowControlEnum.FORCE_END == flowControlEnum) {
                LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, String.format("[%s] 触发force_end全流程终止标记! ", treeModel.getTitle()));
                break;
            }

            // 这个标记只适合在没有子分支的情况下使用,同级别不再继续分支
            if (FlowControlEnum.RETURN_END == flowControlEnum) {
                LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, String.format("[%s] 触发 %s,同级分支不在执行! ", treeModel.getTitle(), flowControlEnum.name()));
                break;
            }

        }
    }

    private FlowControlEnum expressionProcessor(ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel treeModel, ExpressionService expressionService) {
        ExpressionContextResult executeResult;
        final Long expressionId = treeModel.getExpressionId();
        String expressionType = treeModel.getExpressionType();
        String expressionCode = treeModel.getExpressionCode();
        String expression = treeModel.getExpression();
        String title = treeModel.getTitle();
        Object execute = null;
        try {
            // 将表达式配置对象注入到上下文中
            envContext.addEnvThreadClassInfo(treeModel);

            // 清理当前分支存储结果
//            envContext.clearBranchResult(expressionId);

            executionCallbackList.forEach(var -> var.before(treeModel, baseRequest, envContext));

            // 如果表达式是空的,那么默认认为是可执行的
            ExpressionFilterChain filterChain = new ExpressionFilterChain(expressionExecutorFilters, () -> expressionService.execute(expression, envContext.getSourceMap()));
            if (StringUtils.isNotEmpty(expression)) {
                final ExpressionContextResult expressionContextResult = filterChain.doFilter(envContext, configInfo, treeModel, baseRequest);
                execute = expressionContextResult.getResult();
                executionCallbackList.forEach(var -> var.after(treeModel, baseRequest, envContext, expressionContextResult));
            } else {
                execute = true;
            }
        } catch (Exception e) {
            //LogHelper.trace(envContext, baseRequest, LogEventEnum.CALL_ERROR, "[{}] error - [{}] [title:{}],[表达式:{}]", expressionType, expressionId, title, expression);
            LOG.warn("[{}] error - [{}] [title:{}],[表达式:{}] => {}", expressionType, expressionId, title, expression, e.getMessage());
            executionCallbackList.forEach(var -> var.error(treeModel, baseRequest, envContext, e));
            throw e;
        }

        LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, " [{}] [{}] [title:{}],[表达式:{}] -> 结果:[{}]", expressionType, expressionId, title, expression, execute);

        final FlowControlEnum flowControl = calculateFlowControlInfo(envContext, treeModel);

        if (execute instanceof Boolean) {
            if ((Boolean) execute) {
                // 下面可能到时候还需要重构一版: 无法组合使用
                // 跳转分支的处理,从这里开始实现,目前来看这个功能有一定的场景实用性,但感觉不大,实现起来也比较麻烦.
                // 它的场景: 就是思维导图的方式无法直接把一些公用的逻辑抽离出来(比如A分支需要执行一段子分支逻辑C,B分支也需要执行一段子逻辑分支C)
                // 当然你可以通过各种复制粘贴的方式去冗余实现,但弊端就是不太好管理.而且追踪会覆盖.当然需要你设计好你的逻辑分支,避免重复进入公共分支去
                String redirectExpressionCode = (String) envContext.getBranchResult(expressionId).getOrDefault(ContextKeyConstant.FUNCTION_REDIRECT_EXPRESSION_CODE, "");
                if (StringUtils.isNotEmpty(redirectExpressionCode)) {
                    final ExpressionConfigTreeModel expressionConfigTreeModel = configInfo.getConfigExpressionCodeMap().get(redirectExpressionCode);
                    if (expressionConfigTreeModel != null) {
                        expressionProcessor(baseRequest, envContext, configInfo, expressionConfigTreeModel, expressionService);
                    }
                } else if (ConfigurabilityHelper.isEnableExpressionConfigurability(treeModel.getConfigurabilityMap(), ExpressionCoxnfigurabilitySwitchEnum.enableNodeAsync)) {
                    // 启用子分支的异步能力，这里需要注意的是：1、当前一级子分支的逻辑需要互不干扰启用才会有意义,如果你的逻辑是有依赖的，那么请不要启用异步能力。
                    // 如果使用了fn_in_end,类似 break \ continue 等等, 会导致并发出错.请慎重!
                    if (!(envContext.getSourceMap() instanceof ConcurrentHashMap)) {
                        LOG.warn("[异步能力开启警告]当前环境上下文不是线程安全[非ConcurrentHashMap]的，可能会导致并发操作上下文出现异常，请注意检查！可以在源头上进行处理，比如使用ConcurrentHashMap!");
                    }
                    LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, " [{}] [{}] [title:{}],[表达式:{}] -> 启用子分支异步能力", expressionType, expressionId, title, expression);
                    final List<ExpressionConfigTreeModel> nodeExpressionList = treeModel.getNodeExpression();
                    final List<CompletableFuture<Void>> taskList = nodeExpressionList.stream().map(nodeExpression -> CompletableFuture.runAsync(() -> expressionProcessor(baseRequest, envContext, configInfo, nodeExpression, expressionService), TtlExecutors.getTtlExecutorService(executor.executorService()))).collect(Collectors.toList());
                    CompletableFuture.allOf(taskList.toArray(new CompletableFuture[0])).join();
                } else if (ConfigurabilityHelper.isEnableExpressionConfigurability(treeModel.getConfigurabilityMap(), ExpressionCoxnfigurabilitySwitchEnum.enableGlobalLock)) {
                    // 如果启用了全局锁的能力,这部分就是应对并发的加锁的逻辑
                    // 这里是为了实现局部子分支统一管理比如说：上锁
                    ExpressionNodeFilterChain chain = new ExpressionNodeFilterChain(expressionNodeExecutorFilters, () -> {
                        executorExpression(baseRequest, envContext, configInfo, treeModel.getNodeExpression());
                        return null;
                    });
                    chain.doFilter(baseRequest, envContext, configInfo, treeModel, execute);
                } else {
                    // 未开启任何能力的逻辑
                    executorExpression(baseRequest, envContext, configInfo, treeModel.getNodeExpression());
                }
            }
        }

        return flowControl;
    }

    /**
     * 计算流程控制信息
     *
     * @param envContext
     * @param treeModel
     * @return
     */
    private FlowControlEnum calculateFlowControlInfo(ExpressionEnvContext envContext, ExpressionConfigTreeModel treeModel) {
//        FlowControlEnum flowControl = FlowControlEnum.GO_ON;
        // 是否终止下一个同级别分支
//        boolean isBreakNextBranch = !(envContext.isTopEnd() && envContext.restTopEnd());
        // 如果并发就有点难搞了-_-!

        FlowControlEnum flowControl = envContext.getBranchFlowResult(treeModel.getExpressionId());

//        if (envContext.isTopEnd() && envContext.restTopEnd()) {
//            flowControl = FlowControlEnum.IN_END;
//        } else if (envContext.isReturnEnd()) {
//            if (!CollectionUtils.isEmpty(treeModel.getNodeExpression())) {
//                flowControl = FlowControlEnum.IN_RETURN_END;
//            } else {
//                flowControl = FlowControlEnum.RETURN_END;
//            }
//            envContext.restReturnEnd();
//        }
        return flowControl;
    }

}
