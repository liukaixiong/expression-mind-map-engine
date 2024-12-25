package com.liukx.expression.engine.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.server.constants.enums.ErrorEnum;
import com.liukx.expression.engine.server.mapper.ExpressionGlobalTraceLogMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionGlobalTraceLog;
import com.liukx.expression.engine.server.model.dto.request.AddGlobalTraceLogRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditGlobalTraceLogRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryGlobalTraceLogRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionGlobalTraceLogDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.GlobalTraceLogService;
import com.liukx.expression.engine.server.util.CodeUtils;
import com.liukx.expression.engine.server.util.ServiceCommonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * 全局日志表,负责记录表达式执行过程的日志记录,负责排查执行过程 服务实现类
 * </p>
 *
 * @author bsy
 * @since 2022-06-15
 */
@Service
@Slf4j
public class ExpressionGlobalTraceLogServiceImpl extends ServiceImpl<ExpressionGlobalTraceLogMapper, ExpressionGlobalTraceLog> implements GlobalTraceLogService {
    @Autowired
    private ServiceCommonUtil<ExpressionGlobalTraceLog, ExpressionGlobalTraceLogDTO> serviceCommonUtil;

    @Override
    public RestResult<ExpressionGlobalTraceLogDTO> addOne(AddGlobalTraceLogRequest addRequest) {
        ExpressionGlobalTraceLog entity = new ExpressionGlobalTraceLog();
        BeanUtil.copyProperties(addRequest, entity);
        entity.setCreateTime(LocalDateTime.now());
        entity.setDeleted(false);
        entity.setExecuteSuccess(CodeUtils.getDefaultValue(addRequest.getExecuteSuccess(), false));

        LambdaQueryChainWrapper<ExpressionGlobalTraceLog> lambdaQuery = lambdaQuery()
                .eq(ExpressionGlobalTraceLog::getBusinessCode, addRequest.getBusinessCode())
                .eq(ExpressionGlobalTraceLog::getEventCode, addRequest.getEventCode())
                .eq(ExpressionGlobalTraceLog::getCreateTime, LocalDateTime.now())
                .eq(ExpressionGlobalTraceLog::getDeleted, false);
        RestResult<ExpressionGlobalTraceLogDTO> result = serviceCommonUtil.addOne(addRequest, entity, new ExpressionGlobalTraceLogDTO(), lambdaQuery(), this);
        return result;
    }

    @Override
    public RestResult<ExpressionGlobalTraceLogDTO> updateOne(EditGlobalTraceLogRequest editRequest) {
        ExpressionGlobalTraceLog existOne = lambdaQuery().eq(ExpressionGlobalTraceLog::getId, editRequest.getId())
                .eq(ExpressionGlobalTraceLog::getDeleted, false).last("limit 1")
                .orderByDesc(ExpressionGlobalTraceLog::getCreateTime).one();
        if (existOne == null || existOne.getId() == null) {
            return RestResult.failed(ErrorEnum.UPDATE_NOT_EXIST_DATA.code(), ErrorEnum.UPDATE_TO_DB_ERROR.message());

        }
        BeanUtil.copyProperties(editRequest, existOne, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        ExpressionGlobalTraceLogDTO resultDTO = new ExpressionGlobalTraceLogDTO();
        RestResult<ExpressionGlobalTraceLogDTO> updateResult = serviceCommonUtil.updateOne(editRequest, existOne, resultDTO, lambdaUpdate(), this);
        return updateResult;
    }

    @Override
    public RestResult<List<ExpressionGlobalTraceLogDTO>> queryDtoList(QueryGlobalTraceLogRequest queryRequest) {
        LambdaQueryChainWrapper<ExpressionGlobalTraceLog> lambdaQuery = lambdaQuery();
        if (StringUtils.isNotBlank(queryRequest.getServiceName())) {
            lambdaQuery.eq(ExpressionGlobalTraceLog::getServiceName, queryRequest.getServiceName());

        }
        RestResult<List<ExpressionGlobalTraceLogDTO>> dtoRestResult = serviceCommonUtil.queryDtoList(new ExpressionGlobalTraceLogDTO(), lambdaQuery);
        return dtoRestResult;
    }

    @Override
    public RestResult<?> logicDeleteByIdList(DeleteByIdListRequest delRequest) {
        Set<Long> idSet = new HashSet<>();
        delRequest.getIdList().stream().filter(Objects::nonNull).forEach(idSet::add);
        LambdaQueryWrapper<ExpressionGlobalTraceLog> queryWrapper = new LambdaQueryWrapper<ExpressionGlobalTraceLog>().in(ExpressionGlobalTraceLog::getId, idSet)
                .eq(ExpressionGlobalTraceLog::getDeleted, 0);
        LambdaUpdateWrapper<ExpressionGlobalTraceLog> updateWrapper = new LambdaUpdateWrapper<ExpressionGlobalTraceLog>().set(ExpressionGlobalTraceLog::getUpdateBy, delRequest.getUpdateBy())
                .set(ExpressionGlobalTraceLog::getDeleted, 1)
                .set(ExpressionGlobalTraceLog::getUpdateTime, LocalDateTime.now())
                .in(ExpressionGlobalTraceLog::getId, idSet);
        RestResult<?> result = ServiceCommonUtil.batchDelete(delRequest, "找不到全局日志相关记录，不用执行删除操作", getBaseMapper(), queryWrapper, updateWrapper);
        return result;
    }


}
