package com.liukx.expression.engine.core.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author liukaixiong
 * @date 2023/12/12
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpressionConfigInfo {

    private Long executorId;

    private String serviceName;

    private String businessCode;

    private String executorCode;

    private String executorName;

    private String varDefinition;

    /**
     * 全局的变量配置
     */
    private String globalVarConfig;

    /**
     * 拓展能力
     */
    private Map<String, Object> configurabilityMap;

    /**
     * 表达式的数结构
     */
    private List<ExpressionConfigTreeModel> configTreeModelList;

    /**
     * 表达式编码映射
     */
    private Map<String, ExpressionConfigTreeModel> configExpressionCodeMap;

    /**
     * 时间戳,后续可以考虑做版本号使用
     */
    private Long timestamp;

}
