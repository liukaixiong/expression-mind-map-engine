package com.liukx.expression.engine.server.model.dto.response;

import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 表达式追踪日志详情
 *
 * @author liukaixiong
 * @date 2024/7/19 - 15:21
 */

@EqualsAndHashCode(callSuper = true)
@Data
public class ExpressionTraceInfoDTO extends ExpressionTraceLogIndex {

    private List<ExpressionTraceLogInfo> traceLogInfoList;

}
