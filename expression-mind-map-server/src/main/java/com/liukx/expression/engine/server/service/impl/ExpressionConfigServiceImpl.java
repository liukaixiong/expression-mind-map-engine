package com.liukx.expression.engine.server.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.core.enums.ExpressionTypeEnum;
import com.liukx.expression.engine.server.constants.enums.ErrorEnum;
import com.liukx.expression.engine.server.constants.enums.ResponseCodeEnum;
import com.liukx.expression.engine.server.event.ExecutorConfigRefreshEvent;
import com.liukx.expression.engine.server.exception.Throws;
import com.liukx.expression.engine.server.mapper.ExpressionConfigMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorDetailConfig;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;
import com.liukx.expression.engine.server.model.dto.request.AddExpressionConfigRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditExpressionConfigRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionConfigRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionExecutorDetailConfigDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionConfigService;
import com.liukx.expression.engine.server.service.ExpressionTraceLogInfoService;
import com.liukx.expression.engine.server.util.ServiceCommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * <p>
 * 表达式配置 服务实现类
 * </p>
 *
 * @author bsy
 * @since 2022-06-12
 */
@Service
public class ExpressionConfigServiceImpl extends ServiceImpl<ExpressionConfigMapper, ExpressionExecutorDetailConfig> implements ExpressionConfigService {

