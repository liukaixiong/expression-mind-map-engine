package com.liukx.expression.engine.client.process;

import cn.hutool.core.convert.Convert;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.api.ExpressionFunctionFilter;
import com.liukx.expression.engine.client.api.ExpressionFunctionPostProcessor;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.example.DemoFunDescDefinitionService;
import com.liukx.expression.engine.client.log.LogEventEnum;
import com.liukx.expression.engine.client.log.LogHelper;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 简单的函数定义
 *
 * @author liukaixiong
 * @date 2023/12/6
 */
public abstract class AbstractSimpleFunction extends AbstractVariadicFunction implements ExpressFunctionDocumentLoader {

    @Autowired(required = false)
    private List<ExpressionFunctionPostProcessor> functionPostProcessorList = new ArrayList<>();
    @Autowired(required = false)
    private List<ExpressionFunctionFilter> functionFilters = new ArrayList<>();

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        // 将函数变量转换成对应的java对象
        List<Object> funcArgs = convertArgsList(env, args);

        final ExpressionEnvContext expressionEnvContext = new ExpressionEnvContext(env);
        // 提取通用参数
        ExpressionBaseRequest request = expressionEnvContext.getEnvClassInfo(ExpressionBaseRequest.class);
        ExpressionConfigTreeModel configTreeModel = expressionEnvContext.getEnvClassInfo(ExpressionConfigTreeModel.class);
        final FunctionApiModel functionApiModel = loadFunctionInfo();
        try {
            // 将函数结果进行本地缓存,方便同一个线程的执行结果公用
            String cacheKey = generateCacheKey(expressionEnvContext, funcArgs, request);
            functionPostProcessorList.forEach(var -> var.functionBefore(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs));

            FunctionFilterChain functionFilterChain = new FunctionFilterChain(functionFilters, () -> functionCall(expressionEnvContext, cacheKey, configTreeModel, request, funcArgs));

            final Object processor = functionFilterChain.doFilter(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs);

            functionPostProcessorList.forEach(var -> var.afterFunction(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs, processor));

            // 包装结果
            return FunctionUtils.wrapReturn(processor);
        } catch (Exception e) {
            functionPostProcessorList.forEach(var -> var.functionError(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs, e));
            throw e;
        }
    }

    private Object functionCall(ExpressionEnvContext expressionEnvContext, String cacheKey, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funcArgs) {
        Object processor;
        // 是否缓存
        if (isAllowedCache()) {
            processor = expressionEnvContext.getFunctionCache(cacheKey);
            if (processor == null) {
                //子类识别处理
                processor = processor(expressionEnvContext, configTreeModel, request, funcArgs);
                LogHelper.trace(expressionEnvContext, request, LogEventEnum.FUNCTION_CALL, "function result {} 结果: {}", cacheKey, processor);
                expressionEnvContext.addFunctionCache(cacheKey, processor);
            } else {
                expressionEnvContext.recordTraceDebugContent(getName(), "命中本地缓存", cacheKey + "=" + processor);
                LogHelper.trace(expressionEnvContext, request, LogEventEnum.FUNCTION_CALL, "★★★命中缓存★★★ {} 中获取值: {}", cacheKey, processor);
            }
        } else {
            // 强制执行函数
            processor = processor(expressionEnvContext, configTreeModel, request, funcArgs);
            LogHelper.trace(expressionEnvContext, request, LogEventEnum.FUNCTION_CALL, "skip cache function result {} 结果: {}", cacheKey, processor);
        }
        return processor;
    }

    /**
     * 函数的缓存key设置，如果不满足，子类可以通过重写该方法，自己指定
     *
     * @param env
     * @param funcArgs
     * @param request
     * @return
     */
    protected String generateCacheKey(ExpressionEnvContext env, List<Object> funcArgs, ExpressionBaseRequest request) {
        return getName() + ":" + StringUtils.join(funcArgs, ":");
    }

    /**
     * 该函数是否允许被缓存，当然也可以重写{@link AbstractSimpleFunction#generateCacheKey(ExpressionEnvContext, List, ExpressionBaseRequest)}
     * 缓存函数永远命中不了也行
     *
     * @return
     */
    protected boolean isAllowedCache() {
        return true;
    }


    /**
     * 执行函数逻辑
     *
     * @param env             变量上下文
     * @param configTreeModel 表达式配置对象
     * @param request         请求参数
     * @param funArgs         函数变量
     * @return 对象       <b>尽可能转换成java对象</b>
     */
    public abstract Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs);

    /**
     * 定义该函数的所有相关内容:
     * <b>强烈建议使用枚举来定义相关的函数或者变量的描述</b>
     * 请定义一个枚举类，并且实现{@link ExpressFunctionDocumentLoader}
     * <b>如果你不清除如何去做请参考:{@link DemoFunDescDefinitionService}</b>
     *
     * @return
     */
    public abstract Enum<? extends ExpressFunctionDocumentLoader> documentRegister();

    @Override
    public String getName() {
        return ((ExpressFunctionDocumentLoader) documentRegister()).loadFunctionInfo().getName();
    }

    @Override
    public FunctionApiModel loadFunctionInfo() {
        return ((ExpressFunctionDocumentLoader) documentRegister()).loadFunctionInfo();
    }

    @Override
    public final boolean isDynamicRefresh() {
        return false;
    }

    /**
     * 加载集合类型的实现，但基于本接口，还是不要让子类去实现比较好。
     *
     * @return
     */
    @Override
    public final List<FunctionApiModel> loadFunctionList() {
        return ExpressFunctionDocumentLoader.super.loadFunctionList();
    }

    /**
     * 获取参数对象
     * <p>
     * 需要注意的是，数字用Long类型
     *
     * @param objectList
     * @param index
     * @param defaultValue
     * @param <T>
     * @return
     */
    protected <T> T getArgsIndexValue(List<Object> objectList, int index, T defaultValue) {
        if (objectList != null && objectList.size() > index) {
            return (T) objectList.get(index);
        }

        Assert.isTrue(defaultValue != null, "函数[" + getName() + "] 第[" + index + "]个参数为空!");

        return defaultValue;
    }

    protected <T> T getConvertValue(List<Object> objectList, int index, Class<T> classType) {
        return getConvertValue(objectList, index, classType, null);
    }

    protected <T> T getConvertValue(List<Object> objectList, int index, Class<T> classType, T defaultValue) {
        if (objectList != null && objectList.size() > index) {
            return (T) Convert.convert(classType, objectList.get(index));
        }
        Assert.isTrue(defaultValue != null, "函数[" + getName() + "] 第[" + index + "]个参数为空!");
        return defaultValue;
    }

    protected <T> T getArgsIndexValue(List<Object> objectList, int index) {
        return getArgsIndexValue(objectList, index, null);
    }

    private List<Object> convertArgsList(Map<String, Object> env, AviatorObject[] args) {
        List<Object> argList = new ArrayList<>();

        for (AviatorObject arg : args) {
            argList.add(arg.getValue(env));
        }

        return argList;
    }

}
