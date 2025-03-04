package com.liukx.expression.engine;

import cn.hutool.core.lang.UUID;
import com.liukx.expression.engine.client.config.ExpressionConfiguration;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.factory.ExpressionExecutorFactory;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

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
public class ClientSpringCase {

    @Autowired
    private ExpressionExecutorFactory executorFactory;

    @Test
    public void executorRemoter() {
        final ExpressionService expressionService = executorFactory.getExpressionService();
        ExpressionBaseRequest request = new ExpressionBaseRequest();
        request.setExecutorCode("visible_bak");
        request.setBusinessCode("shop");
        request.setUserId(1L);
        request.setUnionId(UUID.fastUUID().toString());
        Map<String, Object> envContext = new HashMap<>();
        final ExpressionEnvContext expressionEnvContext = new ExpressionEnvContext(envContext);
        expressionEnvContext.addEnvClassInfo(request);
        final Object execute = expressionService.execute("fn_env_invoke_method(env_date_local_date_time,'getYear')", envContext);
        System.out.println(execute);
    }

}
