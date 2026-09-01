package com.liukx.expression.engine.client.process;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import com.google.common.base.Splitter;
import com.googlecode.aviator.runtime.function.AbstractVariadicFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.googlecode.aviator.runtime.type.AviatorRuntimeJavaType;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.example.DemoFunDescDefinitionService;
import com.liukx.expression.engine.client.feature.FunctionContextManager;
import com.liukx.expression.engine.client.helper.FunHelper;
import com.liukx.expression.engine.client.log.LogEventEnum;
import com.liukx.expression.engine.client.log.LogHelper;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.utils.AssertUtils;
import com.liukx.expression.engine.core.utils.Jsons;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;

/**
 * 简单的函数定义
 *
 * @author liukaixiong
 * @date 2023/12/6
 */
public abstract class AbstractSimpleFunction extends AbstractVariadicFunction implements ExpressFunctionDocumentLoader {

    @Autowired
    private FunctionContextManager functionContextManager = new FunctionContextManager();

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        // 将函数变量转换成对应的java对象
        List<Object> funcArgs = FunHelper.convertArgsList(env, args);
        final FunctionApiModel functionApiModel = loadFunctionInfo();
        final ExpressionEnvContext expressionEnvContext = ExpressionEnvContext.of(env);
        // 提取通用参数
        ExpressionBaseRequest request = expressionEnvContext.getEnvClassInfo(ExpressionBaseRequest.class);
        ExpressionConfigTreeModel configTreeModel = expressionEnvContext.getConfigTreeModel();
        String cacheKey = generateCacheKey(expressionEnvContext, funcArgs, request);
        final Object processor = functionContextManager.executor(functionApiModel, env, funcArgs, () -> functionCall(expressionEnvContext, cacheKey, configTreeModel, request, funcArgs));
        // 包装结果
        return FunctionUtils.wrapReturn(processor);

//        final ExpressionEnvContext expressionEnvContext = ExpressionEnvContext.of(env);
//        // 提取通用参数
//        ExpressionBaseRequest request = expressionEnvContext.getEnvClassInfo(ExpressionBaseRequest.class);
//        ExpressionConfigTreeModel configTreeModel = expressionEnvContext.getEnvClassInfo(ExpressionConfigTreeModel.class);
//        final FunctionApiModel functionApiModel = loadFunctionInfo();
//        try {
//            // 将函数结果进行本地缓存,方便同一个线程的执行结果公用
//            String cacheKey = generateCacheKey(expressionEnvContext, funcArgs, request);
//            functionPostProcessorList.forEach(var -> var.functionBefore(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs));
//
//            FunctionFilterChain functionFilterChain = new FunctionFilterChain(functionFilters, () -> functionCall(expressionEnvContext, cacheKey, configTreeModel, request, funcArgs));
//
//            final Object processor = functionFilterChain.doFilter(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs);
//
//            functionPostProcessorList.forEach(var -> var.afterFunction(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs, processor));
//
//            // 包装结果
//            return FunctionUtils.wrapReturn(processor);
//        } catch (Exception e) {
//            functionPostProcessorList.forEach(var -> var.functionError(expressionEnvContext, configTreeModel, request, functionApiModel, funcArgs, e));
//            throw e;
//        }
    }

    private Object functionCall(ExpressionEnvContext expressionEnvContext, String cacheKey, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funcArgs) {
        Object processor;
        // 是否缓存
        if (isAllowedCache()) {
            processor = expressionEnvContext.getFunctionCache(cacheKey);
            if (processor == null) {
                //子类识别处理
                processor = processor(expressionEnvContext, configTreeModel, request, funcArgs);
                LogHelper.trace(expressionEnvContext, request, LogEventEnum.FUNCTION_CALL, "function result {} 结果: {}", cacheKey, logValue(processor));
                expressionEnvContext.addFunctionCache(cacheKey, processor);
            } else {
                expressionEnvContext.recordTraceDebugContent(getName(), "命中本地缓存", cacheKey + "=" + processor);
                LogHelper.trace(expressionEnvContext, request, LogEventEnum.FUNCTION_CALL, "★★★命中缓存★★★ {} 中获取值: {}", cacheKey, logValue(processor));
            }
        } else {
            // 强制执行函数
            processor = processor(expressionEnvContext, configTreeModel, request, funcArgs);
            LogHelper.trace(expressionEnvContext, request, LogEventEnum.FUNCTION_CALL, "skip cache function result {} 结果: {}", cacheKey, logValue(processor));
        }
        return processor;
    }

    /**
     * 打印数据
     *
     * @param value
     * @return
     */
    public String logValue(Object value) {

        if (value == null) {
            return "null";
        }

        if (ClassUtil.isSimpleValueType(value.getClass())) {
            return value.toString();
        } else {
            return value.getClass().getSimpleName();
        }
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
     * 获取参数的可选值,允许下标的值为空
     *
     * @param objectList 对象
     * @param index      下标
     * @param <T>
     * @return
     */
    protected <T> T getArgsIndexOptionalValue(List<Object> objectList, int index) {
        if (objectList != null && objectList.size() > index) {
            //noinspection unchecked
            return (T) objectList.get(index);
        }
        return null;
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
            //noinspection unchecked
            return (T) objectList.get(index);
        }

        AssertUtils.Function.isTrue(defaultValue != null, "函数[" + getName() + "] 第[" + index + "]个参数为空!");

        return defaultValue;
    }

    protected <T> T getConvertValue(List<Object> objectList, int index, Class<T> classType) {
        return getConvertValue(objectList, index, classType, null);
    }

    protected <T> T getConvertValue(List<Object> objectList, int index, Class<T> classType, T defaultValue) {
        if (objectList != null && objectList.size() > index) {
            return (T) Convert.convert(classType, objectList.get(index));
        }
        AssertUtils.Function.isTrue(defaultValue != null, "函数[" + getName() + "] 第[" + index + "]个参数为空!");
        return defaultValue;
    }

    protected <T> T getArgsIndexValue(List<Object> objectList, int index) {
        return getArgsIndexValue(objectList, index, null);
    }

    protected Map<Object, Object> convertMap(List<Object> funcArgs) {
        return convertMap(funcArgs, Object.class, Object.class);
    }

    protected <K, V> Map<K, V> convertMap(List<Object> funcArgs, Integer startIndex, Class<K> k, Class<V> v) {
        if (funcArgs.size() > startIndex) {
            final List<Object> objectMap = funcArgs.subList(startIndex, funcArgs.size());
            return convertMap(objectMap, k, v);
        }
        return new HashMap<>();
    }

    /**
     * 将参数构建成map
     *
     * @param funcArgs
     * @return
     */
    protected <K, V> Map<K, V> convertMap(List<Object> funcArgs, Class<K> k, Class<V> v) {
        if (funcArgs != null && funcArgs.size() % 2 != 0) {
            Assert.isTrue(true, "函数参数的长度必须为2的倍数,否则无法构建K,V结构!");
        }

        Map<K, V> map = new HashMap<>(funcArgs != null ? funcArgs.size() / 2 : 10);
        if (funcArgs != null) {
            for (int i = 0; i < funcArgs.size(); ) {
                map.put(getConvertValue(funcArgs, i, k), getConvertValue(funcArgs, i + 1, v));
                i += 2;
            }
        }
        return map;
    }

    protected Date getArgsIndexDate(List<Object> objectList, int index) {
        return getArgsIndexDate(objectList, index, null);
    }

    /**
     * 获取变量中的时间类型，统一转换成Date。
     * 包含：Date,Long,String-> [年月日:2025-01-01,年月日时分秒:2025-01-01 00:11:22]
     *
     * @param objectList   参数类型
     * @param index        参数下标
     * @param defaultValue 找不到的默认值
     * @return Date
     */
    protected Date getArgsIndexDate(List<Object> objectList, int index, Date defaultValue) {
        Object date = getArgsIndexValue(objectList, index, defaultValue);
        return getDate(date);
    }

    /**
     * 获取变量中的时间字段统一转换
     *
     * @param envDate 变量中的时间
     * @return
     */
    protected Date getDate(Object envDate) {
        if (envDate instanceof Long) {
            return new Date((Long) envDate);
        } else if (envDate instanceof Date) {
            return (Date) envDate;
        } else if (envDate instanceof LocalDateTime) {
            LocalDateTime localDateTime = (LocalDateTime) envDate;
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } else if (envDate instanceof LocalDate) {
            LocalDate localDate = (LocalDate) envDate;
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        } else if (envDate instanceof String) {
            String dateStr = (String) envDate;
            if (dateStr.length() == 10) {
                return DateUtil.beginOfDay(DateUtil.parseDate(dateStr));
            } else {
                return DateUtil.parseDateTime(dateStr);
            }
        }
        return null;
    }

    /**
     * 获取集合信息,参数如果是,号分割的字符串对象
     *
     * @param objectList
     * @param index
     * @return
     */
    protected List<String> getArgsIndexList(List<Object> objectList, int index) {
        return getArgsIndexList(objectList, index, String.class);
    }

    /**
     * 获取集合信息,参数如果是,号分割的字符串对象
     *
     * @param objectList   参数对象
     * @param index        下标
     * @param convertClazz 转换类型
     * @param <T>
     * @return
     */
    @SuppressWarnings({"unchecked"})
    protected <T> List<T> getArgsIndexList(List<Object> objectList, int index, Class<T> convertClazz) {
        final String argsString = getArgsIndexValue(objectList, index, "");
        List<T> list = new ArrayList<>();
        final List<String> argList = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(argsString);
        if (convertClazz.isAssignableFrom(String.class)) {
            return (List<T>) argList;
        } else {
            for (String value : argList) {
                list.add(Jsons.parseObject(value, convertClazz));
            }
        }
        return list;
    }

    /**
     * 获取 lambda 函数，支持指定输入类型
     *
     * @param env       表达式环境
     * @param funArgs   函数参数列表
     * @param index     参数索引
     * @param inputType 输入类型（null 表示自动推断为 Object）
     * @param clazz     返回类型
     * @param <I>       输入类型泛型
     * @param <T>       返回类型泛型
     * @return Function 对象
     */
    @SuppressWarnings("unchecked")
    protected <I, T> Function<I, T> getArgsIndexFunction(ExpressionEnvContext env, List<Object> funArgs, int index, Class<I> inputType, Class<T> clazz) {
        final Map<String, Object> sourceMap = env.getSourceMap();
        AviatorFunction lambda = FunctionUtils.getFunction(FunctionUtils.wrapReturn(funArgs.get(index)), sourceMap, 1);

        // 转换为 Java Function 以便复用
        return input -> {
            AviatorObject result = lambda.call(sourceMap, AviatorRuntimeJavaType.valueOf(input));
            return Convert.convert(clazz, result.getValue(sourceMap));
        };
    }

}
