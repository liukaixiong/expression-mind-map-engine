package com.liukx.expression.engine.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liukx.expression.engine.server.mapper.entity.ExpressionVariableConfig;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionVariableRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionVariableDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;

import java.util.List;

/**
 * <p>
 * 表达式引擎通用变量配置表 服务类
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
public interface ExpressionVariableConfigService extends IService<ExpressionVariableConfig> {

//    RestResult<ExpressionVariableDTO> addExpressionVariable(AddExpressionVariableRequest addRequest);

//    RestResult<ExpressionVariableDTO> editExpressionVariable(EditExpressionVariableRequest editRequest);

    RestResult<List<ExpressionVariableDTO>> queryExpressionVariable(QueryExpressionVariableRequest queryRequest);

//    RestResult<?> batchDeleteByIdList(DeleteByIdListRequest delRequest);
}
