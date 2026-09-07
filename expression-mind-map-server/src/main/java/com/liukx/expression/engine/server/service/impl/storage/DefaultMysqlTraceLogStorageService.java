package com.liukx.expression.engine.server.service.impl.storage;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.core.api.model.api.ExpressionExecutorResultDTO;
import com.liukx.expression.engine.core.api.model.api.ExpressionResultLogDTO;
import com.liukx.expression.engine.core.api.model.api.FunctionApiModel;
import com.liukx.expression.engine.core.enums.ExpressionLogTypeEnum;
import com.liukx.expression.engine.core.enums.MetricKeyEnum;
import com.liukx.expression.engine.core.utils.Jsons;
import com.liukx.expression.engine.core.utils.MetricHelper;
import com.liukx.expression.engine.core.utils.TraceLogSanitizer;
import com.liukx.expression.engine.server.manager.MysqlTableManager;
import com.liukx.expression.engine.server.mapper.ExpressionTraceLogIndexMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionTraceRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionTraceInfoDTO;
import com.liukx.expression.engine.server.model.dto.response.TraceLogPageResult;
import com.liukx.expression.engine.server.service.ExpressionTraceLogInfoService;
import com.liukx.expression.engine.server.service.TraceLogStorageService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * 追踪日志默认 MySQL 存储实现
 * <p>
 * 不加 @Service，由 ServiceConfiguration 通过 @Bean + @ConditionalOnMissingBean 注册，
 * 以便接入方替换为自定义存储引擎。
 * </p>
 *
 * @author liukx
 */
