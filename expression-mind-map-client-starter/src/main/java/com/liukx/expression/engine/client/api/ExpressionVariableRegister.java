package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.core.model.ContextTemplateRequest;

/**
 * 表达式变量
 *
 * @author liukaixiong
 */
public interface ExpressionVariableRegister extends ExpressVariableDocumentLoader {

    /**
     * 组名称
     *
     * @return
     */
    default boolean isMatch(String groupName) {
        return true;
    }

    /**
     * 查找对象
     *
     * @param name
     * @return
     */
    public boolean finderVariable(String name);

    /**
     * 变量搜索器
     *
     * @param cache 变量参数
     * @return
     */
    public Object invoke(String name, ContextTemplateRequest cache);


}
