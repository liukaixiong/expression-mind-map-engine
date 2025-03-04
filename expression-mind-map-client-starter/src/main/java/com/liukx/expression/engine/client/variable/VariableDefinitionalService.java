package com.liukx.expression.engine.client.variable;

/**
 * 变量定义
 *
 * @author liukaixiong
 * @date 2025/2/27 - 15:39
 */
public interface VariableDefinitionalService {

    public String getVariableName();

    public String getVariableDescription();

    public Class<?> getVariableReturnType();

}
