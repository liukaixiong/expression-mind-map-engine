package com.liukx.expression.engine.server.model.dto.request;

import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorInfoConfig;
import lombok.Data;

import java.util.List;

/**
 * 粘贴节点
 *
 * @author liukaixiong
 * @date 2025/9/28 - 17:26
 */
@Data
public class PasteExpressionConfigRequest {

    /**
     * 执行器编号
     */
    private Long executorId;

    /**
     * 表达式编号
     */
    private Long expressionId;

    /**
     * 导入类型:  insert 、override
     */
    private boolean override;

    /**
     * 导入的节点列表
     */
    private List<ExpressionExecutorInfoConfig> nodeList;

}
