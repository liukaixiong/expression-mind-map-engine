package com.liukx.expression.engine.server.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.core.api.model.api.ExpressionExecutorResultDTO;
import com.liukx.expression.engine.core.api.model.api.ExpressionResultLogCollect;
import com.liukx.expression.engine.core.api.model.api.ExpressionResultLogDTO;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.enums.ExpressionLogTypeEnum;
import com.liukx.expression.engine.core.utils.Jsons;
import com.liukx.expression.engine.server.mapper.ExpressionTraceLogIndexMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionTraceRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionTraceInfoDTO;
import com.liukx.expression.engine.server.service.ExpressionTraceLogIndexService;
import com.liukx.expression.engine.server.service.ExpressionTraceLogInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author liukx
 * @since 2024-07-18
 */
@Service
@Slf4j
public class ExpressionTraceLogIndexServiceImpl extends ServiceImpl<ExpressionTraceLogIndexMapper, ExpressionTraceLogIndex> implements IService<ExpressionTraceLogIndex>, ExpressionTraceLogIndexService, InitializingBean {
    @Autowired
    private ExpressionTraceLogInfoService traceLogInfoService;

    @Override
    public void afterPropertiesSet() {
        ThreadUtil.newSingleExecutor().execute(() -> {
            while (true) {
                try {
                    final List<ExpressionExecutorResultDTO> resultList = ExpressionResultLogCollect.getInstance().pollBatch(10);
                    if (!CollectionUtils.isEmpty(resultList)) {
                        for (ExpressionExecutorResultDTO expressionExecutorResultDTO : resultList) {
                            saveIndexInfo(expressionExecutorResultDTO);
                        }
                    } else {
                        ThreadUtil.sleep(50);
                    }
                } catch (Exception e) {
                    log.warn("追踪日志消费失败:{}", e.getMessage());
                } finally {
                    ThreadUtil.sleep(5);
                }
            }
        });
    }

    @Override
    public Page<ExpressionTraceLogIndex> queryExpressionTraceLogList(QueryExpressionTraceRequest queryRequest) {
        Page<ExpressionTraceLogIndex> page = new Page<>(queryRequest.getPageNum(), queryRequest.getPageSize());
        page.setSearchCount(false);
        LambdaQueryWrapper<ExpressionTraceLogIndex> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.isNotEmpty(queryRequest.getServiceName()), ExpressionTraceLogIndex::getServiceName, queryRequest.getServiceName())
                .eq(StringUtils.isNotEmpty(queryRequest.getBusinessCode()), ExpressionTraceLogIndex::getBusinessCode, queryRequest.getBusinessCode())
                .eq(StringUtils.isNotEmpty(queryRequest.getEventName()), ExpressionTraceLogIndex::getEventName, queryRequest.getEventName())
                .eq(StringUtils.isNotEmpty(queryRequest.getUnionId()), ExpressionTraceLogIndex::getUnionId, queryRequest.getUnionId())
                .eq(StringUtils.isNotEmpty(queryRequest.getExecutorCode()), ExpressionTraceLogIndex::getExecutorCode, queryRequest.getExecutorCode())
                .eq(StringUtils.isNotEmpty(queryRequest.getTraceId()), ExpressionTraceLogIndex::getTraceId, queryRequest.getTraceId())
                .eq(queryRequest.getUserId() != null, ExpressionTraceLogIndex::getUserId, queryRequest.getUserId())
                .eq(queryRequest.getExecutorId() != null, ExpressionTraceLogIndex::getExecutorId, queryRequest.getExecutorId()).orderByDesc(ExpressionTraceLogIndex::getId);

        final Page<ExpressionTraceLogIndex> expressionTraceLogIndexPage = getBaseMapper().selectPage(page, wrapper);
        final int currentPageSize = expressionTraceLogIndexPage.getRecords().size();
        expressionTraceLogIndexPage.setTotal(currentPageSize >= queryRequest.getPageSize() ? 100 : currentPageSize);
        return expressionTraceLogIndexPage;
    }

    @Override
    public ExpressionTraceInfoDTO getTraceInfoList(Long id) {
        final ExpressionTraceLogIndex traceLogIndex = getById(id);
        if (traceLogIndex != null) {
            List<ExpressionTraceLogInfo> traceLogInfos = traceLogInfoService.getInfoListByTraceLogId(id);
            ExpressionTraceInfoDTO expressionTraceInfoDTO = new ExpressionTraceInfoDTO();
            BeanUtils.copyProperties(traceLogIndex, expressionTraceInfoDTO);
            expressionTraceInfoDTO.setTraceLogInfoList(traceLogInfos);
            return expressionTraceInfoDTO;
        }
        return null;
    }

    @Override
    public boolean addTraceLog(List<ExpressionExecutorResultDTO> request) {
        request.forEach(ExpressionResultLogCollect.getInstance()::add);
        return true;
    }

    private void saveIndexInfo(ExpressionExecutorResultDTO expressionExecutorResultDTO) {
        ExpressionTraceLogIndex index = new ExpressionTraceLogIndex();
        BeanUtils.copyProperties(expressionExecutorResultDTO, index);
        index.setEnvBody(null);
        final boolean save = this.save(index);

        final Long id = index.getId();

        final List<ExpressionResultLogDTO> resultLogList = expressionExecutorResultDTO.getResultLogList();

        // 构建函数信息
        final List<ExpressionTraceLogInfo> saveInfoList = new ArrayList<>();
        for (ExpressionResultLogDTO expressionResultLogDTO : resultLogList) {
            ExpressionTraceLogInfo traceLogInfo = new ExpressionTraceLogInfo();
            BeanUtils.copyProperties(expressionResultLogDTO, traceLogInfo);
            final String resultType = expressionResultLogDTO.getResultType();
            final ExpressionLogTypeEnum expressionLogTypeEnum = ExpressionLogTypeEnum.valueOf(resultType);
            traceLogInfo.setModuleType(expressionResultLogDTO.getResultType());
            traceLogInfo.setExpressionDescription(expressionResultLogDTO.getDescription());
            traceLogInfo.setTraceLogId(id);
            traceLogInfo.setDebugTraceContent(Jsons.toJsonString(expressionResultLogDTO.getDebugTraceContent()));
            traceLogInfo.setExecutorId(expressionExecutorResultDTO.getExecutorId());
            // 结果构建
            final Object result = expressionResultLogDTO.getResult();
            if (result instanceof Boolean) {
                traceLogInfo.setExpressionResult((Boolean) result ? 1 : 0);
            } else {
                traceLogInfo.setExpressionResult(result != null ? 1 : 0);
            }

            if (expressionLogTypeEnum == ExpressionLogTypeEnum.function) {
                final FunctionApiModel functionApiModel = expressionResultLogDTO.getFunctionApiModel();
                final List<Object> funcArgs = expressionResultLogDTO.getFuncArgs();
                // 构建函数信息
                final String name = functionApiModel.getName();
                final String param = StringUtils.join(funcArgs, ",");
                String functionName = name + "(" + param + ")";
                traceLogInfo.setExpressionContent(functionName);
                if (StringUtils.isEmpty(expressionResultLogDTO.getDescription())) {
                    traceLogInfo.setExpressionDescription(functionApiModel.getDescribe());
                }
            } else {
                traceLogInfo.setExpressionContent(expressionResultLogDTO.getExpression());
                traceLogInfo.setExpressionDescription(expressionResultLogDTO.getDescription());
            }
            saveInfoList.add(traceLogInfo);
        }

        traceLogInfoService.saveBatch(saveInfoList);
    }


}
