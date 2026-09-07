package com.liukx.expression.engine.core.api.model.api;

import com.liukx.expression.engine.core.enums.MetricKeyEnum;
import com.liukx.expression.engine.core.utils.MetricHelper;
import com.liukx.expression.engine.core.utils.TraceLogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 追踪日志待发送队列（客户端进程内收集 → 提交服务端；服务端进程内接收 → 异步落库）。
 * <p>
 * 队列原本只有条数上限（10000 条），单条 DTO 携带业务大对象时可达到数十 KB，
 * 突发流量下按字节计仍可把堆打爆。现增加：
 * <ul>
 *   <li>入队前统一瘦身 {@link TraceLogSanitizer}（幂等，老客户端数据在服务端入队处兜底）；</li>
 *   <li>队列字节预算（估算值）：超过预算的入队直接丢弃并计数告警，防止 OOM。</li>
 * </ul>
 *
 * @author liukaixiong
 * @date 2024/7/17 - 17:46
 */
public class ExpressionResultLogCollect {

    private static final Logger log = LoggerFactory.getLogger(ExpressionResultLogCollect.class);

    private static final ExpressionResultLogCollect INSTANCE = new ExpressionResultLogCollect();

    /**
     * 队列估算字节预算上限（默认 32MB）。volatile 便于测试调节。
     */
    static volatile long maxQueueBytes = 32L * 1024 * 1024;

    /** 队列内 DTO 的估算字节占用 */
    private final AtomicLong queueBytes = new AtomicLong(0);

    /** 累计丢弃条数（用于限频日志） */
    private final AtomicLong dropCount = new AtomicLong(0);

    private final ArrayBlockingQueue<ExpressionExecutorResultDTO> dataQueue = new ArrayBlockingQueue<>(10000);

    public static ExpressionResultLogCollect getInstance() {
        return INSTANCE;
    }

    /**
     * 入队：先瘦身再按字节预算准入。
     *
     * @return true 入队成功；false 因超预算或队列满被丢弃
     */
    public boolean add(ExpressionExecutorResultDTO dto) {
        if (dto == null) {
            return false;
        }
        try {
            TraceLogSanitizer.sanitizeExecutorLog(dto);
        } catch (Exception e) {
            log.warn("追踪日志瘦身失败,按原样入队:{}", e.getMessage());
        }
        final long size = TraceLogSanitizer.estimateSize(dto);
        if (queueBytes.get() + size > maxQueueBytes) {
            drop("over-byte-budget", size);
            return false;
        }
        if (!dataQueue.offer(dto)) {
            drop("queue-full", size);
            return false;
        }
        queueBytes.addAndGet(size);
        return true;
    }

    public ExpressionExecutorResultDTO poll() {
        final ExpressionExecutorResultDTO dto = dataQueue.poll();
        if (dto != null) {
            queueBytes.addAndGet(-TraceLogSanitizer.estimateSize(dto));
        }
        return dto;
    }

    public List<ExpressionExecutorResultDTO> pollBatch(int size) {
        List<ExpressionExecutorResultDTO> resultList = new ArrayList<>();
        dataQueue.drainTo(resultList, size);
        releaseBytes(resultList);
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
        releaseBytes(resultList);
        return resultList;
    }

    /**
     * 当前队列估算字节占用（观测/测试用）。
     */
    public long queueByteSize() {
        return queueBytes.get();
    }

    private void releaseBytes(List<ExpressionExecutorResultDTO> resultList) {
        if (resultList == null || resultList.isEmpty()) {
            return;
        }
        long size = 0;
        for (ExpressionExecutorResultDTO dto : resultList) {
            size += TraceLogSanitizer.estimateSize(dto);
        }
        queueBytes.addAndGet(-size);
    }

    private void drop(String reason, long size) {
        MetricHelper.increment(MetricKeyEnum.expression_trace_log_queue_drop_count, 1, "reason", reason);
        final long count = dropCount.incrementAndGet();
        if (count == 1 || count % 1000 == 0) {
            log.warn("追踪日志队列丢弃(累计第{}条) reason={} 本条估算{}B 当前队列估算{}B", count, reason, size, queueBytes.get());
        }
    }

}
