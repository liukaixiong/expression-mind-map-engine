package com.liukx.expression.engine.client.function.env;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

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
        // 兼容单查询
        String key = getConvertValue(funArgs, 1, String.class, "");
        Object object;
        if (StringUtils.hasText(key)) {
            Map<String, Object> groupContext = (Map<String, Object>) env.getResultContext().computeIfAbsent(group, k -> new HashMap<>());
            object = groupContext.get(key);
        } else {
            object = env.getResultContext().get(group);
        }
        env.recordTraceDebugContent(getName(), "debug", String.format("查询属性: [%s] - %s = %s", group, key, object));
        return object;
    }
}
