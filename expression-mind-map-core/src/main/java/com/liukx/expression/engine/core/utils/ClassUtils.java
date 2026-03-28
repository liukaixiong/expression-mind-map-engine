package com.liukx.expression.engine.core.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author liukaixiong
 * @date 2024/11/14 - 15:00
 */
public class ClassUtils {
    private static final Map<String, Class<?>> namePrimitiveMap = new HashMap<>();

    static {
        namePrimitiveMap.put("boolean", Boolean.TYPE);
        namePrimitiveMap.put("string", String.class);
        namePrimitiveMap.put("byte", Byte.TYPE);
        namePrimitiveMap.put("char", Character.TYPE);
        namePrimitiveMap.put("short", Short.TYPE);
        namePrimitiveMap.put("int", Integer.TYPE);
        namePrimitiveMap.put("long", Long.TYPE);
        namePrimitiveMap.put("double", Double.TYPE);
        namePrimitiveMap.put("float", Float.TYPE);
        namePrimitiveMap.put("void", Void.TYPE);
        namePrimitiveMap.put("list", ArrayList.class);
        namePrimitiveMap.put("map", HashMap.class);
    }

    /**
     * 获取短名称的类
     *
     * @param shotClass 短名称
     * @param clazz     默认类
     * @return
     */
    public static Class<?> getShotClass(String shotClass, Class<?> clazz) {
        return namePrimitiveMap.getOrDefault(shotClass, clazz);
    }

    /**
     * 获取短名类
     *
     * @param shotClass 短名
     * @return
     */
    public static Class<?> getShotClass(String shotClass) {
        return getShotClass(shotClass, Object.class);
    }

}
