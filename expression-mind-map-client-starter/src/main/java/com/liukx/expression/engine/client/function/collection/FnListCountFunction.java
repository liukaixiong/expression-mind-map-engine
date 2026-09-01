package com.liukx.expression.engine.client.function.collection;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.helper.ListFunHelper;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

/**
 * 统计集合元素数量, 支持传递lambda条件统计满足条件的数量
 * <pre>
 * fn_list_count(orderList)                                          数量
 * fn_list_count(orderList, lambda(x) -> x.status == 'PAID' end)     满足条件的数量
 * </pre>
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
@Component
public class FnListCountFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        final List<Object> list = ListFunHelper.toList(getName(), funArgs.get(0));
        if (funArgs.size() <= 1) {
            return (long) list.size();
        }
        final Function<Object, Boolean> condition = getArgsIndexFunction(env, funArgs, 1, Object.class, Boolean.class);
        long count = 0;
        for (Object element : list) {
            if (Boolean.TRUE.equals(condition.apply(element))) {
                count++;
            }
        }
        return count;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.LIST_COUNT;
    }
}
