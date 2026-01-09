package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 获取当前分支内部变量
 *
 * @author liukaixiong
 * @date 2024/9/24 - 17:41
 */
@Component
public class FnEnvGetBranchValueFunction extends AbstractSimpleFunction {
    @Override
    protected boolean isAllowedCache() {
        return false;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.ENV_GET_BRANCH_VALUE;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        String key = getArgsIndexValue(funArgs, 0);
        final Map<String, Object> branchResult = env.getBranchResult(configTreeModel.getExpressionId());
        final Object value = branchResult.get(key);
        env.recordTraceDebugContent(getName(), "debug", String.format("获取分支变量属性: %s = %s", key, value));
        return value;
    }
}
