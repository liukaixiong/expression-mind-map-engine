package com.liukx.expression.engine.server.util;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.server.constants.enums.ErrorEnum;
import com.liukx.expression.engine.server.constants.enums.ResponseCodeEnum;
import com.liukx.expression.engine.server.mapper.ExpressionNodeConfigMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionNodeConfig;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ServiceCommonUtil<ENTITY, DTO> {
    /**
     * 根据id批量逻辑删除 公共方法
     *
     * @param delRequest
     * @param errorMsg
     * @param mapper
     * @param queryWrapper
     * @param updateWrapper
     * @param <T>
     * @return
     */
    public static <T> RestResult<?> batchDelete(DeleteByIdListRequest delRequest, String errorMsg, BaseMapper<T> mapper, LambdaQueryWrapper<T> queryWrapper, LambdaUpdateWrapper<T> updateWrapper) {
        if (CollectionUtil.isEmpty(delRequest.getIdList())) {
            return RestResult.failed(ResponseCodeEnum.E_400.getCode(), "参数不合法，idList中的参数不能为空，只能包含数字");
        }
        List<T> existList = mapper.selectList(queryWrapper);
        if (CollectionUtil.isEmpty(existList)) {
            return RestResult.failed(errorMsg);
        }
        int updateCount = mapper.update(null, updateWrapper);
        return updateCount > 0 ? RestResult.ok(updateCount) : RestResult.failed("数据库逻辑删除操作失败");
    }

    public RestResult<DTO> addOne(Object addRequest, ENTITY newOne, DTO dtoObj, LambdaQueryChainWrapper<ENTITY> queryChainWrapper, ServiceImpl serviceImp) {
        List<ENTITY> existList = queryChainWrapper.list();
        if (CollectionUtil.isNotEmpty(existList)) {
            return RestResult.failed(ErrorEnum.REPEATED_ADD_DB.code(), ErrorEnum.REPEATED_ADD_DB.message());
        }
        BeanUtil.copyProperties(addRequest, newOne, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
        boolean saveSuccess = serviceImp.save(newOne);
        BeanUtil.copyProperties(newOne, dtoObj, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
        return saveSuccess ? RestResult.ok(dtoObj) : RestResult.failed(ErrorEnum.ADD_TO_DB_ERROR.code(), ErrorEnum.ADD_TO_DB_ERROR.message());
    }

    /**
     * 更新单个entity
     *
     * @param updateEntity
     * @param resultDTO
     * @param lambdaUpdateWrapper
     * @param serviceImpl
     * @param <DTO>
     * @return
     */
    public <DTO> RestResult<DTO> updateOne(Object updateRequest, ENTITY updateEntity, DTO resultDTO, LambdaUpdateChainWrapper<ENTITY> lambdaUpdateWrapper, ServiceImpl serviceImpl) {
        BeanUtil.copyProperties(updateRequest, updateEntity, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
        boolean updated = serviceImpl.update(updateEntity, lambdaUpdateWrapper);
        BeanUtil.copyProperties(updateEntity, resultDTO);
        return updated ? RestResult.ok(resultDTO) : RestResult.failed(ErrorEnum.UPDATE_TO_DB_ERROR.code(), ErrorEnum.UPDATE_TO_DB_ERROR.message());
    }

    /**
     * 查询数据列表通用方法
     *
     * @param newDto
     * @param lambdaQueryWrapper
     * @return
     */
    public RestResult<List<DTO>> queryDtoList(DTO newDto, LambdaQueryChainWrapper<ENTITY> lambdaQueryWrapper) {
        List<ENTITY> entityList = lambdaQueryWrapper.list();
        List<DTO> dtoList = new ArrayList<>();
        if (CollectionUtil.isNotEmpty(entityList)) {
            entityList.forEach(entity -> {
                BeanUtil.copyProperties(entity, newDto);
                dtoList.add(newDto);
            });
        }
        return RestResult.ok(dtoList);
    }

    /**
     * 查看服务名是否存在
     *
     * @param serviceName
     * @return
     */
    public boolean checkServiceNameExists(String serviceName) {
        LambdaQueryChainWrapper<ExpressionNodeConfig> lambdaQuery = new LambdaQueryChainWrapper<>(
                SpringUtil.getBean(ExpressionNodeConfigMapper.class));
        List<ExpressionNodeConfig> existList = lambdaQuery.eq(ExpressionNodeConfig::getServiceName, serviceName)
                .eq(ExpressionNodeConfig::getDeleted, false).list();
        return CollectionUtil.isNotEmpty(existList);
    }
}
