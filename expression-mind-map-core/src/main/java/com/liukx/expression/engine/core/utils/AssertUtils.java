package com.liukx.expression.engine.core.utils;

import com.liukx.expression.engine.core.exception.ExecutorException;
import com.liukx.expression.engine.core.exception.ExpressionException;
import com.liukx.expression.engine.core.exception.FunctionException;

/**
 * 断言工具类
 *
 * @author liukaixiong
 * @date 2025/12/1 - 15:31
 */
public class AssertUtils {

    public static class Function {
        public static void isTrue(boolean expression, String message) {
            if (!expression) {
                throw new FunctionException(message);
            }
        }
    }

    public static class Expression {
        public static void isTrue(boolean expression, String message) {
            if (!expression) {
                throw new ExpressionException(message);
            }
        }
    }

    public static class Executor {
        public static void isTrue(boolean expression, String message) {
            if (!expression) {
                throw new ExecutorException(message);
            }
        }
    }


}
