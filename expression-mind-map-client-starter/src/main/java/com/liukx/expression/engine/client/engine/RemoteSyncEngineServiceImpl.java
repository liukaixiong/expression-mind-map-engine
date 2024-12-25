//package com.liukx.expression.engine.client.engine;
//
//import com.liukx.expression.engine.client.api.ClientEngineInvokeService;
//import com.liukx.expression.engine.client.api.RemoteHttpService;
//import com.liukx.expression.engine.client.enums.EngineCallType;
//import model.api.com.liukx.expression.engine.core.ClientExpressionSubmitRequest;
//import model.api.com.liukx.expression.engine.core.ExpressionBaseRequest;
//import consts.com.liukx.expression.engine.core.ExpressionConstants;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//
//import java.util.Map;
//
///**
// * http 远端执行服务
// */
//@Deprecated
//public class RemoteSyncEngineServiceImpl implements ClientEngineInvokeService {
//
//    private final Logger logger = LoggerFactory.getLogger(getClass());
//
//    @Autowired
//    private RemoteHttpService remoteHttpService;
//
//    @Value("${spring.application.name:unknown}")
//    private String serviceName;
//
//    @Override
//    public EngineCallType type() {
//        return EngineCallType.REMOTE_SYNC;
//    }
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
////        return remoteHttpService.call(ExpressionConstants.ENGINE_SERVER_ID, ExpressionConstants.SERVER_EXECUTOR_PATH, baseRequest, Object.class);
//        return null;
//    }
//
//}
