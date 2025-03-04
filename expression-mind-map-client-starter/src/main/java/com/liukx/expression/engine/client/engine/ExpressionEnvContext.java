package com.liukx.expression.engine.client.engine;

import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * 环境上下文内容，包含一些函数内部能力开启源头
 *
 * @author liukaixiong
 * @date 2024/7/23 - 10:15
 */
@SuppressWarnings("unchecked")
public class ExpressionEnvContext {
    /**
     * 追踪能力标识
     */
    public static final String ENABLE_TRACE_KEY = "_feature_enableTrace";
    public static final String END_TOP_KEY = "_feature_end_top";
    public static final String END_RETURN_KEY = "_feature_end_return";
    public static final String END_FORCE_KEY = "_feature_end_force";


    /**
     * 功能: 执行特定的表达式配置, 包含子表达式功能
     */
    public static final String FEATURE_EXPRESSION_CONFIG_ID_CONTAIN_KEY = "_feature_expression_config_id_contain";
    /**
     * 功能: 跳过某些表达式配置 , 包含子表达式功能
     */
    public static final String FEATURE_EXPRESSION_CONFIG_ID_SKIP_KEY = "_feature_expression_config_id_skip";
    /**
     * 根据特定的函数方法名称过滤
     */
    public static final String FEATURE_EXPRESSION_FUNCTION_NAME_SKIP_KEY = "_feature_expression_function_name_skip";
    public static final String RESULT_KEY = "resultContext";
    /**
     * 追踪数据埋点
     */
    private final static String DEBUG_TRACE_PREFIX = "env_debug_trace_%s_%s";
    private static final String FUNCTION_CACHE_PREFIX = "env_function_cache";
    private final Logger logger = LoggerFactory.getLogger(ExpressionEnvContext.class);
    private final Map<String, Object> sourceMap;

    private final Map<String, Object> businessEnvContext = new HashMap<>();

    public ExpressionEnvContext(Map<String, Object> m) {
        this.sourceMap = m;
    }

    public ExpressionEnvContext() {
        this.sourceMap = new HashMap<>();
    }

    public String getEventName() {
        return getValue("eventName");
    }

    public void setEventName(String eventName) {
        addEnvContext("eventName", eventName);
    }

    public Object getRequest() {
        return getObjectValue("request");
    }

    public void setRequest(Object request) {
        addEnvContext("request", request);
    }

    @SuppressWarnings("unchecked")
    public void recordTraceDebugContent(String name, String key, Object value) {
        Map<String, Object> debugTraceMap = (Map<String, Object>) this.sourceMap.computeIfAbsent(getCacheFunctionKeyName(name, getConfigTreeModel()), k -> new LinkedHashMap<String, Object>());
        debugTraceMap.put(key, value);
        logger.info("函数追踪埋点 :{} -> {} -> {} ", name, key, value);
    }

    public ExpressionConfigTreeModel getConfigTreeModel() {
        return getEnvClassInfo(ExpressionConfigTreeModel.class);
    }

    /**
     * 获取特定的追踪内容
     *
     * @param name
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getTraceDebugContent(String name) {
        return (Map<String, Object>) this.sourceMap.getOrDefault(getCacheFunctionKeyName(name, getConfigTreeModel()), Collections.emptyMap());
    }

    private String getCacheFunctionKeyName(String name, ExpressionConfigTreeModel configTreeModel) {
        return String.format(DEBUG_TRACE_PREFIX, configTreeModel == null ? "" : configTreeModel.getExpressionId(), name);
    }

    public void addFunctionCache(String key, Object value) {
        putNodeCache(FUNCTION_CACHE_PREFIX, key, value);
    }


    public Object getFunctionCache(String key) {
        final Map<String, Object> nodeCache = getNodeCache(FUNCTION_CACHE_PREFIX);
        return nodeCache.get(key);
    }

    /**
     * 清理函数缓存执行
     */
    public void clearFunctionCache() {
        clearNodeCache(FUNCTION_CACHE_PREFIX);
    }

