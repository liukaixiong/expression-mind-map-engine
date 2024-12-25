package com.liukx.expression.engine.server.controller;


import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.model.dto.request.AddLinkResultLogRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditLinkResultLogRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryLinkResultLogRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionLinkResultLogDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionLinkResultLogService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 成功回调日志表,记录执行完成的日志记录,成功回调日志表的压缩版 前端控制器
 * </p>
 *
 * @author bsy
 * @since 2022-06-15
 */
@Api(tags = "成功调度日志管理")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/log/success")
public class LinkResultLogController {
    @Autowired
    private ExpressionLinkResultLogService expressionLinkResultLogService;

    @ApiOperation("添加单个成功回调日志")
    @PostMapping("/addOne")
    public RestResult<ExpressionLinkResultLogDTO> addOne(@Validated @RequestBody AddLinkResultLogRequest addRequest) {
        return expressionLinkResultLogService.addOne(addRequest);
    }

    @ApiOperation("编辑单个成功回调日志")
    @PostMapping("/editOne")
    public RestResult<ExpressionLinkResultLogDTO> editOne(@RequestBody @Validated EditLinkResultLogRequest editRequest) {
        return expressionLinkResultLogService.updateOne(editRequest);
    }

    @ApiOperation("查询成功回调日志")
    @PostMapping("/findLinkResultLogList")
    public RestResult<List<ExpressionLinkResultLogDTO>> findExpressionList(@RequestBody QueryLinkResultLogRequest queryRequest) {
        return expressionLinkResultLogService.queryDtoList(queryRequest);
    }

    @ApiOperation("批量逻辑删除成功回调日志")
    @PostMapping("/batchDelete")
    public RestResult<?> batchDelete(@RequestBody @Validated DeleteByIdListRequest delRequest) {
        return expressionLinkResultLogService.logicDeleteByIdList(delRequest);
    }
}
