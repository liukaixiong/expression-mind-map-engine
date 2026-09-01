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
 * 判断集合中是否全部元素都满足条件, 空集合返回true
 * <pre>
 * fn_list_all_match(orderList, lambda(x) -> x.status == 'PAID' end)
 * </pre>
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
@Component
public class FnListAllMatchFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        AssertUtils.Function.isTrue(funArgs.size() > 1, "函数[" + getName() + "] 需要传递集合对象和过滤条件!");
        final List<Object> list = ListFunHelper.toList(getName(), funArgs.get(0));
        final Function<Object, Boolean> condition = getArgsIndexFunction(env, funArgs, 1, Object.class, Boolean.class);
        for (Object element : list) {
            if (!Boolean.TRUE.equals(condition.apply(element))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.LIST_ALL_MATCH;
    }
}
