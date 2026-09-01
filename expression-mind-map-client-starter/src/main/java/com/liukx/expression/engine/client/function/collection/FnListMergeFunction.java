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
 * 合并多个集合返回新集合, 空集合会被忽略, 不修改原集合
 * <pre>
 * fn_list_merge(blackUsers, grayUsers, whiteUsers)
 * </pre>
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
@Component
public class FnListMergeFunction extends AbstractSimpleFunction {

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        List<Object> mergeList = new ArrayList<>();
        for (Object arg : funArgs) {
            mergeList.addAll(ListFunHelper.toList(getName(), arg));
        }
        return mergeList;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.LIST_MERGE;
    }
}
