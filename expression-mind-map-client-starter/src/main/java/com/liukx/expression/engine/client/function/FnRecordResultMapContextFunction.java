package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.*;


/**
 * 设置结果成一个组KY结构到结果集
 *
 * @author liukx
 */
@Component
public class FnRecordResultMapContextFunction extends AbstractSimpleFunction {
    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.RECORD_RESULT_MAP_CONTEXT;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        String group = getConvertValue(funArgs, 0, String.class);
        String key = getConvertValue(funArgs, 1, String.class);
        Object value = getConvertValue(funArgs, 2, Object.class);
        Map<String, Object> groupContext = (Map<String, Object>) env.getResultContext().computeIfAbsent(group, k -> new HashMap<>());

        if (value instanceof Collection<?>) {
            Set<Object> code = (Set<Object>) groupContext.computeIfAbsent(key, k -> new HashSet<>());
            code.addAll((Collection<?>) value);
        } else {
            groupContext.put(key, value);
        }
        env.recordTraceDebugContent(getName(), "debug", String.format("新增属性到上下文中: [%s] %s = %s", group, key, value));
        return true;
    }
}
