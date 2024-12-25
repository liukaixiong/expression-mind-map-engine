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

    List<ExpressionTraceLogInfo> getInfoListByTraceLogId(Long traceLogId);
}
