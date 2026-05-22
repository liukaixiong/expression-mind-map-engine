package com.liukx.expression.engine.server.components.ai.prompt;

import com.liukx.expression.engine.client.enums.EnginCacheKeyEnums;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 从 Redis Hash 加载提示词模板，每次直接读取保证实时性。
 * Hash Key: {ENGINE_SERVER_ID}:ai_prompt
 * Hash Field: "base"（基础模板）或 "{serviceName}"（服务专属模板）
 */
@Component
public class RedisPromptLoader implements SystemPromptLoader {

    private static final Logger log = LoggerFactory.getLogger(RedisPromptLoader.class);
    private static final String BASE_FIELD = "base";

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private String getHashKey() {
        return EnginCacheKeyEnums.AI_PROMPT.generateKey();
    }

    @Override
    public String loadBase() {
        String content = (String) redisTemplate.opsForHash().get(getHashKey(), BASE_FIELD);
        if (content != null) {
            log.debug("基础提示词从 Redis 加载成功，长度: {}", content.length());
        }
        return content;
    }

    @Override
    public String loadServicePrompt(String serviceName) {
        String content = (String) redisTemplate.opsForHash().get(getHashKey(), serviceName);
        if (content != null) {
            log.debug("服务 [{}] 专属提示词从 Redis 加载成功，长度: {}", serviceName, content.length());
        }
        return content;
    }
}
