package com.liukx.expression.engine.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.server.mapper.ExpressionVariableConfigMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionVariableConfig;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionVariableRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionVariableDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionVariableConfigService;
import com.liukx.expression.engine.server.service.VariableService;
import com.liukx.expression.engine.server.service.model.variable.VariableInfoDto;
import com.liukx.expression.engine.server.util.ConvertObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 表达式引擎通用变量配置表 服务实现类
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
@Service
public class ExpressionVariableConfigServiceImpl extends ServiceImpl<ExpressionVariableConfigMapper, ExpressionVariableConfig> implements ExpressionVariableConfigService, VariableService {

//    @Override
//    public RestResult<ExpressionVariableDTO> addExpressionVariable(AddExpressionVariableRequest addRequest) {
//        ExpressionVariableConfig nodeConfig = new ExpressionVariableConfig();
//        //先查询服务名称是否已经存在
//        boolean checkExist = new ServiceCommonUtil<>().checkServiceNameExists(addRequest.getServiceName());
//        if (!checkExist) {
//            return RestResult.failed(ErrorEnum.NON_EXIST_SERVICE_NAME.code(), ErrorEnum.NON_EXIST_SERVICE_NAME.message());
//        }
//        //先查询数据是否已经存在
//        LambdaQueryChainWrapper<ExpressionVariableConfig> lambdaQuery = this.lambdaQuery().ge(ExpressionVariableConfig::getServiceName, addRequest.getServiceName())
//                .ge(ExpressionVariableConfig::getServiceName, addRequest.getServiceName());
//        lambdaQuery.ge(ExpressionVariableConfig::getDeleted, 0)
//                .ge(ExpressionVariableConfig::getVarCode, addRequest.getVarCode())
//                .orderByDesc(ExpressionVariableConfig::getCreateTime)
//                .last("limit 1");
//        ExpressionVariableConfig existOne = lambdaQuery.one();
//        if (existOne != null && existOne.getId() != null) {
//            return RestResult.failed("待添加的注册变量已存在");
//        }
//        BeanUtil.copyProperties(addRequest, nodeConfig, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
//        nodeConfig.setCreateTime(LocalDateTime.now());
//        nodeConfig.setDeleted(false);
//        boolean addSuccess = this.save(nodeConfig);
//
//        RestResult<ExpressionVariableDTO> result = new RestResult<>();
//        if (addSuccess) {
//            ExpressionVariableDTO nodeDTO = new ExpressionVariableDTO();
//            BeanUtil.copyProperties(nodeConfig, nodeDTO, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
//            result.setCode(ResponseCodeEnum.E_200.getCode());
//            result.setMsg(ResponseCodeEnum.E_200.getMsg());
//            result.setData(nodeDTO);
//        } else {
//            result.setCode(ResponseCodeEnum.E_500.getCode());
//            result.setMsg("保存注册变量失败，原因:数据入库失败");
//        }
//        return result;
//    }

//    @Override
//    public RestResult<ExpressionVariableDTO> editExpressionVariable(EditExpressionVariableRequest editRequest) {
//        ExpressionVariableConfig varConfig = new ExpressionVariableConfig();
//        ExpressionVariableConfig existOne = this.getById(editRequest.getId());
//        if (existOne == null || StringUtils.isBlank(existOne.getVarCode())) {
//            return RestResult.failed(String.format("找不到id为%d的注册变量，无法完成修改", editRequest.getId()));
//        }
//        BeanUtil.copyProperties(editRequest, varConfig, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
//        varConfig.setUpdateTime(LocalDateTime.now());
//        boolean updateSuccess = this.updateById(varConfig);
//        ExpressionVariableDTO ExpressionVariableDTO = new ExpressionVariableDTO();
//        BeanUtil.copyProperties(varConfig, ExpressionVariableDTO);
//        RestResult<ExpressionVariableDTO> result = updateSuccess ? RestResult.ok(ExpressionVariableDTO) : RestResult.failed("数据更新到数据库失败");
//        return result;
//    }

