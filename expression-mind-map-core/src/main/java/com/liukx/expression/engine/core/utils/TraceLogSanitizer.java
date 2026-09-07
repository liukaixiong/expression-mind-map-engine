package com.liukx.expression.engine.core.utils;

import com.liukx.expression.engine.core.api.model.api.ExpressionExecutorResultDTO;
import com.liukx.expression.engine.core.api.model.api.ExpressionResultLogDTO;
import com.liukx.expression.engine.core.enums.ExpressionLogTypeEnum;
import com.liukx.expression.engine.core.enums.MetricKeyEnum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 追踪日志瘦身工具：在日志进入待发送队列前对 DTO 统一"减重"。
 * <p>
 * 背景：追踪明细存在大量重复（空调试函数行约 40%、循环重复调用约 80%，
 * 参数/变量快照携带业务大对象引用），突发流量下按条数限流的队列仍会被
 * 字节数打爆。
 * <p>
 * 规则：
 * <ul>
 *   <li>G1 无调试内容的函数调用行整行丢弃（异常行带 errorMessage，不受影响）；</li>
 *   <li>G2 函数参数按值拷贝为短字符串，脱离业务对象引用；</li>
 *   <li>G3 表达式行连续相同的变量快照只保留第一份，重复份置空；</li>
 *   <li>G4 同一追踪内完全相同的函数调用（名称+参数+结果）只保留第一次；</li>
 *   <li>G5 尺寸上限：见各常量。</li>
 * </ul>
 * 幂等：重复调用无副作用。客户端生产端与服务端入队处各执行一次，老客户端
 * 升级前由服务端侧兜底。
 *
 * @author liukx
 */
public final class TraceLogSanitizer {

    // ==================== G5 尺寸上限 ====================
    /** 单条表达式文本上限（字符） */
    public static final int MAX_EXPRESSION_LENGTH = 1024;
    /** 单个调试变量值上限（字符） */
    public static final int MAX_DEBUG_VALUE_LENGTH = 512;
    /** 单行调试快照的变量个数上限 */
    public static final int MAX_DEBUG_ENTRY_COUNT = 50;
    /** 函数参数个数上限（超出部分计数截断） */
    public static final int MAX_FUNC_ARG_COUNT = 20;
    /** 单个函数参数上限（字符） */
    public static final int MAX_FUNC_ARG_LENGTH = 128;
    /** 字符串形式的结果上限（Boolean/数值保持原样） */
    public static final int MAX_RESULT_STRING_LENGTH = 512;
    /** 单次追踪的明细行数上限 */
    public static final int MAX_DETAIL_ROW_COUNT = 500;

    private static final String TRUNCATE_SUFFIX = "...";

    private TraceLogSanitizer() {
    }

    /**
     * 对一次执行器追踪整体瘦身（原地修改）。null 安全，异常向上抛由调用方兜底。
     */
    public static void sanitizeExecutorLog(ExpressionExecutorResultDTO dto) {
        if (dto == null) {
            return;
        }
        List<ExpressionResultLogDTO> rows = dto.getResultLogList();
        if (rows == null || rows.isEmpty()) {
            return;
        }

        List<ExpressionResultLogDTO> keptRows = new ArrayList<>(rows.size());
        // G3：上一条保留快照的表达式行指纹
        String prevSnapshotFingerprint = null;
        // G4：已见函数调用指纹
        Set<String> seenCallKeys = new HashSet<>();

        for (ExpressionResultLogDTO row : rows) {
            if (row == null) {
                continue;
            }
            final boolean isFunctionRow = ExpressionLogTypeEnum.function.name().equals(row.getResultType());
            final Map<String, Object> debug = row.getDebugTraceContent();
            final boolean debugEmpty = debug == null || debug.isEmpty();

            // G1：无调试内容的函数调用行直接丢弃
            if (isFunctionRow && debugEmpty) {
                skip("function-empty-debug");
                continue;
            }

            // G2/G5：字段瘦身（参数值拷贝、超长截断）
            sanitizeDetailLog(row);

            if (isFunctionRow) {
                // G4：完全相同的函数调用只保留第一次
                String callKey = functionCallKey(row);
                if (!seenCallKeys.add(callKey)) {
                    skip("duplicate-call");
                    continue;
                }
            } else {
                // G3：连续相同的变量快照只保留第一份
                String fingerprint = snapshotFingerprint(row.getDebugTraceContent());
                if (fingerprint != null && fingerprint.equals(prevSnapshotFingerprint)) {
                    row.setDebugTraceContent(null);
                    skip("duplicate-snapshot");
                } else {
                    prevSnapshotFingerprint = fingerprint;
                }
            }

            // G5：明细行数上限
            if (keptRows.size() >= MAX_DETAIL_ROW_COUNT) {
                skip("detail-row-cap");
                continue;
            }
            keptRows.add(row);
        }

        dto.setResultLogList(keptRows);
    }

    /**
     * 单行明细瘦身：表达式截断、调试快照按值拷贝并截断、参数字符串化、结果字符串截断。
     */
    public static void sanitizeDetailLog(ExpressionResultLogDTO row) {
        if (row == null) {
            return;
        }
        row.setExpression(truncate(row.getExpression(), MAX_EXPRESSION_LENGTH));
        row.setDebugTraceContent(compactDebugMap(row.getDebugTraceContent()));
        row.setFuncArgs(compactArgs(row.getFuncArgs()));
        row.setResult(compactResult(row.getResult()));
    }

