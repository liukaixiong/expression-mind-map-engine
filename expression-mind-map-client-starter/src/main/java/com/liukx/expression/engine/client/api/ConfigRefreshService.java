package com.liukx.expression.engine.client.api;

/**
 * 配置更新事件
 *
 * @author liukaixiong
 * @date 2023/12/13
 */
public interface ConfigRefreshService {


    /**
     * 事件更新触发
     *
     * @param businessCode 业务编码
     */
    public void trigger(String businessCode);

}
