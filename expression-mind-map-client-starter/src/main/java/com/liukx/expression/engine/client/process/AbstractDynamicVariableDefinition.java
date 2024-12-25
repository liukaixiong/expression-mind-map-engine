//package com.liukx.expression.engine.client.process;
//
//import com.liukx.expression.engine.client.api.ExpressionVariableRegister;
//import com.liukx.expression.engine.core.anno.VariableKey;
//import api.model.api.com.liukx.expression.engine.core.VariableApiModel;
//
//import java.lang.reflect.Method;
//import java.util.List;
//
///**
// * 动态变量定义
// *
// * @param <R>
// */
//@Deprecated
//public abstract class AbstractDynamicVariableDefinition<R> extends AbstractExpressionVariableDefinition<R> {
//
//    @Override
//    protected ExpressionVariableRegister findVariableRegister(String groupName, String variableName) {
//        // 获取子类的动态实现
//        ExpressionVariableRegister defaultExpressionVariableRegister = getDynamicVariableRegister(variableName);
//        if (defaultExpressionVariableRegister != null) {
//            return defaultExpressionVariableRegister;
//        }
//        return super.findVariableRegister(groupName, variableName);
//    }
//
//    /**
//     * 根据变量动态获取信息
//     *
//     * @param variableName
//     * @return
//     */
//    public abstract ExpressionVariableRegister getDynamicVariableRegister(String variableName);
//
//    /**
//     * 动态变量的定义
//     *
//     * @return
//     */
//    public abstract List<VariableApiModel> findVariableApiModelList();
//
//
//    @Override
//    public List<VariableApiModel> loadVariableList() {
//        return this.findVariableApiModelList();
//    }
//
//    @Override
//    public boolean isDynamicRefresh() {
//        return true;
//    }
//
//    @Override
//    protected void afterMethod(VariableKey annotation, Method method) {
//        super.afterMethod(annotation, method);
//    }
//}
