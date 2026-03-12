package com.liukx.expression.engine.server.controller;


import cn.hutool.core.bean.BeanUtil;
import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorInfoConfig;
import com.liukx.expression.engine.server.mapper.entity.ExpressionHistoryVersion;
import com.liukx.expression.engine.server.model.dto.request.*;
import com.liukx.expression.engine.server.model.dto.response.ExpressionExecutorDetailConfigDTO;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionConfigService;
import com.liukx.expression.engine.server.service.ExpressionHistoryVersionService;
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

    @Autowired
    private ExpressionHistoryVersionService expressionHistoryVersionService;

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
        ExpressionExecutorInfoConfig config = new ExpressionExecutorInfoConfig();
        config.setId(editRequest.getId());
        config.setParentId(editRequest.getParentId());
        return RestResult.ok(expressionConfigService.updateById(config));
    }

    @ApiOperation("复制节点")
    @PostMapping("/copyNode")
    public RestResult<Boolean> copyNode(@RequestBody @Validated EditExpressionConfigRequest editRequest) {
        ExpressionExecutorInfoConfig config = new ExpressionExecutorInfoConfig();
        config.setId(editRequest.getId());
        config.setParentId(editRequest.getParentId());
        return RestResult.ok(expressionConfigService.copyNode(config));
    }

    @ApiOperation("导入节点")
    @PostMapping("/importNode")
    public RestResult<Boolean> pasteNode(@RequestBody @Validated PasteExpressionConfigRequest pasteExpressionConfigRequest) {
        return RestResult.ok(expressionConfigService.importExpressionNode(pasteExpressionConfigRequest));
    }

    @ApiOperation("查询表达式")
    @PostMapping("/findExpressionList")
    public RestResult<List<ExpressionExecutorDetailConfigDTO>> findExpressionList(@RequestBody QueryExpressionConfigRequest queryRequest) {
        return expressionConfigService.queryExpression(queryRequest);
    }

    @ApiOperation("查询单个表达式")
    @PostMapping("/findExpressionInfo")
    public RestResult<ExpressionExecutorDetailConfigDTO> findExpressionInfo(@RequestParam("id") Long id) {
        final ExpressionExecutorInfoConfig config = expressionConfigService.getById(id);
        ExpressionExecutorDetailConfigDTO expressionExecutorDetailConfigDTO = new ExpressionExecutorDetailConfigDTO();
        BeanUtil.copyProperties(config, expressionExecutorDetailConfigDTO);
        return RestResult.ok(expressionExecutorDetailConfigDTO);
    }

    @ApiOperation("批量逻辑删除表达式")
    @PostMapping("/batchDelete")
    public RestResult<?> batchDelete(@RequestBody @Validated DeleteByIdListRequest delRequest) {
        return expressionConfigService.batchDeleteByIdList(delRequest);
    }

    @ApiOperation("获取表达式历史版本列表")
    @PostMapping("/history/list")
    public RestResult<List<ExpressionHistoryVersion>> getExpressionHistory(@RequestParam("expressionId") Long expressionId) {
        List<ExpressionHistoryVersion> history = expressionHistoryVersionService.getHistoryByExpressionId(expressionId);
        return RestResult.ok(history);
    }

    @ApiOperation("获取历史版本详情")
    @PostMapping("/history/detail")
    public RestResult<ExpressionHistoryVersion> getHistoryDetail(@RequestParam("expressionId") Long expressionId,
                                                                 @RequestParam("versionNo") Integer versionNo) {
        ExpressionHistoryVersion history = expressionHistoryVersionService.getHistoryByVersion(expressionId, versionNo);
        return RestResult.ok(history);
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
