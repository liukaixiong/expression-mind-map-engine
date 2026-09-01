package com.liukx.expression.engine.client.function.flow;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.exception.ExpressionException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 异常消息结束
 *
 * @author liukaixiong
 * @date 2025/1/17 - 16:07
 */
@Component
public class FnErrorExceptionFunction extends AbstractSimpleFunction {


    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.END_ERROR_EXCEPTION;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        String msg = getArgsIndexValue(funArgs, 0);
        env.forceEnd();
        env.recordTraceDebugContent(getName(), "record", String.format("抛出异常,并返回msg=%s", msg));
        throw new ExpressionException(msg);
    }
}
