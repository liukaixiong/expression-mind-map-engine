package com.liukx.expression.engine.client.api.configurability;

import com.liukx.expression.engine.client.api.ExpressionExecutorFilter;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.enums.EnginCacheKeyEnums;
import com.liukx.expression.engine.client.enums.ExpressionCoxnfigurabilitySwitchEnum;
import com.liukx.expression.engine.client.helper.ConfigurabilityHelper;
import com.liukx.expression.engine.client.log.LogEventEnum;
import com.liukx.expression.engine.client.log.LogHelper;
import com.liukx.expression.engine.client.process.ExpressionFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.ExpressionContextResult;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Map;

/**
 * redisson 全局锁
 *
 * @author liukaixiong
 * @date 2025/3/25 - 11:35
 */
public class RedissonLockExpressionConfigurabilityProcessor implements ExpressionExecutorFilter {
    private final RedissonClient redissonClient;

    public RedissonLockExpressionConfigurabilityProcessor(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public ExpressionContextResult doExpressionFilter(ExpressionEnvContext env, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest baseRequest, ExpressionFilterChain chain) {
        if (ConfigurabilityHelper.isEnableExpressionConfigurability(configTreeModel.getConfigurabilityMap(), ExpressionCoxnfigurabilitySwitchEnum.enableGlobalLock)) {
            final Map<String, Object> branchResult = env.getBranchResult(configTreeModel.getExpressionId());
            // 设置锁的key
            final String customerLockKey = branchResult.getOrDefault("_lockKey", configTreeModel.getExpressionId() + ":" + baseRequest.getUserId()).toString();
            final String lockKey = EnginCacheKeyEnums.EXPRESSION_LOCK.generateKey(configTreeModel.getExpressionId() + "", customerLockKey);
            final RLock lock = redissonClient.getLock(lockKey);
            lock.lock();
            try {
                LogHelper.trace(baseRequest, LogEventEnum.EXPRESSION_CALL, "表达式编号:{} , 启用全局锁:{}", configTreeModel.getExpressionId(), lockKey);
                return chain.doFilter(env, configInfo, configTreeModel, baseRequest);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
        return chain.doFilter(env, configInfo, configTreeModel, baseRequest);
    }

}
