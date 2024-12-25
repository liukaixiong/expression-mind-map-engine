package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.utils.Jsons;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 打印调试信息
 *
 * @author liukaixiong
 * @date 2024/8/23 - 15:41
 */
@Component
public class FnDebugObjectFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        final Object envContextKey = getArgsIndexValue(funArgs, 0, env.getRequest());
        env.recordTraceDebugContent(getName(), "debug_object", envContextKey == null ? "" : Jsons.objToMap(envContextKey));
        return true;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.DEBUG_OBJECT;
    }
}
