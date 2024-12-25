package com.liukx.expression.engine.server.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liukx.expression.engine.server.mapper.entity.ExpressionFunctionConfig;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionFunctionRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionFunctionDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;

import java.util.List;

/**
 * <p>
 * 函数配置表 服务类
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
public interface ExpressionFunctionConfigService extends IService<ExpressionFunctionConfig> {

    //    RestResult<ExpressionFunctionDTO> addExpressionFunction(AddExpressionFunctionRequest addRequest);
//
//    RestResult<ExpressionFunctionDTO> editExpressionFunction (EditExpressionFunctionRequest editRequest);
//
    RestResult<List<ExpressionFunctionDTO>> queryExpressionFunction(QueryExpressionFunctionRequest queryRequest);
//
//    RestResult<?> batchDeleteByIdList(DeleteByIdListRequest delRequest);
}
