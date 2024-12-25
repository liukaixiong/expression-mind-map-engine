//package com.liukx.expression.engine.client.router;
//
//import cn.hutool.json.JSONUtil;
//import com.liukx.expression.engine.client.api.ExpressionFunctionRegister;
//import com.liukx.expression.engine.client.factory.ExpressionExecutorFactory;
//import com.liukx.expression.engine.client.process.ExpressDocumentManager;
//import com.liukx.expression.engine.client.process.ExpressionVariableProcessAdaptor;
//import model.api.com.liukx.expression.engine.core.ApiResult;
//import model.api.com.liukx.expression.engine.core.ExpressionBaseRequest;
//import com.liukx.expression.engine.core.api.model.api.*;
//import consts.com.liukx.expression.engine.core.ExpressionConstants;
//import enums.com.liukx.expression.engine.core.ExpressionKeyTypeEnums;
//import enums.com.liukx.expression.engine.core.ExpressionVariableTypeEnums;
//import model.com.liukx.expression.engine.core.ContextTemplateRequest;
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import jakarta.annotation.Resource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.LinkedHashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@RestController
//@Deprecated
//@Api(tags = "远端调度API管理")
//public class RouterController {
//
//    private final Logger LOG = LoggerFactory.getLogger(RouterController.class);
//
//    @Resource
//    private ExpressionExecutorFactory factory;
//
//    @Resource
//    private ExpressDocumentManager expressDocumentManager;
//
//    @Deprecated
//    @ApiOperation("节点执行器回调入口")
//    @PostMapping(value = ExpressionConstants.PATH_EXPRESSION_EXECUTOR, produces = "application/json;charset=UTF-8")
//    public ApiResult<String> executor(@RequestBody RemoteExpressionModel expressionModel) throws Exception {
//        LOG.info("表达式执行请求参数：{}", JSONUtil.toJsonStr(expressionModel));
//        ExpressionBaseRequest request = expressionModel.getRequest();
//        List<RemoteExecutorRequest> executorRequests = expressionModel.getExecutorRequests();
//        ExpressionKeyTypeEnums keyType = expressionModel.getKeyType();
//        Map<String, Object> userMap = new LinkedHashMap<>();
//        ContextTemplateRequest envRequest = new ContextTemplateRequest();
//        envRequest.setRequest(request);
//        if (ExpressionKeyTypeEnums.VARIABLE == keyType) {
//            ExpressionVariableProcessAdaptor expressionVariableProcess = factory.getExpressionVariableProcess();
//            Set<String> variableNames = executorRequests.stream().map(RemoteExecutorRequest::getName).collect(Collectors.toSet());
//            userMap.putAll(expressionVariableProcess.processList(variableNames, envRequest));
//        } else {
//            for (RemoteExecutorRequest executorRequest : executorRequests) {
//                ExpressionFunctionRegister functionRegister = factory.getFunctionRegister(executorRequest.getName());
//                if (functionRegister == null) {
//                    userMap.put(executorRequest.getName(), null);
//                    continue;
//                }
//                Map<String, Object> params = executorRequest.getParams();
//                Object callResult = functionRegister.call(executorRequest.getName(), envRequest, params);
//                userMap.put(executorRequest.getName(), callResult);
////                Map<String, Object> resultMap = envRequest.getRequest().getResultMap();
////                if(!resultMap.isEmpty()){
////                    userMap.put("remote_context",resultMap);
////                    LOG.debug("携带remote_context值：{}",resultMap);
////                }
//            }
//        }
//        String result = JSONUtil.toJsonStr(userMap);
//        LOG.info("表达式结果参数:{}", result);
//        return ApiResult.ok(result);
//    }
//
//    @ApiOperation("节点变量函数文档")
//    @PostMapping(value = ExpressionConstants.PATH_EXPRESSION_DOCUMENT, produces = "application/json;charset=UTF-8")
//    public ApiResult<String> document() {
//
//        List<FunctionApiModel> allFunctionApi = expressDocumentManager.getAllFunctionApi();
//
//        // 获取默认配置的相关变量
//        List<VariableApiModel> allVariableApi = expressDocumentManager.getGroupVariableApi(ExpressionVariableTypeEnums.DEFAULT.name());
//
//        GlobalExpressionDocInfo expressionInfo = new GlobalExpressionDocInfo();
//        expressionInfo.setFunctionApi(allFunctionApi);
//        expressionInfo.setVariableApi(allVariableApi);
//
//        return ApiResult.ok(JSONUtil.toJsonStr(expressionInfo));
//    }
//
//}
