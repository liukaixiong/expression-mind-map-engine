package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 变量方法执行器
 * 当变量为一个对象，内部有一些特殊方法需要执行时，可以调用
 *
 * @author liukaixiong
 * @date 2024/9/24 - 17:41
 */
@Component
public class FnEnvMethodExecutorFunction extends AbstractSimpleFunction {
    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.ENV_INVOKE_METHOD;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        Object object = getArgsIndexValue(funArgs, 0);
        String methodName = getConvertValue(funArgs, 1, String.class);
        List<Object> methodArgs = getArgsIndexValue(funArgs, 2, Collections.emptyList());
        try {
            final Object value = MethodUtils.invokeMethod(object, true, methodName, methodArgs.toArray());
            env.recordTraceDebugContent(getName(), "invoke", String.format("value=%s", value));
            return value;
        } catch (Exception e) {
            env.recordTraceDebugContent(getName(), "error", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
