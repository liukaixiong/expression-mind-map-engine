package com.liukx.expression.engine.client.api.thread;

import com.liukx.expression.engine.client.api.ExpressionAsyncThreadExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

/**
 * 线程池配置
 *
 * @author liukaixiong
 * @date 2025/1/15 - 11:19
 */
public class ExpressionSpringThreadExecutor implements ExpressionAsyncThreadExecutor {
    private final Logger log = LoggerFactory.getLogger(ExpressionSpringThreadExecutor.class);

    @Autowired(required = false)
    private List<ThreadPoolTaskExecutor> threadPoolTaskExecutor;

    @Override
    public ExecutorService executorService() {
        if (CollectionUtils.isEmpty(threadPoolTaskExecutor)) {
            log.debug("尝试启用jdk自带的ForkJoinPool线程池!");
            return ForkJoinPool.commonPool();
        }
        return threadPoolTaskExecutor.get(0).getThreadPoolExecutor();
    }
}
