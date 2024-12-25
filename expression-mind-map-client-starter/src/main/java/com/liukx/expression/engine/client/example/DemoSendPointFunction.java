package com.liukx.expression.engine.client.example;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.FunctionRequestDocumentModel;

import java.util.List;

/**
 * 发送积分案例
 *
 * @author liukaixiong
 * @date 2023/12/7
 */
public class DemoSendPointFunction extends AbstractSimpleFunction {

    /**
     * 具体业务逻辑执行
     * @param env             变量上下文
     * @param configTreeModel 表达式配置对象
     * @param request         请求参数
     * @param funArgs         函数变量
     * @return
     */
    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        /*
            以下是一些常用的工具类案例使用方式
         */
        // 获取第一个参数，如果没有获取到则会抛出异常。适用于必填参数是否填写.
        Object firstArgs = getArgsIndexValue(funArgs, 0);

        // 获取第二个参数，如果没有的话，则返回默认值，适用于兼容老的函数
        Long argsIndexValue = getArgsIndexValue(funArgs, 1, 100L);

        // 从上下文变量中获取上游注入的对象：FunctionRequestDocumentModel
        // 适用于针对特定的业务模型函数获取该业务的上下文参数对象
        FunctionRequestDocumentModel documentModel = env.getObject(FunctionRequestDocumentModel.class);
        // 可以加入追踪页面可查找的日志信息
        // getName() 函数名称 ,  key : 标识  value : 日志内容
        env.recordTraceDebugContent(getName(), "key", "内容");
        return null;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        // 函数名称定义
        return DemoFunDescDefinitionService.SEND_POINT;
    }

}
