package com.liukx.expression.engine.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liukx.expression.engine.server.mapper.entity.ExpressionGlobalTraceLog;
import com.liukx.expression.engine.server.model.dto.request.AddGlobalTraceLogRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditGlobalTraceLogRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryGlobalTraceLogRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionGlobalTraceLogDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;

import java.util.List;

/**
 * <p>
 * 全局日志表,负责记录表达式执行过程的日志记录,负责排查执行过程 服务类
 * </p>
 *
 * @author bsy
 * @since 2022-06-15
 */
public interface GlobalTraceLogService extends IService<ExpressionGlobalTraceLog> {

    RestResult<ExpressionGlobalTraceLogDTO> addOne(AddGlobalTraceLogRequest addRequest);

    RestResult<ExpressionGlobalTraceLogDTO> updateOne(EditGlobalTraceLogRequest editRequest);

    RestResult<List<ExpressionGlobalTraceLogDTO>> queryDtoList(QueryGlobalTraceLogRequest queryRequest);

    RestResult<?> logicDeleteByIdList(DeleteByIdListRequest delRequest);
}
