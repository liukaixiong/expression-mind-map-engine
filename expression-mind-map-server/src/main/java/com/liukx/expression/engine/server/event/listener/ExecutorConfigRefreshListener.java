package com.liukx.expression.engine.server.event.listener;

import com.liukx.expression.engine.server.event.ExecutorConfigRefreshEvent;
import com.liukx.expression.engine.server.executor.ExpressionExecutor;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorBaseInfo;
import com.liukx.expression.engine.server.service.ExpressionExecutorConfigService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 执行器刷新配置
 *
 * @author liukaixiong
 * @date 2024/2/20
 */
@Component
public class ExecutorConfigRefreshListener implements ApplicationListener<ExecutorConfigRefreshEvent> {
    private final Logger log = getLogger(ExecutorConfigRefreshListener.class);
    @Autowired
    private ExpressionExecutor expressionExecutor;

    @Autowired
    private ExpressionExecutorConfigService expressionExecutorConfigService;

    @Override
    public void onApplicationEvent(ExecutorConfigRefreshEvent event) {

        Long executorId = (Long) event.getSource();

        log.info("trigger event refresh ExecutorConfigRefreshEvent -> {}", executorId);

        ExpressionExecutorBaseInfo baseInfo = expressionExecutorConfigService.getById(executorId);

        // 重新构建业务数据
        expressionExecutor.getRefreshBusinessConfigInfo(baseInfo.getServiceName(), baseInfo.getBusinessCode(), baseInfo.getExecutorCode());
    }

}
