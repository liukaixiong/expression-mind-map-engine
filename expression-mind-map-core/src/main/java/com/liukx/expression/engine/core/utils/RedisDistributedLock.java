package com.liukx.expression.engine.core.utils;

import cn.hutool.core.thread.ThreadUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * {@link DistributedLock} 的 Redis 实现（SET NX + TTL + Lua 比对 token 释放），用于
 * 集群部署下保证同一逻辑同一时刻只有一个实例执行（定时任务轮转、数据修复、幂等动作等）。
 * <p>
 * 由 client-starter 的自动配置在宿主存在 {@code StringRedisTemplate} 时装配；
 * 宿主也可自行注册 {@link DistributedLock} Bean 覆盖。
 * <p>
 * 行为约定：
 * <ul>
 *   <li>未配置 Redis（{@code StringRedisTemplate} 注入为空，如未带 redis 客户端的
 *       客户端应用）：直接执行动作，等价单机部署；</li>
 *   <li>Redis 不可达等异常：降级为无锁执行——适用于本身有幂等兜底的维护类任务；
 *       对"必须互斥"的场景请使用手动 API {@link #tryLock} 自行处理异常；</li>
 *   <li>锁被其它实例持有：跳过本次执行；</li>
 *   <li>锁带 TTL，持锁实例宕机后自动放行；业务需可重入/幂等，TTL 过期后被
 *       再次执行应当是安全的。</li>
 * </ul>
 * 实际 Redis 键统一为 {@code expression-engine:lock:{lockKey}}，
 * {@link #tryLock} 与 {@link #unlock} 均按该规则拼接，成对使用即可。
 *
 * @author liukx
 */
public class RedisDistributedLock implements DistributedLock {

    private static final Logger log = LoggerFactory.getLogger(RedisDistributedLock.class);

    /** 锁键统一前缀，避免与业务键冲突 */
    public static final String LOCK_KEY_PREFIX = "expression-engine:lock:";

    private static final long RETRY_INTERVAL_MS = 200L;

    /** 比对 token 后删除，避免误删其它实例的锁 */
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT = new DefaultRedisScript<>(
            "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end",
            Long.class);

    /** 可能为空：无参构造 + 字段注入场景（宿主未配置 redis 时不强制依赖） */
    @Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    /** 自动配置装配入口：显式传入模板，不依赖字段注入 */
    public RedisDistributedLock(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** 无 Redis 环境手工构造时使用：模板为空，等价单机直接执行 */
    public RedisDistributedLock() {
    }

    /**
     * 便捷入口：在锁保护下执行动作。
     * <ul>
     *   <li>获取成功 → 执行并在结束后释放；</li>
     *   <li>等待 {@code acquireTimeout} 仍未获取（被其它实例持有）→ 跳过；</li>
     *   <li>Redis 缺失/异常 → 降级为无锁执行。</li>
     * </ul>
     *
     * @param lockKey        锁的逻辑名（自动加统一前缀），同一逻辑的各实例必须一致
     * @param purpose        用途描述（日志用）
     * @param ttl            持锁上限，超时自动放行
     * @param acquireTimeout 获取锁等待时长，零表示不等待
     * @param action         受保护的动作
     */
    @Override
    public void runWithLock(String lockKey, String purpose, Duration ttl, Duration acquireTimeout, Runnable action) {
        supplyWithLock(lockKey, purpose, ttl, acquireTimeout, () -> {
            action.run();
            return null;
        });
    }

    /**
     * {@link #runWithLock} 的带返回值版本。
     *
     * @return 动作执行结果；未获取到锁被跳过时返回 null
     */
    @Override
    public <T> T supplyWithLock(String lockKey, String purpose, Duration ttl, Duration acquireTimeout, Supplier<T> action) {
        if (redisTemplate == null) {
            return action.get();
        }
        final String token = UUID.randomUUID().toString().replace("-", "");
        Boolean acquired = null;
        try {
            acquired = doTryLock(lockKey, token, ttl, acquireTimeout);
        } catch (Exception e) {
            log.warn("[{}]分布式锁获取异常,降级为无锁执行:{}", purpose, e.getMessage());
        }
        if (Boolean.FALSE.equals(acquired)) {
            log.info("[{}]分布式锁[{}]被其它实例持有,跳过本次执行", purpose, lockKey);
            return null;
        }
        try {
            return action.get();
        } finally {
            if (Boolean.TRUE.equals(acquired)) {
                release(lockKey, token);
            }
        }
    }

    /**
     * 手动获取锁（Redis 异常向上抛出，由调用方决策降级策略）。
     *
     * @param lockKey        锁的逻辑名（自动加统一前缀）
     * @param ttl            持锁上限，超时自动放行
     * @param acquireTimeout 获取等待时长，零表示只尝试一次
     * @return 持锁 token，释放时传入 {@link #unlock}；null 表示未获取（被持有或等待超时）
     */
    public String tryLock(String lockKey, Duration ttl, Duration acquireTimeout) {
        if (redisTemplate == null) {
            // 无 Redis 环境无法上锁，返回一次性 token 表示"视为持锁直接执行"
            return UUID.randomUUID().toString().replace("-", "");
        }
        final String token = UUID.randomUUID().toString().replace("-", "");
        final boolean locked = doTryLock(lockKey, token, ttl, acquireTimeout);
        return locked ? token : null;
    }

    /**
     * 手动释放锁（Lua 比对 token，只删自己持有的锁）。
     *
     * @return true 释放成功；false 未持有/已过期
     */
    public boolean unlock(String lockKey, String token) {
        if (redisTemplate == null || token == null) {
            return true;
        }
        try {
            Long released = redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(LOCK_KEY_PREFIX + lockKey), token);
            return released != null && released > 0;
        } catch (Exception e) {
            log.warn("分布式锁[{}]释放异常(锁将随TTL自动过期):{}", lockKey, e.getMessage());
            return false;
        }
    }

    private boolean doTryLock(String lockKey, String token, Duration ttl, Duration acquireTimeout) {
        final String key = LOCK_KEY_PREFIX + lockKey;
        final long deadline = System.currentTimeMillis() + acquireTimeout.toMillis();
        do {
            final Boolean ok = redisTemplate.opsForValue().setIfAbsent(key, token, ttl);
            if (Boolean.TRUE.equals(ok)) {
                return true;
            }
            if (System.currentTimeMillis() >= deadline) {
                return false;
            }
            ThreadUtil.sleep(RETRY_INTERVAL_MS);
        } while (true);
    }

    private void release(String lockKey, String token) {
        try {
            redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(LOCK_KEY_PREFIX + lockKey), token);
        } catch (Exception e) {
            log.warn("分布式锁[{}]释放异常(锁将随TTL自动过期):{}", lockKey, e.getMessage());
        }
    }

}