    /**
     * 添加业务对应的K,V到上下文中
     *
     * @param key
     * @param value
     */
    public void addEnvContext(String key, Object value) {
        this.sourceMap.put(key, value);
        this.businessEnvContext.put(key, value);
    }

    public Map<String, Object> getBusinessEnvContext() {
        return businessEnvContext;
    }

    /**
     * 添加对象到上下文
     *
     * @param obj
     */
    public void addEnvClassInfo(Object obj) {
        this.sourceMap.put(obj.getClass().getName(), obj);
    }


    /**
     * 记录结果到上下文中
     *
     * @param key
     * @param value
     * @return
     */
    public void recordResult(String key, Object value) {
        putNodeCache(RESULT_KEY, key, value);
    }

    /**
     * 获取结果上下文
     *
     * @return
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getResultContext() {
        return getNodeCache(RESULT_KEY);
    }


    /**
     * 清理结果上下文
     */
    public void clearResultContext() {
        clearNodeCache(RESULT_KEY);
    }

    /**
     * 根据对象类型获取对象的值
     *
     * @param clazz 对象类型
     * @param <T>
     * @return
     */
    public <T> T getObject(Class<T> clazz) {
        final Object val = this.sourceMap.get(clazz.getName());
        if (val != null) {
            return clazz.cast(val);
        }
        return null;
    }

    /**
     * 获取原本的值
     *
     * @param key
     * @return
     */
    public Object getObjectValue(String key) {
        return this.sourceMap.get(key);
    }

    public <T> T getObjectValue(String key, Class<T> clazz) {
        final Object objectValue = getObjectValue(key);
        return objectValue == null ? null : clazz.cast(objectValue);
    }

    /**
     * 获取值并转换成String
     *
     * @param key
     * @return
     */
    public String getValue(String key) {
        final Object val = getObjectValue(key);
        if (val != null) {
            return val.toString();
        }
        return null;
    }

    /**
     * 获取上下文中设置的Class对象
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> T getEnvClassInfo(Class<T> clazz) {
        return (T) this.sourceMap.get(clazz.getName());
    }

    public Map<String, Object> getSourceMap() {
        return sourceMap;
    }

    /**
     * 关闭追踪
     */
    public void disableTrace() {
        this.sourceMap.put(ENABLE_TRACE_KEY, false);
    }

    public void enableTrace() {
        this.sourceMap.put(ENABLE_TRACE_KEY, true);
    }

    /**
     * 是否开启追踪能力
     *
     * @return
     */
    public boolean isEnableTrace() {
        return (boolean) this.sourceMap.getOrDefault(ENABLE_TRACE_KEY, false);
    }

    /**
     * 启用 特定的函数配置编号的表达式
     *
     * @param expressionIds 表达式配置编号
     */
    public void enableExpressionConfigIdContainFilter(Set<Long> expressionIds) {
        this.sourceMap.put(FEATURE_EXPRESSION_CONFIG_ID_CONTAIN_KEY, expressionIds);
    }

    /**
     * 获取表达式配置编号的信息
     *
     * @return 执行的表达式编号信息
     * @see #enableExpressionConfigIdContainFilter(Set)
     */
    @SuppressWarnings("unchecked")
    public Set<Long> getExpressionConfigIdContainFilter() {
        return this.getObjectValue(FEATURE_EXPRESSION_CONFIG_ID_CONTAIN_KEY) == null ? null : (Set<Long>) this.getObjectValue(FEATURE_EXPRESSION_CONFIG_ID_CONTAIN_KEY);
    }

    /**
     * 跳过特定的表达式配置编号过滤
     *
     * @param skipExpressionConfigIds
     */
    public void enableExpressionConfigIdSkipFilter(Set<Long> skipExpressionConfigIds) {
        this.sourceMap.put(FEATURE_EXPRESSION_CONFIG_ID_SKIP_KEY, skipExpressionConfigIds);
    }

