package com.liukx.expression.engine.client.api;

import java.util.concurrent.ExecutorService;

/**
 *
 *
 * @author liukaixiong
 * @date 2025/1/15 - 11:18
 */
public interface ExpressionAsyncThreadExecutor {

    /**
     * 异步执行
     */
    ExecutorService executorService();
}
