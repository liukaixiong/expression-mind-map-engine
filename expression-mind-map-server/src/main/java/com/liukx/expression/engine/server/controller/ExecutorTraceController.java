package com.liukx.expression.engine.server.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionTraceRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionTraceInfoDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionTraceLogIndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author bsy
 * @since 2022-06-12
 */
@Api(tags = "执行器表达式追踪")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/executor/trace")
public class ExecutorTraceController {
    @Autowired
    private ExpressionTraceLogIndexService traceLogIndexService;

    @ApiOperation("查询追踪日志列表")
    @PostMapping("/list")
    public RestResult<Page<ExpressionTraceLogIndex>> findExecutorList(@RequestBody QueryExpressionTraceRequest queryRequest) {
        Page<ExpressionTraceLogIndex> page = traceLogIndexService.queryExpressionTraceLogList(queryRequest);
        return RestResult.ok(page);
    }

    @ApiOperation("查询单个追踪链路")
    @PostMapping("/info")
    public RestResult<ExpressionTraceInfoDTO> findExecutorList(@RequestParam("id") Long id) {
        final ExpressionTraceInfoDTO info = traceLogIndexService.getTraceInfoList(id);
        return RestResult.ok(info);
    }

    @ApiOperation("获取表达式样本参数")
    @PostMapping("/getExpressionSampleBody")
    public RestResult<ExpressionTraceLogIndex> getExpressionSampleBody(@RequestParam("expressionId") Long expressionId) {
        ExpressionTraceLogIndex logIndex = traceLogIndexService.getExpressionSampleBody(expressionId);
        return RestResult.ok(logIndex);
    }

}
