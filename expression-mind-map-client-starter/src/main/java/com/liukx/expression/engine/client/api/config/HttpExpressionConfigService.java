package com.liukx.expression.engine.client.api.config;

import com.liukx.expression.engine.client.api.RemoteExpressionConfigService;
import com.liukx.expression.engine.client.api.RemoteHttpService;
import com.liukx.expression.engine.client.enums.ExpressionConfigCallEnum;
import com.liukx.expression.engine.core.api.model.ConfigDiscoverRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.consts.ExpressionConstants;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 远端获取配置
 * 从服务端中获取相应的配置信息
 *
 * @author liukaixiong
 * @date 2024/9/10 - 13:45
 */
public class HttpExpressionConfigService implements RemoteExpressionConfigService {

    @Autowired
    private RemoteHttpService remoteHttpService;

    @Override
    public String configKey() {
        return ExpressionConfigCallEnum.http.name();
    }

    @Override
    public ExpressionConfigInfo getConfigInfo(String serviceName, String businessCode, String executorCode) {
        ConfigDiscoverRequest request = new ConfigDiscoverRequest();
        request.setServiceName(serviceName);
        request.setBusinessCode(businessCode);
        request.setExecutorCode(executorCode);
        return remoteHttpService.call(ExpressionConstants.ENGINE_SERVER_ID, ExpressionConstants.SERVER_CONFIG_DISCOVERY, request, ExpressionConfigInfo.class);
    }


}
