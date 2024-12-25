package com.liukx.expression.engine.client.api.configurability;

import com.liukx.expression.engine.client.api.ExpressionExecutorPostProcessor;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.enums.ConfigurabilitySwitchEnum;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 抽象的配置能力构建
 *
 * @author liukaixiong
 * @date 2024/9/11 - 11:12
 */
public abstract class AbstractConfigurabilityProcessor implements ExpressionExecutorPostProcessor {
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
        if (configurabilityMap != null) {
            List<String> list = (List<String>) configurabilityMap.getOrDefault("enableSwitch", Collections.emptyList());
            return !list.isEmpty() && list.contains(configurabilityKey().name());
        }
        return false;
    }

    @Override
    public void afterExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configTreeModelList) {

    }

    public abstract ConfigurabilitySwitchEnum configurabilityKey();
}