    private final Logger LOG = getLogger(ExpressionConfigServiceImpl.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ExpressionTraceLogInfoService traceLogInfoService;

    private static List<ExpressionExecutorDetailConfigDTO> convertExpressionExecutorDetailConfigDTO(List<ExpressionExecutorDetailConfig> expressionExecutorDetailConfigs) {
        return expressionExecutorDetailConfigs.stream().map(config -> Convert.convert(ExpressionExecutorDetailConfigDTO.class, config)).collect(Collectors.toList());
    }

    @Override
    public RestResult<ExpressionExecutorDetailConfigDTO> addExpression(AddExpressionConfigRequest request) {
        Throws.check(StringUtils.isEmpty(request.getExpressionCode()), ErrorEnum.EXPRESSION_CODE_NULL.message());
        if (StrUtil.isBlank(request.getExpressionContent())) {
            return RestResult.failed(ErrorEnum.EXPRESSION_CONTENT_NULL.code(), ErrorEnum.EXPRESSION_CONTENT_NULL.message());
        }

        checkExpressionCodeUnion(request.getExecutorId(), null, request.getExpressionCode());

        if (!checkExpressionTypeLegal(request.getExpressionType())) {
            return RestResult.failed(ErrorEnum.EXPRESSION_ILLEGAL_TYPE_ADD.code(), ErrorEnum.EXPRESSION_ILLEGAL_TYPE_ADD.message());
        }

        ExpressionExecutorDetailConfig expressionExecutorDetailConfig = new ExpressionExecutorDetailConfig();
        BeanUtil.copyProperties(request, expressionExecutorDetailConfig, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
        expressionExecutorDetailConfig.setCreateTime(LocalDateTime.now());
        expressionExecutorDetailConfig.setDeleted(false);
        boolean addSuccess = this.save(expressionExecutorDetailConfig);
        LOG.debug("add config expression result : {} , {} , {}", addSuccess, expressionExecutorDetailConfig.getExpressionCode(), expressionExecutorDetailConfig.getId());

        if (addSuccess) {
            refreshConfigPost(expressionExecutorDetailConfig);
        }

        RestResult<ExpressionExecutorDetailConfigDTO> result = new RestResult<>();
        if (addSuccess) {
            ExpressionExecutorDetailConfigDTO nodeDTO = new ExpressionExecutorDetailConfigDTO();
            BeanUtil.copyProperties(expressionExecutorDetailConfig, nodeDTO, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
            result.setCode(ResponseCodeEnum.E_200.getCode());
            result.setMessage(ResponseCodeEnum.E_200.getMsg());
            result.setData(nodeDTO);
        } else {
            result.setCode(ErrorEnum.ADD_TO_DB_ERROR.getCode());
            result.setMessage(ErrorEnum.ADD_TO_DB_ERROR.message());
        }
        return result;
    }

    private void checkExpressionCodeUnion(Long executorId, Long id, String expressionCode) {
        //表达式编码必须唯一
        LambdaQueryChainWrapper<ExpressionExecutorDetailConfig> lambdaQuery = lambdaQuery()
                .eq(ExpressionExecutorDetailConfig::getExecutorId, executorId)
                .eq(ExpressionExecutorDetailConfig::getExpressionCode, expressionCode)
                .eq(ExpressionExecutorDetailConfig::getDeleted, false)
                .orderByAsc(ExpressionExecutorDetailConfig::getId)
                .last("limit 1");
        ExpressionExecutorDetailConfig existOne = lambdaQuery.one();

        Throws.check((id == null && existOne != null) || (existOne != null && !existOne.getId().equals(id)), ErrorEnum.REPEATED_EXPRESSION_ADD.message());
    }

    private boolean checkExpressionTypeLegal(String conditionType) {
        return Arrays.stream(ExpressionTypeEnum.values()).anyMatch(typeEnum -> typeEnum.getCode().equals(conditionType));
    }

    @Override
    public RestResult<ExpressionExecutorDetailConfigDTO> editExpression(EditExpressionConfigRequest editRequest) {

        ExpressionExecutorDetailConfig existOne = this.getById(editRequest.getId());

        if (existOne == null || StringUtils.isBlank(existOne.getExpressionContent())) {
            return RestResult.failed(ErrorEnum.UPDATE_NOT_EXIST_DATA.code(), String.format("找不到id为%d的表达式，无法完成修改", editRequest.getId()));
        }

        if (!checkExpressionTypeLegal(editRequest.getExpressionType())) {
            return RestResult.failed(ErrorEnum.EXPRESSION_ILLEGAL_TYPE_ADD.code(), ErrorEnum.EXPRESSION_ILLEGAL_TYPE_ADD.message());
        }

        // 检查表达式编码是否唯一
        checkExpressionCodeUnion(existOne.getExecutorId(), editRequest.getId(), editRequest.getExpressionCode());

        ExpressionExecutorDetailConfig nodeConfig = new ExpressionExecutorDetailConfig();
        BeanUtil.copyProperties(editRequest, nodeConfig, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        nodeConfig.setUpdateTime(LocalDateTime.now());
        boolean updateSuccess = this.updateById(nodeConfig);

        if (updateSuccess) {
            refreshConfigPost(existOne);
        }

        ExpressionExecutorDetailConfigDTO expressionExecutorDetailConfigDTO = new ExpressionExecutorDetailConfigDTO();
        BeanUtil.copyProperties(editRequest, expressionExecutorDetailConfigDTO);
        return updateSuccess ? RestResult.ok(expressionExecutorDetailConfigDTO) : RestResult.failed("数据更新到数据库失败");
    }

    private void refreshConfigPost(ExpressionExecutorDetailConfig nodeConfig) {
        this.eventPublisher.publishEvent(new ExecutorConfigRefreshEvent(nodeConfig.getExecutorId()));
    }

    @Override
    public RestResult<List<ExpressionExecutorDetailConfigDTO>> queryExpression(QueryExpressionConfigRequest queryRequest) {
        Throws.nullError(queryRequest.getExecutorId(), "executorId");
        List<ExpressionExecutorDetailConfigDTO> dtoList = new ArrayList<>();
//        if (queryRequest.getParentId() == null) {
//            queryRequest.setParentId(BaseConstants.BASE_ROOT_ID);
//        }
        LambdaQueryChainWrapper<ExpressionExecutorDetailConfig> lambdaQuery = lambdaQuery().eq(ExpressionExecutorDetailConfig::getDeleted, 0);
        lambdaQuery.eq(ExpressionExecutorDetailConfig::getExecutorId, queryRequest.getExecutorId());
        lambdaQuery.eq(queryRequest.getParentId() != null, ExpressionExecutorDetailConfig::getParentId, queryRequest.getParentId());
        lambdaQuery.eq(StringUtils.isNotBlank(queryRequest.getExpressionType()), ExpressionExecutorDetailConfig::getExpressionType, queryRequest.getExpressionType());
        lambdaQuery.eq(queryRequest.getExpressionStatus() != null, ExpressionExecutorDetailConfig::getExpressionStatus, queryRequest.getExpressionStatus());
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getExpressionContent()), ExpressionExecutorDetailConfig::getExpressionContent, queryRequest.getExpressionContent());
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getExpressionDescription()), ExpressionExecutorDetailConfig::getExpressionDescription, queryRequest.getExpressionDescription());
        lambdaQuery.orderByDesc(ExpressionExecutorDetailConfig::getPriorityOrder).orderByAsc(ExpressionExecutorDetailConfig::getId);
        List<ExpressionExecutorDetailConfig> expressionExecutorDetailConfigList = lambdaQuery.list();


        if (CollectionUtil.isNotEmpty(expressionExecutorDetailConfigList)) {
            Map<Long, List<ExpressionTraceLogInfo>> traceConfigMap = new HashMap<>();
            final Long traceLogId = queryRequest.getTraceLogId();
            // 如果涵盖追踪编号,那么获取对应的追踪信息绑定到数据中
            if (traceLogId != null) {
                final List<ExpressionTraceLogInfo> infoListByTraceLogId = traceLogInfoService.getInfoListByTraceLogId(traceLogId);
                if (CollectionUtil.isNotEmpty(infoListByTraceLogId)) {
                    traceConfigMap = infoListByTraceLogId.stream().collect(Collectors.groupingBy(ExpressionTraceLogInfo::getExpressionConfigId));
                }
            }
            Map<Long, List<ExpressionTraceLogInfo>> finalTraceConfigMap = traceConfigMap;
            expressionExecutorDetailConfigList.forEach(expressionExecutorDetailConfig -> {
                ExpressionExecutorDetailConfigDTO expressionExecutorDetailConfigDTO = new ExpressionExecutorDetailConfigDTO();
                BeanUtil.copyProperties(expressionExecutorDetailConfig, expressionExecutorDetailConfigDTO);
                // 绑定追踪日志
                expressionExecutorDetailConfigDTO.setTraceLogInfos(finalTraceConfigMap.get(expressionExecutorDetailConfigDTO.getId()));
                dtoList.add(expressionExecutorDetailConfigDTO);
            });
        }
        return RestResult.ok(dtoList);
    }

