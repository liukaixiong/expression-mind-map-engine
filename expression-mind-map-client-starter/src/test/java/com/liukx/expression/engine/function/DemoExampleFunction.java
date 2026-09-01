package com.liukx.expression.engine.function;

import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.example.DemoFunDescDefinitionService;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.api.model.FunctionRequestDocumentModel;
import com.liukx.expression.engine.model.DemoModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 发送积分案例
 *
 * @author liukaixiong
 * @date 2023/12/7
 */
@Component
public class DemoExampleFunction extends AbstractSimpleFunction {
    private Logger logger = LoggerFactory.getLogger(DemoExampleFunction.class);

    /**
     * 具体业务逻辑执行
     *
     * @param env             变量上下文
     * @param configTreeModel 表达式配置对象
     * @param request         请求参数
     * @param funArgs         函数变量
     * @return
     */
    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        /*
            以下是一些常用的工具类案例使用方式
            注意参数是什么类型，怎么获取在设计之初有开发者自行定义。两边约定好即可
         */
        // 获取第一个参数，如果没有获取到则会抛出异常。适用于必填参数是否填写.Object可自行转换具体对象
        Object firstArgs = getArgsIndexValue(funArgs, 0);
        logger.info("第1个Object参数:{}", firstArgs);

        // 这里千万要注意,如果是数字类型使用该方法都会转换为Long类型
        Long longNumber = getArgsIndexValue(funArgs, 1);
        logger.info("第2个Long参数:{}", longNumber);

        // 这里可转换成任意类型的参数
        Integer numArgs = getConvertValue(funArgs, 2, Integer.class);
        logger.info("第3个Int参数:{}", numArgs);

        /**
         *  这里可传递 字符串 或者 Long 或者 Date 或者 LocalDateTime 反正只要符合时间格式即可
         * 具体可参考: {@link AbstractSimpleFunction#getDate(Object)}
         */
        Date dateArg = getArgsIndexDate(funArgs, 3);
        logger.info("第4个Date参数:{}", dateArg);

        // 这里可传递字符串请按照,号分割 比如 "1,2,3"
        final List<Long> argsIndexList = getArgsIndexList(funArgs, 4, Long.class);
        logger.info("第5个集合参数:{}", argsIndexList);

        // 获取第二个参数，如果没有的话，则返回默认值，适用于兼容老的函数
//        Long defaultIndexValue = getArgsIndexValue(funArgs, 1, 100L);
//        logger.info("第6个默认参数:{}", defaultIndexValue);

        /**
         * Map类型的参数
         * 固定参数有一个弊端就是拓展，比如你需要追加一个参数，那么最好是可选而非必填，因为一旦这个函数被应用到表达式中，都会受影响。
         * Map类型的话，可以根据key去获取，顺序不会受影响，相对于固定参数，会更灵活，但是会增加代码的复杂度
         */
        final Map<String, Object> stringObjectMap = convertMap(funArgs, 5, String.class, Object.class);
        logger.info("第7个Map参数:{}", stringObjectMap);

        // 通过上下文中根据key获取对象，不建议使用到函数中，因为这样耦合性会比较高，函数不通用，仅仅演示使用方式
        final Object objectValue = env.getObjectValue("test_env_text");
        logger.info("objectValue参数:{}", objectValue);

        // 从上下文变量中获取上游注入的对象：FunctionRequestDocumentModel
        // 适用于针对特定的业务模型函数获取该业务的上下文参数对象
        FunctionRequestDocumentModel documentModel = env.getObject(FunctionRequestDocumentModel.class);
        logger.info("DemoModel参数:{}", documentModel);

        // 从上下文变量中获取上游注入的对象：DemoModel
        // 如果你确定这个函数只用于某一块业务能力,而非通用能力可以这么获取
        // 否则不建议使用到函数中，因为这样耦合性会比较高，函数不通用，仅仅演示使用方式
        DemoModel demoModel = env.getObject(DemoModel.class);
        logger.info("DemoModel参数:{}", demoModel);

        // 可以加入追踪页面可查找的日志信息
        // getName() 函数名称 ,  key : 标识  value : 日志内容
        env.recordTraceDebugContent(getName(), "key", "内容");

        return true;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        // 函数名称定义
        return DemoFunDescDefinitionService.DEMO_EXAMPLE;
    }

}
