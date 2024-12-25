package com.liukx.expression.engine.server.executor.impl;

import com.liukx.expression.engine.server.enums.RemoteInvokeTypeEnums;
import com.liukx.expression.engine.server.service.model.node.NodeServiceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 内部服务调用
 *
 * @author liukaixiong
 * @date 2022/8/25 - 10:34
 */
@Component
@Slf4j
public class InternalServiceInvoke extends HttpRemoteInvoke {


    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @Override
    public String parseDomain(NodeServiceDto domain) {
        return "http://" + domain.getServiceName();
    }

    @Override
    public RemoteInvokeTypeEnums type() {
        return RemoteInvokeTypeEnums.INTERNAL;
    }

    @Override
    public RestTemplate getRestTemplate() {
        return this.restTemplate;
    }
}
