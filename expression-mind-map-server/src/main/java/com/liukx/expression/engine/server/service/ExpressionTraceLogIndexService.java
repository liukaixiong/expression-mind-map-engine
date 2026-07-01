package com.liukx.expression.engine.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liukx.expression.engine.core.api.model.api.ExpressionExecutorResultDTO;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionTraceRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionTraceInfoDTO;
import com.liukx.expression.engine.server.model.dto.response.TraceLogPageResult;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author liukx
 * @since 2024-07-18
 */
public interface ExpressionTraceLogIndexService extends IService<ExpressionTraceLogIndex> {

    boolean addTraceLog(List<ExpressionExecutorResultDTO> request);

    TraceLogPageResult<ExpressionTraceLogIndex> queryExpressionTraceLogList(QueryExpressionTraceRequest queryRequest);

    ExpressionTraceInfoDTO getTraceInfoList(Long id);

    /**
     * 查询追踪详情（按 created 路由 info 日表）
     *
     * @param id      追踪日志索引主键
     * @param created 记录创建时间；为空时内部补取
     * @return 追踪日志详情
     */
    ExpressionTraceInfoDTO getTraceInfoList(Long id, java.util.Date created);

    /**
     * 配置编号
     *
     * @param expressionId 表达式编号
     * @return
     */
    ExpressionTraceLogIndex getExpressionSampleBody(Long expressionId);

}
