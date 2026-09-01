package com.liukx.expression.engine.client.function.collection;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.helper.ListFunHelper;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 提取集合指定字段值并用分隔符拼接成字符串, 默认逗号分割, 空值会被忽略
 * <pre>
 * fn_list_join_field(orderList, 'orderId', ',')
 * </pre>
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
@Component
public class FnListJoinFieldFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        final List<Object> list = ListFunHelper.toList(getName(), funArgs.get(0));
        final String field = getConvertValue(funArgs, 1, String.class);
        final String separator = getConvertValue(funArgs, 2, String.class, ",");
        List<String> parts = new ArrayList<>(list.size());
        for (Object element : list) {
            final Object value = ListFunHelper.getFieldValue(element, field);
            if (value != null) {
                parts.add(String.valueOf(value));
            }
        }
        return String.join(separator, parts);
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.LIST_JOIN_FIELD;
    }
}
