package com.liukx.expression.engine.server.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorBaseInfo;
import com.liukx.expression.engine.server.model.dto.request.AddExpressionExecutorRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditExpressionExecutorRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionExecutorRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionExecutorBaseDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author bsy
 * @since 2022-06-12
 */
public interface ExpressionExecutorConfigService extends IService<ExpressionExecutorBaseInfo> {

    RestResult<ExpressionExecutorBaseDTO> addExpressionExecutor(AddExpressionExecutorRequest addRequest);

    RestResult<ExpressionExecutorBaseDTO> editExpressionExecutor(EditExpressionExecutorRequest editRequest);

    Page<ExpressionExecutorBaseInfo> queryExpressionExecutor(QueryExpressionExecutorRequest queryRequest);

    RestResult<?> batchDeleteByIdList(DeleteByIdListRequest delRequest);

    /**
     * 查询执行对象
     *
     * @param serviceName
     * @param businessCode
     * @param executorCode
     * @return
     */
    public ExpressionExecutorBaseDTO queryExecutorInfo(String serviceName, String businessCode, String executorCode);
}
