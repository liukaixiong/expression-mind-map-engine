package com.liukx.expression.engine.client.function.debug;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 描述:
 * 根据结果输出结果日志
 *
 * @author liukx
 * @date 2026/2/28 17:14
 */
@Component
public class FnDebugLogTestFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        String logName = getArgsIndexValue(funArgs, 0);
        Object logValue = getArgsIndexValue(funArgs, 1);
        env.recordTraceDebugContent(getName(), "debug_log", String.format("%s=%s", logName, logValue));
        return logValue;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.DEBUG_TEST_LOG;
    }
}
