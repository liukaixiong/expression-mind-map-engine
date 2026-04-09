package com.liukx.expression.engine.client.debug;

import cn.hutool.core.convert.Convert;
import com.liukx.expression.engine.client.debug.dto.DebugRequest;
import com.liukx.expression.engine.client.debug.dto.DebugResponse;
import com.liukx.expression.engine.client.debug.service.IDebugTokenService;
import com.liukx.expression.engine.client.engine.ExpressionEnvContext;
import com.liukx.expression.engine.client.factory.ExpressionExecutorFactory;
import com.liukx.expression.engine.core.api.model.ApiResult;
import com.liukx.expression.engine.core.api.model.ExpressionBaseRequest;
import com.liukx.expression.engine.core.api.model.ExpressionContextResult;
import com.liukx.expression.engine.core.consts.ExpressionConstants;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Logger logger = LoggerFactory.getLogger(DebugController.class);
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
        ExpressionEnvContext expressionEnvContext = ExpressionEnvContext.of(request.getContext());
        final Object contextRequest = expressionEnvContext.getObjectValue("request");
        
        // 注入请求参数
        if (contextRequest != null) {
            final ExpressionBaseRequest baseRequest = Convert.convert(ExpressionBaseRequest.class, contextRequest);
            expressionEnvContext.addEnvClassInfo(baseRequest);
        }

        try {
            // 执行表达式
            ExpressionContextResult result = executorFactory.getExpressionService().execute(
                    request.getExpression(),
                    expressionEnvContext.getSourceMap()
            );

            response.setResult(result.getResult());
            response.setFunctionNameList(result.getFunctionNameList());
            response.setVariableNameList(result.getVariableNameList());
            response.setExecutionTime(System.currentTimeMillis() - startTime);

        } catch (Exception e) {
            response.setErrorMessage(e.getMessage());
            response.setExecutionTime(System.currentTimeMillis() - startTime);
            logger.warn("引擎调试接口执行失败: {}", e.getMessage());
            return ApiResult.error(500, "执行失败: " + e.getMessage());
        }

        return ApiResult.ok(response);
    }
}
