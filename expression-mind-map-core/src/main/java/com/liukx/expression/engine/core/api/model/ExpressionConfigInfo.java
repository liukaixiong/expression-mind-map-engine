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

    /**
     * 拓展能力
     */
    private Map<String, Object> configurabilityMap;

    private List<ExpressionConfigTreeModel> configTreeModelList;

    private Long timestamp;

}
