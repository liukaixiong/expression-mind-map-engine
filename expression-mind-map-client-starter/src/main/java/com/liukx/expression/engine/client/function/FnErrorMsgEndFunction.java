package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 异常消息结束
 *
 * @author liukaixiong
 * @date 2025/1/17 - 16:07
 */
@Component
public class FnErrorMsgEndFunction extends AbstractSimpleFunction {


    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.END_ERROR_MESSAGE;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        String msg = getArgsIndexValue(funArgs, 0);
        env.recordResult("success", false);
        env.recordResult("message", msg);
        env.forceEnd();
        env.recordTraceDebugContent(getName(), "record", String.format("强制停止流程,并返回msg=%s", msg));
        return true;
    }
}
