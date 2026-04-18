package com.liukx.expression.engine.core.enums;

/**
 * 指标埋点枚举，集中管理所有指标名称、描述、类型和单位。
 * <p>
 * 使用方式参见 {@link com.liukx.expression.engine.core.utils.MetricHelper}
 *
 * @author liukaixiong
 * @date 2026/4/17 - 15:49
 */
public enum MetricKeyEnum {

    // ======================== 服务端指标（追踪日志） ========================
    // 这部分指标由服务端 expression-mind-map-server 产生，反映规则执行后追踪日志的写入情况
    expression_trace_log_index_save_count("表达式引擎-服务端追踪日志索引写入次数", MetricType.COUNTER, "次"),
    expression_trace_log_detail("表达式引擎-服务端单次追踪日志明细条数分布", MetricType.SUMMARY, "条"),

    // ======================== 客户端指标（配置查询 & 执行器） ========================
    // 这部分指标由客户端应用 expression-mind-map-client-starter 产生，反映规则配置获取和本地执行的运行状况

    // ---- 配置查询 ----
    expression_config_query_call("表达式引擎-客户端规则配置查询耗时", MetricType.TIMER, "ms"),

    // ---- 执行器 ----
    expression_executor_local_call("表达式引擎-客户端本地调用执行指标 ", MetricType.TIMER, "ms"),
    ;

    private final String desc;
    private final MetricType type;
    private final String unit;

    MetricKeyEnum(String desc, MetricType type, String unit) {
        this.desc = desc;
        this.type = type;
        this.unit = unit;
    }

    MetricKeyEnum(String desc) {
        this(desc, MetricType.COUNTER, null);
    }

    public String getDesc() {
        return desc;
    }

    public MetricType getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    /**
     * 指标类型
     */
    public enum MetricType {
        /**
         * 计数器，只增不减
         */
        COUNTER,
        /**
         * 计时器，记录耗时分布
         */
        TIMER,
        /**
         * 分布摘要，记录非耗时值的分布
         */
        SUMMARY,
        /**
         * 观测值，记录当前值
         */
        GAUGE
    }
}
