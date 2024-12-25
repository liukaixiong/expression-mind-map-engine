package com.liukx.expression.engine.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liukx.expression.engine.server.mapper.entity.ExpressionLinkResultLog;
import com.liukx.expression.engine.server.model.dto.request.AddLinkResultLogRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditLinkResultLogRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryLinkResultLogRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionLinkResultLogDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;

import java.util.List;

/**
 * <p>
 * 成功回调日志表,记录执行完成的日志记录,全局日志表的压缩版 服务类
 * </p>
 *
 * @author bsy
 * @since 2022-06-15
 */
public interface ExpressionLinkResultLogService extends IService<ExpressionLinkResultLog> {

    RestResult<ExpressionLinkResultLogDTO> addOne(AddLinkResultLogRequest addRequest);

    RestResult<ExpressionLinkResultLogDTO> updateOne(EditLinkResultLogRequest editRequest);

    RestResult<List<ExpressionLinkResultLogDTO>> queryDtoList(QueryLinkResultLogRequest queryRequest);

    RestResult<?> logicDeleteByIdList(DeleteByIdListRequest delRequest);
}