    /**
     * 调试快照瘦身：空快照归一为 null；非空则拷贝为独立 LinkedHashMap，
     * 每个值按字符串截断（脱离业务对象引用），条数超限截断。
     */
    public static Map<String, Object> compactDebugMap(Map<String, Object> debug) {
        if (debug == null || debug.isEmpty()) {
            return null;
        }
        Map<String, Object> compact = new LinkedHashMap<>(Math.min(debug.size(), MAX_DEBUG_ENTRY_COUNT) * 2);
        int kept = 0;
        for (Map.Entry<String, Object> entry : debug.entrySet()) {
            if (kept >= MAX_DEBUG_ENTRY_COUNT) {
                compact.put("__truncated__", "debug entries over " + MAX_DEBUG_ENTRY_COUNT);
                break;
            }
            compact.put(String.valueOf(entry.getKey()), valueToString(entry.getValue(), MAX_DEBUG_VALUE_LENGTH));
            kept++;
        }
        return compact;
    }

    /**
     * 函数参数瘦身：Boolean/数值/短字符串原样保留，其余转短字符串（G2 值拷贝）；
     * 个数超限时以 "...(N more)" 结尾标记。
     */
    public static List<Object> compactArgs(List<Object> funcArgs) {
        if (funcArgs == null || funcArgs.isEmpty()) {
            return funcArgs;
        }
        List<Object> compact = new ArrayList<>(Math.min(funcArgs.size(), MAX_FUNC_ARG_COUNT) + 1);
        for (Object arg : funcArgs) {
            if (compact.size() >= MAX_FUNC_ARG_COUNT) {
                compact.add("...(" + (funcArgs.size() - MAX_FUNC_ARG_COUNT) + " more)");
                break;
            }
            if (arg == null || arg instanceof Boolean || arg instanceof Number || arg instanceof Character) {
                compact.add(arg);
            } else {
                compact.add(valueToString(arg, MAX_FUNC_ARG_LENGTH));
            }
        }
        return compact;
    }

    /**
     * 参数拼接（服务端构建 functionName(args) 用）：逐参短字符串化后逗号拼接。
     */
    public static String joinCompactArgs(List<Object> funcArgs) {
        List<Object> compact = compactArgs(funcArgs);
        if (compact == null || compact.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Iterator<Object> it = compact.iterator(); it.hasNext(); ) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    /**
     * 结果瘦身：Boolean/数值保持原样（服务端依赖 -1/布尔语义），其余转短字符串。
     */
    public static Object compactResult(Object result) {
        if (result == null || result instanceof Boolean || result instanceof Number) {
            return result;
        }
        return valueToString(result, MAX_RESULT_STRING_LENGTH);
    }

    /**
     * 估算单个执行器 DTO 的内存占用（字符数近似，避免逐条完整 JSON 序列化的 CPU 开销）。
     * 应在 {@link #sanitizeExecutorLog} 之后调用，此时各字段已是短字符串。
     */
    public static long estimateSize(ExpressionExecutorResultDTO dto) {
        if (dto == null) {
            return 0;
        }
        long size = 192
                + len(dto.getServiceName()) + len(dto.getBusinessCode()) + len(dto.getEventName())
                + len(dto.getExecutorCode()) + len(dto.getExecutorName()) + len(dto.getUnionId())
                + len(dto.getTraceId()) + len(dto.getEnvBody());
        List<ExpressionResultLogDTO> rows = dto.getResultLogList();
        if (rows == null) {
            return size;
        }
        for (ExpressionResultLogDTO row : rows) {
            if (row == null) {
                continue;
            }
            size += 96 + len(row.getExpression()) + len(row.getDescription());
            Map<String, Object> debug = row.getDebugTraceContent();
            if (debug != null) {
                for (Map.Entry<String, Object> entry : debug.entrySet()) {
                    size += len(String.valueOf(entry.getKey())) + objectLen(entry.getValue());
                }
            }
            List<Object> args = row.getFuncArgs();
            if (args != null) {
                for (Object arg : args) {
                    size += objectLen(arg);
                }
            }
        }
        return size;
    }

    /**
     * 通用截断：超长追加 "..." 标记。
     */
    public static String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + TRUNCATE_SUFFIX;
    }

    // ==================== 内部工具 ====================

    private static String valueToString(Object value, int maxLength) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return truncate((String) value, maxLength);
        }
        return truncate(String.valueOf(value), maxLength);
    }

    private static String functionCallKey(ExpressionResultLogDTO row) {
        // 函数行的 expression 字段存的是函数名
        return lenLimited(row.getExpression(), 128) + "|" + joinCompactArgs(row.getFuncArgs()) + "|" + lenLimited(row.getResult(), 128);
    }

    private static String snapshotFingerprint(Map<String, Object> debug) {
        if (debug == null || debug.isEmpty()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : debug.entrySet()) {
            if (sb.length() > 0) {
                sb.append('|');
            }
            sb.append(entry.getKey()).append('=').append(entry.getValue());
        }
        return sb.toString();
    }

    private static String lenLimited(Object value, int limit) {
        return value == null ? "null" : truncate(String.valueOf(value), limit);
    }

    private static long objectLen(Object value) {
        if (value == null) {
            return 8;
        }
        if (value instanceof String) {
            return ((String) value).length() + 24;
        }
        if (value instanceof Boolean || value instanceof Number || value instanceof Character) {
            return 32;
        }
        return 64;
    }

    private static int len(String text) {
        return text == null ? 0 : text.length();
    }

    private static void skip(String reason) {
        try {
            MetricHelper.increment(MetricKeyEnum.expression_trace_log_sanitize_skip, 1, "reason", reason);
        } catch (Exception ignore) {
            // 指标异常不影响主流程
        }
    }
}
