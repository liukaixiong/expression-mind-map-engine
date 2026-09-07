package com.liukx.expression.engine.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * {@link DistributedLock} 的进程内实现：基于固定条数的 ReentrantLock 条纹锁，
 * 同一 lockKey 恒定映射到同一把锁，保证 JVM 内互斥。
 * <p>
 * 适用场景：单机部署、或宿主应用没有 Redis 的部署——由自动配置在缺少
 * {@code StringRedisTemplate} 时自动选用，不强制依赖 Redis。
 * 注意集群部署下本实现只保证进程内互斥，跨进程不互斥（定时任务会多实例重复执行，
 * 任务本身需幂等）。
 * <p>
 * 设计说明：
 * <ul>
 *   <li>采用固定条纹数组而非 {@code ConcurrentHashMap<String, Lock>} 动态建锁：
 *       释放时从 map 移除锁对象存在竞态（其它线程已拿到旧实例、新线程又建新实例，
 *       同 key 出现两把锁，互斥被破坏），条纹锁内存有界且无此问题；代价是不同
 *       key 可能挤在同一条纹上——只会多等，不会出错；</li>
 *   <li>{@code ttl} 参数被忽略：进程内锁随线程与 JVM 存亡，不存在"持锁实例宕机后
 *       锁残留"的问题，无需超时放行；</li>
 *   <li>等待期间被中断：恢复中断标记并跳过本次执行。</li>
 * </ul>
 *
 * @author liukx
 */
public class LocalDistributedLock implements DistributedLock {

    private static final Logger log = LoggerFactory.getLogger(LocalDistributedLock.class);

    /** 条纹数：64 远大于实际会用到的锁 key 数，不同 key 碰撞概率低 */
    private static final int STRIPE_COUNT = 64;

    private final ReentrantLock[] stripes = new ReentrantLock[STRIPE_COUNT];

    public LocalDistributedLock() {
        for (int i = 0; i < STRIPE_COUNT; i++) {
            stripes[i] = new ReentrantLock();
        }
    }

    @Override
    public void runWithLock(String lockKey, String purpose, Duration ttl, Duration acquireTimeout, Runnable action) {
        supplyWithLock(lockKey, purpose, ttl, acquireTimeout, () -> {
            action.run();
            return null;
        });
    }

    @Override
    public <T> T supplyWithLock(String lockKey, String purpose, Duration ttl, Duration acquireTimeout, Supplier<T> action) {
        final ReentrantLock lock = stripes[Math.floorMod(lockKey.hashCode(), STRIPE_COUNT)];
        final boolean acquired;
        try {
            acquired = lock.tryLock(Math.max(0L, acquireTimeout.toMillis()), TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("[{}]本地锁[{}]等待被中断,跳过本次执行", purpose, lockKey);
            return null;
        }
        if (!acquired) {
            log.info("[{}]本地锁[{}]被其它线程持有,跳过本次执行", purpose, lockKey);
            return null;
        }
        try {
            return action.get();
        } finally {
            lock.unlock();
        }
    }

}
