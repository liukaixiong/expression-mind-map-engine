package com.liukx.expression.engine.client.function.env;

import cn.hutool.core.convert.Convert;
import com.liukx.expression.engine.client.api.ExpressFunctionDocumentLoader;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.function.BaseFunctionDescEnum;
import com.liukx.expression.engine.client.process.AbstractSimpleFunction;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigTreeModel;
import com.liukx.expression.engine.core.utils.AssertUtils;
import com.liukx.expression.engine.core.utils.ClassUtils;
import com.liukx.expression.engine.core.utils.Jsons;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 获取spring中的配置并解析成指定元素类型的List集合
 * <p>
 * 配置值支持两种写法(注意: yml中的数组写法会被spring拆成索引key, getProperty拿不到, 请配置成字符串):
 * <pre>
 * 1. JSON数组字符串: teacher-assign-participating-level-ids: "[1,2,3]"
 * 2. 逗号分隔:      teacher-assign-participating-level-ids: 1,2,3
 * </pre>
 * 表达式用法:
 * <pre>
 * fn_env_spring_get_list('zhiyue-admin.teacher-assign-participating-level-ids', 'long')
 * </pre>
 *
 * @author liukaixiong
 * @date 2026/9/1
 */
@Component
public class FnEnvSpringGetListFunction extends AbstractSimpleFunction implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public Enum<? extends ExpressFunctionDocumentLoader> documentRegister() {
        return BaseFunctionDescEnum.ENV_SPRING_GET_LIST_VALUE;
    }

    @Override
    public Object processor(ExpressionEnvContext env, ExpressionConfigTreeModel configTreeModel, ExpressionBaseRequest request, List<Object> funArgs) {
        String propertyKey = getArgsIndexValue(funArgs, 0);
        String elementName = getArgsIndexValue(funArgs, 1, "string");
        Object defaultValue = getArgsIndexOptionalValue(funArgs, 2);

        final Class<?> elementType = wrapPrimitive(ClassUtils.getShotClass(elementName));
        AssertUtils.Function.isTrue(!(Collection.class.isAssignableFrom(elementType) || Map.class.isAssignableFrom(elementType)),
            "函数[" + getName() + "] 第二个参数是集合的元素类型(string/int/long/double/boolean等),请勿传递list/map!");

        final String propertyValue = applicationContext.getEnvironment().getProperty(propertyKey);
        if (StringUtils.isEmpty(propertyValue)) {
            if (defaultValue != null) {
                env.recordTraceDebugContent(getName(), "defaultValue", defaultValue);
                return parseToList(defaultValue, elementType);
            }
            env.recordTraceDebugContent(getName(), "empty", propertyKey + " 配置不存在,返回空集合");
            return new ArrayList<>();
        }
        return parseToList(propertyValue, elementType);
    }

    /**
     * 基本类型归一化成包装类, 保证hutool Convert与Jackson转换都拿到class类型
     */
    private Class<?> wrapPrimitive(Class<?> clazz) {
        if (clazz == Boolean.TYPE) {
            return Boolean.class;
        } else if (clazz == Byte.TYPE) {
            return Byte.class;
        } else if (clazz == Character.TYPE) {
            return Character.class;
        } else if (clazz == Short.TYPE) {
            return Short.class;
        } else if (clazz == Integer.TYPE) {
            return Integer.class;
        } else if (clazz == Long.TYPE) {
            return Long.class;
        } else if (clazz == Float.TYPE) {
            return Float.class;
        } else if (clazz == Double.TYPE) {
            return Double.class;
        }
        return clazz;
    }

    /**
     * 解析成指定元素类型的集合: [开头走JSON数组解析, 其他按逗号分隔逐个转换
     */
    private List<Object> parseToList(Object value, Class<?> elementType) {
        final String strValue = String.valueOf(value).trim();
        if (strValue.startsWith("[")) {
            final List<?> parsedList = Jsons.parseList(strValue, elementType);
            return parsedList != null ? new ArrayList<>(parsedList) : new ArrayList<>();
        }
        List<Object> list = new ArrayList<>();
        for (String part : strValue.split(",")) {
            final String item = part.trim();
            if (StringUtils.isNotEmpty(item)) {
                list.add(Convert.convert(elementType, item));
            }
        }
        return list;
    }
}
