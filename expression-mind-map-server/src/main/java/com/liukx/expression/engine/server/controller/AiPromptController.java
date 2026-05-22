package com.liukx.expression.engine.server.controller;

import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.model.dto.request.AiPromptRequest;
import com.liukx.expression.engine.server.model.dto.response.AiPromptResponse;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.AiPromptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "AI提示词管理")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/ai/prompt")
public class AiPromptController {

    @Autowired
    private AiPromptService aiPromptService;

    @ApiOperation("查询全部提示词")
    @PostMapping("/list")
    public RestResult<List<AiPromptResponse>> list() {
        return RestResult.ok(aiPromptService.list());
    }

    @ApiOperation("查询单条提示词")
    @PostMapping("/get")
    public RestResult<AiPromptResponse> get(@RequestParam String promptKey) {
        return RestResult.ok(aiPromptService.get(promptKey));
    }

    @ApiOperation("新增/更新提示词")
    @PostMapping("/save")
    public RestResult<AiPromptResponse> save(@RequestBody @Validated AiPromptRequest request) {
        return RestResult.ok(aiPromptService.save(request));
    }

    @ApiOperation("删除提示词")
    @PostMapping("/delete")
    public RestResult<Boolean> delete(@RequestParam String promptKey) {
        return RestResult.ok(aiPromptService.delete(promptKey));
    }
}
