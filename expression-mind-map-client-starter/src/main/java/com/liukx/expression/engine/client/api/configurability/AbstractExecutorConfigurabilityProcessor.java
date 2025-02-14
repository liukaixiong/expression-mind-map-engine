package com.liukx.expression.engine.client.api.configurability;

import com.liukx.expression.engine.client.api.ExpressionExecutorPostProcessor;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.enums.ExecutorCoxnfigurabilitySwitchEnum;
import com.liukx.expression.engine.client.helper.ConfigurabilityHelper;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;

import java.util.Map;

/**
 * 抽象的配置能力构建
 *
 * @author liukaixiong
 * @date 2024/9/11 - 11:12
 */
public abstract class AbstractExecutorConfigurabilityProcessor implements ExpressionExecutorPostProcessor {
    @Override
    public void beforeExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configInfo) {
        if (switchOn(configInfo)) {
            configurabilityExecutor(envContext, baseRequest, configInfo);
        }
    }

    /**
     * 执行具体能力
     *
     * @param envContext
     * @param baseRequest
     * @param configInfo
     */
    public abstract void configurabilityExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configInfo);

    protected boolean switchOn(ExpressionConfigInfo configInfo) {
        final Map<String, Object> configurabilityMap = configInfo.getConfigurabilityMap();
        return ConfigurabilityHelper.isEnableExecutorConfigurability(configurabilityMap, configurabilityKey());
    }

    @Override
    public void afterExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configTreeModelList) {

    }

    public abstract ExecutorCoxnfigurabilitySwitchEnum configurabilityKey();
}