    @Override
    public RestResult<?> batchDeleteByIdList(DeleteByIdListRequest delRequest) {

        Throws.check(CollectionUtil.isEmpty(delRequest.getIdList()), "删除的id集合不能为空！");

        Set<Long> idSet = new HashSet<>(delRequest.getIdList());

        deepAllIdBuilder(idSet, delRequest.getIdList());

        LambdaQueryWrapper<ExpressionExecutorDetailConfig> queryWrapper = new LambdaQueryWrapper<ExpressionExecutorDetailConfig>().in(ExpressionExecutorDetailConfig::getId, idSet)
                .eq(ExpressionExecutorDetailConfig::getDeleted, false);
        LOG.info("批量删除id集合: {} ", idSet);
        LambdaUpdateWrapper<ExpressionExecutorDetailConfig> updateWrapper = new LambdaUpdateWrapper<ExpressionExecutorDetailConfig>().set(ExpressionExecutorDetailConfig::getUpdateBy, delRequest.getUpdateBy())
                .set(ExpressionExecutorDetailConfig::getDeleted, true)
                .set(ExpressionExecutorDetailConfig::getUpdateTime, LocalDateTime.now())
                .in(ExpressionExecutorDetailConfig::getId, idSet);

        return ServiceCommonUtil.batchDelete(delRequest, "找不到相关记录，不用执行删除操作", getBaseMapper(), queryWrapper, updateWrapper);
    }

