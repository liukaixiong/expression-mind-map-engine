package com.liukx.expression.engine.server.controller;


import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionVariableRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionVariableDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionVariableConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 表达式引擎通用变量配置表 前端控制器
 * </p>
 *
 * @author bsy
 * @since 2022-06-08
 */
@Api(tags = "变量管理")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/variable")
public class VariableConfigController {
    @Autowired
    private ExpressionVariableConfigService variableConfigService;

    @ApiOperation("查询注册变量")
    @PostMapping("/findVar")
    public RestResult<List<ExpressionVariableDTO>> findFuncList(@RequestBody QueryExpressionVariableRequest queryRequest) {
        return variableConfigService.queryExpressionVariable(queryRequest);
    }

}
