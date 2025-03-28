package com.liukx.expression.engine;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 *
 * @author liukaixiong
 * @date 2025/2/27 - 15:19
 */
public class AviatorCase {
    final AviatorEvaluatorInstance instance = AviatorEvaluator.getInstance();

    @Before
    public void init() {
        instance.enableSandboxMode();
        final Set<Class<?>> classes = ClassUtil.scanPackageBySuper("com.liukx.expression.engine.client.function", AviatorFunction.class);
        classes.forEach(var -> {
            try {
                instance.addFunction((AviatorFunction) ReflectUtil.newInstance(var));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

    @Test
    public void testExpressionVariable() {
        Map<String, Object> envContext = new HashMap<>();
        envContext.put("test_env_id", 888L);
        final Object execute = instance.execute("fn_object_is_not_null(test_env_id,a)", envContext);
        System.out.println(execute);
    }

    @Test
    public void testExpressionValid() {
        Map<String, Object> envContext = new HashMap<>();
        envContext.put("test_env_id", 888L);
        instance.validate("(fn_object_is_not_null(test_env_id,a)");
    }

    @Test
    public void testExpressionFunction() {
        Map<String, Object> envContext = new HashMap<>();
        envContext.put("test_env_id", 888L);
        final List<String> functionNames = instance.compile(" fn_env_put_value('activityInfo',activity_get_code_prefix_info('lottery_random_loop')) && boolean(fn_env_get_value('activityInfo'))&& (family_black_white_list('test','user',0) || family_read_student_valid('familyId',env_user_obj.familyId))").getFunctionNames();
        System.out.println(functionNames);
    }

}
