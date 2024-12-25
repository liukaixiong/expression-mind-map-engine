package com.liukx.expression.engine.client.example;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;

import java.util.Arrays;


/**
 * 基础使用方式
 *
 * @author liukaixiong
 * @date 2023/12/7
 */
public enum DemoFunDescDefinitionService implements ExpressFunctionDocumentLoader {

    SEND_POINT("activity", "send_point", "赠送积分", new String[]{"积分编码", "积分值"}, "true || false", "send_point('aa','50')");

    private FunctionApiModel functionApiModel;

    DemoFunDescDefinitionService(String group, String code, String describe, String[] requestDesc, String returnDescribe, String example) {
        FunctionApiModel def = new FunctionApiModel();
        def.setName(code);
        def.setGroupName(group);
        def.setDescribe(describe);
        def.setResultClassType(returnDescribe);
        def.setArgs(Arrays.asList(requestDesc));
        def.setExample(example);
        this.functionApiModel = def;
    }


    @Override
    public FunctionApiModel loadFunctionInfo() {
        return this.functionApiModel;
    }
}
