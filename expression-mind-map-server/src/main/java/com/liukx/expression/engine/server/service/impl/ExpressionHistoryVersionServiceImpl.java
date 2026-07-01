package com.liukx.expression.engine.server.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liukx.expression.engine.core.model.ExpressionUserContext;
import com.liukx.expression.engine.server.mapper.ExpressionHistoryVersionMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorInfoConfig;
import com.liukx.expression.engine.server.mapper.entity.ExpressionHistoryVersion;
import com.liukx.expression.engine.server.service.ExpressionHistoryVersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 表达式历史版本 服务实现类
 * </p>
 *
 * @author expression-engine
 * @since 2025-03-12
 */
@Service
public class ExpressionHistoryVersionServiceImpl extends ServiceImpl<ExpressionHistoryVersionMapper, ExpressionHistoryVersion> implements ExpressionHistoryVersionService {

    private final Logger LOG = LoggerFactory.getLogger(ExpressionHistoryVersionServiceImpl.class);

    @Override
    public void saveHistory(ExpressionExecutorInfoConfig expression, String changeType) {
        if (expression == null || expression.getId() == null) {
            LOG.warn("表达式为空或id为空，无法保存历史版本");
            return;
        }

        // 获取当前表达式最新版本号
        Integer maxVersionNo = baseMapper.selectMaxVersionNo(expression.getId());
        int nextVersionNo = (maxVersionNo == null ? 0 : maxVersionNo) + 1;

        // 操作人统一从 token 上下文获取（未登录兜底 system），禁止由调用方传入
        String operator = ExpressionUserContext.currentUsernameOrSystem();

        // 创建历史版本记录
        ExpressionHistoryVersion historyVersion = new ExpressionHistoryVersion();
        historyVersion.setExpressionId(expression.getId());
        historyVersion.setExecutorId(expression.getExecutorId());
        historyVersion.setExpressionCode(expression.getExpressionCode());
        historyVersion.setExpressionTitle(expression.getExpressionTitle());
        historyVersion.setExpressionContent(expression.getExpressionContent());
        historyVersion.setExpressionDescription(expression.getExpressionDescription());
        historyVersion.setVersionNo(nextVersionNo);
        historyVersion.setChangeType(changeType);
        historyVersion.setOperator(operator);
        historyVersion.setCreateTime(LocalDateTime.now());

        boolean saveResult = this.save(historyVersion);
        if (saveResult) {
            LOG.info("保存表达式历史版本成功: expressionId={}, versionNo={}, changeType={}",
                    expression.getId(), nextVersionNo, changeType);
        } else {
            LOG.error("保存表达式历史版本失败: expressionId={}", expression.getId());
        }
    }

    @Override
    public List<ExpressionHistoryVersion> getHistoryByExpressionId(Long expressionId) {
        if (expressionId == null) {
            return List.of();
        }
        return baseMapper.selectHistoryByExpressionId(expressionId);
    }

    @Override
    public List<ExpressionHistoryVersion> getHistoryByExecutorId(Long executorId) {
        if (executorId == null) {
            return List.of();
        }
        return baseMapper.selectHistoryByExecutorId(executorId);
    }

    @Override
    public ExpressionHistoryVersion getHistoryByVersion(Long expressionId, Integer versionNo) {
        if (expressionId == null || versionNo == null) {
            return null;
        }

        LambdaQueryWrapper<ExpressionHistoryVersion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ExpressionHistoryVersion::getExpressionId, expressionId);
        queryWrapper.eq(ExpressionHistoryVersion::getVersionNo, versionNo);
        return this.getOne(queryWrapper);
    }
}
