package com.liukx.expression.engine.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liukx.expression.engine.server.mapper.entity.ExpressionNodeConfig;
import com.liukx.expression.engine.server.model.dto.request.AddExpressionNodeRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditExpressionNodeRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionNodeRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionNodeDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;

import java.util.List;

/**
 * <p>
 * 引擎节点信息 服务类
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
public interface ExpressionNodeConfigService extends IService<ExpressionNodeConfig> {

    RestResult<ExpressionNodeDTO> addExpressionNode(AddExpressionNodeRequest addRequest);

    RestResult<ExpressionNodeDTO> editExpressionNode(EditExpressionNodeRequest editRequest);

    RestResult<List<ExpressionNodeDTO>> queryExpressionNode(QueryExpressionNodeRequest queryRequest);

    RestResult<?> batchDeleteByIdList(DeleteByIdListRequest delRequest);
}
