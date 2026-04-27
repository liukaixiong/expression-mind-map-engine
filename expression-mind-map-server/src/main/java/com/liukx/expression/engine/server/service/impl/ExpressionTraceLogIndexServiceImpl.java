package com.liukx.expression.engine.server.service.impl;

import cn.hutool.core.thread.ThreadUtil;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.core.api.model.api.ExpressionExecutorResultDTO;
import com.liukx.expression.engine.core.api.model.api.ExpressionResultLogCollect;
import com.liukx.expression.engine.server.mapper.ExpressionTraceLogIndexMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionTraceRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionTraceInfoDTO;
import com.liukx.expression.engine.server.model.dto.response.TraceLogPageResult;
import com.liukx.expression.engine.server.service.ExpressionTraceLogIndexService;
import com.liukx.expression.engine.server.service.TraceLogStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

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
    private TraceLogStorageService traceLogStorageService;

    @Override
    public void afterPropertiesSet() {
        ThreadUtil.newSingleExecutor().execute(() -> {
            while (true) {
                try {
                    final List<ExpressionExecutorResultDTO> resultList = ExpressionResultLogCollect.getInstance().pollBatch(10);
                    if (!CollectionUtils.isEmpty(resultList)) {
                        for (ExpressionExecutorResultDTO expressionExecutorResultDTO : resultList) {
                            traceLogStorageService.saveTraceLog(expressionExecutorResultDTO);
                        }
                    } else {
                        ThreadUtil.sleep(50);
                    }
                } catch (Exception e) {
                    log.error("追踪日志消费失败", e);
                } finally {
                    ThreadUtil.sleep(5);
                }
            }
        });
    }

    @Override
    public TraceLogPageResult<ExpressionTraceLogIndex> queryExpressionTraceLogList(QueryExpressionTraceRequest queryRequest) {
        return traceLogStorageService.queryTraceLogList(queryRequest);
    }

    @Override
    public ExpressionTraceInfoDTO getTraceInfoList(Long id) {
        return traceLogStorageService.getTraceInfo(id);
    }

    @Override
    public boolean addTraceLog(List<ExpressionExecutorResultDTO> request) {
        request.forEach(ExpressionResultLogCollect.getInstance()::add);
        return true;
    }

    @Override
    public ExpressionTraceLogIndex getExpressionSampleBody(Long expressionId) {
        return traceLogStorageService.getExpressionSampleBody(expressionId);
    }
}
