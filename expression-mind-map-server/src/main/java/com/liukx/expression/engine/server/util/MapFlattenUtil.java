package com.liukx.expression.engine.server.util;

import java.util.LinkedHashMap;
import java.util.Map;

public class MapFlattenUtil {
    private static final String SEPARATOR = ".";

    public static Map<String, Object> flatten(Map<String, Object> source) {
        Map<String, Object> flatMap = new LinkedHashMap<>();
        flattenInternal(source, flatMap, "");
        return flatMap;
    }

    private static void flattenInternal(Map<String, Object> source, Map<String, Object> flatMap, String parentKey) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = parentKey.isEmpty() ? entry.getKey() : parentKey + SEPARATOR + entry.getKey();
            if (entry.getValue() instanceof Map) {
                flattenInternal((Map<String, Object>) entry.getValue(), flatMap, key);
            } else {
                flatMap.put(key, entry.getValue());
            }
        }
    }
}
