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
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorInfoConfig;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;
import com.liukx.expression.engine.server.model.dto.request.AddExpressionConfigRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditExpressionConfigRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionConfigRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionExecutorDetailConfigDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionConfigService;
import com.liukx.expression.engine.server.service.ExpressionTraceLogInfoService;
import com.liukx.expression.engine.server.util.ExpressionUtils;
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
public class ExpressionConfigServiceImpl extends ServiceImpl<ExpressionConfigMapper, ExpressionExecutorInfoConfig> implements ExpressionConfigService {

    private final Logger LOG = getLogger(ExpressionConfigServiceImpl.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Autowired
    private ExpressionTraceLogInfoService traceLogInfoService;

    private static List<ExpressionExecutorDetailConfigDTO> convertExpressionExecutorDetailConfigDTO(List<ExpressionExecutorInfoConfig> expressionExecutorDetailConfigs) {
        return expressionExecutorDetailConfigs.stream().map(config -> Convert.convert(ExpressionExecutorDetailConfigDTO.class, config)).collect(Collectors.toList());
    }

    @Override
    public RestResult<ExpressionExecutorDetailConfigDTO> addExpression(AddExpressionConfigRequest request) {
        Throws.check(StringUtils.isEmpty(request.getExpressionCode()), ErrorEnum.EXPRESSION_CODE_NULL.message());
        final String expressionContent = request.getExpressionContent();
        if (StrUtil.isBlank(expressionContent)) {
            return RestResult.failed(ErrorEnum.EXPRESSION_CONTENT_NULL.code(), ErrorEnum.EXPRESSION_CONTENT_NULL.message());
        }

        checkExpressionCodeUnion(request.getExecutorId(), null, request.getExpressionCode());

        if (!checkExpressionTypeLegal(request.getExpressionType())) {
            return RestResult.failed(ErrorEnum.EXPRESSION_ILLEGAL_TYPE_ADD.code(), ErrorEnum.EXPRESSION_ILLEGAL_TYPE_ADD.message());
        }

        validExpressionValid(expressionContent);

        ExpressionExecutorInfoConfig expressionExecutorDetailConfig = new ExpressionExecutorInfoConfig();
        BeanUtil.copyProperties(request, expressionExecutorDetailConfig, CopyOptions.create().setIgnoreError(true).setIgnoreNullValue(true));
        expressionExecutorDetailConfig.setCreateTime(LocalDateTime.now());
        expressionExecutorDetailConfig.setDeleted(false);
        boolean addSuccess = this.save(expressionExecutorDetailConfig);
        LOG.debug("add config expression result : {} , {} , {}", addSuccess, expressionExecutorDetailConfig.getExpressionCode(), expressionExecutorDetailConfig.getId());

        if (addSuccess) {
            refreshConfigPost(expressionExecutorDetailConfig.getExecutorId());
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

    /**
     * 校验表达式格式是否有效
     * @param expressionContent 表达式内容
     */
    private void validExpressionValid(String expressionContent) {
        final String validExpression = ExpressionUtils.isValidExpression(expressionContent);
        Throws.check(StringUtils.isNotEmpty(validExpression), "表达式不符合格式:" + validExpression);
    }

    private void checkExpressionCodeUnion(Long executorId, Long id, String expressionCode) {
        //表达式编码必须唯一
        LambdaQueryChainWrapper<ExpressionExecutorInfoConfig> lambdaQuery = lambdaQuery()
                .eq(ExpressionExecutorInfoConfig::getExecutorId, executorId)
                .eq(ExpressionExecutorInfoConfig::getExpressionCode, expressionCode)
                .eq(ExpressionExecutorInfoConfig::getDeleted, false)
                .orderByAsc(ExpressionExecutorInfoConfig::getId)
                .last("limit 1");
        ExpressionExecutorInfoConfig existOne = lambdaQuery.one();

        Throws.check((id == null && existOne != null) || (existOne != null && !existOne.getId().equals(id)), ErrorEnum.REPEATED_EXPRESSION_ADD.message());
    }

    private boolean checkExpressionTypeLegal(String conditionType) {
        return Arrays.stream(ExpressionTypeEnum.values()).anyMatch(typeEnum -> typeEnum.getCode().equals(conditionType));
    }

    @Override
    public RestResult<ExpressionExecutorDetailConfigDTO> editExpression(EditExpressionConfigRequest editRequest) {

        ExpressionExecutorInfoConfig existOne = this.getById(editRequest.getId());

        if (existOne == null || StringUtils.isBlank(existOne.getExpressionContent())) {
            return RestResult.failed(ErrorEnum.UPDATE_NOT_EXIST_DATA.code(), String.format("找不到id为%d的表达式，无法完成修改", editRequest.getId()));
        }

        if (!checkExpressionTypeLegal(editRequest.getExpressionType())) {
            return RestResult.failed(ErrorEnum.EXPRESSION_ILLEGAL_TYPE_ADD.code(), ErrorEnum.EXPRESSION_ILLEGAL_TYPE_ADD.message());
        }

        // 检查表达式编码是否唯一
        checkExpressionCodeUnion(existOne.getExecutorId(), editRequest.getId(), editRequest.getExpressionCode());

        validExpressionValid(editRequest.getExpressionContent());

        ExpressionExecutorInfoConfig nodeConfig = new ExpressionExecutorInfoConfig();
        BeanUtil.copyProperties(editRequest, nodeConfig, CopyOptions.create().setIgnoreNullValue(true).setIgnoreError(true));
        nodeConfig.setUpdateTime(LocalDateTime.now());
        boolean updateSuccess = this.updateById(nodeConfig);

        if (updateSuccess) {
            refreshConfigPost(existOne.getExecutorId());
        }

        ExpressionExecutorDetailConfigDTO expressionExecutorDetailConfigDTO = new ExpressionExecutorDetailConfigDTO();
        BeanUtil.copyProperties(editRequest, expressionExecutorDetailConfigDTO);
        return updateSuccess ? RestResult.ok(expressionExecutorDetailConfigDTO) : RestResult.failed("数据更新到数据库失败");
    }

    private void refreshConfigPost(Long executorId) {
        this.eventPublisher.publishEvent(new ExecutorConfigRefreshEvent(executorId));
    }

    @Override
    public RestResult<List<ExpressionExecutorDetailConfigDTO>> queryExpression(QueryExpressionConfigRequest queryRequest) {
        Throws.nullError(queryRequest.getExecutorId(), "executorId");
        List<ExpressionExecutorDetailConfigDTO> dtoList = new ArrayList<>();
//        if (queryRequest.getParentId() == null) {
//            queryRequest.setParentId(BaseConstants.BASE_ROOT_ID);
//        }
        LambdaQueryChainWrapper<ExpressionExecutorInfoConfig> lambdaQuery = lambdaQuery().eq(ExpressionExecutorInfoConfig::getDeleted, 0);
        lambdaQuery.eq(ExpressionExecutorInfoConfig::getExecutorId, queryRequest.getExecutorId());
        lambdaQuery.eq(queryRequest.getParentId() != null, ExpressionExecutorInfoConfig::getParentId, queryRequest.getParentId());
        lambdaQuery.eq(StringUtils.isNotBlank(queryRequest.getExpressionType()), ExpressionExecutorInfoConfig::getExpressionType, queryRequest.getExpressionType());
        lambdaQuery.eq(queryRequest.getExpressionStatus() != null, ExpressionExecutorInfoConfig::getExpressionStatus, queryRequest.getExpressionStatus());
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getExpressionContent()), ExpressionExecutorInfoConfig::getExpressionContent, queryRequest.getExpressionContent());
        lambdaQuery.like(StringUtils.isNotBlank(queryRequest.getExpressionDescription()), ExpressionExecutorInfoConfig::getExpressionDescription, queryRequest.getExpressionDescription());
        lambdaQuery.orderByDesc(List.of(ExpressionExecutorInfoConfig::getExpressionStatus, ExpressionExecutorInfoConfig::getPriorityOrder)).orderByAsc(ExpressionExecutorInfoConfig::getId);
        List<ExpressionExecutorInfoConfig> expressionExecutorDetailConfigList = lambdaQuery.list();

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

        LambdaQueryWrapper<ExpressionExecutorInfoConfig> queryWrapper = new LambdaQueryWrapper<ExpressionExecutorInfoConfig>().in(ExpressionExecutorInfoConfig::getId, idSet)
                .eq(ExpressionExecutorInfoConfig::getDeleted, false);


        LOG.info("批量删除id集合: {} ", idSet);
        LambdaUpdateWrapper<ExpressionExecutorInfoConfig> updateWrapper = new LambdaUpdateWrapper<ExpressionExecutorInfoConfig>().set(ExpressionExecutorInfoConfig::getUpdateBy, delRequest.getUpdateBy())
                .set(ExpressionExecutorInfoConfig::getDeleted, true)
                .set(ExpressionExecutorInfoConfig::getUpdateTime, LocalDateTime.now())
                .in(ExpressionExecutorInfoConfig::getId, idSet);

        final RestResult<?> restResult = ServiceCommonUtil.batchDelete(delRequest, "找不到相关记录，不用执行删除操作", getBaseMapper(), queryWrapper, updateWrapper);

        if (restResult.isOk()) {
            final ExpressionExecutorInfoConfig detailConfig = getOne(queryWrapper);
            if (detailConfig != null) {
                refreshConfigPost(detailConfig.getExecutorId());
            }
        }
        return restResult;
    }

    /**
     * 递归查询相关子节点
     *
     * @param deleteIdList 需要删除的集合容器
     * @param parentIdList 父类节点
     */
    private void deepAllIdBuilder(Set<Long> deleteIdList, List<Long> parentIdList) {
        LambdaQueryChainWrapper<ExpressionExecutorInfoConfig> lambdaQuery = lambdaQuery().eq(ExpressionExecutorInfoConfig::getDeleted, 0);
        // 查询所有子节点
        lambdaQuery.in(ExpressionExecutorInfoConfig::getParentId, parentIdList);
        List<ExpressionExecutorInfoConfig> expressionExecutorDetailConfigs = lambdaQuery.select(ExpressionExecutorInfoConfig::getId).list();

        if (!CollectionUtil.isEmpty(expressionExecutorDetailConfigs)) {
            List<Long> ids = expressionExecutorDetailConfigs.stream().map(ExpressionExecutorInfoConfig::getId).collect(Collectors.toList());
            deleteIdList.addAll(ids);
            deepAllIdBuilder(deleteIdList, ids);
        }
    }

    @Override
    public List<ExpressionExecutorDetailConfigDTO> getEventInfo(Long baseId, Long parentId, String eventName, ExpressionTypeEnum typeEnum) {

        LambdaQueryWrapper<ExpressionExecutorInfoConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionExecutorInfoConfig::getExecutorId, baseId);
        queryWrapper.eq(ExpressionExecutorInfoConfig::getParentId, parentId);
        queryWrapper.eq(ExpressionExecutorInfoConfig::getExpressionType, typeEnum.getCode());
        queryWrapper.eq(ExpressionExecutorInfoConfig::getExpressionStatus, true);
        queryWrapper.eq(ExpressionExecutorInfoConfig::getDeleted, false);
        queryWrapper.eq(StringUtils.isNotEmpty(eventName), ExpressionExecutorInfoConfig::getExpressionCode, eventName);
        List<ExpressionExecutorInfoConfig> expressionExecutorDetailConfigs = this.getBaseMapper().selectList(queryWrapper);

        return convertExpressionExecutorDetailConfigDTO(expressionExecutorDetailConfigs);
    }

