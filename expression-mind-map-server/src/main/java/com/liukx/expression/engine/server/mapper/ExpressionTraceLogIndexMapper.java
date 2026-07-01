package com.liukx.expression.engine.server.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author liukx
 * @since 2024-07-18
 */
@Mapper
public interface ExpressionTraceLogIndexMapper extends BaseMapper<ExpressionTraceLogIndex> {
    @Select("SELECT * FROM ${tableName} ${ew.customSqlSegment}")
    Page<ExpressionTraceLogIndex> selectPageByTable(Page<ExpressionTraceLogIndex> page, @Param("tableName") String tableName,@Param("ew") LambdaQueryWrapper<ExpressionTraceLogIndex> wrapper);

    @Select("SELECT * FROM ${tableName} ${ew.customSqlSegment}")
    ExpressionTraceLogIndex selectByTable(@Param("tableName") String tableName, @Param("ew") LambdaQueryWrapper<ExpressionTraceLogIndex> wrapper);
}
