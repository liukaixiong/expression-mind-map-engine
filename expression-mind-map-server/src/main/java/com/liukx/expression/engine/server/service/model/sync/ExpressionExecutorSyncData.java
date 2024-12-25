package com.liukx.expression.engine.server.service.model.sync;

import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorBaseInfo;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorDetailConfig;
import lombok.Data;

import java.util.List;

/**
 * @author liukaixiong
 * @date 2024/2/20
 */
@Data
public class ExpressionExecutorSyncData {


    private ExpressionExecutorBaseInfo baseInfo;

    private List<ExpressionExecutorDetailConfig> nodeInfo;


}
