package com.liukx.expression.engine.client.function.collection;

import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import cn.hutool.core.convert.Convert;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.helper.ListFunHelper;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 集合去重, 可传递字段名按字段值去重, 不传字段名按元素本身去重, 保留首次出现的顺序
 * <pre>
 * fn_list_distinct_field(userList, 'userId')
 * </pre>
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
@Component
public class FnListDistinctFieldFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        final List<Object> list = ListFunHelper.toList(getName(), funArgs.get(0));
        final Object fieldArg = getArgsIndexOptionalValue(funArgs, 1);
        final String field = fieldArg == null ? null : Convert.convert(String.class, fieldArg);
        List<Object> result = new ArrayList<>(list.size());
        Set<Object> distinctKeys = new HashSet<>();
        for (Object element : list) {
            final Object key = field == null ? element : ListFunHelper.getFieldValue(element, field);
            if (distinctKeys.add(key)) {
                result.add(element);
            }
        }
        return result;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.LIST_DISTINCT_FIELD;
    }
}
