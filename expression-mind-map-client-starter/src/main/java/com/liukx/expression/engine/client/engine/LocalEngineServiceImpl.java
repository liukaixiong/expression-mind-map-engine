package com.liukx.expression.engine.client.engine;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ObjectUtil;
import com.liukx.expression.engine.client.api.ClientEngineInvokeService;
import com.liukx.expression.engine.client.api.ConfigRefreshService;
import com.liukx.expression.engine.client.api.ExpressionConfigExecutorIntercept;
import com.liukx.expression.engine.client.api.ExpressionExecutorPostProcessor;
import com.liukx.expression.engine.client.api.config.ExpressionConfigCallManager;
import com.liukx.expression.engine.client.enums.EngineCallType;
import com.liukx.expression.engine.client.factory.ExpressionExecutorFactory;
import com.liukx.expression.engine.client.log.LogEventEnum;
import com.liukx.expression.engine.client.log.LogHelper;
import com.liukx.expression.engine.core.api.model.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Autowired(required = false)
    private List<ExpressionConfigExecutorIntercept> executionCallbackList = new ArrayList<>();

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
    public Object invoke(ClientExpressionSubmitRequest request, Map<String, Object> envContext) {
        final String businessCode = request.getBusinessCode();
        final String eventName = request.getEventName();
        final String executorCode = request.getExecutorCode();
        ExpressionBaseRequest baseRequest = new ExpressionBaseRequest();
        baseRequest.setServiceName(serviceName);
        baseRequest.setBusinessCode(businessCode);
        baseRequest.setEventName(eventName);
        baseRequest.setExecutorCode(executorCode);
        baseRequest.setUnionId(request.getUnionId());
        baseRequest.setUserId(request.getUserId());
        Supplier<String> defaultValueSupplier = () -> UUID.fastUUID().toString();
        baseRequest.setTraceId(ObjectUtil.defaultIfEmpty(request.getTraceId(), defaultValueSupplier));

        ExpressionEnvContext expressionEnvContext = new ExpressionEnvContext(envContext);
        // 上游存在循环调用，会导致结果集混乱
        expressionEnvContext.clearAllExpressionFunctionCache();

        expressionEnvContext.setEventName(eventName);
        // 将请求入参注入到上下文中
        expressionEnvContext.setRequest(request.getRequest());
        expressionEnvContext.addEnvClassInfo(baseRequest);
        expressionEnvContext.addEnvClassInfo(request);

        List<ExpressionConfigTreeModel> configTreeModelList;
        ExpressionConfigInfo configInfo = null;
        try {
            // 向引擎获取对应的业务编码相关的配置
            configInfo = getExpressionConfigInfo(expressionEnvContext.getSourceMap(), businessCode, executorCode);

            ExpressionConfigInfo finalConfigInfo = configInfo;

            executorPostProcessors.forEach(var -> var.beforeExecutor(expressionEnvContext, baseRequest, finalConfigInfo));

            Assert.notNull(configInfo, "业务编码：" + businessCode + ",远端配置获取失败");

            // 仅支持单事件匹配，如果存在多事件请循环处理。
            envContext.put("event", eventName);

            configTreeModelList = configInfo.getConfigTreeModelList();

            executorExpression(baseRequest, expressionEnvContext, configTreeModelList, true);
        } finally {
            ExpressionConfigInfo finalConfigInfo = configInfo;
            executorPostProcessors.forEach(var -> var.afterExecutor(expressionEnvContext, baseRequest, finalConfigInfo));
        }
        return true;
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
        ExpressionConfigInfo configInfo = configCallManager.getConfigInfo(serviceName, businessGroupCode, executorCode);
        envContext.put(key, configInfo);
        return configInfo;
    }


    /**
     * 执行表达式
     *
     * @param baseRequest
     * @param envContext
     * @param configTreeModelList
     * @param isTopBranch
     */
    private void executorExpression(ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, List<ExpressionConfigTreeModel> configTreeModelList, boolean isTopBranch) {
        if (configTreeModelList == null) {
            return;
        }

        ExpressionService expressionService = executorFactory.getExpressionService();
        for (ExpressionConfigTreeModel treeModel : configTreeModelList) {
            String expressionType = treeModel.getExpressionType();
            String expressionCode = treeModel.getExpressionCode();
            String expression = treeModel.getExpression();
            String title = treeModel.getTitle();
            Object execute;

            if (envContext.isForceEnd() || (isTopBranch && envContext.isTopEnd())) {
                LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, " 触发end流程终止标记! ");
                break;
            }

            try {
                // 将表达式配置对象注入到上下文中
                envContext.addEnvClassInfo(treeModel);

                executionCallbackList.forEach(var -> var.before(treeModel, baseRequest, envContext));

                // 如果表达式是空的,那么默认认为是可执行的
                execute = StringUtils.isNotEmpty(expression) ? expressionService.execute(expression, envContext.getSourceMap()) : true;

                executionCallbackList.forEach(var -> var.after(treeModel, baseRequest, envContext, execute));
            } catch (Exception e) {
                LogHelper.trace(envContext, baseRequest, LogEventEnum.CALL_ERROR, "[{}] error - [{}] [title:{}],[表达式:{}]", expressionType, expressionCode, title, expression);
                executionCallbackList.forEach(var -> var.error(treeModel, baseRequest, envContext, e));
                throw e;
            }

            LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, " [{}] [{}] [title:{}],[表达式:{}] -> 结果:[{}]", expressionType, expressionCode, title, expression, execute);

            if (execute instanceof Boolean && (Boolean) execute) {
                executorExpression(baseRequest, envContext, treeModel.getNodeExpression(), false);
            }
        }
    }

}
