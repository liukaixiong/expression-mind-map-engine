package com.liukx.expression.engine.client.function.flow;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.enums.ContextKeyConstant;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.utils.AssertUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * 重定向分支处理
 *
 * @author liukaixiong
 * @date 2025.12.01 - 13:56
 */
@Component
public class FnRedirectFunction extends AbstractSimpleFunction {
    @Override
    protected boolean isAllowedCache() {
        return false;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        final String expressionCode = getArgsIndexValue(funArgs, 0);
        final Map<String, ExpressionConfigTreeModel> configExpressionCodeMap = env.getEnvClassInfo(ExpressionConfigInfo.class).getConfigExpressionCodeMap();
        final ExpressionConfigTreeModel expressionConfigTreeModel = configExpressionCodeMap.get(expressionCode);

        AssertUtils.Function.isTrue(expressionConfigTreeModel != null, "无法跳转,没有找到对应的表达式编码:" + expressionCode);

        env.recordBranchResult(configTreeModel.getExpressionId(), ContextKeyConstant.FUNCTION_REDIRECT_EXPRESSION_CODE, expressionCode);
        env.recordTraceDebugContent(getName(), "逻辑跳转分支", String.format("%s 分支重定向至 %s - %s", configTreeModel.getExpressionId(), expressionConfigTreeModel.getExpressionId(), expressionConfigTreeModel.getTitle()));
        return true;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.END_REDIRECT;
    }
}
