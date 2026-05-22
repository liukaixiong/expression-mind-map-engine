package com.liukx.expression.engine.server.config.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@Data
@ConfigurationProperties(prefix = "spring.expression.server.ai")
@RefreshScope
public class AiExpressionProperties {
    /**
     * 是否启用
     */
    private boolean enabled = false;
    /**
     * API 地址
     */
    private String apiUrl = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    /**
     * API Key
     */
    private String apiKey = "";
    /**
     * 模型名称
     */
    private String model = "glm-5.1";
    /**
     * 最大 token 长度
     */
    private int maxTokens = 10240;
    /**
     * 采样温度
     */
    private double temperature = 0.3;
    /**
     * 连接超时时间
     */
    private int connectTimeout = 10000;
    /**
     * 读取超时时间
     */
    private int readTimeout = 120000;
    /**
     * 跟踪会话的采样次数
     */
    private int maxTraceSamples = 3;
    /**
     * 最大会话轮数
     */
    private int maxConversationTurns = 10;
    /**
     * 最大上下文补充轮次（第一轮用精简上下文，检测到 NEED_MORE_CONTEXT 后补充全量上下文重试）
     */
    private int maxContextRounds = 2;
}
