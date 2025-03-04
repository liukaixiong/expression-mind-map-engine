package com.liukx.expression.engine.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.constants.enums.ErrorEnum;
import com.liukx.expression.engine.server.constants.enums.ResponseCodeEnum;
import com.liukx.expression.engine.server.event.ExecutorConfigRefreshEvent;
import com.liukx.expression.engine.server.exception.Throws;
import com.liukx.expression.engine.server.mapper.ExpressionExecutorConfigMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorBaseInfo;
import com.liukx.expression.engine.server.model.dto.request.AddExpressionExecutorRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditExpressionExecutorRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionExecutorRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionExecutorBaseDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionExecutorConfigService;
import com.liukx.expression.engine.server.util.ServiceCommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * <p>
 * 引擎执行器实现类
 * </p>
 *
 * @author bsy
 * @since 2022-06-12
 */
@Service
public class ExpressionExecutorConfigServiceImpl extends ServiceImpl<ExpressionExecutorConfigMapper, ExpressionExecutorBaseInfo> implements ExpressionExecutorConfigService {

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Override
    public RestResult<ExpressionExecutorBaseDTO> addExpressionExecutor(AddExpressionExecutorRequest addRequest) {
        Throws.nullError(addRequest.getServiceName(), "serviceName");
        Throws.nullError(addRequest.getBusinessCode(), "businessCode");
        ExpressionExecutorBaseInfo executorConfig = new ExpressionExecutorBaseInfo();
        //先查询服务名称是否已经存在
//        boolean checkExist = new ServiceCommonUtil<>().checkServiceNameExists(addRequest.getServiceName());
//        Throws.boolError(!checkExist, ErrorEnum.NON_EXIST_SERVICE_NAME);

        //先查询数据是否已经存在
        ExpressionExecutorBaseInfo existOne = lambdaQuery()
                .eq(ExpressionExecutorBaseInfo::getExecutorCode, addRequest.getExecutorDescription())
                .eq(ExpressionExecutorBaseInfo::getBusinessCode, addRequest.getBusinessCode())
                .and(query -> query.ge(ExpressionExecutorBaseInfo::getDeleted, false).eq(ExpressionExecutorBaseInfo::getServiceName, addRequest.getServiceName()))
                .orderByDesc(ExpressionExecutorBaseInfo::getCreateTime)
                .last("limit 1").one();

        if (existOne != null) {
            return RestResult.failed(ErrorEnum.REPEATED_EXECUTOR_NAME_ADD.code(), ErrorEnum.REPEATED_EXECUTOR_NAME_ADD.message());
        }

        BeanUtil.copyProperties(addRequest, executorConfig, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
        executorConfig.setCreateTime(LocalDateTime.now());
        executorConfig.setStatus(addRequest.getStatus() != null ? addRequest.getStatus() : BaseConstants.BASE_VALID_STATUS);
        executorConfig.setDeleted(false);
        boolean addSuccess = this.save(executorConfig);

        RestResult<ExpressionExecutorBaseDTO> result = new RestResult<>();
        if (addSuccess) {
            ExpressionExecutorBaseDTO executorDTO = new ExpressionExecutorBaseDTO();
            BeanUtil.copyProperties(executorConfig, executorDTO, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
            result.setCode(ResponseCodeEnum.E_200.getCode());
            result.setMessage(ResponseCodeEnum.E_200.getMsg());
            result.setData(executorDTO);
        } else {
            result.setCode(ErrorEnum.UPDATE_TO_DB_ERROR.getCode());
            result.setMessage(ErrorEnum.UPDATE_TO_DB_ERROR.message());
        }
        return result;
    }

    @Override
    public RestResult<ExpressionExecutorBaseDTO> editExpressionExecutor(EditExpressionExecutorRequest editRequest) {
        Throws.nullError(editRequest.getServiceName(), "serviceName");
        Throws.nullError(editRequest.getBusinessCode(), "businessCode");

        // 校验业务编码是否存在重复
        ExpressionExecutorBaseDTO expressionExecutorBaseDTO = queryExecutorInfo(editRequest.getServiceName(), editRequest.getBusinessCode(), editRequest.getExecutorCode());
        Throws.check(expressionExecutorBaseDTO != null && !expressionExecutorBaseDTO.getId().equals(editRequest.getId()), "业务编码已经存在!");
        // 校验节点是否注册
//        boolean checkExist = new ServiceCommonUtil<>().checkServiceNameExists(editRequest.getServiceName());
//        Throws.boolError(!checkExist, ErrorEnum.NON_EXIST_SERVICE_NAME);

        // 数据真实性
        ExpressionExecutorBaseInfo executorConfig = new ExpressionExecutorBaseInfo();
        ExpressionExecutorBaseInfo existOne = this.getById(editRequest.getId());
        Throws.check(existOne == null || StringUtils.isBlank(existOne.getBusinessCode()), "找不到数据");

        BeanUtil.copyProperties(editRequest, executorConfig, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        executorConfig.setUpdateTime(LocalDateTime.now());
        boolean updateSuccess = this.updateById(executorConfig);

        if (updateSuccess) {
            this.eventPublisher.publishEvent(new ExecutorConfigRefreshEvent(executorConfig.getId()));
        }

        expressionExecutorBaseDTO = new ExpressionExecutorBaseDTO();
        BeanUtil.copyProperties(executorConfig, expressionExecutorBaseDTO);
        return updateSuccess ? RestResult.ok(expressionExecutorBaseDTO) :
                RestResult.failed(ErrorEnum.UPDATE_TO_DB_ERROR.code(), ErrorEnum.UPDATE_TO_DB_ERROR.message());
    }

    @Override
    public Page<ExpressionExecutorBaseInfo> queryExpressionExecutor(QueryExpressionExecutorRequest queryRequest) {
        LambdaQueryWrapper<ExpressionExecutorBaseInfo> lambdaQuery = new LambdaQueryWrapper<>();
        lambdaQuery.eq(ExpressionExecutorBaseInfo::getDeleted, false);
        //服务名称
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getServiceName()), ExpressionExecutorBaseInfo::getServiceName, queryRequest.getServiceName());
        //业务编码
        lambdaQuery.eq(StringUtils.isNotBlank(queryRequest.getBusinessCode()), ExpressionExecutorBaseInfo::getBusinessCode, queryRequest.getBusinessCode());
        //执行器名称
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getExecutorCode()), ExpressionExecutorBaseInfo::getExecutorCode, queryRequest.getExecutorCode());
        //执行器状态
        lambdaQuery.eq(queryRequest.getStatus() != null, ExpressionExecutorBaseInfo::getStatus, queryRequest.getStatus());
        lambdaQuery.orderByDesc(ExpressionExecutorBaseInfo::getId);
        Page<ExpressionExecutorBaseInfo> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());

        return getBaseMapper().selectPage(page, lambdaQuery);
    }

    @Override
    public RestResult<?> batchDeleteByIdList(DeleteByIdListRequest delRequest) {
        Set<Long> idSet = new HashSet<>();
        delRequest.getIdList().stream().filter(Objects::nonNull).forEach(idSet::add);
        LambdaQueryWrapper<ExpressionExecutorBaseInfo> queryWrapper = new LambdaQueryWrapper<ExpressionExecutorBaseInfo>().in(ExpressionExecutorBaseInfo::getId, idSet)
                .eq(ExpressionExecutorBaseInfo::getDeleted, false);
        LambdaUpdateWrapper<ExpressionExecutorBaseInfo> updateWrapper = new LambdaUpdateWrapper<ExpressionExecutorBaseInfo>().set(ExpressionExecutorBaseInfo::getUpdateBy, delRequest.getUpdateBy())
                .set(ExpressionExecutorBaseInfo::getDeleted, true)
                .set(ExpressionExecutorBaseInfo::getUpdateTime, LocalDateTime.now())
                .in(ExpressionExecutorBaseInfo::getId, idSet);
        return ServiceCommonUtil.batchDelete(delRequest, "找不到相关记录，不用执行删除操作", getBaseMapper(), queryWrapper, updateWrapper);
    }


    @Override
    public ExpressionExecutorBaseDTO queryExecutorInfo(String serviceName, String businessCode, String executorCode) {
        LambdaQueryWrapper<ExpressionExecutorBaseInfo> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionExecutorBaseInfo::getServiceName, serviceName);
        queryWrapper.eq(ExpressionExecutorBaseInfo::getBusinessCode, businessCode);
        queryWrapper.eq(ExpressionExecutorBaseInfo::getExecutorCode, executorCode);
        queryWrapper.eq(ExpressionExecutorBaseInfo::getDeleted, false);
        ExpressionExecutorBaseInfo expressionExecutorBaseInfo = this.getBaseMapper().selectOne(queryWrapper);
        if (expressionExecutorBaseInfo != null) {
            return Convert.convert(ExpressionExecutorBaseDTO.class, expressionExecutorBaseInfo);
        }
        return null;
    }
}
