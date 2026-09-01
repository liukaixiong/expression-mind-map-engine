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

/**
 * 判断集合中是否包含指定值, 数值类型跨精度比较(Integer/Long/BigDecimal), 空集合返回false
 * <pre>
 * fn_list_contains(userRoles, 'admin')
 * </pre>
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
@Component
public class FnListContainsFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        AssertUtils.Function.isTrue(funArgs.size() > 1, "函数[" + getName() + "] 需要传递集合对象和判断的值!");
        final List<Object> list = ListFunHelper.toList(getName(), funArgs.get(0));
        final Object targetValue = funArgs.get(1);
        for (Object element : list) {
            if (ListFunHelper.equalsValue(element, targetValue)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.LIST_CONTAINS;
    }
}
