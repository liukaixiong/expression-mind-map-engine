package com.liukx.expression.engine.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorInfoConfig;
import com.liukx.expression.engine.server.mapper.entity.ExpressionHistoryVersion;

import java.util.List;

/**
 * <p>
 * 表达式历史版本 服务类
 * </p>
 *
 * @author expression-engine
 * @since 2025-03-12
 */
public interface ExpressionHistoryVersionService extends IService<ExpressionHistoryVersion> {

    /**
     * 保存历史版本
     *
     * @param expression  表达式配置
     * @param changeType  变更类型: CREATE-创建, UPDATE-更新, DELETE-删除
     * @param operator    操作人
     */
    void saveHistory(ExpressionExecutorInfoConfig expression, String changeType, String operator);

    /**
     * 获取表达式历史版本列表
     *
     * @param expressionId 表达式id
     * @return 历史版本列表
     */
    List<ExpressionHistoryVersion> getHistoryByExpressionId(Long expressionId);

    /**
     * 获取执行器下所有历史版本（跨表达式对比）
     *
     * @param executorId 执行器id
     * @return 历史版本列表
     */
    List<ExpressionHistoryVersion> getHistoryByExecutorId(Long executorId);

    /**
     * 根据版本号获取历史详情
     *
     * @param expressionId 表达式id
     * @param versionNo    版本号
     * @return 历史版本详情
     */
    ExpressionHistoryVersion getHistoryByVersion(Long expressionId, Integer versionNo);
}