    /**
     * 递归查询相关子节点
     *
     * @param deleteIdList 需要删除的集合容器
     * @param parentIdList 父类节点
     */
    private void deepAllIdBuilder(Set<Long> deleteIdList, List<Long> parentIdList) {
        LambdaQueryChainWrapper<ExpressionExecutorDetailConfig> lambdaQuery = lambdaQuery().eq(ExpressionExecutorDetailConfig::getDeleted, 0);
        // 查询所有子节点
        lambdaQuery.in(ExpressionExecutorDetailConfig::getParentId, parentIdList);
        List<ExpressionExecutorDetailConfig> expressionExecutorDetailConfigs = lambdaQuery.select(ExpressionExecutorDetailConfig::getId).list();

        if (!CollectionUtil.isEmpty(expressionExecutorDetailConfigs)) {
            List<Long> ids = expressionExecutorDetailConfigs.stream().map(ExpressionExecutorDetailConfig::getId).collect(Collectors.toList());
            deleteIdList.addAll(ids);
            deepAllIdBuilder(deleteIdList, ids);
        }
    }

    @Override
    public List<ExpressionExecutorDetailConfigDTO> getEventInfo(Long baseId, Long parentId, String eventName, ExpressionTypeEnum typeEnum) {

        LambdaQueryWrapper<ExpressionExecutorDetailConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionExecutorDetailConfig::getExecutorId, baseId);
        queryWrapper.eq(ExpressionExecutorDetailConfig::getParentId, parentId);
        queryWrapper.eq(ExpressionExecutorDetailConfig::getExpressionType, typeEnum.getCode());
        queryWrapper.eq(ExpressionExecutorDetailConfig::getExpressionStatus, true);
        queryWrapper.eq(ExpressionExecutorDetailConfig::getDeleted, false);
        queryWrapper.eq(StringUtils.isNotEmpty(eventName), ExpressionExecutorDetailConfig::getExpressionCode, eventName);
        List<ExpressionExecutorDetailConfig> expressionExecutorDetailConfigs = this.getBaseMapper().selectList(queryWrapper);

        return convertExpressionExecutorDetailConfigDTO(expressionExecutorDetailConfigs);
    }

    @Override
    public List<ExpressionExecutorDetailConfigDTO> getNodeExpressionInfo(Long baseId, Long parentId) {
        LambdaQueryWrapper<ExpressionExecutorDetailConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionExecutorDetailConfig::getExecutorId, baseId);
        queryWrapper.eq(parentId != null, ExpressionExecutorDetailConfig::getParentId, parentId);
        queryWrapper.eq(ExpressionExecutorDetailConfig::getExpressionStatus, true);
        queryWrapper.eq(ExpressionExecutorDetailConfig::getDeleted, false);
        queryWrapper.orderByDesc(ExpressionExecutorDetailConfig::getPriorityOrder).orderByAsc(ExpressionExecutorDetailConfig::getId);
        List<ExpressionExecutorDetailConfig> expressionExecutorDetailConfigs = this.getBaseMapper().selectList(queryWrapper);
        return convertExpressionExecutorDetailConfigDTO(expressionExecutorDetailConfigs);
    }

    @Override
    public List<ExpressionExecutorDetailConfig> getExpressionListByBaseId(Long baseId) {
        LambdaQueryWrapper<ExpressionExecutorDetailConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionExecutorDetailConfig::getExecutorId, baseId);
        // 禁用的数据,也需要查询出来
//        queryWrapper.eq(ExpressionExecutorDetailConfig::getExpressionStatus, true);
        queryWrapper.eq(ExpressionExecutorDetailConfig::getDeleted, false);
        queryWrapper.orderByAsc(ExpressionExecutorDetailConfig::getId);
        return this.getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public boolean copyNode(ExpressionExecutorDetailConfig config) {
        final Long id = config.getId();
        final Long parentId = config.getParentId();
        final ExpressionExecutorDetailConfig executorDetailConfig = getById(id);
        // 清空主键
        executorDetailConfig.setId(null);
        final String expressionCode = executorDetailConfig.getExpressionCode();
        executorDetailConfig.setParentId(parentId);
        executorDetailConfig.setExpressionCode(expressionCode + "_copy_" + RandomUtil.randomString(5));
        final boolean result = save(executorDetailConfig);
        LOG.info("【复制节点】 将 {} 对象 加入到 {} 中 -> {}", id, parentId, executorDetailConfig);
        return result;
    }
}
