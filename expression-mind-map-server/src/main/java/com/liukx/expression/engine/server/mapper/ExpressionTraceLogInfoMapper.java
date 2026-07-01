package com.liukx.expression.engine.server.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author liukx
 * @since 2024-07-18
 */
@Mapper
public interface ExpressionTraceLogInfoMapper extends BaseMapper<ExpressionTraceLogInfo> {
    @Select("SELECT * FROM ${tableName} ${ew.customSqlSegment}")
    List<ExpressionTraceLogInfo> selectPageByTable(@Param("tableName") String tableName, @Param("ew") LambdaQueryWrapper<ExpressionTraceLogInfo> wrapper);

    @Select("SELECT * FROM ${tableName} ${ew.customSqlSegment}")
    List<ExpressionTraceLogInfo> selectListByTable(@Param("tableName") String tableName, @Param("ew") LambdaQueryWrapper<ExpressionTraceLogInfo> wrapper);

}
