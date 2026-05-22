package com.liukx.expression.engine.server.model.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@ApiModel("AI 表达式生成响应")
public class AiExpressionResponse implements Serializable {

    @ApiModelProperty("生成的 Aviator 表达式")
    private String expression;

    @ApiModelProperty("AI 的原始回复内容")
    private String rawResponse;

    @ApiModelProperty("AI 的思考过程/推理内容")
    private String reasoningContent;

    @ApiModelProperty("是否错误")
    private boolean error;

    @ApiModelProperty("错误信息")
    private String errorMessage;

    @ApiModelProperty("上下文统计信息")
    private ContextStats contextStats;

    @Data
    @ApiModel("上下文统计")
    public static class ContextStats implements Serializable {
        @ApiModelProperty("传入的函数数量")
        private int functionCount;
        @ApiModelProperty("传入的变量数量")
        private int variableCount;
        @ApiModelProperty("传入的追踪样本数量")
        private int sampleCount;
        @ApiModelProperty("估算总 token 数")
        private int estimatedTokens;
        @ApiModelProperty("实际使用的上下文轮次")
        private int roundsUsed;
    }

    public static AiExpressionResponse success(String expression, String rawResponse, String reasoningContent) {
        AiExpressionResponse resp = new AiExpressionResponse();
        resp.setExpression(expression);
        resp.setRawResponse(rawResponse);
        resp.setReasoningContent(reasoningContent);
        resp.setError(false);
        return resp;
    }

    public static AiExpressionResponse error(String message) {
        AiExpressionResponse resp = new AiExpressionResponse();
        resp.setError(true);
        resp.setErrorMessage(message);
        resp.setRawResponse(message);
        return resp;
    }
}
