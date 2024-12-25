//package com.liukx.expression.engine.server.expression;
//
//import cn.hutool.core.convert.Convert;
//import com.liukx.expression.engine.server.service.ExpressionKeyService;
//import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
//import com.liukx.expression.engine.client.api.ExpressionFunctionRegister;
//import api.model.api.com.liukx.expression.engine.core.FunctionApiModel;
//import model.com.liukx.expression.engine.core.ContextTemplateRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * 远端函数获取
// *
// * @author liukaixiong
// * @date : 2022/6/9 - 14:06
// */
//@Component
//@Deprecated
//public class RemoteFunctionRegister implements ExpressionFunctionRegister<Object>, ExpressFunctionDocumentLoader {
//
//    @Autowired
//    private ExpressionKeyService expressionKeyService;
//
//    @Override
//    public boolean finderFunction(String functionName) {
//        return expressionKeyService.isFunctionDefined(functionName);
//    }
//
//    @Override
//    public Object call(String functionName, ContextTemplateRequest env, Map<String, Object> request) throws Exception {
//        return expressionKeyService.invokeFunction(functionName, env, request);
//    }
//
//    @Override
//    public List<FunctionApiModel> loadFunctionList() {
//        return expressionKeyService.loadAllFunctionDefined().stream()
//            .map(fun -> Convert.convert(FunctionApiModel.class, fun)).collect(Collectors.toList());
//    }
//
//    @Override
//    public boolean isDynamicRefresh() {
//        // 表示需要动态刷新
//        return true;
//    }
//}
