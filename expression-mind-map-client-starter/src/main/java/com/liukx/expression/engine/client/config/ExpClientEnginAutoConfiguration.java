package com.liukx.expression.engine.client.config;

import com.liukx.expression.engine.client.api.ClientEngineInvokeService;
import com.liukx.expression.engine.client.api.RemoteExpressionConfigService;
import com.liukx.expression.engine.client.api.config.ExpressionConfigCallManager;
import com.liukx.expression.engine.client.api.config.HttpExpressionConfigService;
import com.liukx.expression.engine.client.api.config.RedisExpressionConfigService;
import com.liukx.expression.engine.client.api.configurability.TraceSwitchConfigurabilityProcessor;
import com.liukx.expression.engine.client.engine.ClientEngineFactory;
import com.liukx.expression.engine.client.engine.LocalEngineServiceImpl;
import com.liukx.expression.engine.client.http.RestRemoteHttpService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * 客户端引擎执行层
 *
 * @author liukaixiong
 * @date 2023/12/12
 */
@Configuration
public class ExpClientEnginAutoConfiguration {

    @Bean
    public ClientEngineFactory engineFactory() {
        return new ClientEngineFactory();
    }

    @Bean
    public ClientEngineInvokeService localEngineServiceImpl() {
        return new LocalEngineServiceImpl();
    }

    @Bean
    public RestRemoteHttpService remoteHttpService() {
        return new RestRemoteHttpService();
    }

    @Bean
    @LoadBalanced
    public RestTemplate loadRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public RemoteExpressionConfigService httpExpressionConfigService() {
        return new HttpExpressionConfigService();
    }

    @Bean
    @ConditionalOnBean(RedisTemplate.class)
    public RedisExpressionConfigService redisExpressionConfigService() {
        return new RedisExpressionConfigService();
    }

    @Bean
    public ExpressionConfigCallManager expressionConfigCallManager() {
        return new ExpressionConfigCallManager();
    }

    @Bean
    public TraceSwitchConfigurabilityProcessor tracingProcessor() {
        return new TraceSwitchConfigurabilityProcessor();
    }

}
