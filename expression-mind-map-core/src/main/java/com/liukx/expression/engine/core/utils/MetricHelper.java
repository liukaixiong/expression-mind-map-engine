package com.liukx.expression.engine.core.utils;

import com.liukx.expression.engine.core.enums.MetricKeyEnum;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * 基于 Micrometer 的通用指标埋点工具类。
 * <p>
 * 所有指标定义集中在 {@link MetricKeyEnum}，通过枚举引用指标名称、描述和单位，
 * 避免业务代码中出现硬编码字符串。配合 {@code micrometer-registry-prometheus}
 * 通过 {@code /actuator/prometheus} 端点直接暴露 Prometheus 格式指标。
 *
 * <h3>推荐用法（枚举方式）</h3>
 * <pre>{@code
 * // 计数
 * MetricHelper.increment(MetricKeyEnum.expression_trace_log_index_save_count, 1,
 *     "serviceName", svc, "executorCode", code);
 *
 * // 分布值
 * MetricHelper.record(MetricKeyEnum.expression_trace_log_detail, 12,
 *     "serviceName", svc, "executorCode", code);
 *
 * // 耗时
 * Config config = MetricHelper.timed(MetricKeyEnum.expression_config_query_call, () ->
 *     queryConfig(svc, biz, code), "serviceName", svc, "executorCode", code);
 * }</pre>
 *
 * <h3>新增指标</h3>
 * 在 {@link MetricKeyEnum} 中添加枚举值即可，无需修改工具类。
 *
 * @author liukaixiong
 * @see MetricKeyEnum
 * @see Metrics
 */
public class MetricHelper {

    private MetricHelper() {
    }

    // ==================== Counter（枚举）====================

    /** 计数器 +1（枚举方式，自动携带描述和单位）。 */
    public static void increment(MetricKeyEnum metric) {
        resolveCounter(metric.name(), metric.getDesc(), metric.getUnit()).increment();
    }

    /** 计数器 +value（枚举方式）。 */
    public static void increment(MetricKeyEnum metric, long value) {
        resolveCounter(metric.name(), metric.getDesc(), metric.getUnit()).increment(value);
    }

    /** 计数器 +value，附带标签（枚举方式）。 */
    public static void increment(MetricKeyEnum metric, long value, String... keyValueTags) {
        resolveCounter(metric.name(), metric.getDesc(), metric.getUnit(), keyValueTags).increment(value);
    }

    // ==================== DistributionSummary（枚举）====================

    /** 记录一个 double 观测值（枚举方式）。 */
    public static void record(MetricKeyEnum metric, double value) {
        resolveSummary(metric.name(), metric.getDesc(), metric.getUnit()).record(value);
    }

    /** 记录一个 double 观测值，附带标签（枚举方式）。 */
    public static void record(MetricKeyEnum metric, double value, String... keyValueTags) {
        resolveSummary(metric.name(), metric.getDesc(), metric.getUnit(), keyValueTags).record(value);
    }

    /** 记录一个 long 观测值（枚举方式）。 */
    public static void record(MetricKeyEnum metric, long value) {
        resolveSummary(metric.name(), metric.getDesc(), metric.getUnit()).record((double) value);
    }

    /** 记录一个 long 观测值，附带标签（枚举方式）。 */
    public static void record(MetricKeyEnum metric, long value, String... keyValueTags) {
        resolveSummary(metric.name(), metric.getDesc(), metric.getUnit(), keyValueTags).record((double) value);
    }

    // ==================== UpDownCounter ====================

    private static final ConcurrentMap<String, AtomicLong> GAUGE_HOLDERS = new ConcurrentHashMap<>();

    /** 增减计数器 +delta。 */
    public static void change(String name, long delta) {
        gaugeHolder(name).addAndGet(delta);
    }

    /** 增减计数器 +delta，附带标签。 */
    public static void change(String name, long delta, String... keyValueTags) {
        gaugeHolder(name, keyValueTags).addAndGet(delta);
    }

    // ==================== Timer（枚举）====================

    /** 执行逻辑并自动将耗时记录到 Timer（枚举方式，自动携带描述和单位）。 */
    public static void timed(MetricKeyEnum metric, Runnable action, String... keyValueTags) {
        resolveTimer(metric.name(), metric.getDesc(), metric.getUnit(), keyValueTags).record(action);
    }

