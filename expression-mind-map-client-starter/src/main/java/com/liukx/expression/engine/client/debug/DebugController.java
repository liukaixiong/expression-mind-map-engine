package com.liukx.expression.engine.client.debug;

import com.liukx.expression.engine.client.debug.dto.DebugRequest;
import com.liukx.expression.engine.client.debug.dto.DebugResponse;
import com.liukx.expression.engine.client.debug.service.IDebugTokenService;
import com.liukx.expression.engine.client.factory.ExpressionExecutorFactory;
import com.liukx.expression.engine.core.api.model.ApiResult;
import com.liukx.expression.engine.core.api.model.ExpressionContextResult;
import com.liukx.expression.engine.core.consts.ExpressionConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端远程调试控制器
 *
 * @author liukaixiong
 */
@Api(tags = "客户端远程调试")
@RestController
@RequestMapping
public class DebugController {

    @Autowired
    private IDebugTokenService debugTokenService;

    @Autowired
    private ExpressionExecutorFactory executorFactory;

    /**
     * 远程调试表达式执行
     *
     * @param request 调试请求
     * @return 执行结果
     */
    @PostMapping(ExpressionConstants.CLIENT_DEBUG_EXECUTOR)
    @ApiOperation("远程调试表达式执行")
    public ApiResult<DebugResponse> debug(@RequestBody @Validated DebugRequest request) {
        // Token校验
        if (!debugTokenService.checkDebugToken(request.getToken())) {
            return ApiResult.error(401, "验证失败");
        }

        long startTime = System.currentTimeMillis();
        DebugResponse response = new DebugResponse();

        try {
            // 执行表达式
            ExpressionContextResult result = executorFactory.getExpressionService().execute(
                    request.getExpression(),
                    request.getContext()
            );

            response.setResult(result.getResult());
            response.setFunctionNameList(result.getFunctionNameList());
            response.setVariableNameList(result.getVariableNameList());
            response.setExecutionTime(System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            response.setErrorMessage(e.getMessage());
            response.setExecutionTime(System.currentTimeMillis() - startTime);
            return ApiResult.error(500, "执行失败: " + e.getMessage());
        }

        return ApiResult.ok(response);
    }
}
