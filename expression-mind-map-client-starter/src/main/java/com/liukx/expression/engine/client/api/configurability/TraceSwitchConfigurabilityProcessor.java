package com.liukx.expression.engine.client.api.configurability;

import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.enums.ConfigurabilitySwitchEnum;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;

/**
 * 动态开关追踪能力
 *
 * @author liukaixiong
 * @date 2024/9/11 - 11:36
 */
public class TraceSwitchConfigurabilityProcessor extends AbstractConfigurabilityProcessor {

    @Override
    public void configurabilityExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configInfo) {
        // 开启追踪能力
        envContext.enableTrace();
    }

    @Override
    public ConfigurabilitySwitchEnum configurabilityKey() {
        return ConfigurabilitySwitchEnum.enableTrace;
    }
}
