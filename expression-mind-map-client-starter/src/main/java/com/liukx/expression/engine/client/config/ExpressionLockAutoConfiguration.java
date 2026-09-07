package com.liukx.expression.engine.client.config;

import com.liukx.expression.engine.core.utils.DistributedLock;
import com.liukx.expression.engine.core.utils.LocalDistributedLock;
import com.liukx.expression.engine.core.utils.RedisDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * 通用分布式锁自动装配：按部署环境自动选择 {@link DistributedLock} 实现，
 * 不强制依赖 Redis。
 * <ul>
 *   <li>宿主配置了 Redis（存在 {@code StringRedisTemplate}）→
 *       {@link RedisDistributedLock}，跨进程互斥；</li>
 *   <li>未配置 Redis（如纯客户端应用）→ {@link LocalDistributedLock}，
 *       JVM 内互斥（单机语义，集群下任务需自身幂等）。</li>
 * </ul>
 * 宿主自行注册 {@code DistributedLock} Bean 即可覆盖默认选型。
 * <p>
 * 选型在 Bean 创建期通过 {@link ObjectProvider} 探测，不依赖自动配置类
 * 之间的处理顺序（避免 @ConditionalOnBean 的注册顺序陷阱）。
 *
 * @author liukx
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class ExpressionLockAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(DistributedLock.class)
    public DistributedLock distributedLock(ObjectProvider<StringRedisTemplate> stringRedisTemplate) {
        final StringRedisTemplate template = stringRedisTemplate.getIfAvailable();
        if (template != null) {
            log.info("expression-engine -> 分布式锁: 启用 Redis 实现(跨进程互斥)");
            return new RedisDistributedLock(template);
        }
        log.info("expression-engine -> 分布式锁: 未检测到 Redis, 启用本地 JVM 锁(单机互斥)");
        return new LocalDistributedLock();
    }

}
