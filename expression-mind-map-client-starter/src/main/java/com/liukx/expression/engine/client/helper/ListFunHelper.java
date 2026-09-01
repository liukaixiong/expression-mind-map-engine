package com.liukx.expression.engine.client.helper;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 集合函数的静态工具类
 * <p>
 * 提供集合函数的通用能力: 空安全的集合转换、元素字段提取(支持Map/对象/a.b嵌套)、宽松数值比较
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
public final class ListFunHelper {

    private ListFunHelper() {
    }

    /**
     * 将参数转换成集合对象, null安全:
     * null返回空集合、集合对象拷贝转换、数组转换成集合
     *
     * @param functionName 函数名称, 用于异常提示
     * @param value        函数参数对象
     * @return 集合拷贝对象, 函数内部修改不会影响原集合
     */
    public static List<Object> toList(String functionName, Object value) {
        if (value == null) {
            return new ArrayList<>();
        }
        if (value instanceof Collection) {
            return new ArrayList<>((Collection<?>) value);
        }
        if (value instanceof Object[]) {
            return new ArrayList<>(Arrays.asList((Object[]) value));
        }
        throw new IllegalArgumentException(
            "函数[" + functionName + "] 的集合参数必须是集合类型! 当前类型: " + value.getClass().getName());
    }

    /**
     * 提取元素的字段值, 字段不存在返回null
     * <p>
     * 支持三种元素结构:
     * 1. Map结构: 直接get
     * 2. 普通对象: 反射字段获取, 包含父类字段
     * 3. 嵌套字段: 通过.分割逐层获取, 比如 user.name
     *
     * @param element 集合元素
     * @param field   字段名
     * @return 字段值
     */
    public static Object getFieldValue(Object element, String field) {
        if (element == null || field == null || field.isEmpty()) {
            return null;
        }
        Object current = element;
        for (String fieldName : field.split("\\.")) {
            if (current == null) {
                return null;
            }
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(fieldName);
            } else {
                current = getBeanFieldValue(current, fieldName);
            }
        }
        return current;
    }

    /**
     * 反射获取对象的字段值, 查找范围包含父类字段, 字段不存在返回null
     */
    private static Object getBeanFieldValue(Object bean, String fieldName) {
        for (Class<?> clazz = bean.getClass(); clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            try {
                Field field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(bean);
            } catch (NoSuchFieldException ignore) {
                // 当前类找不到就继续找父类字段
            } catch (IllegalAccessException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 宽松等值判断: 数值类型跨精度比较(Integer/Long/Double/BigDecimal), 其他走对象等值
     */
    public static boolean equalsValue(Object a, Object b) {
        if (Objects.equals(a, b)) {
            return true;
        }
        if (a instanceof Number && b instanceof Number) {
            return toBigDecimal(a).compareTo(toBigDecimal(b)) == 0;
        }
        return false;
    }

    /**
     * 数值转换, 用于跨精度比较与求和
     */
    public static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        return new BigDecimal(value.toString());
    }

    /**
     * 通用比较器: 数值统一转BigDecimal比较, 其他按字符串比较, null值排最后
     */
    public static int compareValue(Object a, Object b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return 1;
        }
        if (b == null) {
            return -1;
        }
        if (a instanceof Number && b instanceof Number) {
            return toBigDecimal(a).compareTo(toBigDecimal(b));
        }
        return a.toString().compareTo(b.toString());
    }
}
