//package com.liukx.expression.engine.client.engine;
//
//import com.liukx.expression.engine.client.api.ClientEngineInvokeService;
//import com.liukx.expression.engine.client.api.RemoteHttpService;
//import com.liukx.expression.engine.client.enums.EngineCallType;
//import model.api.com.liukx.expression.engine.core.ClientExpressionSubmitRequest;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//
//import java.util.Map;
//
///**
// * http 远端执行服务
// */
//@Deprecated
//public class RemoteASyncEngineServiceImpl implements ClientEngineInvokeService {
//
//    @Autowired
//    private RemoteHttpService remoteHttpService;
//
//    @Value("${spring.application.name:unknown}")
//    private String serviceName;
//
//    @Override
//    public EngineCallType type() {
//        return EngineCallType.REMOTE_ASYNC;
//    }
//
//
//    @Override
//    public Object invoke(ClientExpressionSubmitRequest request, Map<String, Object> envContext) {
////        ExpressionBaseRequest baseRequest = new ExpressionBaseRequest();
////        baseRequest.setServiceName(serviceName);
////        baseRequest.setBusinessCode(businessGroupCode);
////        baseRequest.setEventName(eventName);
////        baseRequest.setUnionId(unionId);
////        baseRequest.setParamsMap(envContext);
////        baseRequest.setUserId(userId);
////        baseRequest.setTraceId(traceId);
////        return remoteHttpService.call(ExpressionConstants.ENGINE_SERVER_ID, ExpressionConstants.SERVER_ASYNC_EXECUTOR_PATH, baseRequest, Object.class);
//        return null;
//    }
//
//
//}
