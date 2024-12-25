package com.liukx.expression.engine.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * 简化代碼工具類
 *
 * @author liukaixiong
 * @date : 2022/6/20 - 11:06
 */
public class CodeUtils {

    private static Logger logger = LoggerFactory.getLogger(CodeUtils.class);

    public static <T> T safeInvoker(Supplier<T> run) {
        return safeInvoker(run, () -> null);
    }

    /**
     * 安全执行内部方法,目的是为了不影响核心流程运行
     *
     * @param run          希望正常执行的方法
     * @param exceptionRun 异常执行的方法处理
     * @param <T>
     * @return
     */
    public static <T> T safeInvoker(Supplier<T> run, Supplier<T> exceptionRun) {
        try {
            return run.get();
        } catch (Exception e) {
            logger.error("代码执行异常", e);
            return exceptionRun.get();
        }
    }

    public static <T> T getDefaultValue(String value, Function<String, T> function, T defaultValue) {
        try {
            return function.apply(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 默认的方法获取
     *
     * @param value        值
     * @param defaultValue 值出现问题则有默认值补充
     * @param <T>
     * @return
     */
    public static <T> T getDefaultValue(T value, T defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        return value;
    }

}
