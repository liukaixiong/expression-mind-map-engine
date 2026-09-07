package com.liukx.expression.engine.core.utils;

import com.liukx.expression.engine.core.api.model.api.ExpressionExecutorResultDTO;
import com.liukx.expression.engine.core.api.model.api.ExpressionResultLogDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 追踪日志瘦身规则单测（G1~G5）。
 *
 * @author liukx
 */
class TraceLogSanitizerTest {

    private static ExpressionResultLogDTO functionRow(String name, List<Object> args, Object result, Map<String, Object> debug) {
        ExpressionResultLogDTO row = new ExpressionResultLogDTO();
        row.setResultType("function");
        row.setExpression(name);
        row.setFuncArgs(args);
        row.setResult(result);
        row.setDebugTraceContent(debug);
        return row;
    }

    private static ExpressionResultLogDTO expressionRow(String expression, Map<String, Object> snapshot) {
        ExpressionResultLogDTO row = new ExpressionResultLogDTO();
        row.setResultType("expression");
        row.setExpression(expression);
        row.setDebugTraceContent(snapshot);
        row.setResult(Boolean.TRUE);
        return row;
    }

    private static ExpressionExecutorResultDTO trace(ExpressionResultLogDTO... rows) {
        ExpressionExecutorResultDTO dto = new ExpressionExecutorResultDTO();
        for (ExpressionResultLogDTO row : rows) {
            dto.getResultLogList().add(row);
        }
        return dto;
    }

    // ============ G1：无调试内容的函数行丢弃 ============

    @Test
    void functionRowWithEmptyDebug_dropped() {
        ExpressionExecutorResultDTO dto = trace(
                functionRow("fn_string_include", null, Boolean.TRUE, null),
                functionRow("fn_debug_log", null, "x", singletonDebug("k", "v")));
        TraceLogSanitizer.sanitizeExecutorLog(dto);
        assertEquals(1, dto.getResultLogList().size());
        assertEquals("fn_debug_log", dto.getResultLogList().get(0).getExpression());
    }

    @Test
    void functionRowWithError_debug_dropped() {
        Map<String, Object> debug = new HashMap<>();
        debug.put("errorMessage", "boom");
        ExpressionExecutorResultDTO dto = trace(functionRow("fn_x", null, "boom", debug));
        TraceLogSanitizer.sanitizeExecutorLog(dto);
        assertEquals(1, dto.getResultLogList().size());
    }

    @Test
    void expressionRowWithEmptyDebug_kept() {
        ExpressionExecutorResultDTO dto = trace(expressionRow("a > 1", null));
        TraceLogSanitizer.sanitizeExecutorLog(dto);
        assertEquals(1, dto.getResultLogList().size());
    }

    // ============ G2/G5：参数值拷贝与尺寸上限 ============

    @Test
    void funcArgs_valueCopyAndTruncate() {
        List<Object> args = new ArrayList<>();
        args.add(123);
        args.add(Boolean.TRUE);
        args.add("short");
        args.add(new BigObject("abcdefg", 5000));
        ExpressionResultLogDTO row = functionRow("fn_a", args, "r", singletonDebug("k", "v"));

        TraceLogSanitizer.sanitizeDetailLog(row);

        List<Object> compact = row.getFuncArgs();
        assertEquals(123, compact.get(0));
        assertEquals(Boolean.TRUE, compact.get(1));
        assertEquals("short", compact.get(2));
        String objectified = (String) compact.get(3);
        assertTrue(objectified.length() <= TraceLogSanitizer.MAX_FUNC_ARG_LENGTH + 3, "大对象参数应被截断");
        assertTrue(objectified.startsWith("BigObject"));
    }