    /** 执行有返回值的逻辑并自动将耗时记录到 Timer（枚举方式）。 */
    public static <T> T timed(MetricKeyEnum metric, Supplier<T> action, String... keyValueTags) {
        return resolveTimer(metric.name(), metric.getDesc(), metric.getUnit(), keyValueTags).record(() -> action.get());
    }

    // ==================== Gauge ====================

    /** 注册一个 Long Gauge。 */
    public static void observeLong(String name, String description, String unit, Supplier<Long> supplier) {
        observeLong(name, description, unit, supplier, new String[0]);
    }

    /** 注册一个 Long Gauge（无描述和单位）。 */
    public static void observeLong(String name, Supplier<Long> supplier) {
        observeLong(name, null, null, supplier);
    }

    /** 注册一个带标签的 Long Gauge。 */
    public static void observeLong(String name, String description, String unit,
                                   Supplier<Long> supplier, String... keyValueTags) {
        io.micrometer.core.instrument.Gauge.builder(name, supplier, s -> s.get() == null ? 0.0 : s.get().doubleValue())
                .description(defaultIfEmpty(description, ""))
                .tags(Tags.of(keyValueTags))
                .register(Metrics.globalRegistry);
    }

    /** 注册一个 Double Gauge。 */
    public static void observeDouble(String name, String description, String unit, Supplier<Double> supplier) {
        observeDouble(name, null, null, supplier, new String[0]);
    }

    /** 注册一个 Double Gauge（无描述和单位）。 */
    public static void observeDouble(String name, Supplier<Double> supplier) {
        observeDouble(name, null, null, supplier);
    }

    /** 注册一个带标签的 Double Gauge。 */
    public static void observeDouble(String name, String description, String unit,
                                     Supplier<Double> supplier, String... keyValueTags) {
        io.micrometer.core.instrument.Gauge.builder(name, supplier, s -> s.get() == null ? 0.0 : s.get())
                .description(defaultIfEmpty(description, ""))
                .tags(Tags.of(keyValueTags))
                .register(Metrics.globalRegistry);
    }

    // ==================== Meter 解析（自动从枚举读取描述/单位）====================

    private static io.micrometer.core.instrument.Counter resolveCounter(String name, String desc, String unit) {
        return io.micrometer.core.instrument.Counter.builder(name)
                .description(defaultIfEmpty(desc, ""))
                .baseUnit(defaultIfEmpty(unit, ""))
                .register(Metrics.globalRegistry);
    }

    private static io.micrometer.core.instrument.Counter resolveCounter(String name, String desc, String unit, String[] tags) {
        return io.micrometer.core.instrument.Counter.builder(name)
                .description(defaultIfEmpty(desc, ""))
                .baseUnit(defaultIfEmpty(unit, ""))
                .tags(Tags.of(tags))
                .register(Metrics.globalRegistry);
    }

    private static DistributionSummary resolveSummary(String name, String desc, String unit) {
        return DistributionSummary.builder(name)
                .description(defaultIfEmpty(desc, ""))
                .baseUnit(defaultIfEmpty(unit, ""))
                .register(Metrics.globalRegistry);
    }

    private static DistributionSummary resolveSummary(String name, String desc, String unit, String[] tags) {
        return DistributionSummary.builder(name)
                .description(defaultIfEmpty(desc, ""))
                .baseUnit(defaultIfEmpty(unit, ""))
                .tags(Tags.of(tags))
                .register(Metrics.globalRegistry);
    }

    private static Timer resolveTimer(String name, String desc, String unit, String[] tags) {
        return Timer.builder(name)
                .description(defaultIfEmpty(desc, ""))
                .publishPercentileHistogram()
                .tags(Tags.of(tags))
                .register(Metrics.globalRegistry);
    }

    // ==================== UpDownCounter 内部工具 ====================

    private static AtomicLong gaugeHolder(String name, String... tags) {
        String key = gaugeCacheKey(name, tags);
        return GAUGE_HOLDERS.computeIfAbsent(key, k ->
                Metrics.gauge(name, Tags.of(tags), new AtomicLong(0)));
    }

    private static String gaugeCacheKey(String name, String... tags) {
        if (tags == null || tags.length == 0) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        for (String tag : tags) {
            sb.append(':').append(tag);
        }
        return sb.toString();
    }

    private static String defaultIfEmpty(String value, String defaultValue) {
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
}
