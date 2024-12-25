//package com.liukx.expression.engine.server.expression;
//
//import com.liukx.expression.engine.server.enums.TraceStageEnums;
//import com.liukx.expression.engine.client.process.AbstractExpressionFunctionDefinition;
//import com.liukx.expression.engine.core.anno.FunctionKey;
//import model.com.liukx.expression.engine.core.ContextTemplateRequest;
//import com.liukx.expression.engine.server.components.TraceLogHelper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Map;
//import java.util.stream.Collectors;
//
///**
// * @author liukaixiong
// * @date : 2022/6/9 - 14:29
// */
//@Component
//@Slf4j
//public class LocalFunctionRegister extends AbstractExpressionFunctionDefinition<Object> {
//
//    @Override
//    public String functionPrefix() {
//        return "fn_local";
//    }
//
//    @Override
//    protected Object defaultExceptionInvoke(Exception e, String name, ContextTemplateRequest cache) {
//        TraceLogHelper.recordError(cache, e);
//        return super.defaultExceptionInvoke(e, name, cache);
//    }
//
//    @Override
//    protected Object defaultNoNameInvoke(String name, ContextTemplateRequest cache) {
//        TraceLogHelper.recordLog(cache, TraceStageEnums.FUNCTION_PARSE, String.format("函数没有找到定义:%s", name));
//        return super.defaultNoNameInvoke(name, cache);
//    }
//
////    @FunctionKey(name = "result_append_list",
////        describe = "默认返回集合",
////        requestClass = List.class,
////        requestExample = "fn_local.result_append_list('返回结果key','a,b,c')")
////    public boolean returnMap(ContextTemplateRequest request, Map<String, Object> codes) {
////        for (String key : codes.keySet()) {
////            String value = codes.get(key).toString();
////            List<Object> list =
////                (List<Object>)request.getRequest().getResultMap().computeIfAbsent(key, var -> new ArrayList<>());
////            List<String> arrayData = Arrays.stream(value.split(",")).collect(Collectors.toList());
////            list.addAll(arrayData);
////        }
////        return true;
////    }
//
//}
