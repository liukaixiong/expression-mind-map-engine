package com.liukx.expression.engine.server.controller;


import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.model.dto.request.AddGlobalTraceLogRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditGlobalTraceLogRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryGlobalTraceLogRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionGlobalTraceLogDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.GlobalTraceLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 全局日志表,负责记录全局日志执行过程的日志记录,负责排查执行过程 前端控制器
 * </p>
 *
 * @author bsy
 * @since 2022-06-15
 */
@Api(tags = "全局日志管理")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/log/trace")
public class GlobalTraceLogController {
    @Autowired
    private GlobalTraceLogService expressionGlobalTraceLogService;

    @ApiOperation("添加单个全局日志")
    @PostMapping("/addOne")
    public RestResult<ExpressionGlobalTraceLogDTO> addOne(@RequestBody AddGlobalTraceLogRequest addRequest) {
        return expressionGlobalTraceLogService.addOne(addRequest);
    }

    @ApiOperation("编辑单个全局日志")
    @PostMapping("/editOne")
    public RestResult<ExpressionGlobalTraceLogDTO> editOne(@RequestBody EditGlobalTraceLogRequest editRequest) {
        return expressionGlobalTraceLogService.updateOne(editRequest);
    }

    @ApiOperation("查询全局日志")
    @PostMapping("/findGlobalTraceLogList")
    public RestResult<List<ExpressionGlobalTraceLogDTO>> findExpressionList(@RequestBody QueryGlobalTraceLogRequest queryRequest) {
        return expressionGlobalTraceLogService.queryDtoList(queryRequest);
    }

    @ApiOperation("批量逻辑删除全局日志")
    @PostMapping("/batchDelete")
    public RestResult<?> batchDelete(@RequestBody DeleteByIdListRequest delRequest) {
        return expressionGlobalTraceLogService.logicDeleteByIdList(delRequest);
    }
}
