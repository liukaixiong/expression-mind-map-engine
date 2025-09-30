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

    private Long executorId;

    private Long expressionId;

    private List<ExpressionExecutorInfoConfig> nodeList;

}
