package com.liukx.expression.engine.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.core.enums.ExpressionLogTypeEnum;
import com.liukx.expression.engine.server.manager.MysqlTableManager;
import com.liukx.expression.engine.server.mapper.ExpressionTraceLogInfoMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;
import com.liukx.expression.engine.server.service.ExpressionTraceLogInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
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
    @Autowired
    private MysqlTableManager tableManager;

    @Override
    public List<ExpressionTraceLogInfo> getInfoListByTraceLogId(Long traceLogId) {
        LambdaQueryWrapper<ExpressionTraceLogInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpressionTraceLogInfo::getTraceLogId, traceLogId);
        final List<ExpressionTraceLogInfo> list = list(wrapper);

        if (list.isEmpty()) {
            final String pastTableName = tableManager.getLastTableNameList(ExpressionTraceLogInfo.class, -1);
            return getBaseMapper().selectPageByTable(pastTableName, wrapper);
        }

        return list;
    }

    @Override
    public List<ExpressionTraceLogInfo> getInfoListByTraceLogId(Long traceLogId, Date created) {
        if (created == null) {
            return getInfoListByTraceLogId(traceLogId);
        }
        final String infoTable = tableManager.getTableNameByDate(ExpressionTraceLogInfo.class, created);
        if (infoTable == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ExpressionTraceLogInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpressionTraceLogInfo::getTraceLogId, traceLogId);
        return getBaseMapper().selectListByTable(infoTable, wrapper);
    }

    @Override
    public ExpressionTraceLogInfo getExpressionRecentlySuccessLog(Long expressionId) {
        LambdaQueryWrapper<ExpressionTraceLogInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpressionTraceLogInfo::getExpressionConfigId, expressionId);
        wrapper.eq(ExpressionTraceLogInfo::getExpressionResult, 1);
        wrapper.eq(ExpressionTraceLogInfo::getModuleType, ExpressionLogTypeEnum.expression.name());
        wrapper.orderByDesc(ExpressionTraceLogInfo::getId);
        wrapper.last("limit 1");
        return getOne(wrapper, false);
    }

    @Override
    public boolean getExpressionRecentlySuccessCount(Long expressionId, Date startDate) {
        LambdaQueryWrapper<ExpressionTraceLogInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExpressionTraceLogInfo::getExpressionConfigId, expressionId);
        wrapper.eq(ExpressionTraceLogInfo::getExpressionResult, 1);
        wrapper.eq(ExpressionTraceLogInfo::getModuleType, ExpressionLogTypeEnum.expression.name());
        wrapper.ge(startDate != null, ExpressionTraceLogInfo::getCreated, startDate);
        wrapper.last("limit 1");
        return getOne(wrapper, false) != null;
    }

}
