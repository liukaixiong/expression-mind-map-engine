package com.liukx.expression.engine.client.function.collection;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.helper.ListFunHelper;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

/**
 * 按指定字段对集合排序, 返回排好序的新集合, 默认升序
 * <pre>
 * fn_list_sort_field(orderList, 'amount', 'desc')
 * </pre>
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
@Component
public class FnListSortFieldFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        final List<Object> list = ListFunHelper.toList(getName(), funArgs.get(0));
        final String field = getConvertValue(funArgs, 1, String.class);
        final String order = getConvertValue(funArgs, 2, String.class, "asc");
        Comparator<Object> comparator = (a, b) ->
            ListFunHelper.compareValue(ListFunHelper.getFieldValue(a, field), ListFunHelper.getFieldValue(b, field));
        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        list.sort(comparator);
        return list;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.LIST_SORT_FIELD;
    }
}
