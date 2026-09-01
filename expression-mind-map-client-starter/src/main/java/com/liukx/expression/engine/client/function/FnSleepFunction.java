package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * 描述: 休眠
 *
 * @author liukx
 * @date 2025/2/13 14:22
 */
@Component
public class FnSleepFunction extends AbstractSimpleFunction {

    @Override
    protected boolean isAllowedCache() {
        return false;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        final Long ms = getArgsIndexValue(funArgs, 0);
        try {
            Thread.sleep(ms);
            env.recordTraceDebugContent(getName(), "sleep", ms);
        } catch (InterruptedException e) {
            return false;
        }
        return true;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.SYS_SLEEP;
    }
}
