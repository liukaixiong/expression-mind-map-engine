package com.liukx.expression.engine.client.feature;

import com.google.common.base.Splitter;
import com.liukx.expression.engine.client.api.ExpressionExecutorPostProcessor;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.log.LogEventEnum;
import com.liukx.expression.engine.client.log.LogHelper;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 将执行器配置的变量注册到上下文中
 *
 * @author liukaixiong
 * @date 2025/2/25 - 17:50
 */
public class ExpressionVarConfigRegisterSupport implements ExpressionExecutorPostProcessor {

    @Override
    public void beforeExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configInfo) {
        if (StringUtils.isNotEmpty(configInfo.getVarDefinition())) {
            try {
                final Map<String, String> configEnvMap = Splitter.on(",").withKeyValueSeparator("=").split(configInfo.getVarDefinition());
                configEnvMap.forEach((key, value) -> {
                    if (envContext.getObjectValue(key) == null) {
                        envContext.addEnvContext(key, value);
                        LogHelper.trace(envContext, baseRequest, LogEventEnum.EXPRESSION_CALL, "注册执行器变量到上下文: {} = {}", key, value);
                    }
                });
            } catch (Exception e) {
                LogHelper.trace(envContext, baseRequest, LogEventEnum.CALL_ERROR, "注册执行器变量失败:" + e.getMessage(), configInfo.getVarDefinition());
            }
        }
    }

    @Override
    public void afterExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configTreeModelList) {

    }
}
