package com.liukx.expression.engine.server.controller;


import cn.hutool.core.bean.BeanUtil;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorDetailConfig;
import com.liukx.expression.engine.server.model.dto.request.AddExpressionConfigRequest;
import com.liukx.expression.engine.server.model.dto.request.DeleteByIdListRequest;
import com.liukx.expression.engine.server.model.dto.request.EditExpressionConfigRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionConfigRequest;
import com.liukx.expression.engine.server.model.dto.response.ExpressionExecutorDetailConfigDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionConfigService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 表达式配置 前端控制器
 * </p>
 *
 * @author bsy
 * @since 2022-06-12
 */
@Api(tags = "执行器表达式管理")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/executor/expression")
public class ExecutorExpressionController {
    @Autowired
    private ExpressionConfigService expressionConfigService;

    @ApiOperation("添加单个表达式")
    @PostMapping("/addOne")
    public RestResult<ExpressionExecutorDetailConfigDTO> addOne(@Validated @RequestBody AddExpressionConfigRequest addRequest) {
        return expressionConfigService.addExpression(addRequest);
    }

    @ApiOperation("编辑单个表达式")
    @PostMapping("/editOne")
    public RestResult<ExpressionExecutorDetailConfigDTO> editOne(@RequestBody @Validated EditExpressionConfigRequest editRequest) {
        return expressionConfigService.editExpression(editRequest);
    }

    @ApiOperation("修改父子关系")
    @PostMapping("/editParentId")
    public RestResult<Boolean> editParentId(@RequestBody @Validated EditExpressionConfigRequest editRequest) {
        ExpressionExecutorDetailConfig config = new ExpressionExecutorDetailConfig();
        config.setId(editRequest.getId());
        config.setParentId(editRequest.getParentId());
        return RestResult.ok(expressionConfigService.updateById(config));
    }

    @ApiOperation("复制节点")
    @PostMapping("/copyNode")
    public RestResult<Boolean> copyNode(@RequestBody @Validated EditExpressionConfigRequest editRequest) {
        ExpressionExecutorDetailConfig config = new ExpressionExecutorDetailConfig();
        config.setId(editRequest.getId());
        config.setParentId(editRequest.getParentId());
        return RestResult.ok(expressionConfigService.copyNode(config));
    }

    @ApiOperation("查询表达式")
    @PostMapping("/findExpressionList")
    public RestResult<List<ExpressionExecutorDetailConfigDTO>> findExpressionList(@RequestBody QueryExpressionConfigRequest queryRequest) {
        return expressionConfigService.queryExpression(queryRequest);
    }

    @ApiOperation("查询单个表达式")
    @PostMapping("/findExpressionInfo")
    public RestResult<ExpressionExecutorDetailConfigDTO> findExpressionInfo(@RequestParam("id") Long id) {
        final ExpressionExecutorDetailConfig config = expressionConfigService.getById(id);
        ExpressionExecutorDetailConfigDTO expressionExecutorDetailConfigDTO = new ExpressionExecutorDetailConfigDTO();
        BeanUtil.copyProperties(config, expressionExecutorDetailConfigDTO);
        return RestResult.ok(expressionExecutorDetailConfigDTO);
    }

    @ApiOperation("批量逻辑删除表达式")
    @PostMapping("/batchDelete")
    public RestResult<?> batchDelete(@RequestBody @Validated DeleteByIdListRequest delRequest) {
        return expressionConfigService.batchDeleteByIdList(delRequest);
    }



//    @ApiOperation("表达式翻译")
//    @PostMapping("/translate")
//    public RestResult<TranslateResult> translate(@RequestBody ExpressionApiModel apiModel) {
//        ExpressionService expressionService = executorFactory.getExpressionService();
//        TranslateResult translate = expressionService.translate(apiModel.getText());
//        return RestResult.ok(translate);
//    }
//
//    @ApiOperation("表达式验证")
//    @PostMapping("/validator")
//    public RestResult<ValidatorResult> validator(@RequestBody ExpressionApiModel apiModel) {
//        ExpressionService expressionService = executorFactory.getExpressionService();
//        ValidatorResult validator = expressionService.validator(apiModel.getText());
//        return RestResult.ok(validator);
//    }
}
