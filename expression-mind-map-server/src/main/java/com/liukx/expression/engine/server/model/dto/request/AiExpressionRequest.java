package com.liukx.expression.engine.server.model.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel("AI表达式生成请求")
public class AiExpressionRequest implements Serializable {

    @ApiModelProperty("执行器ID")
    private Long executorId;

    @ApiModelProperty("表达式类型: action/condition/trigger/callback")
    private String expressionType;

    @ApiModelProperty("编辑器中当前已有的表达式内容")
    private String currentExpression;

    @ApiModelProperty("对话历史")
    private List<ChatMessage> conversationHistory;

    @ApiModelProperty("服务名称，用于加载该服务的专属提示词")
    private String serviceName;

    @ApiModelProperty("用户新输入的自然语言描述")
    private String newUserMessage;

    @Data
    @ApiModel("对话消息")
    public static class ChatMessage implements Serializable {
        @ApiModelProperty("角色: user/assistant")
        private String role;
        @ApiModelProperty("消息内容")
        private String content;
    }
}
