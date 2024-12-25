package com.liukx.expression.engine.server.components;

import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.enums.ExpressionTypeEnum;
import com.liukx.expression.engine.core.model.ContextTemplateRequest;
import com.liukx.expression.engine.server.enums.TraceStageEnums;
import com.liukx.expression.engine.server.service.TraceLogService;
import com.liukx.expression.engine.server.service.model.trace.TraceLogDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 链路日志帮助类
 *
 * @author liukaixiong
 * @date : 2022/6/20 - 11:19
 */
public class TraceLogHelper {
    private static final Logger logger = LoggerFactory.getLogger(TraceLogHelper.class);

    public static void recordLog(ExpressionBaseRequest baseRequest, TraceStageEnums stage, String content) {
        log(baseRequest, stage, false, content);
    }

    public static void recordLog(ContextTemplateRequest contextTemplateRequest, TraceStageEnums stage, String content) {
        ExpressionBaseRequest baseRequest = contextTemplateRequest.getRequest();
        log(baseRequest, stage, false, content);
    }

    public static void recordError(ContextTemplateRequest request, Exception e) {
        ExpressionBaseRequest baseRequest = request.getRequest();
        String exMsg = builderErrorContent(e);
        log(baseRequest, TraceStageEnums.ERROR, true, exMsg);
    }

    public static void recordError(ExpressionBaseRequest baseRequest, Exception e) {
        logger.error("执行异常", e);
        String exMsg = builderErrorContent(e);
        log(baseRequest, TraceStageEnums.ERROR, true, exMsg);
    }

    /**
     * 构建异常消息
     *
     * @param e
     * @return
     */
    private static String builderErrorContent(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    private static void log(ExpressionBaseRequest baseRequest, TraceStageEnums stage, boolean isException,
                            String content) {
        logger.debug("【{}】[{},{}]" + content, baseRequest.getTraceId(), baseRequest.getServiceName(),
                baseRequest.getBusinessCode());
        TraceLogService traceLogService = SpringHelper.getBean(TraceLogService.class);

        TraceLogDto logDto =
                TraceLogDto.builder().serviceName(baseRequest.getServiceName()).businessCode(baseRequest.getBusinessCode())
                        .eventType(stage.name()).logId(baseRequest.getTraceId()).unionId(baseRequest.getUnionId())
                        .content(content).isException(isException).build();

        traceLogService.log(logDto);
    }

    public static void successLog(ExpressionBaseRequest baseRequest, ExpressionTypeEnum stage) {
        TraceLogService traceLogService = SpringHelper.getBean(TraceLogService.class);
        TraceLogDto logDto =
                TraceLogDto.builder().serviceName(baseRequest.getServiceName()).businessCode(baseRequest.getBusinessCode())
                        .eventType(stage.name()).logId(baseRequest.getTraceId()).unionId(baseRequest.getUnionId()).build();
        traceLogService.success(logDto);
    }

    public static void successLog(ContextTemplateRequest request, ExpressionTypeEnum stage) {
        ExpressionBaseRequest baseRequest = (ExpressionBaseRequest) request.getRequest();
        successLog(baseRequest, stage);
    }

}
