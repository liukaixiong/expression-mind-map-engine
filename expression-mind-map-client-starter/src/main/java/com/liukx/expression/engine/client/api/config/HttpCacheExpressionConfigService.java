package com.liukx.expression.engine.client.api.config;

import com.liukx.expression.engine.client.api.RemoteExpressionConfigService;
import com.liukx.expression.engine.client.enums.ExpressionConfigCallEnum;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简单的http+redis备份逻辑
 * 适用场景： 客户端和服务端不共用同一个redis, 但是服务端出现故障，会影响客户端获取规则配置，导致业务受影响，可以提前将规则配置暂存在客户端的缓存中，出现故障可以直接兜底
 * 超时问题后续也需考虑~这只是一个简单的兜底逻辑
 *
 * @author liukaixiong
 * @date 2025/1/10 - 13:45
 */
public class HttpCacheExpressionConfigService implements RemoteExpressionConfigService {
    private final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    private HttpExpressionConfigService httpExpressionConfigService;

    @Autowired(required = false)
    private RedisExpressionConfigService redisExpressionConfigService;
    /**
     * 版本记录
     */
    private final Map<String, Long> versionMap = new ConcurrentHashMap<>();

    /**
     * 本地配置缓存
     */
    private final Map<String, ExpressionConfigInfo> configInfoMap = new ConcurrentHashMap<>();

    @Override
    public String configKey() {
        return ExpressionConfigCallEnum.http_cache.name();
    }

    @Override
    public ExpressionConfigInfo getConfigInfo(String serviceName, String businessCode, String executorCode) {
        ExpressionConfigInfo configInfo;
        try {
            configInfo = httpExpressionConfigService.getConfigInfo(serviceName, businessCode, executorCode);
            backUpConfig(serviceName, businessCode, executorCode, configInfo);
        } catch (Exception e) {
            log.error("http获取配置失败，尝试使用缓存获取配置", e);
            configInfo = getConfigCache(serviceName, businessCode, executorCode);
        }
        return configInfo;
    }

    private void backUpConfig(String serviceName, String businessCode, String executorCode, ExpressionConfigInfo configInfo) {
        // backup 逻辑, 只要服务端没有发生变动，可减少redis更新次数
        final Long timestamp = configInfo.getTimestamp();
        if (timestamp != null) {
            final String key = getCacheKey(serviceName, businessCode, executorCode);
            final Long versionTimestamp = versionMap.getOrDefault(key, 0L);
            if (!Objects.equals(versionTimestamp, timestamp)) {
                versionMap.put(key, timestamp);
                refreshConfigCache(serviceName, businessCode, executorCode, configInfo);
            }
        }
    }

    /**
     * 获取缓存中的信息
     * @param serviceName   服务名称
     * @param businessCode  业务编码
     * @param executorCode  执行器编码
     */
    private ExpressionConfigInfo getConfigCache(String serviceName, String businessCode, String executorCode) {
        if (redisExpressionConfigService != null) {
            return redisExpressionConfigService.getConfigInfo(serviceName, businessCode, executorCode);
        }
        return configInfoMap.get(getCacheKey(serviceName, businessCode, executorCode));
    }

    private String getCacheKey(String serviceName, String businessCode, String executorCode) {
        return serviceName + "-" + businessCode + "-" + executorCode;
    }

    /**
     * 刷新缓存
     * @param serviceName   服务名称
     * @param businessCode  业务编码
     * @param executorCode  执行器编码
     * @param configInfo    配置信息
     */
    private void refreshConfigCache(String serviceName, String businessCode, String executorCode, ExpressionConfigInfo configInfo) {
        try {
            if (redisExpressionConfigService != null) {
                redisExpressionConfigService.refreshConfigInfo(serviceName, businessCode, executorCode, configInfo);
            } else {
                configInfoMap.put(getCacheKey(serviceName, businessCode, executorCode), configInfo);
            }
        } catch (Exception e) {
            log.error("表达式配置缓存刷新失败", e);
        }
    }
}
