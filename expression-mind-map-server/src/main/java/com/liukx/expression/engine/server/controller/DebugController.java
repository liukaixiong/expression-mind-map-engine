package com.liukx.expression.engine.server.controller;

import com.liukx.expression.engine.client.debug.dto.DebugRequest;
import com.liukx.expression.engine.core.api.model.ApiResult;
import com.liukx.expression.engine.core.consts.ExpressionConstants;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorBaseInfo;
import com.liukx.expression.engine.server.model.dto.request.DebugExecuteRequest;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionExecutorConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 服务端远程调试控制器
 *
 * @author liukaixiong
 */
@Api(tags = "远程调试管理")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/debug")
public class DebugController {

    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExpressionExecutorConfigService executorConfigService;

    /**
     * 远程调试表达式执行
     *
     * @param request 调试请求
     * @return 执行结果
     */
    @PostMapping("/execute")
    @ApiOperation("远程调试表达式执行")
    public RestResult<Object> remoteDebug(@RequestBody @Validated DebugExecuteRequest request) {
        // 1. 获取客户端服务地址
        String clientUrl;
        if (StringUtils.isNotBlank(request.getServiceAddress())) {
            clientUrl = request.getServiceAddress();
        } else {
            clientUrl = getServiceAddressFromNacos(request.getServiceName());
        }

        // 2. 构建客户端请求
        DebugRequest clientRequest = new DebugRequest();
        clientRequest.setToken(request.getToken());
        clientRequest.setExpression(request.getExpression());
        clientRequest.setContext(request.getContext());

        // 3. 调用客户端
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<DebugRequest> requestEntity = new HttpEntity<>(clientRequest, headers);

            @SuppressWarnings("rawtypes")
            ResponseEntity<ApiResult> response = restTemplate.postForEntity(
                    clientUrl + ExpressionConstants.CLIENT_DEBUG_EXECUTOR,
                    requestEntity,
                    ApiResult.class
            );

            @SuppressWarnings("unchecked")
            ApiResult apiResult = response.getBody();
            if (apiResult != null && apiResult.getCode() == 200) {
                return RestResult.ok(apiResult.getData());
            } else {
                return RestResult.failed("调试失败: " + (apiResult != null ? apiResult.getMessage() : "未知错误"));
            }

        } catch (Exception e) {
            return RestResult.failed("调用客户端失败: " + e.getMessage());
        }
    }

    /**
     * 获取执行器绑定的服务信息
     *
     * @param executorId 执行器ID
     * @return 执行器服务信息
     */
    @PostMapping("/executor-info")
    @ApiOperation("获取执行器绑定的服务信息")
    public RestResult<Map<String, Object>> getExecutorInfo(@RequestParam("executorId") Long executorId) {
        ExpressionExecutorBaseInfo executorInfo = executorConfigService.getById(executorId);
        if (executorInfo == null) {
            return RestResult.failed("执行器不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("serviceName", executorInfo.getServiceName());
        result.put("businessCode", executorInfo.getBusinessCode());
        result.put("executorCode", executorInfo.getExecutorCode());
        result.put("executorDescription", executorInfo.getExecutorDescription());

        // 如果启用了Nacos，尝试获取服务实例
        if (discoveryClient != null && StringUtils.isNotBlank(executorInfo.getServiceName())) {
            List<ServiceInstance> instances = discoveryClient.getInstances(executorInfo.getServiceName());
            if (!CollectionUtils.isEmpty(instances)) {
                List<Map<String, String>> instanceList = instances.stream()
                        .map(instance -> {
                            Map<String, String> instanceInfo = new HashMap<>();
                            instanceInfo.put("host", instance.getHost());
                            instanceInfo.put("port", String.valueOf(instance.getPort()));
                            instanceInfo.put("url", "http://" + instance.getHost() + ":" + instance.getPort());
                            return instanceInfo;
                        })
                        .collect(Collectors.toList());
                result.put("serviceInstances", instanceList);
            }
        }

        return RestResult.ok(result);
    }

    /**
     * 获取Nacos注册中心的所有服务列表
     *
     * @return 服务列表
     */
    @PostMapping("/services")
    @ApiOperation("获取Nacos注册中心的所有服务列表")
    public RestResult<List<Map<String, Object>>> getNacosServices() {
        if (discoveryClient == null) {
            return RestResult.ok(new ArrayList<>());
        }

        List<String> services = discoveryClient.getServices();
        List<Map<String, Object>> serviceList = new ArrayList<>();

        for (String service : services) {
            Map<String, Object> serviceInfo = new HashMap<>();
            serviceInfo.put("name", service);

            List<ServiceInstance> instances = discoveryClient.getInstances(service);
            if (!CollectionUtils.isEmpty(instances)) {
                List<Map<String, String>> instanceList = instances.stream()
                        .map(instance -> {
                            Map<String, String> instanceInfo = new HashMap<>();
                            instanceInfo.put("host", instance.getHost());
                            instanceInfo.put("port", String.valueOf(instance.getPort()));
                            instanceInfo.put("url", "http://" + instance.getHost() + ":" + instance.getPort());
                            return instanceInfo;
                        })
                        .collect(Collectors.toList());
                serviceInfo.put("instances", instanceList);
                serviceInfo.put("instanceCount", instances.size());
            }

            serviceList.add(serviceInfo);
        }

        return RestResult.ok(serviceList);
    }

    /**
     * 从Nacos获取服务地址
     *
     * @param serviceName 服务名
     * @return 服务地址
     */
    private String getServiceAddressFromNacos(String serviceName) {
        if (discoveryClient == null) {
            throw new RuntimeException("Nacos未启用，请手动输入服务地址");
        }

        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (CollectionUtils.isEmpty(instances)) {
            throw new RuntimeException("服务 " + serviceName + " 未在Nacos中找到实例");
        }

        ServiceInstance instance = instances.get(0);
        return "http://" + instance.getHost() + ":" + instance.getPort();
    }
}
