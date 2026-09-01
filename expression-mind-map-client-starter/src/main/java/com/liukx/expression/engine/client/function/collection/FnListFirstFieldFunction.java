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
 * 获取集合中首个满足过滤条件元素的指定字段值
 * <p>
 * 第三个参数支持两种取值形式:
 * <pre>
 * 1. lambda取值: 走表达式引擎自身取值, 无反射、支持计算表达式
 * fn_list_first_field(orderList, lambda(x) -> x.status == 'PAID' end, lambda(x) -> x.amount * x.count end)
 * 2. 字段名: 简单场景直接取字段, 支持a.b嵌套
 * fn_list_first_field(orderList, lambda(x) -> x.status == 'PAID' end, 'amount')
 * </pre>
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
@Component
public class FnListFirstFieldFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        AssertUtils.Function.isTrue(funArgs.size() > 2, "函数[" + getName() + "] 需要传递集合对象、过滤条件、取值lambda或字段名!");
        final List<Object> list = ListFunHelper.toList(getName(), funArgs.get(0));
        final Function<Object, Boolean> condition = getArgsIndexFunction(env, funArgs, 1, Object.class, Boolean.class);
        final Function<Object, Object> extractor = buildValueExtractor(env, funArgs);
        for (Object element : list) {
            if (Boolean.TRUE.equals(condition.apply(element))) {
                return extractor.apply(element);
            }
        }
        return null;
    }

    /**
     * 构建取值函数: 参数为lambda时走表达式引擎自身取值(无反射、可计算), 为字符串时按字段名提取
     */
    private Function<Object, Object> buildValueExtractor(ExpressionEnvContext env, List<Object> funArgs) {
        final Object extractArg = funArgs.get(2);
        if (extractArg instanceof String) {
            final String field = (String) extractArg;
            return element -> ListFunHelper.getFieldValue(element, field);
        }
        return getArgsIndexFunction(env, funArgs, 2, Object.class, Object.class);
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.LIST_FIRST_FIELD;
    }
}
