package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 描述:
 * 根据结果输出结果日志
 *
 * @author liukx
 * @date 2026/2/28 17:14
 */
@Component
public class FnListMapToString extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        Collection<Object> list = getArgsIndexValue(funArgs, 0);
        final Function<Object, Object> argsIndexFunction = getArgsIndexFunction(env, funArgs, 1, Object.class, Object.class);
        return list.stream().map(argsIndexFunction).collect(Collectors.toList());
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.LIST_STREAM_MAP;
    }
}
