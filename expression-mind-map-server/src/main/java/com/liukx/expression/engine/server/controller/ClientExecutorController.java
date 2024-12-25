package com.liukx.expression.engine.server.controller;

import com.liukx.expression.engine.core.api.model.ConfigDiscoverRequest;
import com.liukx.expression.engine.core.api.model.ExpressionConfigInfo;
import com.liukx.expression.engine.core.api.model.api.ExpressionExecutorResultDTO;
import com.liukx.expression.engine.core.api.model.api.GlobalExpressionDocInfo;
import com.liukx.expression.engine.core.consts.ExpressionConstants;
import com.liukx.expression.engine.server.exception.Throws;
import com.liukx.expression.engine.server.executor.ExpressionExecutor;
import com.liukx.expression.engine.server.model.dto.response.RestResult;
import com.liukx.expression.engine.server.service.ExpressionKeyService;
import com.liukx.expression.engine.server.service.ExpressionTraceLogIndexService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 远端节点提交执行器
 */
@Api(tags = "远端节点提交执行器")
@Validated
@RestController
@CrossOrigin(origins = "*")
@RequestMapping
public class ClientExecutorController {
    @Autowired
    private ExpressionExecutor expressionExecutor;
    @Autowired
    private ExpressionTraceLogIndexService traceService;
    @Autowired
    private ExpressionKeyService expressionKeyService;

    @ApiOperation("函数变量doc提交")
    @PostMapping(ExpressionConstants.SERVER_DOC_SUBMIT)
    public RestResult<Object> varFnDocSubmit(
            @RequestBody
            @Validated GlobalExpressionDocInfo docInfo) {
        CompletableFuture.runAsync(() -> expressionKeyService.refreshDocument(docInfo));
        return RestResult.ok("ok");
    }

    /**
     * 客户端获取远端的表达式配置，然后交由客户端自行执行
     *
     * @param baseRequest
     * @return
     */
    @ApiOperation("远端表达式配置发现")
    @PostMapping(ExpressionConstants.SERVER_CONFIG_DISCOVERY)
    public RestResult<Object> configDiscovery(
            @RequestBody
            @Validated ConfigDiscoverRequest baseRequest) {
        Throws.nullError(baseRequest.getServiceName(), "serviceName");
        Throws.nullError(baseRequest.getBusinessCode(), "businessCode");
        ExpressionConfigInfo businessConfigInfo = expressionExecutor.queryBusinessConfigInfo(baseRequest.getServiceName(), baseRequest.getBusinessCode(), baseRequest.getExecutorCode());
        return RestResult.ok(businessConfigInfo);
    }

    @ApiOperation("提交客户端追踪日志")
    @PostMapping(ExpressionConstants.SERVER_EXECUTOR_TRACE_SUBMIT_PATH)
    public RestResult<Object> traceLogSubmit(
            @RequestBody
            @Validated List<ExpressionExecutorResultDTO> request) {

        boolean result = traceService.addTraceLog(request);

        return RestResult.ok(result);
    }

}
