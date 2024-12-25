package com.liukx.expression.engine.core.api.model;

import java.util.Map;

public interface ExpressionService {

    /**
     * 执行表达式
     *
     * @param expression 表达式
     * @param env        上下文参数
     * @return
     */
    public Object execute(final String expression, final Map<String, Object> env);

    /**
     * 是否开启debug模式
     *
     * @param isEnableDebug
     */
    public void enableDebug(boolean isEnableDebug);

    /**
     * 校验表达式是否符合
     *
     * @param expression
     * @return
     */
    public ValidatorResult validator(String expression);


    /**
     * 翻译表达式
     *
     * @param expression
     */
    public TranslateResult translate(String expression);

}
