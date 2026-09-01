package com.liukx.expression.engine.client.function.collection;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.helper.ListFunHelper;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.utils.AssertUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * 获取集合中首个满足条件的元素
 * <pre>
 * fn_list_first(orderList, lambda(x) -> x.status == 'PAID' end)
 * </pre>
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
@Component
public class FnListFirstFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        AssertUtils.Function.isTrue(funArgs.size() > 1, "函数[" + getName() + "] 需要传递集合对象和过滤条件!");
        final List<Object> list = ListFunHelper.toList(getName(), funArgs.get(0));
        final Function<Object, Boolean> condition = getArgsIndexFunction(env, funArgs, 1, Object.class, Boolean.class);
        for (Object element : list) {
            if (Boolean.TRUE.equals(condition.apply(element))) {
                return element;
            }
        }
        return null;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.LIST_FIRST;
    }
}
