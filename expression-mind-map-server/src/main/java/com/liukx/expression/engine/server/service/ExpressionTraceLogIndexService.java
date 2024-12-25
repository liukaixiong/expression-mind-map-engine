package com.liukx.expression.engine.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liukx.expression.engine.core.api.model.api.ExpressionExecutorResultDTO;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionTraceRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionTraceInfoDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liukx
 * @since 2024-07-18
 */
public interface ExpressionTraceLogIndexService extends IService<ExpressionTraceLogIndex> {

    boolean addTraceLog(List<ExpressionExecutorResultDTO> request);

    Page<ExpressionTraceLogIndex> queryExpressionTraceLogList(QueryExpressionTraceRequest queryRequest);

    ExpressionTraceInfoDTO getTraceInfoList(Long id);
}
