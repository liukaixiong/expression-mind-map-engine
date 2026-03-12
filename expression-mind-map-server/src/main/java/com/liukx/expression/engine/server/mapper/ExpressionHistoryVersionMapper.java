package com.liukx.expression.engine.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionHistoryVersion;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 表达式历史版本 Mapper 接口
 * </p>
 *
 * @author expression-engine
 * @since 2025-03-12
 */
@Mapper
public interface ExpressionHistoryVersionMapper extends BaseMapper<ExpressionHistoryVersion> {

    /**
     * 查询表达式历史版本列表（按版本号降序）
     *
     * @param expressionId 表达式id
     * @return 历史版本列表
     */
    List<ExpressionHistoryVersion> selectHistoryByExpressionId(@Param("expressionId") Long expressionId);

    /**
     * 查询执行器下所有表达式的历史版本（跨表达式对比）
     *
     * @param executorId 执行器id
     * @return 历史版本列表
     */
    List<ExpressionHistoryVersion> selectHistoryByExecutorId(@Param("executorId") Long executorId);

    /**
     * 获取表达式最新版本号
     *
     * @param expressionId 表达式id
     * @return 最新版本号
     */
    Integer selectMaxVersionNo(@Param("expressionId") Long expressionId);
}