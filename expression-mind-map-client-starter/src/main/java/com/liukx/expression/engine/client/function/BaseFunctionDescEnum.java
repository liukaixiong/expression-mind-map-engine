package com.liukx.expression.engine.client.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;

import java.util.Arrays;


/**
 * 基础函数定义
 * 基础的函数就以默认的fn开头作为规范
 *
 * @author liukaixiong
 * @date 2023/12/7
 */
public enum BaseFunctionDescEnum implements ExpressFunctionDocumentLoader {

    END("base", "fn_end", "走完该分支流程之后结束", new String[]{}, "true || false", "fn_end()"),
    END_FORCE("base", "fn_force_end", "强制终止流程,不在继续执行任何流程", new String[]{}, "true || false", "fn_force_end()"),
    DEBUG_BODY("base", "debug_body", "打印请求参数", new String[]{"上下文key"}, "true || false", "debug_body('request')"),
    DEBUG_OBJECT("base", "debug_object", "打印请求参数", new String[]{"上下文对象"}, "true || false", "debug_object(request)"),
    SYS_DATE_HOUR_RANGE("base", "fn_sys_date_hour_range", "是否在小时时间范围处理(基于系统时间)", new String[]{"开始小时数", "结束小时数"}, "true || false", "fn_sys_date_hour_range('9','18')"),
    SYS_DATE_DAY_RANGE("base", "fn_sys_date_day_range", "是否在日期时间范围处理(基于系统时间)", new String[]{"开始日期", "结束日期"}, "true || false", "fn_sys_date_day_range('2024-08-21','2024-08-25')"),
    RECORD_RESULT_CONTEXT("base", "fn_record_result_context", "设置结果到上下文中", new String[]{"键", "值"}, "true || false", "fn_record_result_context('result','abc')"),
    ;
    private final FunctionApiModel functionApiModel;

    BaseFunctionDescEnum(String group, String code, String describe, String[] requestDesc, String returnDescribe, String example) {
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
