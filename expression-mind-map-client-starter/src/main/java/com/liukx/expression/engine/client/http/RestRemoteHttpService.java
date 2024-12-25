package com.liukx.expression.engine.client.http;

import cn.hutool.core.convert.Convert;
import com.liukx.expression.engine.client.api.RemoteHttpService;
import com.liukx.expression.engine.client.config.props.ExpressionProperties;
import com.liukx.expression.engine.core.api.model.ApiResult;
import com.liukx.expression.engine.core.consts.ExpressionConstants;
import com.liukx.expression.engine.core.utils.Jsons;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * rest请求
 *
 * @author liukaixiong
 * @date 2023/12/12
 */
public class RestRemoteHttpService implements RemoteHttpService {
    private final Logger LOG = getLogger(RestRemoteHttpService.class);

    @Autowired
    @LoadBalanced
    private RestTemplate loadRestTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ExpressionProperties expressionProperties;

    @Override
    public <T> T call(String serviceName, String path, Object requestBody, Class<T> clazz) {

        String remoteEngineUrl = expressionProperties.getRemoteEngineUrl();

        RestTemplate rest;
        if (!StringUtils.hasText(remoteEngineUrl)) {
            remoteEngineUrl = "http://" + ExpressionConstants.ENGINE_SERVER_ID + path;
            rest = loadRestTemplate;
        } else {
            remoteEngineUrl = remoteEngineUrl + path;
            rest = restTemplate;
        }

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Type", "application/json");
        HttpEntity<String> stringHttpEntity = new HttpEntity<>(Jsons.toJsonString(requestBody), httpHeaders);
        LOG.debug("请求远端 -> URL : {} , response :{}", remoteEngineUrl, stringHttpEntity);

        ApiResult body = rest.postForObject(remoteEngineUrl, stringHttpEntity, ApiResult.class);

        LOG.debug("返回远端引擎结果 : {}", body);

        if (body != null && body.isOk()) {
            return Convert.convert(clazz, body.getData());
        }

        return null;


    }
}
