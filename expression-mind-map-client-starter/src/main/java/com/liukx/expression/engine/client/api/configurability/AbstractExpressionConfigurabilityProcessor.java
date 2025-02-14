package com.liukx.expression.engine.client.api.configurability;

import com.liukx.expression.engine.client.api.ExpressionExecutorFilter;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.enums.ExpressionCoxnfigurabilitySwitchEnum;
import com.liukx.expression.engine.client.helper.ConfigurabilityHelper;
import com.liukx.expression.engine.client.process.ExpressionFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;

import java.util.Map;

/**
 * 抽象的配置能力构建
 *
 * @author liukaixiong
 * @date 2024/9/11 - 11:12
 */
public abstract class AbstractExpressionConfigurabilityProcessor implements ExpressionExecutorFilter {

    /**
     * 执行具体能力
     *
     * @param envContext
     * @param baseRequest
     * @param configInfo
     */
    public abstract Object configurabilityExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel configTreeModel, ExpressionFilterChain chain);

    protected boolean switchOn(ExpressionConfigTreeModel configInfo) {
        final Map<String, Object> configurabilityMap = configInfo.getConfigurabilityMap();
        return ConfigurabilityHelper.isEnableExpressionConfigurability(configurabilityMap, configurabilityKey());
    }

    @Override
    public Object doExpressionFilter(ExpressionEnvContext env, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, ExpressionFilterChain chain) {
        if (switchOn(configTreeModel)) {
            return configurabilityExecutor(env, request, configInfo, configTreeModel, chain);
        } else {
            return chain.doFilter(env, configInfo, configTreeModel, request);
        }
    }

    public abstract ExpressionCoxnfigurabilitySwitchEnum configurabilityKey();
}
