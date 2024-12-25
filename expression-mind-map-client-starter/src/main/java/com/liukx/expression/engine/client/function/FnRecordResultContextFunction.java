package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 记录结果到上下文能力
 *
 * @author liukaixiong
 * @date 2024/8/15 - 14:15
 */
@Component
@Slf4j
public class FnRecordResultContextFunction extends AbstractSimpleFunction {
    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.RECORD_RESULT_CONTEXT;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        final String key = getConvertValue(funArgs, 0, String.class);
        final Object val = getArgsIndexValue(funArgs, 1);
        env.recordResult(key, val);
        env.recordTraceDebugContent(getName(), "record", String.format("k=%s;v=%s", key, val));
        return true;
    }
}
