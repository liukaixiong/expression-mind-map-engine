package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.client.enums.EnginCacheKeyEnums;
import com.liukx.expression.engine.server.model.dto.request.AiPromptRequest;
import com.liukx.expression.engine.server.model.dto.response.AiPromptResponse;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AiPromptService {

    private static final Logger log = LoggerFactory.getLogger(AiPromptService.class);

    private static final String SERVICE_ALL = "all";
    private static final String BASE_FIELD = "base";

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    private String getHashKey() {
        return EnginCacheKeyEnums.AI_PROMPT.generateKey();
    }

    /**
     * serviceName → Redis Hash field
     * all → "base"，其他 → serviceName 本身
     */
    private String toField(String serviceName) {
        if (StringUtils.isBlank(serviceName) || SERVICE_ALL.equals(serviceName)) {
            return BASE_FIELD;
        }
        return serviceName;
    }

    /**
     * 根据类型和服务名生成文件名标识
     */
    private String toFileName(String promptType, String serviceName) {
        String baseName = "system".equalsIgnoreCase(promptType) ? "ai-system-prompt" : promptType;
        if (StringUtils.isBlank(serviceName) || SERVICE_ALL.equals(serviceName)) {
            return baseName + ".md";
        }
        return baseName + "-" + serviceName + ".md";
    }

    private AiPromptResponse buildResponse(String field, String promptType, String serviceName, String content) {
        AiPromptResponse resp = new AiPromptResponse();
        resp.setPromptKey(field);
        resp.setFileName(toFileName(promptType, serviceName));
        resp.setPromptType(promptType != null ? promptType : "SYSTEM");
        resp.setServiceName(serviceName != null ? serviceName : SERVICE_ALL);
        resp.setContent(content != null ? content : "");
        resp.setLength(resp.getContent().length());
        return resp;
    }

    public List<AiPromptResponse> list() {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(getHashKey());
        List<AiPromptResponse> result = new ArrayList<>(entries.size());
        entries.forEach((k, v) -> {
            String field = String.valueOf(k);
            String content = v != null ? String.valueOf(v) : "";
            String serviceName = BASE_FIELD.equals(field) ? SERVICE_ALL : field;
            result.add(buildResponse(field, "SYSTEM", serviceName, content));
        });
        return result;
    }

    public AiPromptResponse get(String promptKey) {
        Object value = redisTemplate.opsForHash().get(getHashKey(), promptKey);
        String content = value != null ? String.valueOf(value) : "";
        String serviceName = BASE_FIELD.equals(promptKey) ? SERVICE_ALL : promptKey;
        return buildResponse(promptKey, "SYSTEM", serviceName, content);
    }

    public AiPromptResponse save(AiPromptRequest request) {
        String field = toField(request.getServiceName());

        Object existing = redisTemplate.opsForHash().get(getHashKey(), field);
        if (existing != null && !existing.equals(request.getContent())) {
            log.info("AI提示词将被覆盖: field={}, oldLength={}, newLength={}",
                    field, String.valueOf(existing).length(), request.getContent().length());
        }

        redisTemplate.opsForHash().put(getHashKey(), field, request.getContent());
        log.info("AI提示词已保存: field={}, service={}, length={}",
                field, request.getServiceName(), request.getContent().length());

        return buildResponse(field, request.getPromptType(), request.getServiceName(), request.getContent());
    }

    public boolean delete(String promptKey) {
        Long deleted = redisTemplate.opsForHash().delete(getHashKey(), promptKey);
        log.info("AI提示词已删除: field={}, deleted={}", promptKey, deleted);
        return deleted != null && deleted > 0;
    }
}
