package com.liukx.expression.engine.client.api.configurability;

import com.liukx.expression.engine.client.api.ExpressionNodeExecutorFilter;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.enums.EnginCacheKeyEnums;
import com.liukx.expression.engine.client.enums.ExpressionCoxnfigurabilitySwitchEnum;
import com.liukx.expression.engine.client.helper.ConfigurabilityHelper;
import com.liukx.expression.engine.client.log.LogEventEnum;
import com.liukx.expression.engine.client.log.LogHelper;
import com.liukx.expression.engine.client.process.ExpressionNodeFilterChain;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.Map;

/**
 * redisson 全局锁
 *
 * @author liukaixiong
 * @date 2025/3/25 - 11:35
 */
public class RedissonLockExpressionConfigurabilityProcessor implements ExpressionNodeExecutorFilter {
    private final RedissonClient redissonClient;

    public RedissonLockExpressionConfigurabilityProcessor(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void doExpressionNodeFilter(ExpressionBaseRequest baseRequest, ExpressionEnvContext envContext, ExpressionConfigInfo configInfo, ExpressionConfigTreeModel treeModel, Object execute, ExpressionNodeFilterChain chain) {
        if (ConfigurabilityHelper.isEnableExpressionConfigurability(treeModel.getConfigurabilityMap(), ExpressionCoxnfigurabilitySwitchEnum.enableGlobalLock)) {
            final Map<String, Object> branchResult = envContext.getBranchResult(treeModel.getExpressionId());
            // 设置锁的key
            final String customerLockKey = branchResult.getOrDefault("_lockKey", "").toString();
            final String lockKey = EnginCacheKeyEnums.EXPRESSION_LOCK.generateKey(treeModel.getExpressionId() + "", customerLockKey, execute.toString());
            LogHelper.trace(baseRequest, LogEventEnum.EXPRESSION_CALL, "表达式编号:{} , 启用全局锁:{}", treeModel.getExpressionId(), lockKey);
            final RLock lock = redissonClient.getLock(lockKey);
            lock.lock();
            try {
                chain.doFilter(baseRequest, envContext, configInfo, treeModel, execute);
            } finally {
                if (lock.isHeldByCurrentThread()) {
                    lock.unlock();
                }
            }
        }
    }
}
