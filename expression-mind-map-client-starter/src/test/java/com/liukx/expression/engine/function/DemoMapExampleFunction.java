package com.liukx.expression.engine.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.example.DemoFunDescDefinitionService;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 发送积分案例
 *
 * @author liukaixiong
 * @date 2023/12/7
 */
@Component
public class DemoMapExampleFunction extends AbstractSimpleFunction {
    private final Logger logger = LoggerFactory.getLogger(DemoMapExampleFunction.class);

    /**
     * 具体业务逻辑执行
     *
     * @param env             变量上下文
     * @param configTreeModel 表达式配置对象
     * @param request         请求参数
     * @param funArgs         函数变量
     * @return
     */
    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        // 注意map函数必须是K,V成双成对类似Map.of()
        final Map<Object, Object> objectObjectMap = convertMap(funArgs);
        logger.info("Map参数:{}", objectObjectMap);

        final Map<String, Object> paramMap = (Map<String, Object>) objectObjectMap.getOrDefault("paramMap", Collections.emptyMap());
        logger.info("paramMap参数:{}", paramMap);
        env.recordTraceDebugContent(getName(), "mapObject", objectObjectMap);
        return true;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        // 函数名称定义
        return DemoFunDescDefinitionService.DEMO_MAP_EXAMPLE;
    }

}
