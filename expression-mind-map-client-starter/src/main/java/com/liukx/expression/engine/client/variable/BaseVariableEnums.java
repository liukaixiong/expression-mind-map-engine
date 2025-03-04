package com.liukx.expression.engine.client.variable;

import lombok.Getter;

import java.time.LocalDate;

/**
 * 表达式全局上下文配置
 *
 * @author liukaixiong
 * @date 2024/11/5 - 15:47
 */
@Getter
public enum BaseVariableEnums implements VariableDefinitionalService {
    env_date_local_date_time("获取LocalDate对象", LocalDate.class),
    ;


    private final String desc;
    private final Class<?> returnType;

    BaseVariableEnums(String desc, Class<?> returnType) {
        this.desc = desc;
        this.returnType = returnType;
    }


    @Override
    public String getVariableName() {
        return this.name();
    }

    @Override
    public String getVariableDescription() {
        return getDesc();
    }

    @Override
    public Class<?> getVariableReturnType() {
        return getReturnType();
    }
}
