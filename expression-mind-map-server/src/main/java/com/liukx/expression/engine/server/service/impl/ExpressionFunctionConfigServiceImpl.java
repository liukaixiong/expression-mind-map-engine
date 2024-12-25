package com.liukx.expression.engine.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.core.utils.Jsons;
import com.liukx.expression.engine.server.mapper.ExpressionFunctionConfigMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionFunctionConfig;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionFunctionRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionFunctionDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionFunctionConfigService;
import com.liukx.expression.engine.server.service.FunctionService;
import com.liukx.expression.engine.server.service.model.function.FunctionInfoDto;
import com.liukx.expression.engine.server.util.ConvertObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 函数配置表 服务实现类
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
@Service
public class ExpressionFunctionConfigServiceImpl extends ServiceImpl<ExpressionFunctionConfigMapper, ExpressionFunctionConfig> implements ExpressionFunctionConfigService, FunctionService {
    @Override
    public RestResult<List<ExpressionFunctionDTO>> queryExpressionFunction(QueryExpressionFunctionRequest queryRequest) {
        LambdaQueryChainWrapper<ExpressionFunctionConfig> lambdaQuery = lambdaQuery().eq(ExpressionFunctionConfig::getDeleted, 0);
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getServiceName()), ExpressionFunctionConfig::getServiceName, queryRequest.getServiceName());
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getFuncName()), ExpressionFunctionConfig::getFuncName, queryRequest.getFuncName());
        List<ExpressionFunctionConfig> functionConfigList = lambdaQuery.orderByDesc(ExpressionFunctionConfig::getCreateTime).list();
        List<ExpressionFunctionDTO> functionList = ConvertObjectUtils.convertList(functionConfigList, ExpressionFunctionDTO.class);
        return RestResult.ok(functionList);
    }

    @Override
    public FunctionInfoDto getKeyInfo(String key) {
        LambdaQueryWrapper<ExpressionFunctionConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionFunctionConfig::getFuncName, key);
        ExpressionFunctionConfig expressionFunctionConfig = getBaseMapper().selectOne(queryWrapper);

        if (expressionFunctionConfig == null) {
            return null;
        }

        FunctionInfoDto functionInfoDto = FunctionInfoDto.builder().describe(expressionFunctionConfig.getFuncDescription()).name(expressionFunctionConfig.getFuncName()).serviceName(expressionFunctionConfig.getServiceName()).registerType("remote").build();
        return functionInfoDto;
    }

    @Override
    public boolean refresh(String serviceName, List<FunctionInfoDto> variableInfoDto) {

        LambdaQueryWrapper<ExpressionFunctionConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionFunctionConfig::getServiceName, serviceName);
        queryWrapper.eq(ExpressionFunctionConfig::getDeleted, false);
        List<ExpressionFunctionConfig> functionDBList = getBaseMapper().selectList(queryWrapper);

        for (FunctionInfoDto functionInfoDto : variableInfoDto) {
            ExpressionFunctionConfig functionConfig = new ExpressionFunctionConfig();
            functionConfig.setServiceName(functionInfoDto.getServiceName());
            functionConfig.setFuncName(functionInfoDto.getName());
            functionConfig.setFuncDescription(functionInfoDto.getDescribe());
            functionConfig.setParamDoc(Jsons.toJsonString(functionInfoDto.getDocumentModel()));
            functionConfig.setDeleted(0);
            // todo 参数释义后续调整： 入参、出参等等，前端功能调整。
            this.save(functionConfig);
        }

        List<Long> ids = functionDBList.stream().map(ExpressionFunctionConfig::getId).collect(Collectors.toList());

        if (!ids.isEmpty()) {
            this.getBaseMapper().deleteBatchIds(ids);
        }

        return false;
    }

    @Override
    public List<FunctionInfoDto> loadAllFunctionDefined() {

        LambdaQueryWrapper<ExpressionFunctionConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionFunctionConfig::getStatus, 0);

        List<ExpressionFunctionConfig> functionConfigs = getBaseMapper().selectList(queryWrapper);
        if (CollectionUtils.isEmpty(functionConfigs)) {
            return new ArrayList<>();
        }
        return functionConfigs.stream()
                .map(ConvertObjectUtils::convertFunctionInfoDto)
                .collect(Collectors.toList());
    }
}
