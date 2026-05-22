package com.liukx.expression.engine;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.AviatorEvaluatorInstance;
import com.googlecode.aviator.Expression;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.runtime.type.AviatorFunction;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.log.AviatorLoggerTraceOutputStream;
import com.liukx.expression.engine.client.process.AviatorEvaluatorServiceImpl;
import com.liukx.expression.engine.core.api.model.ExpressionContextResult;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.autoconfigure.security.SecurityProperties;

import java.util.*;

/**
 * @author liukaixiong
 * @date 2025/2/27 - 15:19
 */
public class AviatorCase {
    final AviatorEvaluatorInstance instance = AviatorEvaluator.getInstance();

    private final AviatorEvaluatorServiceImpl aviatorEvaluatorService = new AviatorEvaluatorServiceImpl();

    @Before
    public void init() {
        // 这里线上环境不能轻易打开，因为他会默认执行所有表达是里面的内容，仅调试使用,请谨慎使用。
        // 比如 false && save(xxx) , 理论上第一个为false,后面就不会执行save方法了,但调试阶段的话就会执行
        instance.setOption(Options.TRACE_EVAL, true);
        instance.setTraceOutputStream(new AviatorLoggerTraceOutputStream());
        instance.enableSandboxMode();
        final Set<Class<?>> classes = ClassUtil.scanPackageBySuper(BaseFunctionDescEnum.class.getPackageName(), AviatorFunction.class);
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
        envContext.put("a", "1111");
        final Object execute = instance.execute("fn_object_is_not_null(test_env_id,a)", envContext);
        System.out.println(execute);
    }

    @Test
    public void testExpressionVariableInfo() {
        Map<String, Object> envContext = new HashMap<>();


        SecurityProperties.User user = new SecurityProperties.User();
        user.setName("lkx");
        Map<String, Object> nodeEnv = new HashMap<>();
        nodeEnv.put("node_A", 1);
        nodeEnv.put("node_B", "2");
        nodeEnv.put("node_C", true);
        nodeEnv.put("nodeInfo", user);

        envContext.put("test_env_id", 888L);
        envContext.put("a", "1111");
        envContext.put("node", nodeEnv);
        final String exp = "test_env_id < node.node_A && !node.node_C && fn_object_is_not_null(test_env_id,a) && fn_object_is_not_null(node.nodeInfo.name,a)";
        final Expression compile = instance.compile(exp, true);
        final Object execute = compile.execute(envContext);
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

    @Test
    public void testStaticFunction() {
        Map<String, Object> env = new LinkedHashMap<>();
        final ExpressionEnvContext envContext = ExpressionEnvContext.of(env);
        envContext.addEnvContext("startDate", DateUtil.parseDate("2025-02-27"));
        envContext.addEnvContext("endDate", DateUtil.parseDate("2025-03-27"));
        envContext.enableTrace();
//        final Object execute = aviatorEvaluatorService.execute("objectUtils.compare(startDate,endDate)", env);
        final Object execute = aviatorEvaluatorService.execute("startDate >= '2026-01-01'", env);
        System.out.println(execute);
    }

    @Test
    public void testTrueKeyword() {
        Map<String, Object> env = new LinkedHashMap<>();
        final ExpressionEnvContext envContext = ExpressionEnvContext.of(env);

        int count = 100000;
        String expression = "true";
        long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            final ExpressionContextResult result = aviatorEvaluatorService.execute(expression, env);
        }
        long time = System.currentTimeMillis() - start;
        System.out.println("expression : " + time);

        long start1 = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            final boolean equals = "true".equals(expression);
        }
        long time1 = System.currentTimeMillis() - start1;
        System.out.println("eq : " + time1);

    }

}
