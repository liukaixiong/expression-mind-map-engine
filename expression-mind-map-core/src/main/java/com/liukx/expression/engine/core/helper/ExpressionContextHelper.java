package com.liukx.expression.engine.core.helper;

import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 上下文变量
 *
 * @author liukaixiong
 * @date 2023/12/6
 */
@Deprecated
public class ExpressionContextHelper {

    private final static String RES_PREFIX = "env_res_";

    private final static String DEBUG_TRACE_PREFIX = "env_debug_trace_%s_%s";

    /**
     * 注入上下文变量池
     *
     * @param obj 变量对象
     * @return
     */
    public static ExpressionInjectContext inject(Object obj) {
        return new ExpressionInjectContext().inject(obj);
    }

    public static ExpressionInjectContext inject(String key, Object obj) {
        return new ExpressionInjectContext().inject(key, obj);
    }

    public static void injectObjectContext(Map<String, Object> envCache, Object obj) {
        envCache.put(obj.getClass().getName(), obj);
    }

    /**
     * 注入入参
     *
     * @param envCache
     * @param obj
     */
    public static void injectRequestObjectContext(Map<String, Object> envCache, Object obj) {
        envCache.put("var", obj);
    }

    /**
     * 获取入参
     *
     * @param envCache
     * @return
     */
    public static Object getRequestObjectContext(Map<String, Object> envCache) {
        return envCache.get("var");
    }

    public static <T> T getObject(Map<String, Object> envCache, Class<T> clazz) {
        return (T) envCache.get(clazz.getName());
    }

    public static void registerResult(Map<String, Object> envCache, String key, Object obj) {
        envCache.put(RES_PREFIX + key, obj);
    }

    public static <T> T getResult(Map<String, Object> envCache, String key, T defaultValue) {
        return (T) envCache.getOrDefault(RES_PREFIX + key, defaultValue);
    }

    /**
     * 记录追踪调试内容
     *
     * @param env             上下文环境变量
     * @param configTreeModel 表达式配置对象
     * @param key
     * @param value
     */
    public static void recordTraceDebugContent(Map<String, Object> env, ExpressionConfigTreeModel configTreeModel, String name, String key, Object value) {
        Map<String, Object> debugTraceMap = (Map<String, Object>) env.computeIfAbsent(getCacheFunctionKeyName(name, configTreeModel), k -> new LinkedHashMap<String, Object>());
        debugTraceMap.put(key, value);
    }


    /**
     * 获取当前函数追踪的内容对象
     *
     * @param env
     * @param configTreeModel
     * @param name
     * @return
     */
    public static Map<String, Object> getTraceDebugContent(Map<String, Object> env, ExpressionConfigTreeModel configTreeModel, String name) {
        return (Map<String, Object>) env.getOrDefault(getCacheFunctionKeyName(name, configTreeModel), Collections.emptyMap());
    }

    private static String getCacheFunctionKeyName(String name, ExpressionConfigTreeModel configTreeModel) {
        return String.format(DEBUG_TRACE_PREFIX, configTreeModel.getExpressionId(), name);
    }

    public static class ExpressionInjectContext {

        private final Map<String, Object> envCache = new HashMap<>(32);

        public ExpressionInjectContext inject(Object obj) {
            return inject(obj.getClass().getName(), obj);
        }

        public ExpressionInjectContext inject(String key, Object obj) {
            envCache.put(key, obj);
            return this;
        }

        public Map<String, Object> builder() {
            return envCache;
        }

    }

//    public static void main(String[] args) {
//        FunctionApiModel functionApiModel = new FunctionApiModel();
//        GlobalExpressionInfo globalExpressionInfo = new GlobalExpressionInfo();
//        Map<String, Object> builder = ExpressionContextHelper.inject(functionApiModel).inject(globalExpressionInfo).builder();
//
//        FunctionApiModel object = ExpressionContextHelper.getObject(builder, FunctionApiModel.class);
//        GlobalExpressionInfo object1 = ExpressionContextHelper.getObject(builder, GlobalExpressionInfo.class);
//        System.out.println(object1);
//    }
}
