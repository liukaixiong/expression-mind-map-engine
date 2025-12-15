package com.liukx.expression.engine.client.process;

/**
 * 表达式配置定制
 *
 * @author liukaixiong
 * @date 2025/12/10 - 17:33
 */
public interface ExpressionConfigureCustomizer {

    public void customize(AbstractExpressionService expressionService);
}
