//package com.liukx.expression.engine.client.engine;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.cloud.client.ServiceInstance;
//import org.springframework.cloud.client.discovery.DiscoveryClient;
//import org.springframework.util.CollectionUtils;
//
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
///**
// * 远端发现逻辑
// *
// * @author liukaixiong
// * @date 2022/8/25 - 10:13
// */
//@Deprecated
//public class DiscoveryClientService {
//    private Logger logger = LoggerFactory.getLogger(getClass());
//    @Autowired(required = false)
//    private DiscoveryClient discoveryClient;
//
//    private AtomicInteger randomIndex = new AtomicInteger();
//
//    /**
//     * 获取域名
//     *
//     * @param serverId
//     * @return
//     */
//    public String getDomain(String serverId) {
//        if (discoveryClient == null) {
//            return null;
//        }
//
//        List<ServiceInstance> instances = discoveryClient.getInstances(serverId);
//        if (CollectionUtils.isEmpty(instances)) {
//            logger.warn("无法从注册中心获取相关的节点信息 : {}", serverId);
//            return null;
//        }
//        int size = instances.size();
//        if (size == 1) {
//            return builderDomain(instances, 0);
//        } else {
//            int selectIndex = Math.floorMod(randomIndex.incrementAndGet(), size);
//            return builderDomain(instances, selectIndex);
//        }
//    }
//
//    private String builderDomain(List<ServiceInstance> instances, int selectIndex) {
//        ServiceInstance serviceInstance = instances.get(selectIndex);
//        return "http://"+serviceInstance.getHost() + ":" + serviceInstance.getPort();
//    }
//
//}
