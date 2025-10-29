package com.liukx.expression.engine.client.feature;

import com.googlecode.aviator.runtime.function.ClassMethodFunction;
import com.googlecode.aviator.runtime.type.AviatorJavaType;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 类方法追踪执行函数
 * 该类核心作用就是处理那些静态方法加入时统一处理
 *
 * @author liukaixiong
 * @date 2025/10/28 - 18:04
 */
public class ClassMethodTraceFunction extends ClassMethodFunction {

    public ClassMethodTraceFunction(Class<?> clazz, boolean isStatic, String name, String methodName, List<Method> methods) throws IllegalAccessException, NoSuchMethodException {
        super(clazz, isStatic, name, methodName, methods);
    }

    @Override
    public AviatorObject variadicCall(Map<String, Object> env, AviatorObject... args) {
        final ExpressionEnvContext envContext = ExpressionEnvContext.of(env);
        final AviatorObject aviatorObject = super.variadicCall(env, args);
        if (envContext.isEnableTrace()) {
            try {
                envContext.recordTraceDebugContent(getName(), "req", Arrays.stream(args).map(var -> formatString(env, var)).collect(Collectors.joining(",")));
                envContext.recordTraceDebugContent(getName(), "res", aviatorObject == null ? "null" : aviatorObject.desc(env));
            } catch (Exception e) {
                envContext.recordTraceDebugContent(getName(), "err", e.getMessage());
            }
        }
        return aviatorObject;
    }


    public String formatString(Map<String, Object> env, AviatorObject aviatorObject) {
        StringBuilder sb = new StringBuilder();
        if (aviatorObject instanceof AviatorJavaType) {
            String name = ((AviatorJavaType) aviatorObject).getName();
            sb.append(name).append("=").append(aviatorObject.getValue(env));
        } else {
            sb.append(aviatorObject.desc(env));
        }
        return sb.toString();
    }

}
