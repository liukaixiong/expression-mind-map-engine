package com.liukx.expression.engine.server.service.impl.doc;

import cn.hutool.core.convert.Convert;
import com.liukx.expression.engine.client.enums.EnginCacheKeyEnums;
import com.liukx.expression.engine.server.service.ExpressionDocService;
import com.liukx.expression.engine.server.service.model.doc.ExpressionDocDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 *
 * @author liukaixiong
 * @date 2024/11/12 - 16:49
 */
@Service
public class RedisFunctionService implements ExpressionDocService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static String getDocCacheKey(String serviceName) {
        return EnginCacheKeyEnums.EXPRESSION_DOC_KEY.generateKey(serviceName);
    }

    private static String getVarTypeCacheKey(String serviceName) {
        return EnginCacheKeyEnums.EXPRESSION_VAR_TYPE_KEY.generateKey(serviceName);
    }

    @Override
    public ExpressionDocDto getKeyInfo(String serviceName, String name) {
        final String key = getDocCacheKey(serviceName);
        return (ExpressionDocDto) redisTemplate.opsForHash().get(key, name);
    }

    @Override
    public boolean refresh(String serviceName, List<ExpressionDocDto> docDtos) {
        final Map<String, ExpressionDocDto> docMap = docDtos.stream().collect(Collectors.toMap(ExpressionDocDto::getName, Function.identity(), (o1, o2) -> o1));
        final String key = getDocCacheKey(serviceName);
        redisTemplate.opsForHash().putAll(key, docMap);
        return true;
    }

    @Override
    public List<ExpressionDocDto> getLikeName(String serviceName, String name, Integer size) {
        final String cacheKey = getDocCacheKey(serviceName);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(cacheKey);
        return entries.keySet().stream().filter(key -> key.toString().contains(name)).map(var -> Convert.convert(ExpressionDocDto.class, entries.get(var))).limit(size).collect(Collectors.toList());
    }
}
