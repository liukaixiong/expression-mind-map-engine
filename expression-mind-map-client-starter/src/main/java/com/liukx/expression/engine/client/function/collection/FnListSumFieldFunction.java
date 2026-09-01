package com.liukx.expression.engine.client.function.collection;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.helper.ListFunHelper;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 对集合中指定数值字段求和, 空值元素会被忽略, 空集合返回0
 * <pre>
 * fn_list_sum_field(orderList, 'amount')
 * </pre>
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
@Component
public class FnListSumFieldFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        final List<Object> list = ListFunHelper.toList(getName(), funArgs.get(0));
        final String field = getConvertValue(funArgs, 1, String.class);
        BigDecimal sum = BigDecimal.ZERO;
        for (Object element : list) {
            final Object value = ListFunHelper.getFieldValue(element, field);
            if (value != null) {
                sum = sum.add(ListFunHelper.toBigDecimal(value));
            }
        }
        return sum;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.LIST_SUM_FIELD;
    }
}
