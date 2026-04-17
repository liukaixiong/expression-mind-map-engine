package com.liukx.expression.engine.client.api.config;

import com.liukx.expression.engine.client.api.RemoteExpressionConfigService;
import com.liukx.expression.engine.client.config.props.ExpressionProperties;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.enums.MetricKeyEnum;
import com.liukx.expression.engine.core.utils.MetricHelper;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 表达式配置调用管理
 *
 * @author liukaixiong
 * @date 2024/9/11 - 15:41
 */
public class ExpressionConfigCallManager implements InitializingBean {

    @Autowired
    private List<RemoteExpressionConfigService> remoteExpressionConfigServiceList;

    private Map<String, RemoteExpressionConfigService> keyMap;

    @Autowired
    private ExpressionProperties expressionProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.keyMap = remoteExpressionConfigServiceList.stream().collect(Collectors.toMap(RemoteExpressionConfigService::configKey, i -> i));
    }

    public ExpressionConfigInfo getConfigInfo(String serviceName, String businessCode, String executorCode) {
        final String expressionConfigCall = expressionProperties.getExpressionConfigCall();
        return MetricHelper.timed(MetricKeyEnum.expression_config_query_call, () -> keyMap.get(expressionConfigCall).getConfigInfo(serviceName, businessCode, executorCode), "serviceName", serviceName, "businessCode", businessCode, "executorCode", executorCode, "callType", expressionConfigCall);
    }

}
