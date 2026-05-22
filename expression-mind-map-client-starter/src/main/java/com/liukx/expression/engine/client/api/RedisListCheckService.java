package com.liukx.expression.engine.client.api;

import com.liukx.expression.engine.client.enums.EnginCacheKeyEnums;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 基于 Redis Set 的名单检查服务实现
 * <p>
 * Redis key 通过 {@link EnginCacheKeyEnums#WHITE_BLACK_LIST} 生成，值为 Set 结构。
 *
 * @author liukaixiong
 * @date 2026/5/14
 */
@Slf4j
public class RedisListCheckService implements ListCheckService {

    private final RedisTemplate<Object, Object> redisTemplate;

    public RedisListCheckService(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean contains(String group, String key, String value) {
        String redisKey = EnginCacheKeyEnums.WHITE_BLACK_LIST.generateKey(group, key);
        Boolean result = redisTemplate.opsForSet().isMember(redisKey, value);
        return Boolean.TRUE.equals(result);
    }

    @Override
    public boolean add(String group, String key, String value) {
        String redisKey = EnginCacheKeyEnums.WHITE_BLACK_LIST.generateKey(group, key);
        Long count = redisTemplate.opsForSet().add(redisKey, value);
        return count != null && count > 0;
    }

    @Override
    public boolean remove(String group, String key, String value) {
        String redisKey = EnginCacheKeyEnums.WHITE_BLACK_LIST.generateKey(group, key);
        Long count = redisTemplate.opsForSet().remove(redisKey, value);
        return count != null && count > 0;
    }
}
