package com.liukx.expression.engine.client.function;

import cn.hutool.core.convert.Convert;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.utils.ClassUtils;
import com.liukx.expression.engine.core.utils.Jsons;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.List;

/**
 * 将字符串对象转换成json对象
 *
 * @author liukaixiong
 * @date 2025/3/18 - 13:50
 */
@Component
public class FnEnvSpringGetValueFunction extends AbstractSimpleFunction implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.ENV_SPRING_GET_VALUE;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        // fn_env_spring_get_value("server.port", "int", "defaultValue")
        String propertyKey = getArgsIndexValue(funArgs, 0);
        String propertyClass = getArgsIndexValue(funArgs, 1, "string");
        Object defaultValue = getArgsIndexOptionalValue(funArgs, 2);

        final Class<?> shotClass = ClassUtils.getShotClass(propertyClass);
        final String strValue = applicationContext.getEnvironment().getProperty(propertyKey);
        final Object value = Jsons.parseObject(strValue, shotClass);

        if (ObjectUtils.isEmpty(value) && defaultValue != null) {
            env.recordTraceDebugContent(getName(), "defaultValue", defaultValue);
            return Convert.convert(shotClass, defaultValue);
        }

        return value;
    }
}