    @Override
    public RestResult<List<ExpressionVariableDTO>> queryExpressionVariable(QueryExpressionVariableRequest queryRequest) {
        LambdaQueryChainWrapper<ExpressionVariableConfig> lambdaQuery = lambdaQuery().eq(ExpressionVariableConfig::getDeleted, 0);
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getServiceName()), ExpressionVariableConfig::getServiceName, queryRequest.getServiceName());
        lambdaQuery.eq(StringUtils.isNotBlank(queryRequest.getServiceName()), ExpressionVariableConfig::getServiceName, queryRequest.getServiceName());
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getVarCode()), ExpressionVariableConfig::getVarCode, queryRequest.getVarCode());
        lambdaQuery.eq(StringUtils.isNotBlank(queryRequest.getVarDataType()), ExpressionVariableConfig::getVarDataType, queryRequest.getVarDataType());
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getVarDescription()), ExpressionVariableConfig::getVarDescription, queryRequest.getVarDescription());
        lambdaQuery.eq(StringUtils.isNotBlank(queryRequest.getVarSource()), ExpressionVariableConfig::getVarSource, queryRequest.getVarSource());
        lambdaQuery.eq(queryRequest.getStatus() != null, ExpressionVariableConfig::getStatus, queryRequest.getStatus());
        List<ExpressionVariableConfig> varConfigList = lambdaQuery.orderByDesc(ExpressionVariableConfig::getCreateTime).list();

        List<ExpressionVariableDTO> expressionVariableDTOS = ConvertObjectUtils.convertList(varConfigList, ExpressionVariableDTO.class);

        return RestResult.ok(expressionVariableDTOS);
    }

//    @Override
//    public RestResult<?> batchDeleteByIdList(DeleteByIdListRequest delRequest) {
//        Set<Long> idSet = new HashSet<>();
//        delRequest.getIdList().stream().filter(idStr -> StringUtils.isNotBlank(idStr) && StringUtils.isNumeric(idStr)).forEach(idStr -> idSet.add(Long.valueOf(idStr)));
//        LambdaQueryWrapper<ExpressionVariableConfig> queryWrapper = new LambdaQueryWrapper<ExpressionVariableConfig>().in(ExpressionVariableConfig::getId, idSet)
//                .eq(ExpressionVariableConfig::getDeleted, false);
//        LambdaUpdateWrapper<ExpressionVariableConfig> updateWrapper = new LambdaUpdateWrapper<ExpressionVariableConfig>().set(ExpressionVariableConfig::getUpdateBy, delRequest.getUpdateBy())
//                .set(ExpressionVariableConfig::getDeleted, true)
//                .set(ExpressionVariableConfig::getUpdateTime, LocalDateTime.now())
//                .in(ExpressionVariableConfig::getId, idSet);
//        RestResult<?> result = ServiceCommonUtil.batchDelete(delRequest, "找不到相关记录，不用执行删除操作",getBaseMapper(), queryWrapper,updateWrapper);
//        return result;
//    }

    @Override
    public VariableInfoDto getKeyInfo(String key) {
        LambdaQueryWrapper<ExpressionVariableConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionVariableConfig::getVarCode, key);
        ExpressionVariableConfig expressionVariableConfig = getBaseMapper().selectOne(queryWrapper);

        if (expressionVariableConfig == null) {
            return null;
        }

        return VariableInfoDto.builder().name(expressionVariableConfig.getVarCode()).describe(expressionVariableConfig.getVarDescription()).registerType("remote").serviceName(expressionVariableConfig.getServiceName()).type(expressionVariableConfig.getVarDataType()).build();
    }

    @Override
    public boolean refresh(String serviceName, List<VariableInfoDto> variableInfoDto) {

        LambdaQueryWrapper<ExpressionVariableConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionVariableConfig::getServiceName, serviceName);
        queryWrapper.eq(ExpressionVariableConfig::getDeleted, false);
        // 保留之前存在的数据
        List<ExpressionVariableConfig> expressionVariableConfigs = getBaseMapper().selectList(queryWrapper);

        // 插入新数据
        for (VariableInfoDto infoDto : variableInfoDto) {
            ExpressionVariableConfig variableConfig = new ExpressionVariableConfig();
            // todo 验证变量定义是否存在过。如果预先被其他变量定义过,那么不允许定义
            variableConfig.setServiceName(infoDto.getServiceName());
            variableConfig.setVarCode(infoDto.getName());
            variableConfig.setVarDescription(infoDto.getDescribe());
            variableConfig.setVarDataType(infoDto.getType());
            variableConfig.setDeleted(false);
            variableConfig.setVarSource("remote");
            this.save(variableConfig);
        }

        // 删除老数据
        List<Long> ids = expressionVariableConfigs.stream().map(ExpressionVariableConfig::getId).collect(Collectors.toList());
        if (!ids.isEmpty()) {
            // 清空掉之前服務存在的节点
            getBaseMapper().deleteBatchIds(ids);
        }
        return true;
    }

    @Override
    public List<VariableInfoDto> loadAllVariableDefined() {
        LambdaQueryWrapper<ExpressionVariableConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionVariableConfig::getStatus, 0);
        List<ExpressionVariableConfig> expressionVariableConfigs = getBaseMapper().selectList(queryWrapper);
        if (CollectionUtils.isEmpty(expressionVariableConfigs)) {
            return new ArrayList<>();
        }
        return expressionVariableConfigs.stream().map(ConvertObjectUtils::convertVariableInfoDto).collect(Collectors.toList());
    }
}
