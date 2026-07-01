package com.liukx.expression.engine.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;

import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author liukx
 * @since 2024-07-18
 */
public interface ExpressionTraceLogInfoService extends IService<ExpressionTraceLogInfo> {

    /**
     * 获取该追踪编号的记录
     * @param traceLogId    追踪编号
     * @return
     */
    List<ExpressionTraceLogInfo> getInfoListByTraceLogId(Long traceLogId);

    /**
     * 按 created 精确路由到 info 日表查询该追踪编号的明细。
     * 日表不存在（旧月表数据 / 已清理）返回空列表。
     *
     * @param traceLogId 追踪编号主键
     * @param created    记录创建时间（用于定位日级分表）；为空回退到无 created 重载
     * @return 明细列表
     */
    List<ExpressionTraceLogInfo> getInfoListByTraceLogId(Long traceLogId, Date created);

    /**
     * 获取表达式最近成功一次成功的记录
     * @param expressionId  表达式编号
     * @return
     */
    ExpressionTraceLogInfo getExpressionRecentlySuccessLog(Long expressionId);

    boolean getExpressionRecentlySuccessCount(Long expressionId, Date startDate);
}
