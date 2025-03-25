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
 * 将字符串对象转换成json对象
 *
 * @author liukaixiong
 * @date 2025/3/18 - 13:50
 */
@Component
public class FnStrToJsonFunction extends AbstractSimpleFunction {
    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        String jsonStr = getArgsIndexValue(funArgs, 0);
        env.recordTraceDebugContent(getName(), "record", String.format("jsonStr=%s", jsonStr));
        return Jsons.parseMap(jsonStr);
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.OBJECT_STR_TO_JSON;
    }
}
