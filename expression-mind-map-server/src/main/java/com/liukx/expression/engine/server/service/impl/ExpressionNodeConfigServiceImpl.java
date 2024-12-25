package com.liukx.expression.engine.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.server.constants.enums.ErrorEnum;
import com.liukx.expression.engine.server.enums.RemoteInvokeTypeEnums;
import com.liukx.expression.engine.server.exception.Throws;
import com.liukx.expression.engine.server.mapper.ExpressionNodeConfigMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionNodeConfig;
import com.liukx.expression.engine.server.model.dto.request.AddExpressionNodeRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditExpressionNodeRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionNodeRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionNodeDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionKeyService;
import com.liukx.expression.engine.server.service.ExpressionNodeConfigService;
import com.liukx.expression.engine.server.service.NodeService;
import com.liukx.expression.engine.server.service.model.node.NodeServiceDto;
import com.liukx.expression.engine.server.util.ServiceCommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 引擎节点信息 服务实现类
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
@Service
public class ExpressionNodeConfigServiceImpl extends ServiceImpl<ExpressionNodeConfigMapper, ExpressionNodeConfig>
        implements ExpressionNodeConfigService, NodeService {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    private ServiceCommonUtil<ExpressionNodeConfig, ExpressionNodeDTO> serviceCommonUtil;

    @Autowired
    private ExpressionKeyService keyService;

    @Override
    public RestResult<ExpressionNodeDTO> addExpressionNode(AddExpressionNodeRequest addRequest) {
        ExpressionNodeConfig nodeConfig = ExpressionNodeConfig.builder().build();
        //先查询数据是否已经存在
        boolean checkExist = serviceCommonUtil.checkServiceNameExists(addRequest.getServiceName());
        Throws.boolError(checkExist, ErrorEnum.REPEATED_ADD_DB);

        BeanUtil
                .copyProperties(addRequest, nodeConfig, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
        nodeConfig.setCreateTime(LocalDateTime.now());
        nodeConfig.setDeleted(false);

        boolean addSuccess = this.save(nodeConfig);
        Throws.boolError(!addSuccess, ErrorEnum.PARAMS_ERROR);
        ExpressionNodeDTO convert = Convert.convert(ExpressionNodeDTO.class, nodeConfig);
        return RestResult.ok(convert);
    }

    @Override
    public RestResult<ExpressionNodeDTO> editExpressionNode(EditExpressionNodeRequest editRequest) {
        ExpressionNodeConfig nodeConfig = ExpressionNodeConfig.builder().build();
        ExpressionNodeConfig existOne = this.getById(editRequest.getId());
        Throws.check(existOne == null, "数据不存在");

        BeanUtil
                .copyProperties(editRequest, existOne, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        nodeConfig.setUpdateTime(LocalDateTime.now());
        existOne.setDomain(editRequest.getDomain());
        boolean updateSuccess = this.updateById(existOne);
        Throws.check(!updateSuccess, "更新失败");
        ExpressionNodeDTO convert = Convert.convert(ExpressionNodeDTO.class, nodeConfig);
        return RestResult.ok(convert);
    }

    @Override
    public RestResult<List<ExpressionNodeDTO>> queryExpressionNode(QueryExpressionNodeRequest queryRequest) {
        List<ExpressionNodeDTO> dtoList = new ArrayList<>();
        LambdaQueryChainWrapper<ExpressionNodeConfig> lambdaQuery =
                lambdaQuery().eq(ExpressionNodeConfig::getDeleted, 0);
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getServiceName()), ExpressionNodeConfig::getServiceName,
                queryRequest.getServiceName());
        lambdaQuery.eq(StringUtils.isNotBlank(queryRequest.getCallMethod()), ExpressionNodeConfig::getCallMethod,
                queryRequest.getCallMethod());
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getDomain()), ExpressionNodeConfig::getDomain,
                queryRequest.getDomain());
        lambdaQuery.eq(queryRequest.getStatus() != null, ExpressionNodeConfig::getStatus, queryRequest.getStatus());
        List<ExpressionNodeConfig> nodeConfigs = lambdaQuery.orderByDesc(ExpressionNodeConfig::getCreateTime).list();

        if (CollectionUtil.isNotEmpty(nodeConfigs)) {
            List<ExpressionNodeDTO> nodeDTOList =
                    nodeConfigs.stream().map(var -> Convert.convert(ExpressionNodeDTO.class, var))
                            .collect(Collectors.toList());
            return RestResult.ok(nodeDTOList);
        }
        return RestResult.ok(dtoList);
    }

    @Override
    public RestResult<?> batchDeleteByIdList(DeleteByIdListRequest delRequest) {
        Set<Long> idSet = new HashSet<>();
        delRequest.getIdList().stream().filter(Objects::nonNull)
                .forEach(idSet::add);
        LambdaQueryWrapper<ExpressionNodeConfig> queryWrapper =
                new LambdaQueryWrapper<ExpressionNodeConfig>().in(ExpressionNodeConfig::getId, idSet)
                        .eq(ExpressionNodeConfig::getDeleted, false);
        LambdaUpdateWrapper<ExpressionNodeConfig> updateWrapper = new LambdaUpdateWrapper<ExpressionNodeConfig>()
                .set(ExpressionNodeConfig::getUpdateBy, delRequest.getUpdateBy())
                .set(ExpressionNodeConfig::getDeleted, true).set(ExpressionNodeConfig::getUpdateTime, LocalDateTime.now())
                .in(ExpressionNodeConfig::getId, idSet);
        RestResult<?> result =
                ServiceCommonUtil.batchDelete(delRequest, "找不到相关记录，不用执行删除操作", getBaseMapper(), queryWrapper, updateWrapper);
        return result;
    }

    @Override
    public NodeServiceDto getNodeInfo(String serviceName) {
        LambdaQueryWrapper<ExpressionNodeConfig> query = Wrappers.lambdaQuery();
        query.eq(ExpressionNodeConfig::getServiceName, serviceName);
        ExpressionNodeConfig expressionNodeConfig = getBaseMapper().selectOne(query);
        String callMethod = expressionNodeConfig.getCallMethod();
        RemoteInvokeTypeEnums remoteInvokeTypeEnums = RemoteInvokeTypeEnums.valueOf(callMethod);
        NodeServiceDto nodeServiceDto =
                NodeServiceDto.builder().serviceName(serviceName).domain(expressionNodeConfig.getDomain())
                        .remoteInvokeType(remoteInvokeTypeEnums).build();
        return nodeServiceDto;
    }

    @Override
    public void refreshNodeInfo() {

    }

    @Override
    public void refreshNodeInfo(String serviceName) {
//        boolean result = keyService.refreshDocument(serviceName);
//        if (!result) {
//            logger.warn("服务 : {} 更新失败", serviceName);
//        }
    }
}
