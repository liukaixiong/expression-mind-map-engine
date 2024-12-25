package com.liukx.expression.engine.client.api.config;

import com.liukx.expression.engine.client.api.RemoteExpressionConfigService;
import com.liukx.expression.engine.client.enums.EnginCacheKeyEnums;
import com.liukx.expression.engine.client.enums.ExpressionConfigCallEnum;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.utils.Jsons;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 通过redis缓存获取对应的配置信息
 * 适用场景: 客户端和服务端共用同一个redis的库
 * 刷新配置是在服务端进行刷新: @see ExpressionExecutor#getRefreshBusinessConfigInfo(java.lang.String, java.lang.String, java.lang.String)
 *
 * @author liukaixiong
 * @date 2024/9/10 - 13:39
 */
public class RedisExpressionConfigService implements RemoteExpressionConfigService {
    private final Logger log = LoggerFactory.getLogger(RedisExpressionConfigService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public String configKey() {
        return ExpressionConfigCallEnum.redis.name();
    }

    @Override
    public ExpressionConfigInfo getConfigInfo(String serviceName, String businessCode, String executorCode) {
        String cacheKey = EnginCacheKeyEnums.EXECUTOR_REFRESH_KEY.generateKey(serviceName, businessCode, executorCode);
        log.debug("get cache key : {} ", cacheKey);
        Object configInfoObject = redisTemplate.opsForValue().get(cacheKey);
        if (configInfoObject != null) {
            return Jsons.parseObject(configInfoObject.toString(), ExpressionConfigInfo.class);
        }
        return null;
    }

    public void refreshConfigInfo(String serviceName, String businessCode, String executorCode, ExpressionConfigInfo expressionConfigInfo) {
        String cacheKey = EnginCacheKeyEnums.EXECUTOR_REFRESH_KEY.generateKey(serviceName, businessCode, executorCode);
        if (expressionConfigInfo != null) {
            redisTemplate.opsForValue().set(cacheKey, Jsons.toJsonString(expressionConfigInfo));
        }
    }
}
