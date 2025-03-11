package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.client.config.props.ExpressionProperties;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;

/**
 * 远端表达式配置获取
 *
 * @author liukaixiong
 * @date 2024/9/10 - 13:33
 */
public interface RemoteExpressionConfigService {


    /**
     * 获取远端配置信息key
     * {@link ExpressionProperties#setExpressionConfigCall} 可通过配置文件指定具体实现
     * @return
     */
    public String configKey();

    /**
     * 获取远端配置信息
     * @param serviceName   服务名称
     * @param businessCode  业务组编码
     * @param executorCode  执行器编码
     * @return
     */
    public ExpressionConfigInfo getConfigInfo(String serviceName, String businessCode, String executorCode);

}
