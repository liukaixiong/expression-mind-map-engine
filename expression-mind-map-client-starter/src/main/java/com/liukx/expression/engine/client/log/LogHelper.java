package com.liukx.expression.engine.client.log;

import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 日志帮助类
 * @author liukaixiong
 * @date 2023/12/22
 */
public class LogHelper implements ApplicationContextAware {
    private static ApplicationContext applicationContext;

    public static void trace(ExpressionEnvContext envContext, ExpressionBaseRequest request, LogEventEnum logType, String text, Object... logParam) {
        if (envContext.isEnableTrace()) {
            trace(request, logType, text, logParam);
        }
    }

    public static void trace(ExpressionBaseRequest request, LogEventEnum logType, String text, Object... logParam) {
        // 可能在非Spring的环境下，比如单元测试
        if (LogHelper.applicationContext != null) {
            LogHelper.applicationContext.getBean(LogTraceService.class).trace(request, logType, text, logParam);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        LogHelper.applicationContext = applicationContext;
    }

}
