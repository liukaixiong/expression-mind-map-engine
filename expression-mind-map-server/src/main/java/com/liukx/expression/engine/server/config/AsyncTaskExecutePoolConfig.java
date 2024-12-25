package com.liukx.expression.engine.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.task.TaskExecutorCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;


@Configuration
public class AsyncTaskExecutePoolConfig implements AsyncConfigurer, TaskExecutorCustomizer {

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskExecutePoolConfig.class);

    @Autowired
    private Executor executor;

    @Override
    public void customize(ThreadPoolTaskExecutor executor) {
        int processorNum = Runtime.getRuntime().availableProcessors() * 2 + 1;
        executor.setCorePoolSize(processorNum);
        executor.setMaxPoolSize(processorNum * 4);
        executor.setQueueCapacity(100);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("ENGIN-ASYNC-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public Executor getAsyncExecutor() {
        return executor;
    }

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error("ENGIN 异步任务异常!", ex);
    }
}