    @Test
    void funcArgs_countCappedWithMarker() {
        List<Object> args = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            args.add(i);
        }
        ExpressionResultLogDTO row = functionRow("fn_a", args, "r", singletonDebug("k", "v"));
        TraceLogSanitizer.sanitizeDetailLog(row);
        List<Object> compact = row.getFuncArgs();
        assertEquals(TraceLogSanitizer.MAX_FUNC_ARG_COUNT + 1, compact.size());
        assertTrue(String.valueOf(compact.get(compact.size() - 1)).contains("more"));
    }

    @Test
    void joinCompactArgs_mixedTypes() {
        List<Object> args = new ArrayList<>();
        args.add(1);
        args.add("a,b");
        assertEquals("1,a,b", TraceLogSanitizer.joinCompactArgs(args));
        assertEquals("", TraceLogSanitizer.joinCompactArgs(null));
    }

    @Test
    void expression_truncated() {
        StringBuilder big = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            big.append("abcdefghij");
        }
        ExpressionResultLogDTO row = expressionRow(big.toString(), null);
        TraceLogSanitizer.sanitizeDetailLog(row);
        assertEquals(TraceLogSanitizer.MAX_EXPRESSION_LENGTH + 3, row.getExpression().length());
        assertTrue(row.getExpression().endsWith("..."));
    }

    @Test
    void debugMap_valueTruncated_entryCapped_emptyToNull() {
        assertNull(TraceLogSanitizer.compactDebugMap(null));
        assertNull(TraceLogSanitizer.compactDebugMap(new HashMap<String, Object>()));

        Map<String, Object> big = new LinkedHashMap<>();
        big.put("title", repeat('x', 5000));
        for (int i = 0; i < 100; i++) {
            big.put("var" + i, "v");
        }
        Map<String, Object> compact = TraceLogSanitizer.compactDebugMap(big);
        assertEquals(TraceLogSanitizer.MAX_DEBUG_ENTRY_COUNT + 1, compact.size());
        assertTrue(((String) compact.get("title")).length() <= TraceLogSanitizer.MAX_DEBUG_VALUE_LENGTH + 3);
        assertNotNull(compact.get("__truncated__"));
    }

    @Test
    void result_numberAndBooleanKept_stringTruncated() {
        assertEquals(-1, TraceLogSanitizer.compactResult(-1));
        assertEquals(Boolean.FALSE, TraceLogSanitizer.compactResult(Boolean.FALSE));
        Object truncated = TraceLogSanitizer.compactResult(repeat('y', 4000));
        assertTrue(((String) truncated).length() <= TraceLogSanitizer.MAX_RESULT_STRING_LENGTH + 3);
    }

    // ============ G3：连续相同变量快照去重 ============

    @Test
    void consecutiveIdenticalSnapshots_onlyFirstKept() {
        Map<String, Object> snapshot = singletonDebug("rename_title", "手机壳");
        ExpressionExecutorResultDTO dto = trace(
                expressionRow("include(rename_title,'手机')", snapshot),
                functionRow("fn_debug_log", null, "x", singletonDebug("k", "v")),
                expressionRow("include(rename_title,'手机')", new LinkedHashMap<>(snapshot)),
                expressionRow("include(rename_title,'手机')", new LinkedHashMap<>(snapshot)));
        TraceLogSanitizer.sanitizeExecutorLog(dto);
        List<ExpressionResultLogDTO> rows = dto.getResultLogList();
        assertEquals(4, rows.size(), "函数行带debug保留,表达式行全部保留");
        assertNotNull(rows.get(0).getDebugTraceContent());
        assertNotNull(rows.get(1).getDebugTraceContent(), "函数行debug不受快照去重影响");
        assertNull(rows.get(2).getDebugTraceContent(), "连续重复快照应置空");
        assertNull(rows.get(3).getDebugTraceContent());
    }

    @Test
    void differentSnapshot_kept() {
        ExpressionExecutorResultDTO dto = trace(
                expressionRow("a", singletonDebug("v", "1")),
                expressionRow("b", singletonDebug("v", "2")));
        TraceLogSanitizer.sanitizeExecutorLog(dto);
        assertEquals("1", dto.getResultLogList().get(0).getDebugTraceContent().get("v"));
        assertEquals("2", dto.getResultLogList().get(1).getDebugTraceContent().get("v"));
    }

    // ============ G4：重复函数调用去重 ============

    @Test
    void duplicateFunctionCalls_onlyFirstKept() {
        ExpressionExecutorResultDTO dto = trace(
                functionRow("fn_string_include", listOf("手机", "手机壳"), Boolean.TRUE, singletonDebug("d", "1")),
                functionRow("fn_string_include", listOf("手机", "手机壳"), Boolean.TRUE, singletonDebug("d", "1")),
                functionRow("fn_string_include", listOf("手机", "手机壳"), Boolean.FALSE, singletonDebug("d", "1")));
        TraceLogSanitizer.sanitizeExecutorLog(dto);
        assertEquals(2, dto.getResultLogList().size(), "相同参数相同结果只保留一次,结果不同保留");
    }

    // ============ G5：明细行数上限 ============

    @Test
    void detailRowCapped() {
        ExpressionResultLogDTO[] rows = new ExpressionResultLogDTO[TraceLogSanitizer.MAX_DETAIL_ROW_COUNT + 100];
        for (int i = 0; i < rows.length; i++) {
            rows[i] = expressionRow("expr_" + i, singletonDebug("v", String.valueOf(i)));
        }
        ExpressionExecutorResultDTO dto = trace(rows);
        TraceLogSanitizer.sanitizeExecutorLog(dto);
        assertEquals(TraceLogSanitizer.MAX_DETAIL_ROW_COUNT, dto.getResultLogList().size());
    }

    // ============ 幂等 & 估算 ============

    @Test
    void sanitize_idempotent() {
        ExpressionExecutorResultDTO dto = trace(
                expressionRow("a", singletonDebug("v", repeat('x', 4000))),
                functionRow("fn_a", listOf(new BigObject("x", 1)), "r", singletonDebug("k", repeat('y', 4000))));
        TraceLogSanitizer.sanitizeExecutorLog(dto);
        List<ExpressionResultLogDTO> once = dto.getResultLogList();
        TraceLogSanitizer.sanitizeExecutorLog(dto);
        assertEquals(once.size(), dto.getResultLogList().size());
        assertEquals(once.get(0).getExpression(), dto.getResultLogList().get(0).getExpression());
        assertEquals(once.get(1).getFuncArgs().size(), dto.getResultLogList().get(1).getFuncArgs().size());
    }

    @Test
    void estimateSize_positiveAndBounded() {
        ExpressionExecutorResultDTO dto = trace(
                expressionRow(repeat('e', 3000), singletonDebug("v", repeat('x', 4000))));
        TraceLogSanitizer.sanitizeExecutorLog(dto);
        long size = TraceLogSanitizer.estimateSize(dto);
        assertTrue(size > 0);
        // 瘦身后估算值应被各字段上限约束在常量可推算的范围内
        assertTrue(size < TraceLogSanitizer.MAX_EXPRESSION_LENGTH + TraceLogSanitizer.MAX_DEBUG_VALUE_LENGTH + 4096,
                "估算应在尺寸上限量级内,实际=" + size);
    }

    @Test
    void truncate_general() {
        assertEquals("abc", TraceLogSanitizer.truncate("abc", 10));
        assertEquals(null, TraceLogSanitizer.truncate(null, 10));
        assertEquals(13, TraceLogSanitizer.truncate("abcdefghijklmno", 10).length());
    }

    // ============ 工具 ============

    private static Map<String, Object> singletonDebug(String key, Object value) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(key, value);
        return map;
    }

    private static List<Object> listOf(Object... items) {
        return new ArrayList<>(java.util.Arrays.asList(items));
    }

    private static String repeat(char c, int count) {
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append(c);
        }
        return sb.toString();
    }

    /** 模拟携带大字段的业务对象 */
    private static class BigObject {
        private final String name;
        private final int size;

        BigObject(String name, int size) {
            this.name = name;
            this.size = size;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("BigObject{name='").append(name).append('\'');
            sb.append(", payload=");
            for (int i = 0; i < size; i++) {
                sb.append('p');
            }
            sb.append('}');
            return sb.toString();
        }
    }
}
