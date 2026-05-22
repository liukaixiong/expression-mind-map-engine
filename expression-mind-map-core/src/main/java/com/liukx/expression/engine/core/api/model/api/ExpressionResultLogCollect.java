package com.liukx.expression.engine.core.api.model.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author liukaixiong
 * @date 2024/7/17 - 17:46
 */
public class ExpressionResultLogCollect {

    private static final ExpressionResultLogCollect INSTANCE = new ExpressionResultLogCollect();
    private final ArrayBlockingQueue<ExpressionExecutorResultDTO> dataQueue = new ArrayBlockingQueue<>(10000);

    public static ExpressionResultLogCollect getInstance() {
        return INSTANCE;
    }

    public void add(ExpressionExecutorResultDTO dto) {
        dataQueue.offer(dto);
    }

    public ExpressionExecutorResultDTO poll() {
        return dataQueue.poll();
    }

    public List<ExpressionExecutorResultDTO> pollBatch(int size) {
        List<ExpressionExecutorResultDTO> resultList = new ArrayList<>();
        dataQueue.drainTo(resultList, size);
        return resultList;
    }

    /**
     * 阻塞式批量获取，直到队列有数据才返回，避免空轮询浪费 CPU
     *
     * @param size 最大批量大小
     * @return 至少包含一条数据的列表
     * @throws InterruptedException 线程被中断时抛出，用于优雅退出
     */
    public List<ExpressionExecutorResultDTO> takeBatch(int size) throws InterruptedException {
        List<ExpressionExecutorResultDTO> resultList = new ArrayList<>();
        resultList.add(dataQueue.take());
        dataQueue.drainTo(resultList, size - 1);
        return resultList;
    }

}
