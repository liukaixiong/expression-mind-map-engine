package com.liukx.expression.engine.client.example;

import com.liukx.expression.engine.client.api.ExpressionVariableRegister;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;
import com.liukx.expression.engine.core.model.ContextTemplateRequest;

import java.util.List;

/**
 *
 *
 * @author liukaixiong
 * @date 2024/12/20 - 16:04
 */
public class DemoEnvRegister implements ExpressionVariableRegister {

    @Override
    public List<VariableApiModel> loadVariableList() {
        // 如果有多个变量,则返回多个
        return null;
    }

    @Override
    public VariableApiModel getVariableInfo(String group, String name) {
        // 定义变量信息
        return null;
    }

    @Override
    public boolean finderVariable(String name) {
        // 查询数据库是否存在
        return false;
    }

    @Override
    public Object invoke(String name, ContextTemplateRequest cache) {
        // 处理业务逻辑
        return null;
    }

    @Override
    public String groupName() {
        return "db";
    }
}
