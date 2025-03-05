package com.liukx.expression.engine.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * json解析工具类
 * @author liukaixiong
 * @date : 2022/6/14 - 10:05
 */
public class Jsons {
    private final static Logger log = LoggerFactory.getLogger(Jsons.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 所有的日期都统一用yyyy-MM-dd HH:mm:ss格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // 忽略字符串存在，对象不存在的属性
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        //允许使用未带引号的字段名
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        //允许使用单引号
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    public static String toJsonString(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException ex) {
            log.error("json转换异常:{}", obj, ex);
        }
        return null;
    }

    public static <T> T parseObject(String content, Class<T> valueType) {

        if (!StringUtils.hasText(content)) {
            return null;
        }

        T result = null;
        try {
            if (valueType.isAssignableFrom(String.class)) {
                return (T) content;
            }
            result = objectMapper.readValue(content, valueType);
        } catch (JsonProcessingException e) {
            log.error("strToObj error -> {} -> {}", content, valueType, e);
        }
        return result;
    }

    public static <T> T parseObject(String content, TypeReference<T> valueType) {

        if (!StringUtils.hasText(content)) {
            return null;
        }

        T result = null;
        try {
//            if (valueType.isAssignableFrom(String.class)) {
//                return (T) content;
//            }
            result = objectMapper.readValue(content, valueType);
        } catch (JsonProcessingException e) {
            log.error("strToObj error -> {} -> {}", content, valueType, e);
        }
        return result;
    }

    public static <T> List<T> parseList(String content, Class<T> valueType) {
        List<T> list = new ArrayList<>();
        try {
            list = objectMapper.readValue(content,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, valueType));
        } catch (JsonProcessingException ex) {
            log.error("strToObj error -> {} -> {} ", content, valueType, ex);
        }
        return list;
    }

    public static Map<String, Object> parseMap(String json) {
        return parseObject(json, Map.class);
    }

    public static Map<String, Object> objToMap(Object obj) {
        return objectMapper.convertValue(obj, Map.class);
    }

    public static <K, V> Map<K, V> objToMap2(Object obj, Class<K> keyType, Class<V> valueType) {
        return objectMapper.convertValue(obj, new TypeReference<>() {
        });
    }

    /**
     * 压缩json字符串，保留key值，清理掉所有value为String的值，数组集合清空
     *
     * @param jsonStr
     * @return
     */
    public static String compressReserveJsonKeyString(String jsonStr) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonStr);
            clearValuesRecursive(rootNode);
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            log.warn("compressReserveJsonKeyString error : {} body:{}", e.getMessage(), jsonStr);
        }
        return "";
    }

    private static void clearValuesRecursive(JsonNode node) {
        if (node.isObject()) {
            ObjectNode objNode = (ObjectNode) node;
            objNode.fieldNames().forEachRemaining(key -> {
                JsonNode value = objNode.get(key);
                if (value.isObject() || value.isArray()) {
                    clearValuesRecursive(value); // 递归处理子节点
                }
                if (value.isTextual()) {
                    objNode.put(key, ""); // 清空当前Key的Value
                }
            });
        } else if (node.isArray()) {
            // 数组直接清空
            ArrayNode arrayNode = (ArrayNode) node;
            arrayNode.removeAll();
        }
    }

}
