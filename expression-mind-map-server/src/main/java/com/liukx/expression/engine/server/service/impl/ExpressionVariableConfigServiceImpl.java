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
