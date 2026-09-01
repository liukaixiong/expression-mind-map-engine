package com.liukx.expression.engine.client.function.debug;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 打印调试信息
 *
 * @author liukaixiong
 * @date 2025/10/27 - 14:47
 */
@Component
public class FnDebugLogFunction extends AbstractSimpleFunction {

    @Override
    protected boolean isAllowedCache() {
        return false;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        StringBuilder sb = new StringBuilder();
        for (Object funArg : funArgs) {
            sb.append("[").append(ObjectUtils.defaultIfNull(funArg, "null")).append("]");
        }
        env.recordTraceDebugContent(getName(), "debug_log", sb.toString());
        return true;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.DEBUG_LOG;
    }
}
