package com.liukx.expression.engine.core.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.RedisScript;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通用 Redis 分布式锁行为测试：函数式 API 的四种路径、
 * 手动 tryLock/unlock、键前缀、零等待不重试。
 *
 * @author liukx
 */
class RedisDistributedLockTest {

    private RedisDistributedLock lock;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;

    @BeforeEach
    void setUp() throws Exception {
        lock = new RedisDistributedLock();
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        setField(lock, "redisTemplate", redisTemplate);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    @Test
    void runWithLock_acquired_runsActionAndReleasesWithPrefixedKey() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenReturn(1L);
        AtomicInteger runs = new AtomicInteger();

        lock.runWithLock("table-rotation", "测试", Duration.ofSeconds(30), Duration.ZERO, runs::incrementAndGet);

        assertEquals(1, runs.get());
        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(valueOperations).setIfAbsent(keyCaptor.capture(), anyString(), any(Duration.class));
        assertEquals(RedisDistributedLock.LOCK_KEY_PREFIX + "table-rotation", keyCaptor.getValue());
        // 释放走 Lua 比对 token
        verify(redisTemplate).execute(any(RedisScript.class), anyList(), anyString());
    }

    @Test
    void runWithLock_heldByOther_skips() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);
        AtomicInteger runs = new AtomicInteger();

        lock.runWithLock("table-rotation", "测试", Duration.ofSeconds(30), Duration.ZERO, runs::incrementAndGet);

        assertEquals(0, runs.get(), "锁被其它实例持有时应跳过");
        verify(redisTemplate, never()).execute(any(RedisScript.class), anyList(), anyString());
    }

    @Test
    void supplyWithLock_returnsValue_andNullWhenSkipped() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenReturn(1L);
        assertEquals("ok", lock.supplyWithLock("k", "测试", Duration.ofSeconds(5), Duration.ZERO, () -> "ok"));

        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);
        assertNull(lock.supplyWithLock("k", "测试", Duration.ofSeconds(5), Duration.ZERO, () -> "ok"));
    }

    @Test
    void runWithLock_redisError_degradesToRunWithoutLock() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenThrow(new RuntimeException("connection refused"));
        AtomicInteger runs = new AtomicInteger();

        lock.runWithLock("k", "测试", Duration.ofSeconds(30), Duration.ZERO, runs::incrementAndGet);

        assertEquals(1, runs.get(), "Redis 异常应降级为无锁执行,避免任务停摆");
    }

    @Test
    void runWithLock_noRedisTemplate_runsDirectly() throws Exception {
        setField(lock, "redisTemplate", null);
        AtomicInteger runs = new AtomicInteger();

        lock.runWithLock("k", "测试", Duration.ofSeconds(30), Duration.ZERO, runs::incrementAndGet);

        assertEquals(1, runs.get(), "无 Redis 环境(单机)应直接执行");
    }

    @Test
    void runWithLock_zeroAcquireTimeout_singleAttempt() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);
        long start = System.currentTimeMillis();

        lock.runWithLock("k", "测试", Duration.ofSeconds(30), Duration.ZERO, () -> { });

        assertTrue(System.currentTimeMillis() - start < 500, "零等待超时不应重试等待");
    }

    @Test
    void manualTryLockAndUnlock_roundTrip() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        when(redisTemplate.execute(any(RedisScript.class), anyList(), any())).thenReturn(1L);

        String token = lock.tryLock("manual-job", Duration.ofSeconds(10), Duration.ZERO);
        assertNotNull(token);
        assertTrue(lock.unlock("manual-job", token));
        verify(redisTemplate).execute(any(RedisScript.class), eq(java.util.Collections.singletonList(
                RedisDistributedLock.LOCK_KEY_PREFIX + "manual-job")), eq(token));
    }

    @Test
    void manualTryLock_held_returnsNull() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);
        assertNull(lock.tryLock("manual-job", Duration.ofSeconds(10), Duration.ZERO));
    }

    @Test
    void manualTryLock_redisError_propagates() {
        when(valueOperations.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .thenThrow(new RuntimeException("connection refused"));
        // 手动 API 异常向上抛,由调用方决定降级策略
        org.junit.jupiter.api.Assertions.assertThrows(RuntimeException.class,
                () -> lock.tryLock("manual-job", Duration.ofSeconds(10), Duration.ZERO));
    }
}
