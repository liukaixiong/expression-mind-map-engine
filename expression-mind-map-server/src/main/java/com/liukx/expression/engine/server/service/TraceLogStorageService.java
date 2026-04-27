package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.core.api.model.api.ExpressionExecutorResultDTO;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionTraceRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionTraceInfoDTO;
import com.liukx.expression.engine.server.model.dto.response.TraceLogPageResult;

import java.util.Date;
import java.util.List;

/**
 * 追踪日志存储扩展点接口
 * <p>
 * 默认实现为 MySQL 存储，接入方可通过实现该接口将追踪日志存储到任意引擎（ES、ClickHouse 等）。
 * 通过注册自定义 Bean 替换默认实现。
 * </p>
 *
 * @author liukx
 */
public interface TraceLogStorageService {

    /**
     * 保存一条追踪日志（索引+明细）
     *
     * @param traceLog 追踪日志结果
     */
    void saveTraceLog(ExpressionExecutorResultDTO traceLog);

    /**
     * 分页查询追踪日志列表
     *
     * @param queryRequest 查询请求
     * @return 分页结果
     */
    TraceLogPageResult<ExpressionTraceLogIndex> queryTraceLogList(QueryExpressionTraceRequest queryRequest);

    /**
     * 查询追踪日志详情（索引+明细）
     *
     * @param id 追踪日志索引主键
     * @return 追踪日志详情，不存在返回 null
     */
    ExpressionTraceInfoDTO getTraceInfo(Long id);

    /**
     * 获取表达式最近一次成功执行的样本请求体
     *
     * @param expressionId 表达式配置编号
     * @return 样本索引记录，不存在返回 null
     */
    ExpressionTraceLogIndex getExpressionSampleBody(Long expressionId);

    /**
     * 查询追踪日志明细列表
     *
     * @param traceLogId 追踪日志索引主键
     * @return 明细列表
     */
    List<ExpressionTraceLogInfo> getInfoListByTraceLogId(Long traceLogId);

    /**
     * 判断表达式在指定日期之后是否有成功执行的记录
     *
     * @param expressionId 表达式配置编号
     * @param startDate    起始日期
     * @return 有成功记录返回 true，否则 false
     */
    boolean hasRecentlySuccessLog(Long expressionId, Date startDate);
}
