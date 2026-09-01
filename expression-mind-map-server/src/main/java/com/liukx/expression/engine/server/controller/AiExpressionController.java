package com.liukx.expression.engine.server.controller;

import com.liukx.expression.engine.server.constants.BaseConstants;
import com.liukx.expression.engine.server.model.dto.request.AiExpressionRequest;
import com.liukx.expression.engine.server.model.dto.response.AiExpressionResponse;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.AiExpressionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Api(tags = "AI表达式生成")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping(BaseConstants.BASE_PATH + "/ai")
public class AiExpressionController {

    private static final Logger log = LoggerFactory.getLogger(AiExpressionController.class);

    @Autowired
    private AiExpressionService aiExpressionService;

    @ApiOperation("AI生成表达式（支持多轮对话）")
    @PostMapping("/generate")
    public RestResult<AiExpressionResponse> generate(@RequestBody @Validated AiExpressionRequest request) {
        log.info("AI生成请求: executorId={}, expressionType={}, message={}, history={}",
                request.getExecutorId(), request.getExpressionType(),
                request.getNewUserMessage(),
                request.getConversationHistory() != null ? request.getConversationHistory().size() : 0);
        try {
            AiExpressionResponse response = aiExpressionService.generate(request);
            log.info("AI生成结果: error={}, expression={}, stats={}",
                    response.isError(),
                    response.getExpression(),
                    response.getContextStats());
            return RestResult.ok(response);
        } catch (Exception e) {
            log.error("AI生成失败", e);
            return RestResult.failed("AI生成失败: " + e.getMessage());
        }
    }
}
