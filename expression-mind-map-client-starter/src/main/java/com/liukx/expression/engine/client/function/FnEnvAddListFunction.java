package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 设置集合到上下文变量中
 *
 * @author liukaixiong
 * @date 2024/9/24 - 17:41
 */
@Component
public class FnEnvAddListFunction extends AbstractSimpleFunction {
    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.ENV_ADD_LIST;
    }
    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        String key = getConvertValue(funArgs, 0, String.class);
        Object value = getConvertValue(funArgs, 1, Object.class);
        final Set<Object> code = (Set<Object>) env.getSourceMap().computeIfAbsent(key, k -> new HashSet<>());
        code.add(value);
        env.recordTraceDebugContent(getName(), "debug", String.format("新增属性到上下文中: %s = %s", key, code));
        return true;
    }
}
