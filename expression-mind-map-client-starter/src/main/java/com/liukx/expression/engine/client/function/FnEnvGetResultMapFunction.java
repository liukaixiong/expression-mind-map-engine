package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 获取设置的上下文结果集中的数据
 *
 * @author liukaixiong
 * @date 2024/9/24 - 17:41
 */
@Component
public class FnEnvGetResultMapFunction extends AbstractSimpleFunction {

    @Override
    protected boolean isAllowedCache() {
        return false;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.ENV_GET_RESULT_MAP_VALUE;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        String group = getConvertValue(funArgs, 0, String.class);
        String key = getConvertValue(funArgs, 1, String.class);
        Map<String, Object> groupContext = (Map<String, Object>) env.getResultContext().computeIfAbsent(group, k -> new HashMap<>());
        final Object object = groupContext.get(key);
        env.recordTraceDebugContent(getName(), "debug", String.format("查询属性: [%s] - %s = %s", group, key, object));
        return object;
    }
}