public class DefaultMysqlTraceLogStorageService extends ServiceImpl<ExpressionTraceLogIndexMapper, ExpressionTraceLogIndex>
        implements TraceLogStorageService {

    private final Logger LOG = getLogger(DefaultMysqlTraceLogStorageService.class);

    /** 明细分批落库的单批大小 */
    private static final int SAVE_BATCH_SIZE = 500;

    @Autowired
    private ExpressionTraceLogInfoService traceLogInfoService;

    @Autowired
    private MysqlTableManager tableManager;

    @Override
    public void saveTraceLog(ExpressionExecutorResultDTO expressionExecutorResultDTO) {
        // ---- 埋点：追踪日志写入计数 ----
        String serviceName = expressionExecutorResultDTO.getServiceName();
        String businessCode = expressionExecutorResultDTO.getBusinessCode();
        String executorCode = expressionExecutorResultDTO.getExecutorCode();

        MetricHelper.increment(MetricKeyEnum.expression_trace_log_index_save_count, 1,
                "serviceName", serviceName,
                "businessCode", businessCode,
                "executorCode", executorCode);

        ExpressionTraceLogIndex index = new ExpressionTraceLogIndex();
        BeanUtils.copyProperties(expressionExecutorResultDTO, index);
        String envBody = expressionExecutorResultDTO.getEnvBody();

        // 受限于mysql存储字段大小有限制,如果es不会有问题.
        if (StringUtils.isNotEmpty(envBody)) {
            if (envBody.length() > 2000) {
                index.setEnvBody(Jsons.compressReserveJsonKeyString(envBody));
            } else {
                index.setEnvBody(envBody);
            }
        }

        final boolean save = this.save(index);

        final Long id = index.getId();

        final List<ExpressionResultLogDTO> resultLogList = expressionExecutorResultDTO.getResultLogList();

        // ---- 埋点：单次追踪日志明细条数分布 ----
        MetricHelper.record(MetricKeyEnum.expression_trace_log_detail, resultLogList.size(),
                "serviceName", serviceName,
                "businessCode", businessCode,
                "executorCode", executorCode);

        // 构建明细信息
        final List<ExpressionTraceLogInfo> saveInfoList = new ArrayList<>();
        for (ExpressionResultLogDTO expressionResultLogDTO : resultLogList) {
            ExpressionTraceLogInfo traceLogInfo = new ExpressionTraceLogInfo();
            BeanUtils.copyProperties(expressionResultLogDTO, traceLogInfo);
            final String resultType = expressionResultLogDTO.getResultType();
            final ExpressionLogTypeEnum expressionLogTypeEnum = ExpressionLogTypeEnum.valueOf(resultType);
            traceLogInfo.setModuleType(expressionResultLogDTO.getResultType());
            traceLogInfo.setExpressionDescription(expressionResultLogDTO.getDescription());
            traceLogInfo.setTraceLogId(id);
            // 空快照存 NULL（不再落 '{}'）；非空快照先按值截断拷贝再序列化，
            // 避免携带业务大对象的快照整体序列化成巨型字符串后才截断
            final Map<String, Object> debugContent = expressionResultLogDTO.getDebugTraceContent();
            String debugJson = null;
            if (debugContent != null && !debugContent.isEmpty()) {
                debugJson = getMiniString(Jsons.toJsonString(TraceLogSanitizer.compactDebugMap(debugContent)));
            }
            traceLogInfo.setDebugTraceContent(debugJson);
            traceLogInfo.setExecutorId(expressionExecutorResultDTO.getExecutorId());
            // 结果构建
            final Object result = expressionResultLogDTO.getResult();
            if (result instanceof Boolean) {
                traceLogInfo.setExpressionResult((Boolean) result ? 1 : 0);
            } else {
                traceLogInfo.setExpressionResult(Objects.equals(result, -1) ? -1 : 1);
            }

            if (expressionLogTypeEnum == ExpressionLogTypeEnum.function) {
                final FunctionApiModel functionApiModel = expressionResultLogDTO.getFunctionApiModel();
                final List<Object> funcArgs = expressionResultLogDTO.getFuncArgs();
                // 构建函数信息（参数逐个短字符串化后拼接，防止大对象 toString 撑爆）
                final String name = functionApiModel.getName();
                String functionName = name + "(" + TraceLogSanitizer.joinCompactArgs(funcArgs) + ")";
                traceLogInfo.setExpressionContent(getMiniString(functionName));
                if (StringUtils.isEmpty(expressionResultLogDTO.getDescription())) {
                    traceLogInfo.setExpressionDescription(functionApiModel.getDescribe());
                }
            } else {
                // 表达式内容同样受长度上限约束（此前未截断，超长内容会超出字段上限）
                traceLogInfo.setExpressionContent(getMiniString(expressionResultLogDTO.getExpression()));
                traceLogInfo.setExpressionDescription(expressionResultLogDTO.getDescription());
            }
            saveInfoList.add(traceLogInfo);
        }

        // 分批落库，避免单事务批量过大
        for (int start = 0; start < saveInfoList.size(); start += SAVE_BATCH_SIZE) {
            final int end = Math.min(start + SAVE_BATCH_SIZE, saveInfoList.size());
            traceLogInfoService.saveBatch(saveInfoList.subList(start, end));
        }
    }

    @Override
    public TraceLogPageResult<ExpressionTraceLogIndex> queryTraceLogList(QueryExpressionTraceRequest queryRequest) {
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
                .eq(queryRequest.getExecutorId() != null, ExpressionTraceLogIndex::getExecutorId, queryRequest.getExecutorId())
                .ge(queryRequest.getStartDate() != null, ExpressionTraceLogIndex::getCreated, queryRequest.getStartDate())
                .le(queryRequest.getEndDate() != null, ExpressionTraceLogIndex::getCreated, queryRequest.getEndDate())
                .orderByDesc(ExpressionTraceLogIndex::getId);

        final Page<ExpressionTraceLogIndex> expressionTraceLogIndexPage = getBaseMapper().selectPage(page, wrapper);

        final int currentPageSize = expressionTraceLogIndexPage.getRecords().size();
        long total = currentPageSize >= queryRequest.getPageSize() ? 100 : currentPageSize;

        // 尝试拿两部分数据出来处理
        if (currentPageSize < queryRequest.getPageSize()) {
            final String pastTableName = tableManager.getLastTableNameList(ExpressionTraceLogIndex.class, -1);
            if (StringUtils.isNotEmpty(pastTableName)) {
                Page<ExpressionTraceLogIndex> pageV2 = new Page<>(1, queryRequest.getPageSize());
                pageV2.setSearchCount(false);
                final Page<ExpressionTraceLogIndex> expressionTraceLogIndex = getBaseMapper().selectPageByTable(pageV2, pastTableName, wrapper);
                final List<ExpressionTraceLogIndex> recordV2s = expressionTraceLogIndex.getRecords();
                if (!recordV2s.isEmpty()) {
                    expressionTraceLogIndexPage.getRecords().addAll(recordV2s);
                    if (currentPageSize == 0) {
                        total = recordV2s.size() >= queryRequest.getPageSize() ? 100 : recordV2s.size();
                    }
                }
            }
        }

        return new TraceLogPageResult<>(expressionTraceLogIndexPage.getRecords(), total);
    }

    @Override
    public ExpressionTraceInfoDTO getTraceInfo(Long id) {
        return getTraceInfo(id, null);
    }

    @Override
    public ExpressionTraceInfoDTO getTraceInfo(Long id, Date created) {
        Date actualCreated = created;
        ExpressionTraceLogIndex index;

        if (actualCreated != null) {
            // 常态路径：按 created 定位 index 月表
            String indexTable = tableManager.getTableNameByDate(ExpressionTraceLogIndex.class, actualCreated);
            if (indexTable != null) {
                LambdaQueryWrapper<ExpressionTraceLogIndex> w = new LambdaQueryWrapper<>();
                w.eq(ExpressionTraceLogIndex::getId, id);
                index = getBaseMapper().selectByTable(indexTable, w);
            } else {
                index = null;
            }
        } else {
            // 兜底路径：查 index 活跃表拿 created
            index = getById(id);
            actualCreated = (index != null) ? index.getCreated() : null;
        }

        if (index == null) {
            return null;
        }

        // 按 created 精确定位 info 日表（日表不存在返回空列表，符合 D6）
        List<ExpressionTraceLogInfo> traceLogInfos = traceLogInfoService.getInfoListByTraceLogId(index.getId(), actualCreated);

        ExpressionTraceInfoDTO expressionTraceInfoDTO = new ExpressionTraceInfoDTO();
        BeanUtils.copyProperties(index, expressionTraceInfoDTO);
        expressionTraceInfoDTO.setTraceLogInfoList(traceLogInfos);
        return expressionTraceInfoDTO;
    }

    @Override
    public ExpressionTraceLogIndex getExpressionSampleBody(Long expressionId) {
        ExpressionTraceLogInfo traceLogInfo = traceLogInfoService.getExpressionRecentlySuccessLog(expressionId);
        if (traceLogInfo != null) {
            return getById(traceLogInfo.getTraceLogId());
        }
        return null;
    }

    @Override
    public List<ExpressionTraceLogInfo> getInfoListByTraceLogId(Long traceLogId) {
        return traceLogInfoService.getInfoListByTraceLogId(traceLogId);
    }

    @Override
    public List<ExpressionTraceLogInfo> getInfoListByTraceLogId(Long traceLogId, Date created) {
        return traceLogInfoService.getInfoListByTraceLogId(traceLogId, created);
    }

    @Override
    public boolean hasRecentlySuccessLog(Long expressionId, Date startDate) {
        return traceLogInfoService.getExpressionRecentlySuccessCount(expressionId, startDate);
    }

    /**
     * 遇到过长的字符串，保留一部分。（MYSQL的长度限制）
     *
     * @param text 字符串
     * @return 短字符串
     */
    private String getMiniString(String text) {
        if (StringUtils.isNotEmpty(text)) {
            int maxLength = 1500;
            if (text.length() > maxLength) {
                return text.substring(0, maxLength) + "...";
            }
        }
        return text;
    }
}
