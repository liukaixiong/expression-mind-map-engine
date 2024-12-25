package com.liukx.expression.engine.client.log;

import ch.qos.logback.classic.Level;
import com.liukx.expression.engine.client.config.props.ExpressionProperties;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import org.slf4j.Logger;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 规范日志记录
 *
 * @author liukaixiong
 * @date 2023/12/22
 */
public class Sl4jLogServiceImpl implements LogTraceService, InitializingBean {
    private final Logger LOG = getLogger(Sl4jLogServiceImpl.class);
    private final Map<Level, BiConsumer<String, Object[]>> ruleMap = new HashMap<>();
    @Autowired
    private ExpressionProperties expressionProperties;

    @Override
    public void afterPropertiesSet() throws Exception {
        ruleMap.put(Level.DEBUG, LOG::debug);
        ruleMap.put(Level.INFO, LOG::info);
        ruleMap.put(Level.WARN, LOG::warn);
        ruleMap.put(Level.ERROR, LOG::error);
    }


    @Override
    public void trace(ExpressionBaseRequest request, LogEventEnum logEventType, String text, Object... logParam) {
        String message = MessageFormatter.arrayFormat(text, logParam).getMessage();
        String content = "expression trace [{}] [{}]-[{}]-[{}] >>> {}";
        Object[] objects = {logEventType, request.getBusinessCode(), request.getEventName(), request.getUnionId(), message};
        recordLog(content, objects);
    }

    private void recordLog(String content, Object[] objects) {
        String loggerTraceLevel = expressionProperties.getLoggerTraceLevel();
        Level level = Level.toLevel(loggerTraceLevel);
        ruleMap.getOrDefault(level, LOG::debug).accept(content, objects);
    }


}
