package com.liukx.expression.engine;

import cn.hutool.core.lang.UUID;
import com.liukx.expression.engine.client.config.ExpressionConfiguration;
import com.liukx.expression.engine.client.engine.ClientEngineFactory;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.core.api.model.ClientExpressionSubmitRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RedissonClient;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author liukaixiong
 * @date 2025/1/15 - 17:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RedissonAutoConfigurationV2.class, ExpressionConfiguration.class, RestTemplateAutoConfiguration.class})
public class ClientServer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ClientEngineFactory clientEngine;

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void executorRemoter() throws Exception {
        Map<String, Object> envContext = new HashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("username", "xxxx");
        requestMap.put("id", 123L);
        requestMap.put("age", 18);
        requestMap.put("created", new Date());

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setExecutorCode("visible_bak");
        request.setBusinessCode("shop");
        request.setUserId(1L);
        request.setRequest(requestMap);
        request.setEventName("redirect");
        request.setUnionId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);

        context.addEnvContext("test_env_id", 888L);
        context.addEnvContext("test_env_text", "nibudong");

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);

            System.out.println(resultContext);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.in.read();
    }

    @Test
    public void executorLockRemoter() throws Exception {
        Map<String, Object> envContext = new HashMap<>();

        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("username", "xxxx");
        requestMap.put("id", 123L);
        requestMap.put("age", 18);
        requestMap.put("created", new Date());

        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        request.setExecutorCode("visible_bak");
        request.setBusinessCode("shop");
        request.setUserId(1L);
        request.setRequest(requestMap);
        request.setUnionId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);

        context.addEnvContext("test_env_id", 888L);
        context.addEnvContext("test_env_text", "nibudong");

        for (int i = 0; i < 10; i++) {
            CompletableFuture.runAsync(() -> {
                final Map<String, Object> result = clientEngine.invoke(request, context);
                logger.info("result:{}", result);
            });
        }

        System.in.read();
    }

}
