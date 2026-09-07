package com.liukx.expression.engine.core.utils;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 本地 JVM 锁行为测试：同 key 互斥与跳过、等锁获取、不同 key 互不阻塞、
 * 动作结束后锁正确释放。
 *
 * @author liukx
 */
class LocalDistributedLockTest {

    private final LocalDistributedLock lock = new LocalDistributedLock();

    /** 后台线程用 latch 持有指定 key，返回释放门闩 */
    private HeldKey holdKey(String key) throws InterruptedException {
        final CountDownLatch held = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        final Thread holder = new Thread(() -> lock.runWithLock(key, "持锁", Duration.ofMinutes(1), Duration.ZERO, () -> {
            held.countDown();
            try {
                release.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }));
        holder.start();
        assertTrue(held.await(2, TimeUnit.SECONDS), "持锁线程应进入临界区");
        return new HeldKey(holder, release);
    }

    @Test
    void supplyWithLock_returnsActionValue() {
        assertEquals("ok", lock.supplyWithLock("k", "测试", Duration.ofMinutes(1), Duration.ZERO, () -> "ok"));
    }

    @Test
    void sameKeyHeld_zeroTimeoutSkips_andWaitingAcquiresAfterRelease() throws Exception {
        final HeldKey held = holdKey("k");
        try {
            // 零等待：锁被其它线程持有 → 跳过，动作不执行
            AtomicInteger runs = new AtomicInteger();
            lock.runWithLock("k", "竞争", Duration.ofMinutes(1), Duration.ZERO, runs::incrementAndGet);
            assertEquals(0, runs.get(), "被持有的锁零等待应直接跳过");

            // 有限等待：释放后竞争者获得锁并执行
            final CompletableFuture<Integer> waiter = CompletableFuture.supplyAsync(() ->
                    lock.supplyWithLock("k", "等待", Duration.ofMinutes(1), Duration.ofSeconds(5), () -> 42));
            held.release.countDown();
            assertEquals(42, waiter.get(3, TimeUnit.SECONDS).intValue(), "释放后等锁者应获得锁执行");
        } finally {
            held.release.countDown();
            held.thread.join(3000);
        }
    }

    @Test
    void sameKeyHeld_waitTimeoutSkips() throws Exception {
        final HeldKey held = holdKey("k");
        try {
            assertNull(lock.supplyWithLock("k", "等待超时", Duration.ofMinutes(1), Duration.ofMillis(100), () -> "never"),
                    "持锁未释放时等待应超时跳过");
        } finally {
            held.release.countDown();
            held.thread.join(3000);
        }
    }

    @Test
    void differentKeys_doNotBlockEachOther() throws Exception {
        // "a"(hashCode 97) 与 "b"(98) 落在不同条纹，互不影响
        final HeldKey held = holdKey("a");
        try {
            assertEquals("ok", lock.supplyWithLock("b", "其它键", Duration.ofMinutes(1), Duration.ZERO, () -> "ok"));
        } finally {
            held.release.countDown();
            held.thread.join(3000);
        }
    }

    @Test
    void lockReleasedAfterAction_nextAcquireSucceeds() {
        lock.runWithLock("k", "第一次", Duration.ofMinutes(1), Duration.ZERO, () -> { });
        assertEquals("second", lock.supplyWithLock("k", "第二次", Duration.ofMinutes(1), Duration.ZERO, () -> "second"),
                "动作结束后锁应已释放,可再次获取");
    }

    private static final class HeldKey {
        final Thread thread;
        final CountDownLatch release;

        HeldKey(Thread thread, CountDownLatch release) {
            this.thread = thread;
            this.release = release;
        }
    }
}
