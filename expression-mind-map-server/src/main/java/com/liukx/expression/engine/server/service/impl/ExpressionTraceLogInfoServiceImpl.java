package com.liukx.expression.engine.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.server.mapper.ExpressionTraceLogInfoMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;
import com.liukx.expression.engine.server.service.ExpressionTraceLogInfoService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author liukx
 * @since 2024-07-18
 */
@Service
public class ExpressionTraceLogInfoServiceImpl extends ServiceImpl<ExpressionTraceLogInfoMapper, ExpressionTraceLogInfo> implements IService<ExpressionTraceLogInfo>, ExpressionTraceLogInfoService {


    @Override
    public List<ExpressionTraceLogInfo> getInfoListByTraceLogId(Long traceLogId) {
        LambdaQueryWrapper<ExpressionTraceLogInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpressionTraceLogInfo::getTraceLogId, traceLogId);
        return list(wrapper);
    }
}
