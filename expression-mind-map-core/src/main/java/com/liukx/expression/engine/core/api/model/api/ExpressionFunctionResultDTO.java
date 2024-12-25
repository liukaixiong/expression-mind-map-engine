package com.liukx.expression.engine.core.api.model.api;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 表达式函数结果数据转换层
 *
 * @author liukaixiong
 * @date 2024/7/19 - 13:27
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ExpressionFunctionResultDTO extends ExpressionResultLogDTO {

    /**
     * 函数信息
     */
    private FunctionApiModel functionApiModel;

    /**
     * 函数参数
     */
    private List<Object> funcArgs;
}
