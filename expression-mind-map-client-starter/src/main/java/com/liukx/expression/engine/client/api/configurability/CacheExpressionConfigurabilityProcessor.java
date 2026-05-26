package com.liukx.expression.engine.client.api.configurability;

import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.enums.EnginCacheKeyEnums;
import com.liukx.expression.engine.client.enums.ExpressionConfigurabilitySwitchEnum;
import com.liukx.expression.engine.client.log.LogEventEnum;
import com.liukx.expression.engine.client.log.LogHelper;
import com.liukx.expression.engine.client.process.ExpressionFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.ExpressionContextResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;

/**
 * 表达式远端缓存能力，如果你有其他特殊要求，可以继承该类并重写逻辑
 *
 * @author liukaixiong
 * @date 2025/9/9 - 13:38
 */
public class CacheExpressionConfigurabilityProcessor extends AbstractExpressionConfigurabilityProcessor {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Override
    public ExpressionContextResult configurabilityExecutor(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel configTreeModel, ExpressionFilterChain chain) {
        final Long userId = baseRequest.getUserId();

        if (userId == null) {
            LogHelper.trace(baseRequest, LogEventEnum.EXPRESSION_CALL, "用户编号是空的，无法启用远端缓存能力！");
            return chain.doFilter(envContext, configInfo, configTreeModel, baseRequest);
        }

        final EnginCacheKeyEnums cacheKeyEnums = EnginCacheKeyEnums.EXPRESSION_ID_EXECUTE;

        String cacheKey = getCacheKey(envContext, baseRequest, configInfo, configTreeModel, cacheKeyEnums);

        final Object cacheValue = redisTemplate.opsForValue().get(cacheKey);

        if (cacheValue != null) {
            LogHelper.trace(baseRequest, LogEventEnum.EXPRESSION_CALL, "命中远程缓存信息:{} -> {}", cacheKey, cacheValue);
            return (ExpressionContextResult) cacheValue;
        }

        final ExpressionContextResult result = chain.doFilter(envContext, configInfo, configTreeModel, baseRequest);

        if (result.getResult() instanceof Boolean) {
            final Boolean resultBoolean = (Boolean) result.getResult();
            Duration cacheTimeOut = getCacheTimeOut(envContext, baseRequest, configInfo, configTreeModel, cacheKeyEnums);
            LogHelper.trace(baseRequest, LogEventEnum.EXPRESSION_CALL, "缓存表达式结果:{} => 存储时长:{}", resultBoolean, cacheTimeOut);
            redisTemplate.opsForValue().set(cacheKey, result, cacheTimeOut);
        }

        return result;
    }

    /**
     * 获取过期时间
     *
     * @param envContext      上下文信息
     * @param baseRequest     请求参数
     * @param configInfo      执行器信息
     * @param configTreeModel 当前表达式信息
     * @param cacheKeyEnums   缓存key信息
     * @return 返回缓存超时时间
     */
    protected Duration getCacheTimeOut(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel configTreeModel, EnginCacheKeyEnums cacheKeyEnums) {
        return cacheKeyEnums.getTimeOut();
    }

    /**
     * 获取缓存key
     *
     * @param envContext      上下文信息
     * @param baseRequest     请求参数
     * @param configInfo      执行器信息
     * @param configTreeModel 当前表达式信息
     * @param cacheKeyEnums   缓存key信息
     * @return 缓存的key信息
     */
    protected String getCacheKey(ExpressionEnvContext envContext, ExpressionBaseRequest baseRequest, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel configTreeModel, EnginCacheKeyEnums cacheKeyEnums) {
        return cacheKeyEnums.generateKey(baseRequest.getBusinessCode(), baseRequest.getExecutorCode(), configTreeModel.getExpressionId() + "", baseRequest.getUserId() + "", baseRequest.getEventName(), baseRequest.getUnionId());
    }

    @Override
    public ExpressionConfigurabilitySwitchEnum configurabilityKey() {
        return ExpressionConfigurabilitySwitchEnum.enableCache;
    }
}
