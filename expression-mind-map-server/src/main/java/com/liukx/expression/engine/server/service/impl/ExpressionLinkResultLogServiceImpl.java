package com.liukx.expression.engine.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.server.constants.enums.ErrorEnum;
import com.liukx.expression.engine.server.mapper.ExpressionLinkResultLogMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionLinkResultLog;
import com.liukx.expression.engine.server.model.dto.request.AddLinkResultLogRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditLinkResultLogRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryLinkResultLogRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionLinkResultLogDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionLinkResultLogService;
import com.liukx.expression.engine.server.util.ServiceCommonUtil;
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
 * 成功回调日志表,记录执行完成的日志记录,全局日志表的压缩版 服务实现类
 * </p>
 *
 * @author bsy
 * @since 2022-06-15
 */
@Service
@Deprecated
public class ExpressionLinkResultLogServiceImpl extends ServiceImpl<ExpressionLinkResultLogMapper, ExpressionLinkResultLog> implements ExpressionLinkResultLogService {
    @Autowired
    private ServiceCommonUtil<ExpressionLinkResultLog, ExpressionLinkResultLogDTO> serviceCommonUtil;

    @Override
    public RestResult<ExpressionLinkResultLogDTO> addOne(AddLinkResultLogRequest addRequest) {
        ExpressionLinkResultLog entity = ExpressionLinkResultLog.builder().build();
        BeanUtil.copyProperties(addRequest, entity);
        entity.setCreateTime(LocalDateTime.now());
        entity.setDeleted(false);
        LambdaQueryChainWrapper<ExpressionLinkResultLog> lambdaQuery = lambdaQuery()
                .eq(ExpressionLinkResultLog::getBusinessCode, addRequest.getBusinessCode())
                .eq(ExpressionLinkResultLog::getEventCode, addRequest.getEventCode())
                .eq(ExpressionLinkResultLog::getCreateTime, LocalDateTime.now())
                .eq(ExpressionLinkResultLog::getDeleted, false);
        RestResult<ExpressionLinkResultLogDTO> result = serviceCommonUtil.addOne(addRequest, entity, new ExpressionLinkResultLogDTO(), lambdaQuery(), this);
        return result;
    }

    @Override
    public RestResult<ExpressionLinkResultLogDTO> updateOne(EditLinkResultLogRequest editRequest) {
        ExpressionLinkResultLog existOne = lambdaQuery().eq(ExpressionLinkResultLog::getId, editRequest.getId())
                .eq(ExpressionLinkResultLog::getDeleted, false).last("limit 1")
                .orderByDesc(ExpressionLinkResultLog::getCreateTime).one();
        if (existOne == null || existOne.getId() == null) {
            return RestResult.failed(ErrorEnum.UPDATE_NOT_EXIST_DATA.code(), ErrorEnum.UPDATE_TO_DB_ERROR.message());

        }
        BeanUtil.copyProperties(editRequest, existOne, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        ExpressionLinkResultLogDTO resultDTO = new ExpressionLinkResultLogDTO();
        RestResult<ExpressionLinkResultLogDTO> updateResult = serviceCommonUtil.updateOne(editRequest, existOne, resultDTO, lambdaUpdate(), this);
        return updateResult;
    }

    @Override
    public RestResult<List<ExpressionLinkResultLogDTO>> queryDtoList(QueryLinkResultLogRequest queryRequest) {
        LambdaQueryChainWrapper<ExpressionLinkResultLog> lambdaQuery = lambdaQuery();
        if (StringUtils.isNotBlank(queryRequest.getServiceName())) {
            lambdaQuery.eq(ExpressionLinkResultLog::getServiceName, queryRequest.getServiceName());

        }
        RestResult<List<ExpressionLinkResultLogDTO>> dtoRestResult = serviceCommonUtil.queryDtoList(new ExpressionLinkResultLogDTO(), lambdaQuery);
        return dtoRestResult;
    }

    @Override
    public RestResult<?> logicDeleteByIdList(DeleteByIdListRequest delRequest) {
        Set<Long> idSet = new HashSet<>();
        delRequest.getIdList().stream().filter(Objects::nonNull).forEach(idSet::add);
        LambdaQueryWrapper<ExpressionLinkResultLog> queryWrapper = new LambdaQueryWrapper<ExpressionLinkResultLog>().in(ExpressionLinkResultLog::getId, idSet)
                .eq(ExpressionLinkResultLog::getDeleted, 0);
        LambdaUpdateWrapper<ExpressionLinkResultLog> updateWrapper = new LambdaUpdateWrapper<ExpressionLinkResultLog>().set(ExpressionLinkResultLog::getUpdateBy, delRequest.getUpdateBy())
                .set(ExpressionLinkResultLog::getDeleted, 1)
                .set(ExpressionLinkResultLog::getUpdateTime, LocalDateTime.now())
                .in(ExpressionLinkResultLog::getId, idSet);
        RestResult<?> result = ServiceCommonUtil.batchDelete(delRequest, "找不到全局日志相关记录，不用执行删除操作", getBaseMapper(), queryWrapper, updateWrapper);
        return result;
    }
}
