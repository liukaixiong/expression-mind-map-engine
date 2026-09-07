package com.liukx.expression.engine.core.utils;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * 通用锁门面：保证"同一逻辑同一时刻只有一个执行者"。
 * 集群互斥类任务（定时轮转、数据修复、幂等动作等）统一通过本接口加锁，
 * 由部署环境自动选择实现，调用方不感知后端：
 * <ul>
 *   <li>{@link RedisDistributedLock} —— 宿主配置了 Redis 时启用，跨进程互斥；</li>
 *   <li>{@link LocalDistributedLock} —— 无 Redis 时退化为 JVM 内互斥（单机语义）。</li>
 * </ul>
 * 由 client-starter 的自动配置装配（不强制依赖 Redis），宿主也可注册自己的
 * {@code DistributedLock} Bean 覆盖默认选型。
 * <p>
 * 行为约定：
 * <ul>
 *   <li>获取成功 → 执行动作并在结束后释放；</li>
 *   <li>等待 {@code acquireTimeout} 仍未获取 → 跳过本次执行：
 *       {@code runWithLock} 静默返回，{@code supplyWithLock} 返回 null（调用方需容忍）；</li>
 *   <li>{@code acquireTimeout} 为零表示不等待，被持有时直接跳过；</li>
 *   <li>{@code ttl} 为持锁上限，仅对存在"持锁者宕机后锁残留"风险的实现（如 Redis）
 *       有意义；本地锁随 JVM 存亡，实现可忽略。</li>
 * </ul>
 *
 * @author liukx
 */
public interface DistributedLock {

    /**
     * 在锁保护下执行动作。
     *
     * @param lockKey        锁的逻辑名，同一逻辑的各实例/各线程必须一致
     * @param purpose        用途描述（日志用）
     * @param ttl            持锁上限，超时自动放行；本地实现忽略
     * @param acquireTimeout 获取锁等待时长，零表示不等待
     * @param action         受保护的动作
     */
    void runWithLock(String lockKey, String purpose, Duration ttl, Duration acquireTimeout, Runnable action);

    /**
     * {@link #runWithLock} 的带返回值版本。
     *
     * @return 动作执行结果；未获取到锁被跳过时返回 null
     */
    <T> T supplyWithLock(String lockKey, String purpose, Duration ttl, Duration acquireTimeout, Supplier<T> action);

}
