package com.liukx.expression.engine;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.liukx.expression.engine.model.DemoModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 集合函数单元测试
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
public class CollectionFunctionCase {

    private AviatorEvaluatorInstance instance;

    private Map<String, Object> orderEnv() {
        List<Map<String, Object>> orders = new ArrayList<>();
        orders.add(order("A001", "PAID", 200, "u1", "张三"));
        orders.add(order("A002", "PENDING", 50, "u2", "李四"));
        orders.add(order("A003", "PAID", 100, "u1", "张三"));

        Map<String, Object> env = new HashMap<>();
        env.put("orderList", orders);
        return env;
    }

    private Map<String, Object> order(String orderId, String status, int amount, String userId, String userName) {
        Map<String, Object> user = new HashMap<>();
        user.put("userId", userId);
        user.put("name", userName);

        Map<String, Object> order = new LinkedHashMap<>();
        order.put("orderId", orderId);
        order.put("status", status);
        order.put("amount", amount);
        order.put("user", user);
        return order;
    }

    @BeforeEach
    public void init() {
        instance = AviatorEvaluator.newInstance();
        final Set<Class<?>> classes = ClassUtil.scanPackageBySuper("com.liukx.expression.engine.client.function", AviatorFunction.class);
        classes.forEach(var -> instance.addFunction((AviatorFunction) ReflectUtil.newInstance(var)));
    }

    @Test
    public void testListFirstField() {
        // 需求: 获取集合中首个满足过滤条件元素的指定字段值
        Object result = instance.execute("fn_list_first_field(orderList, lambda(x) -> x.status == 'PAID' end, 'amount')", orderEnv());
        assertEquals(200, ((Number) result).intValue());
    }

    @Test
    public void testListFirstFieldNested() {
        Object result = instance.execute("fn_list_first_field(orderList, lambda(x) -> x.status == 'PENDING' end, 'user.name')", orderEnv());
        assertEquals("李四", result);
    }

    @Test
    public void testListFirstFieldNotFound() {
        Object result = instance.execute("fn_list_first_field(orderList, lambda(x) -> x.status == 'CLOSED' end, 'amount')", orderEnv());
        assertNull(result);
    }

    @Test
    public void testListFirstFieldLambdaExtractor() {
        Map<String, Object> env = orderEnv();
        // lambda取值: 简单字段
        Object amount = instance.execute("fn_list_first_field(orderList, lambda(x) -> x.status == 'PAID' end, lambda(x) -> x.amount end)", env);
        assertEquals(200, ((Number) amount).intValue());
        // lambda取值: 计算表达式
        Object doubleAmount = instance.execute("fn_list_first_field(orderList, lambda(x) -> x.status == 'PAID' end, lambda(x) -> x.amount * 2 end)", env);
        assertEquals(400, ((Number) doubleAmount).intValue());
        // lambda取值: 嵌套字段
        Object userName = instance.execute("fn_list_first_field(orderList, lambda(x) -> x.status == 'PENDING' end, lambda(x) -> x.user.name end)", env);
        assertEquals("李四", userName);
    }

    @Test
    public void testListFirst() {
        Object result = instance.execute("fn_list_first(orderList, lambda(x) -> x.amount > 150 end)", orderEnv());
        assertEquals("A001", ((Map<?, ?>) result).get("orderId"));
    }

    @Test
    public void testListFilter() {
        List<?> result = (List<?>) instance.execute("fn_list_filter(orderList, lambda(x) -> x.status == 'PAID' end)", orderEnv());
        assertEquals(2, result.size());
        assertEquals("A001", ((Map<?, ?>) result.get(0)).get("orderId"));
    }

    @Test
    public void testListCount() {
        Map<String, Object> env = orderEnv();
        assertEquals(3L, instance.execute("fn_list_count(orderList)", env));
        assertEquals(2L, instance.execute("fn_list_count(orderList, lambda(x) -> x.status == 'PAID' end)", env));
    }

    @Test
    public void testListMatch() {
        Map<String, Object> env = orderEnv();
        assertEquals(Boolean.TRUE, instance.execute("fn_list_any_match(orderList, lambda(x) -> x.amount > 150 end)", env));
        assertEquals(Boolean.FALSE, instance.execute("fn_list_all_match(orderList, lambda(x) -> x.status == 'PAID' end)", env));
        assertEquals(Boolean.TRUE, instance.execute("fn_list_all_match(orderList, lambda(x) -> x.amount > 10 end)", env));
    }

