package com.liukx.expression.engine.server.controller;


import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionFunctionRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionFunctionDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionFunctionConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 函数配置表 前端控制器
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
@Api(tags = "函数管理")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/function")
@Validated
public class FunctionConfigController {
    @Autowired
    private ExpressionFunctionConfigService functionConfigService;

//    @ApiOperation("添加单个函数")
//    @PostMapping("/addOne")
//    public RestResult<ExpressionFunctionDTO> addOne(@Valid @RequestBody AddExpressionFunctionRequest addExpressionFunctionRequest) {
//        return functionConfigService.addExpressionFunction(addExpressionFunctionRequest);
//    }
//
//    @ApiOperation("编辑单个函数")
//    @PostMapping("/editOne")
//    public RestResult<ExpressionFunctionDTO> editOne(@RequestBody @Valid EditExpressionFunctionRequest editRequest) {
//        return functionConfigService.editExpressionFunction(editRequest);
//    }

    @ApiOperation("查询函数")
    @PostMapping("/findFunc")
    public RestResult<List<ExpressionFunctionDTO>> findFuncList(@RequestBody QueryExpressionFunctionRequest queryRequest) {
        return functionConfigService.queryExpressionFunction(queryRequest);
    }
//
//    @ApiOperation("批量逻辑删除函数")
//    @PostMapping("/batchDelete")
//    public RestResult<?> batchDelete(@RequestBody @Valid DeleteByIdListRequest delRequest) {
//        return functionConfigService.batchDeleteByIdList(delRequest);
//    }
}
