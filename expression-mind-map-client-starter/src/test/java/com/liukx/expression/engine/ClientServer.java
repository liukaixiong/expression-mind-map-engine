package com.liukx.expression.engine;

import cn.hutool.core.lang.UUID;
import com.liukx.expression.engine.client.config.ExpressionConfiguration;
import com.liukx.expression.engine.client.engine.ClientEngineFactory;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.core.api.model.ClientExpressionSubmitRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author liukaixiong
 * @date 2025/1/15 - 17:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ExpressionConfiguration.class, RestTemplateAutoConfiguration.class})
public class ClientServer {

    @Autowired
    private ClientEngineFactory clientEngine;

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
        request.setUnionId(UUID.fastUUID().toString());

        ExpressionEnvContext context = new ExpressionEnvContext(envContext);

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

}