    @Override
    public List<ExpressionExecutorDetailConfigDTO> getNodeExpressionInfo(Long baseId, Long parentId) {
        LambdaQueryWrapper<ExpressionExecutorInfoConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionExecutorInfoConfig::getExecutorId, baseId);
        queryWrapper.eq(parentId != null, ExpressionExecutorInfoConfig::getParentId, parentId);
        queryWrapper.eq(ExpressionExecutorInfoConfig::getExpressionStatus, true);
        queryWrapper.eq(ExpressionExecutorInfoConfig::getDeleted, false);
        queryWrapper.orderByDesc(ExpressionExecutorInfoConfig::getPriorityOrder).orderByAsc(ExpressionExecutorInfoConfig::getId);
        List<ExpressionExecutorInfoConfig> expressionExecutorDetailConfigs = this.getBaseMapper().selectList(queryWrapper);
        return convertExpressionExecutorDetailConfigDTO(expressionExecutorDetailConfigs);
    }

    @Override
    public List<ExpressionExecutorInfoConfig> getExpressionListByBaseId(Long baseId) {
        LambdaQueryWrapper<ExpressionExecutorInfoConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.eq(ExpressionExecutorInfoConfig::getExecutorId, baseId);
        // 禁用的数据,也需要查询出来
//        queryWrapper.eq(ExpressionExecutorDetailConfig::getExpressionStatus, true);
        queryWrapper.eq(ExpressionExecutorInfoConfig::getDeleted, false);
        queryWrapper.orderByAsc(ExpressionExecutorInfoConfig::getId);
        return this.getBaseMapper().selectList(queryWrapper);
    }

    @Override
    public boolean copyNode(ExpressionExecutorInfoConfig config) {
        final Long id = config.getId();
        final Long parentId = config.getParentId();
        final ExpressionExecutorInfoConfig executorDetailConfig = getById(id);
        // 清空主键
        executorDetailConfig.setId(null);
        final String expressionCode = executorDetailConfig.getExpressionCode();
        executorDetailConfig.setParentId(parentId);
        executorDetailConfig.setExpressionCode(expressionCode + "_copy_" + RandomUtil.randomString(5));
        final boolean result = save(executorDetailConfig);
        LOG.info("【复制节点】 将 {} 对象 加入到 {} 中 -> {}", id, parentId, executorDetailConfig);
        return result;
    }

    @Override
    public List<ExpressionExecutorInfoConfig> queryExpressionContent(String expressionContent, Date changeDate) {
        LambdaQueryWrapper<ExpressionExecutorInfoConfig> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(StringUtils.isNotEmpty(expressionContent), ExpressionExecutorInfoConfig::getExpressionContent, expressionContent);
        queryWrapper.and(changeDate != null, var -> var.ge(ExpressionExecutorInfoConfig::getCreateTime, changeDate).or(v2 -> v2.ge(ExpressionExecutorInfoConfig::getUpdateTime, changeDate)));
        return list(queryWrapper);
    }
}
