package com.liukx.expression.engine.core.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author liukaixiong
 * @date 2023/12/11
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpressionConfigTreeModel {

    private Long executorId;

    /**
     * 表达式编号
     */
    private Long expressionId;

    /**
     * 标题
     */
    private String title;

    /**
     * 表达式
     */
    private String expression;

    /**
     * 表达式类型
     */
    private String expressionType;

    /**
     * 表达式编码
     */
    private String expressionCode;

    private List<ExpressionConfigTreeModel> nodeExpression;

    /**
     * 拓展能力
     */
    private Map<String, Object> configurabilityMap;
}
