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
public class FnDebugRequestFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        final String envContextKey = getArgsIndexValue(funArgs, 0, "request");
        final Object objectValue = env.getObjectValue(envContextKey);
        env.recordTraceDebugContent(getName(), envContextKey, objectValue == null ? "" : Jsons.objToMap(objectValue));
        return true;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.DEBUG_BODY;
    }
}
