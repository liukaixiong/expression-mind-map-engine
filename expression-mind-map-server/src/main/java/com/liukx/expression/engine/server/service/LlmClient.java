package com.liukx.expression.engine.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.liukx.expression.engine.server.config.props.AiExpressionProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class LlmClient {

    private static final Logger log = LoggerFactory.getLogger(LlmClient.class);

    private final AiExpressionProperties properties;
    private final RestTemplate aiRestTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public LlmClient(AiExpressionProperties properties) {
        this.properties = properties;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());
        this.aiRestTemplate = new RestTemplate(factory);
    }

    /**
     * 聊天并返回完整响应（包含 reasoning_content）
     *
     * @return ChatResponse 包含 content 和 reasoningContent
     */
    public ChatResponse chatWithReasoning(String systemPrompt, List<Map<String, String>> messages) {
        if (!properties.isEnabled()) {
            throw new RuntimeException("AI 表达式生成功能未启用，请配置 expression.ai.enabled=true");
        }

        try {
            String requestBody = buildRequestBody(systemPrompt, messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + properties.getApiKey());

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            log.info("AI 请求：model={}, messagesCount={}", properties.getModel(), messages.size() + 1);

            ResponseEntity<String> response = aiRestTemplate.exchange(
                    properties.getApiUrl(),
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String body = response.getBody();
                log.info("AI 原始响应：{}", body.length() > 2000 ? body.substring(0, 2000) + "..." : body);

                JsonNode root = objectMapper.readTree(body);
                JsonNode choices = root.get("choices");
                if (choices != null && choices.isArray() && !choices.isEmpty()) {
                    JsonNode choice0 = choices.get(0);
                    String finishReason = choice0.has("finish_reason") ? choice0.get("finish_reason").asText() : "";
                    JsonNode messageNode = choice0.get("message");

                    String content = "";
                    String reasoningContent = null;

                    if (messageNode != null) {
                        if (messageNode.has("content") && !messageNode.get("content").isNull()) {
                            content = messageNode.get("content").asText();
                        }
                        if (messageNode.has("reasoning_content") && !messageNode.get("reasoning_content").isNull()) {
                            reasoningContent = messageNode.get("reasoning_content").asText();
                            log.info("AI 推理内容长度：{}", reasoningContent.length());
                        }
                    }

                    // 推理模型：content 为空时尝试从 reasoning_content 中提取表达式
                    if ((content == null || content.isEmpty()) && reasoningContent != null) {
                        log.warn("AI 推理模型 content 为空，finish_reason={}, reasoning_length={}", finishReason, reasoningContent.length());
                        if ("length".equals(finishReason)) {
                            throw new RuntimeException("AI 推理 token 耗尽 (finish_reason=length)，请增大 max_tokens 配置 (当前：" + properties.getMaxTokens() + ")");
                        }
                    }

                    if (content == null || content.isEmpty()) {
                        throw new RuntimeException("AI 返回内容为空，finish_reason=" + finishReason);
                    }

                    log.info("AI 响应：contentLength={}, finishReason={}", content.length(), finishReason);
                    return new ChatResponse(content, reasoningContent);
                }

                throw new RuntimeException("AI 响应中缺少 choices 字段，完整响应：" + body);
            }

            throw new RuntimeException("AI 服务返回异常：statusCode=" + response.getStatusCode() + ", body=" + response.getBody());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用 AI 服务失败", e);
            throw new RuntimeException("调用 AI 服务失败：" + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String systemPrompt, List<Map<String, String>> messages) throws Exception {
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", properties.getModel());
        requestBody.put("temperature", properties.getTemperature());
        requestBody.put("max_tokens", properties.getMaxTokens());
        ArrayNode messagesArray = requestBody.putArray("messages");
        ObjectNode systemMsg = messagesArray.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);

        for (Map<String, String> msg : messages) {
            ObjectNode msgNode = messagesArray.addObject();
            msgNode.put("role", msg.get("role"));
            msgNode.put("content", msg.get("content"));
        }

        return objectMapper.writeValueAsString(requestBody);
    }

    /**
     * 简单的响应对象，包含 content 和 reasoningContent
     */
    public static class ChatResponse {
        private final String content;
        private final String reasoningContent;

        public ChatResponse(String content, String reasoningContent) {
            this.content = content;
            this.reasoningContent = reasoningContent;
        }

        public String getContent() {
            return content;
        }

        public String getReasoningContent() {
            return reasoningContent;
        }
    }
}
