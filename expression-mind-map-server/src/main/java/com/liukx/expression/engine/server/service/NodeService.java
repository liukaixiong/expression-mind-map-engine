package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.server.service.model.node.NodeServiceDto;

/**
 * 节点管理
 * @author liukaixiong
 * @date : 2022/6/9 - 16:46
 */
public interface NodeService {

    /**
     * 获取节点信息
     *
     * @param serviceName
     * @return
     */
    public NodeServiceDto getNodeInfo(String serviceName);

    /**
     * 刷新对应的服务节点
     *
     * @param serviceName 节点名称
     */
    public void refreshNodeInfo(String serviceName);

    /**
     * 刷新所有服务
     */
    public void refreshNodeInfo();

}
