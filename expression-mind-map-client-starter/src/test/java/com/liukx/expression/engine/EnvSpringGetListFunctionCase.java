package com.liukx.expression.engine;

import cn.hutool.core.util.ReflectUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.liukx.expression.engine.client.function.collection.FnListContainsFunction;
import com.liukx.expression.engine.client.function.env.FnEnvSpringGetListFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.mock.env.MockEnvironment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * spring配置转List函数单元测试
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
public class EnvSpringGetListFunctionCase {

    private AviatorEvaluatorInstance instance;

    @BeforeEach
    public void init() {
        MockEnvironment environment = new MockEnvironment()
            .withProperty("zhiyue-admin.teacher-assign-participating-level-ids", "[1,2,3]")
            .withProperty("level-ids.comma", "1,2,3")
            .withProperty("level-ids.space", " 4, 5 , 6 ")
            .withProperty("teacher-names", "张三,李四");

        StaticApplicationContext context = new StaticApplicationContext();
        context.setEnvironment(environment);

        instance = AviatorEvaluator.newInstance();
        final FnEnvSpringGetListFunction springGetListFunction = new FnEnvSpringGetListFunction();
        ReflectUtil.setFieldValue(springGetListFunction, "applicationContext", context);
        instance.addFunction(springGetListFunction);
        instance.addFunction(new FnListContainsFunction());
    }

    private Map<String, Object> env() {
        return new HashMap<>();
    }

    @Test
    public void testJsonArrayConfig() {
        List<?> result = (List<?>) instance.execute("fn_env_spring_get_list('zhiyue-admin.teacher-assign-participating-level-ids', 'long')", env());
        assertEquals(3, result.size());
        assertTrue(result.get(0) instanceof Long, "元素应该是Long类型");
        assertEquals(3L, result.get(2));
    }

    @Test
    public void testCommaConfig() {
        List<?> result = (List<?>) instance.execute("fn_env_spring_get_list('level-ids.comma', 'long')", env());
        assertEquals(3, result.size());
        assertTrue(result.get(0) instanceof Long);
    }

    @Test
    public void testCommaConfigWithSpace() {
        List<?> result = (List<?>) instance.execute("fn_env_spring_get_list('level-ids.space', 'int')", env());
        assertEquals(3, result.size());
        assertEquals(6, result.get(2));
    }

    @Test
    public void testDefaultStringElementType() {
        List<?> result = (List<?>) instance.execute("fn_env_spring_get_list('teacher-names')", env());
        assertEquals(2, result.size());
        assertEquals("张三", result.get(0));
    }

    @Test
    public void testMissingConfigReturnsEmptyList() {
        List<?> result = (List<?>) instance.execute("fn_env_spring_get_list('not-exists-key', 'long')", env());
        assertEquals(0, result.size());
    }

    @Test
    public void testDefaultValueWhenMissing() {
        List<?> result = (List<?>) instance.execute("fn_env_spring_get_list('not-exists-key', 'long', '9,8')", env());
        assertEquals(2, result.size());
        assertEquals(9L, result.get(0));
    }

    @Test
    public void testWorkWithListContains() {
        // 配置转List后直接配合集合函数使用
        assertEquals(Boolean.TRUE, instance.execute("fn_list_contains(fn_env_spring_get_list('zhiyue-admin.teacher-assign-participating-level-ids', 'long'), 3)", env()));
        assertEquals(Boolean.FALSE, instance.execute("fn_list_contains(fn_env_spring_get_list('zhiyue-admin.teacher-assign-participating-level-ids', 'long'), 9)", env()));
    }
}
