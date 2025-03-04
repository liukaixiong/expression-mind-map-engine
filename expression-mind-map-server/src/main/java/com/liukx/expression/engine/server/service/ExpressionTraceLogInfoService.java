package com.liukx.expression.engine.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;

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
     * 获取表达式最近成功一次成功的记录
     * @param expressionId  表达式编号
     * @return
     */
    ExpressionTraceLogInfo getExpressionRecentlySuccessLog(Long expressionId);
}