    @Test
    public void testListFieldValues() {
        List<?> result = (List<?>) instance.execute("fn_list_field_values(orderList, 'orderId')", orderEnv());
        assertEquals(Arrays.asList("A001", "A002", "A003"), result);
    }

    @Test
    public void testListSumField() {
        Object result = instance.execute("fn_list_sum_field(orderList, 'amount')", orderEnv());
        assertEquals(0, BigDecimal.valueOf(350).compareTo((BigDecimal) result));
    }

    @Test
    public void testListGroup() {
        Map<?, ?> result = (Map<?, ?>) instance.execute("fn_list_group(orderList, 'status')", orderEnv());
        assertEquals(2, ((List<?>) result.get("PAID")).size());
        assertEquals(1, ((List<?>) result.get("PENDING")).size());
    }

    @Test
    public void testListDistinctField() {
        Map<String, Object> env = orderEnv();
        List<?> result = (List<?>) instance.execute("fn_list_distinct_field(orderList, 'user.userId')", env);
        assertEquals(2, result.size());
        assertEquals(3, ((List<?>) instance.execute("fn_list_distinct_field(seq.list(1, 2, 2, 3, 3, 3))", env)).size());
    }

    @Test
    public void testListJoinField() {
        Map<String, Object> env = orderEnv();
        assertEquals("A001,A002,A003", instance.execute("fn_list_join_field(orderList, 'orderId')", env));
        assertEquals("A001|A002|A003", instance.execute("fn_list_join_field(orderList, 'orderId', '|')", env));
    }

    @Test
    public void testListSortField() {
        Map<String, Object> env = orderEnv();
        List<?> desc = (List<?>) instance.execute("fn_list_sort_field(orderList, 'amount', 'desc')", env);
        assertEquals(200, ((Number) ((Map<?, ?>) desc.get(0)).get("amount")).intValue());
        List<?> asc = (List<?>) instance.execute("fn_list_sort_field(orderList, 'amount')", env);
        assertEquals(50, ((Number) ((Map<?, ?>) asc.get(0)).get("amount")).intValue());
    }

    @Test
    public void testListContains() {
        Map<String, Object> env = new HashMap<>();
        env.put("intList", Arrays.asList(1, 2, 3));
        // 数值跨精度比较: 集合中是Integer, 传入的是Long字面量
        assertEquals(Boolean.TRUE, instance.execute("fn_list_contains(intList, 3)", env));
        assertEquals(Boolean.FALSE, instance.execute("fn_list_contains(intList, 4)", env));
    }

    @Test
    public void testListMerge() {
        Map<String, Object> env = new HashMap<>();
        env.put("list1", Arrays.asList("a", "b"));
        env.put("list2", Arrays.asList("c"));
        List<?> result = (List<?>) instance.execute("fn_list_merge(list1, list2, seq.list('d'))", env);
        assertEquals(Arrays.asList("a", "b", "c", "d"), result);
    }

    @Test
    public void testListSize() {
        Map<String, Object> env = orderEnv();
        assertEquals(3L, instance.execute("fn_list_size(orderList)", env));
        assertEquals(0L, instance.execute("fn_list_size(missingList)", env));
    }

    @Test
    public void testBeanFieldExtract() {
        // 普通对象字段提取
        DemoModel model = new DemoModel();
        model.setName("lkx");
        model.setAge(18);

        Map<String, Object> env = new HashMap<>();
        env.put("modelList", Arrays.asList(model));

        assertEquals("lkx", instance.execute("fn_list_first_field(modelList, lambda(x) -> x.age > 10 end, 'name')", env));
        assertEquals(1L, instance.execute("fn_list_count(modelList, lambda(x) -> x.name == 'lkx' end)", env));
    }

    @Test
    public void testBusinessCombination() {
        // 组合业务场景: 首个已支付订单金额存在且总金额大于300时通过
        Map<String, Object> env = orderEnv();
        Object result = instance.execute(
            "fn_list_first_field(orderList, lambda(x) -> x.status == 'PAID' end, 'amount') != nil && fn_list_sum_field(orderList, 'amount') > 300", env);
        assertEquals(Boolean.TRUE, result);
    }
}
