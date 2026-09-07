package com.liukx.expression.engine;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.core.api.model.api.ExpressionExecutorResultDTO;
import com.liukx.expression.engine.core.api.model.api.ExpressionResultLogDTO;
import com.liukx.expression.engine.core.utils.TraceLogSanitizer;
import com.liukx.expression.engine.server.mapper.ExpressionTraceLogIndexMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;
import com.liukx.expression.engine.server.service.ExpressionTraceLogInfoService;
import com.liukx.expression.engine.server.service.impl.storage.DefaultMysqlTraceLogStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 服务端存储映射端到端测试：模拟"瘦身(add) → saveTraceLog 落库映射"链路，
 * 验证 G7 的空快照 NULL、先截断再序列化、函数参数拼接、表达式截断、分批落库。
 *
 * @author liukx
 */
class DefaultMysqlTraceLogStorageMappingTest {

    private static final Long TRACE_ID = 42319L;

    private DefaultMysqlTraceLogStorageService service;
    private ExpressionTraceLogInfoService infoService;

    @BeforeEach
    void setUp() throws Exception {
        service = new DefaultMysqlTraceLogStorageService();
        infoService = mock(ExpressionTraceLogInfoService.class);
        when(infoService.saveBatch(anyCollection())).thenReturn(true);

        ExpressionTraceLogIndexMapper indexMapper = mock(ExpressionTraceLogIndexMapper.class);
        when(indexMapper.insert(any(ExpressionTraceLogIndex.class))).thenAnswer(inv -> {
            inv.getArgument(0, ExpressionTraceLogIndex.class).setId(TRACE_ID);
            return 1;
        });

        setField(service, ServiceImpl.class.getDeclaredField("baseMapper"), indexMapper);
        setField(service, service.getClass().getDeclaredField("traceLogInfoService"), infoService);
    }

    private static void setField(Object target, Field field, Object value) throws Exception {
        field.setAccessible(true);
        field.set(target, value);
    }

    private static ExpressionResultLogDTO expressionRow(String expression, Map<String, Object> snapshot, Object result) {
        ExpressionResultLogDTO row = new ExpressionResultLogDTO();
        row.setResultType("expression");
        row.setExpression(expression);
        row.setDebugTraceContent(snapshot);
        row.setResult(result);
        return row;
    }

    private static ExpressionResultLogDTO functionRow(String name, List<Object> args, Object result, Map<String, Object> debug) {
        ExpressionResultLogDTO row = new ExpressionResultLogDTO();
        row.setResultType("function");
        row.setExpression(name);
        com.liukx.expression.engine.core.api.model.api.FunctionApiModel model =
                new com.liukx.expression.engine.core.api.model.api.FunctionApiModel();
        model.setName(name);
        row.setFunctionApiModel(model);
        row.setFuncArgs(args);
        row.setResult(result);
        row.setDebugTraceContent(debug);
        return row;
    }

    @Test
    void sanitizedTrace_mapsCorrectlyToInfoRows() {
        StringBuilder longExpression = new StringBuilder();
        for (int i = 0; i < 400; i++) {
            longExpression.append("include(a,");
        }
        longExpression.append("a)");

        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("rename_title", "手机壳标题");

        ExpressionExecutorResultDTO dto = new ExpressionExecutorResultDTO();
        dto.setServiceName("svc");
        dto.setBusinessCode("biz");
        dto.setExecutorCode("summary_product_title");
        dto.setExecutorId(152L);
        dto.setEnvBody("{\"userId\":1}");
        // 1. 超长表达式行 2. 重复快照行 3. 带错误的函数行 4. 空调试函数行(应被瘦身丢弃)
        dto.getResultLogList().add(expressionRow(longExpression.toString(), snapshot, Boolean.TRUE));
        dto.getResultLogList().add(expressionRow(longExpression.toString(), new LinkedHashMap<>(snapshot), Boolean.FALSE));
        Map<String, Object> errorDebug = new LinkedHashMap<>();
        errorDebug.put("errorMessage", "boom");
        List<Object> args = new ArrayList<>();
        args.add(1);
        args.add("a,b");
        dto.getResultLogList().add(functionRow("fn_a", args, -1, errorDebug));
        dto.getResultLogList().add(functionRow("fn_b", null, Boolean.TRUE, new LinkedHashMap<>()));

        // 模拟客户端/服务端入队时的统一瘦身
        TraceLogSanitizer.sanitizeExecutorLog(dto);

        service.saveTraceLog(dto);

        List<ExpressionTraceLogInfo> saved = capturedInfoRows();
        assertEquals(3, saved.size(), "空调试函数行不应落库");

        // 行1：超长表达式先按采集瘦身上限截断，再受存储1500上限约束，两者取小
        ExpressionTraceLogInfo row1 = saved.get(0);
        assertEquals(TRACE_ID, row1.getTraceLogId());
        int expectedLength = Math.min(
                Math.min(longExpression.length(), TraceLogSanitizer.MAX_EXPRESSION_LENGTH + 3), 1503);
        assertEquals(expectedLength, row1.getExpressionContent().length(), "表达式按两级上限截断");
        assertTrue(row1.getExpressionContent().endsWith("..."));
        assertEquals(1, row1.getExpressionResult());
        assertTrue(row1.getDebugTraceContent().contains("rename_title"));

        // 行2：重复快照置NULL
        ExpressionTraceLogInfo row2 = saved.get(1);
        assertNull(row2.getDebugTraceContent(), "重复快照应存NULL");
        assertEquals(0, row2.getExpressionResult(), "FALSE映射为0");

        // 行3：函数行参数拼接 + 结果-1语义
        ExpressionTraceLogInfo row3 = saved.get(2);
        assertEquals("fn_a(1,a,b)", row3.getExpressionContent());
        assertEquals(-1, row3.getExpressionResult());
        assertTrue(row3.getDebugTraceContent().contains("errorMessage"));
    }

    @Test
    void oversizedDetailList_savedInBatches() {
        // 未经瘦身的超大明细(如自定义存储直接调用)，验证分批落库
        ExpressionExecutorResultDTO dto = new ExpressionExecutorResultDTO();
        dto.setServiceName("svc");
        dto.setBusinessCode("biz");
        dto.setExecutorCode("executor-1");
        for (int i = 0; i < 600; i++) {
            Map<String, Object> snapshot = new LinkedHashMap<>();
            snapshot.put("v", String.valueOf(i));
            dto.getResultLogList().add(expressionRow("expr_" + i, snapshot, Boolean.TRUE));
        }

        service.saveTraceLog(dto);

        verify(infoService, atLeast(2)).saveBatch(anyCollection());
        List<ExpressionTraceLogInfo> saved = capturedInfoRows();
        assertEquals(600, saved.size(), "分批不应丢数据");
    }

    @SuppressWarnings("unchecked")
    private List<ExpressionTraceLogInfo> capturedInfoRows() {
        ArgumentCaptor<Collection<ExpressionTraceLogInfo>> captor = ArgumentCaptor.forClass(Collection.class);
        verify(infoService, atLeast(0)).saveBatch(captor.capture());
        List<ExpressionTraceLogInfo> all = new ArrayList<>();
        for (Collection<ExpressionTraceLogInfo> batch : captor.getAllValues()) {
            all.addAll(batch);
        }
        return all;
    }
}