    @SuppressWarnings("unchecked")
    public Set<Long> getExpressionConfigIdSkipFilterList() {
        return this.getObjectValue(FEATURE_EXPRESSION_CONFIG_ID_SKIP_KEY) == null ? null : (Set<Long>) this.getObjectValue(FEATURE_EXPRESSION_CONFIG_ID_SKIP_KEY);
    }

    public void enableExpressionFunctionNameSkipFilter(Set<String> skipExpressionFunctionName) {
        this.sourceMap.put(FEATURE_EXPRESSION_FUNCTION_NAME_SKIP_KEY, skipExpressionFunctionName);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getExpressionFunctionNameSkipFilterList() {
        return this.getObjectValue(FEATURE_EXPRESSION_FUNCTION_NAME_SKIP_KEY) == null ? null : (Set<String>) this.getObjectValue(FEATURE_EXPRESSION_FUNCTION_NAME_SKIP_KEY);
    }

    /**
     * 终止分支流程标记,执行完当前表达式的子分支之后,不在继续同级别分支
     */
    public void topEnd() {
        this.sourceMap.put(END_TOP_KEY, true);
    }

    /**
     * 强制终止流程，不在执行任何表达式
     */
    public void forceEnd() {
        this.sourceMap.put(END_FORCE_KEY, true);
    }

    /**
     * 返回上一级标记，执行完当前表达式的子分支之后,不在继续同级别分支
     */
    public void returnEnd() {
        this.sourceMap.put(END_RETURN_KEY, true);
    }

    /**
     * 是否强制终止流程
     *
     * @return
     */
    public boolean isForceEnd() {
        return (boolean) this.sourceMap.getOrDefault(END_FORCE_KEY, false);
    }

    /**
     * 是否分支终止流程
     *
     * @return
     */
    public boolean isTopEnd() {
        return (boolean) this.sourceMap.getOrDefault(END_TOP_KEY, false);
    }

    public boolean restTopEnd() {
        this.sourceMap.put(END_TOP_KEY, false);
        return true;
    }

    /**
     * 是否返回上一级标记
     * @return
     */
    public boolean isReturnEnd() {
        return (boolean) this.sourceMap.getOrDefault(END_RETURN_KEY, false);
    }

    /**
     * 重置返回上一级标记
     */
    public void restReturnEnd() {
        this.sourceMap.put(END_RETURN_KEY, false);
    }


    /**
     * 清理表达式函数痕迹
     */
    public void clearAllExpressionFunctionCache() {
        clearFunctionCache();
        clearResultContext();
        this.sourceMap.put(END_TOP_KEY, false);
        this.sourceMap.put(END_RETURN_KEY, false);
        this.sourceMap.put(END_FORCE_KEY, false);
    }

    /**
     * 获取子对象缓存
     *
     * @param key
     * @return
     */
    private Map<String, Object> getNodeCache(String key) {
        return (Map<String, Object>) this.sourceMap.getOrDefault(key, Collections.emptyMap());
    }

    /**
     * 清理子节点缓存
     *
     * @param key
     */
    private void clearNodeCache(String key) {
        this.sourceMap.put(key, new HashMap<>());
    }

    /**
     * 设置子对象缓存
     *
     * @param groupKey
     * @param key
     * @param value
     */
    private void putNodeCache(String groupKey, String key, Object value) {
        Map<String, Object> resultMap = (Map<String, Object>) this.sourceMap.computeIfAbsent(groupKey, var -> new HashMap<String, Object>());
        final Object oldValue = resultMap.put(key, value);
        if (oldValue != null) {
            logger.warn("设置结果时发现Key存在重复:{} , oldValue: {}  newValue:{}", key, oldValue, value);
        }
    }
}
