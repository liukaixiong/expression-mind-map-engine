package com.liukx.expression.engine.core.api.model.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 追踪日志待发队列的准入与字节预算测试（G6）。
 *
 * @author liukx
 */
class ExpressionResultLogCollectTest {

    private final ExpressionResultLogCollect collect = ExpressionResultLogCollect.getInstance();

    @AfterEach
    void cleanUp() {
        // 排空队列并还原预算，避免单例状态影响其它测试
        collect.pollBatch(10000);
        ExpressionResultLogCollect.maxQueueBytes = 32L * 1024 * 1024;
    }

    private static ExpressionExecutorResultDTO simpleDto() {
        ExpressionExecutorResultDTO dto = new ExpressionExecutorResultDTO();
        dto.setServiceName("test-svc");
        dto.setBusinessCode("biz");
        dto.setExecutorCode("executor-1");
        ExpressionResultLogDTO row = new ExpressionResultLogDTO();
        row.setResultType("expression");
        row.setExpression("a > 1");
        row.setResult(Boolean.TRUE);
        dto.getResultLogList().add(row);
        return dto;
    }

    @Test
    void add_thenDrain_byteBudgetReturnsToZero() {
        assertTrue(collect.add(simpleDto()));
        assertTrue(collect.queueByteSize() > 0, "入队后应有字节占用");
        final List<ExpressionExecutorResultDTO> batch = collect.pollBatch(10);
        assertFalse(batch.isEmpty());
        assertTrue(collect.add(simpleDto()));
        assertNotNull(collect.poll());
        assertEquals(0, collect.queueByteSize());
    }

    @Test
    void add_overByteBudget_dropped() {
        ExpressionResultLogCollect.maxQueueBytes = 1;
        assertFalse(collect.add(simpleDto()), "超字节预算应被丢弃");
        assertEquals(0, collect.queueByteSize());
    }

    @Test
    void add_null_rejected() {
        assertFalse(collect.add(null));
    }

    @Test
    void add_sanitizesPayload() {
        ExpressionExecutorResultDTO fat = simpleDto();
        ExpressionResultLogDTO functionRow = new ExpressionResultLogDTO();
        functionRow.setResultType("function");
        functionRow.setExpression("fn_x");
        fat.getResultLogList().add(functionRow);
        assertTrue(collect.add(fat));
        // 无debug函数行应在入队前被瘦身丢弃
        assertEquals(1, fat.getResultLogList().size());
    }
}
