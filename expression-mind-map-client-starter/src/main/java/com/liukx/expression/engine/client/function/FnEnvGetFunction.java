package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 获取变量
 *
 * @author liukaixiong
 * @date 2024/9/24 - 17:41
 */
@Component
public class FnEnvGetFunction extends AbstractSimpleFunction {
    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.ENV_GET_VALUE;
    }
    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        String key = getConvertValue(funArgs, 0, String.class);
        final Object objectValue = env.getObjectValue(key);
        env.recordTraceDebugContent(getName(), "debug", String.format("上下文中获取值: %s = %s", key, objectValue));
        return objectValue;
    }
}
