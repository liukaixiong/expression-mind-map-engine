package com.liukx.expression.engine.server.util;

import com.googlecode.aviator.AviatorEvaluator;

/**
 * 表达式工具类
 * @author liukaixiong
 * @date 2025/3/6 - 11:29
 */
public class ExpressionUtils {


    /**
     * 校验表达式
     *
     * @param expression 表达式内容
     * @return 错误的内容
     */
    public static String isValidExpression(String expression) {
        try {
            AviatorEvaluator.validate(expression);
        } catch (Exception e) {
            return e.getMessage();
        }
        return null;
    }

}
