package com.liukx.expression.engine;

import cn.hutool.core.lang.UUID;
import com.liukx.expression.engine.client.config.ExpressionConfiguration;
import com.liukx.expression.engine.client.engine.ClientEngineFactory;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.config.TestRedisConfig;
import com.liukx.expression.engine.core.api.model.ClientExpressionSubmitRequest;
import com.liukx.expression.engine.function.DemoExampleFunction;
import com.liukx.expression.engine.function.DemoMapExampleFunction;
import com.liukx.expression.engine.model.DemoModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.spring.starter.RedissonAutoConfigurationV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.client.RestTemplateAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模拟客户端示例
 *
 * @author liukaixiong
 * @date 2025/1/15 - 17:50
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {RedissonAutoConfigurationV2.class, TestRedisConfig.class, ExpressionConfiguration.class, RestTemplateAutoConfiguration.class, DemoExampleFunction.class, DemoMapExampleFunction.class})
public class ClientServer {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ClientEngineFactory clientEngine;

    /**
     * 演示示例:
     * 1、启动服务端【注意redis要连接上喔~具体配置=> src/test/resources/application.yml】
     * 2、执行器配置页面: <a href="http://localhost:20888/template/executor-list.html">进入页面</a>
     * 3、导入规则覆盖：demo_example.json => src/test/resources/demo_example.json
     * 4、执行该用例
     * 5、<a href="http://localhost:20888/template/trace-list.html">查看追踪结果</a>
     *
     * @throws Exception
     */
    @Test
    public void executorDemoExample() throws Exception {
        // 可构建HashMap或者ConcurrentMap,如果有异步需求的话
        Map<String, Object> envContext = new ConcurrentHashMap<>();
        DemoModel demoModel = new DemoModel();
        demoModel.setName("demo");
        demoModel.setAge(12);
        demoModel.setCreated(new Date());
        // 这里模拟一个入参请求
        Map<String, Object> requestMap = new HashMap<>();
        requestMap.put("username", "xxxx");
        requestMap.put("id", 123L);
        requestMap.put("age", 18);
        requestMap.put("created", new Date());

        // 这里是一个执行器请求
        ClientExpressionSubmitRequest request = new ClientExpressionSubmitRequest();
        // 这里是来自: http://localhost:20888/template/executor-list.html 定义的参数
        // 这里对应的是业务组
        request.setBusinessCode("demo");
        // 这里对应的是业务编码
        request.setExecutorCode("example");
        // 这里是用户编号，可以随意传 : 表达式中可通过 userId 获取
        request.setUserId(1314L);
        // 这里对应的是请求参数，表达式中可通过request.xxx 获取任意属性
        request.setRequest(requestMap);
        // 这里是事件名称，你可以理解为一个业务标识或者事件,比如活动编码 可通过event == 'redirect' 去条件区分
        request.setEventName("redirect");
        // 这里是业务编号,比如订单编号或者其他编号,这里只在追踪搜索的时候更快速定位到数据
        request.setUnionId("5201314");
        // 这里是追踪编号,可自行接入追踪系统
        request.setTraceId(UUID.fastUUID().toString());

        ExpressionEnvContext context = ExpressionEnvContext.of(envContext);
        // 这里可传递自定义的上下文变量即可在规则表达式中使用，比如： test_env_id  === 888
        context.addEnvContext("test_env_id", 888L);
        context.addEnvContext("test_env_text", "上下文传递的静态变量,从入口中传递的");
        context.addEnvContext("resourceNumber", 0);


        // 指定分支流程
//        context.addEnvContext("enableExampleDemo", Arrays.asList("flow_demo"));
        // 跑通全流程
        context.addEnvContext("enableExampleDemo", Arrays.asList("base_expression_demo", "base_fun_demo", "branch_fun_demo", "flow_demo"));


        /**
         *这里是将对象注入到上下文中，可在函数中获取，具体可参考：
         * {@link DemoExampleFunction}
         */
        context.addEnvClassInfo(demoModel);

        try {
            final Map<String, Object> resultContext = clientEngine.invoke(request, context);
            logger.info("从规则返回出来的结果参数:{}", resultContext);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.sleep(5000);
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
