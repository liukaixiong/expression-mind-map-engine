package com.liukx.expression.engine.server.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liukx.expression.engine.server.mapper.entity.ExpressionGlobalTraceLog;

/**
 * <p>
 * 全局日志表,负责记录表达式执行过程的日志记录,负责排查执行过程 Mapper 接口
 * </p>
 *
 * @author bsy
 * @since 2022-06-15
 */
public interface ExpressionGlobalTraceLogMapper extends BaseMapper<ExpressionGlobalTraceLog> {

}
