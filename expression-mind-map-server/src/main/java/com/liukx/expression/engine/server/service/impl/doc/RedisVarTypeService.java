package com.liukx.expression.engine.server.service.impl.doc;

import com.liukx.expression.engine.client.enums.EnginCacheKeyEnums;
import com.liukx.expression.engine.core.api.model.api.VariableApiModel;
import com.liukx.expression.engine.server.service.ExpressionVarTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 *
 * @author liukaixiong
 * @date 2024/11/12 - 16:49
 */
@Service
public class RedisVarTypeService implements ExpressionVarTypeService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static String getVarTypeCacheKey(String serviceName) {
        return EnginCacheKeyEnums.EXPRESSION_VAR_TYPE_KEY.generateKey(serviceName);
    }

    @Override
    public List<VariableApiModel> getKeyInfo(String serviceName, String name) {
        final String key = getVarTypeCacheKey(serviceName);
        return (List<VariableApiModel>) redisTemplate.opsForHash().get(key, name);
    }

    @Override
    public boolean refresh(String serviceName, List<VariableApiModel> docDtos) {
        final Map<String, List<VariableApiModel>> type = docDtos.stream().collect(Collectors.groupingBy(VariableApiModel::getGroupName));
        final String varTypeCacheKey = getVarTypeCacheKey(serviceName);
        redisTemplate.opsForHash().putAll(varTypeCacheKey, type);
        return true;
    }

    @Override
    public Set<Object> getAllTypeList(String serviceName) {
        final String varTypeCacheKey = getVarTypeCacheKey(serviceName);
        final Map<Object, Object> entries = redisTemplate.opsForHash().entries(varTypeCacheKey);
        return entries.keySet();
    }
}
