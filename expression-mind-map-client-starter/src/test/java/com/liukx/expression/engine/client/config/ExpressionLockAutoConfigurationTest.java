package com.liukx.expression.engine.client.config;

import com.liukx.expression.engine.core.utils.DistributedLock;
import com.liukx.expression.engine.core.utils.LocalDistributedLock;
import com.liukx.expression.engine.core.utils.RedisDistributedLock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * 分布式锁自动选型测试：无 Redis 退化为本地锁、有 Redis 启用跨进程实现、
 * 宿主自定义 DistributedLock Bean 可覆盖默认选型。
 *
 * @author liukx
 */
class ExpressionLockAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExpressionLockAutoConfiguration.class));

    /** 客户端应用没有 Redis（不强制依赖）→ 本地 JVM 锁 */
    @Test
    void noRedis_fallsBackToLocalLock() {
        runner.run(context -> assertThat(context)
                .hasSingleBean(DistributedLock.class)
                .hasSingleBean(LocalDistributedLock.class));
    }

    /** 服务端/配置了 Redis 的应用 → Redis 跨进程实现 */
    @Test
    void redisPresent_usesRedisLock() {
        runner.withBean("stringRedisTemplate", StringRedisTemplate.class, () -> mock(StringRedisTemplate.class))
                .run(context -> assertThat(context)
                        .hasSingleBean(DistributedLock.class)
                        .hasSingleBean(RedisDistributedLock.class));
    }

    /** 宿主自行注册 DistributedLock（如接 ZooKeeper/etcd）→ 默认选型让位 */
    @Test
    void hostDefinedLockBean_overridesAutoSelection() {
        runner.withBean("customDistributedLock", DistributedLock.class, () -> mock(DistributedLock.class))
                .run(context -> {
                    assertThat(context).hasSingleBean(DistributedLock.class);
                    assertThat(context).doesNotHaveBean(LocalDistributedLock.class);
                    assertThat(context).doesNotHaveBean(RedisDistributedLock.class);
                });
    }
}
