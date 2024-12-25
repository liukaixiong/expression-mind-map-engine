package com.liukx.expression.engine.client.process;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;

import java.util.Collections;
import java.util.List;

/**
 * 抽象普通类型的函数
 *
 * @author liukaixiong
 * @date 2022/9/23 - 14:44
 */
@Deprecated
public abstract class AbstractJavaTypeFunction extends AbstractFunction implements ExpressFunctionDocumentLoader {

    protected String prefix() {
        return "fn";
    }

    @Override
    public String getName() {
        final FunctionApiModel functionApiModel = functionInfo();
        functionApiModel.setGroupName(prefix());
        return prefix() + functionApiModel.getName();
    }


    /**
     * 函数定义的详细描述
     *
     * @return
     */
    protected abstract FunctionApiModel functionInfo();

    @Override
    public List<FunctionApiModel> loadFunctionList() {
        final FunctionApiModel functionApiModel = functionInfo();
        functionApiModel.setName(getName());
        return Collections.singletonList(functionApiModel);
    }
}
