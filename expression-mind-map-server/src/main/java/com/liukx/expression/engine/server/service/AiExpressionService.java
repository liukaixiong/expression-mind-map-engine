package com.liukx.expression.engine.server.service;

import com.liukx.expression.engine.client.enums.EnginCacheKeyEnums;
import com.liukx.expression.engine.server.components.ai.prompt.SystemPromptLoader;
import com.liukx.expression.engine.server.config.props.AiExpressionProperties;
import com.liukx.expression.engine.server.mapper.entity.ExpressionExecutorBaseInfo;
import com.liukx.expression.engine.server.mapper.entity.ExpressionTraceLogIndex;
import com.liukx.expression.engine.server.model.dto.request.AiExpressionRequest;
import com.liukx.expression.engine.server.model.dto.request.QueryExpressionTraceRequest;
import com.liukx.expression.engine.server.model.dto.response.AiExpressionResponse;
import com.liukx.expression.engine.server.model.dto.response.TraceLogPageResult;
import com.liukx.expression.engine.server.service.model.doc.ExpressionDocDto;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class AiExpressionService {

    private static final Logger log = LoggerFactory.getLogger(AiExpressionService.class);

    @Autowired
    private AiExpressionProperties properties;

    @Autowired
    private LlmClient llmClient;

    @Autowired
    private ExpressionExecutorConfigService executorConfigService;

    @Autowired
    private ExpressionTraceLogIndexService traceLogIndexService;

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    @Autowired
    private SystemPromptLoader promptLoader;

    public AiExpressionResponse generate(AiExpressionRequest request) {
        if (!properties.isEnabled()) {
            return AiExpressionResponse.error("AI 功能未启用，请配置 expression.ai.enabled=true");
        }

        AiExpressionResponse.ContextStats stats = new AiExpressionResponse.ContextStats();

        final ExpressionExecutorBaseInfo baseInfo = executorConfigService.getById(request.getExecutorId());
        request.setServiceName(baseInfo.getServiceName());

        // 检查本次会话是否已经触发过全量上下文扩展
        boolean contextExpanded = hasHistoryTriggeredContextExpansion(request.getConversationHistory());

        String traceContext = buildTraceContext(request, stats);

        // 如果已经扩展过，直接构建包含全量函数/变量的 system prompt
        String systemPrompt;
        if (contextExpanded) {
            String functionsContext = buildFunctionsContext(request, stats);
            String variablesContext = buildVariablesContext(request, stats);
            systemPrompt = buildExpandedSystemPrompt(baseInfo, traceContext, functionsContext, variablesContext);
            log.debug("会话已触发过全量扩展，直接使用全量提示词");
        } else {
            systemPrompt = buildSystemPrompt(baseInfo, traceContext);
        }

        log.debug("系统提示词长度：{}", systemPrompt.length());
        stats.setEstimatedTokens(systemPrompt.length() / 2);

        List<Map<String, String>> messages = new ArrayList<>();

        if (request.getConversationHistory() != null) {
            for (AiExpressionRequest.ChatMessage msg : request.getConversationHistory()) {
                Map<String, String> message = new HashMap<>();
                message.put("role", msg.getRole());
                message.put("content", msg.getContent());
                messages.add(message);
            }
        }

        String userContent = request.getNewUserMessage();
        if (StringUtils.isNotBlank(request.getCurrentExpression())) {
            userContent += "\n\n当前表达式：" + request.getCurrentExpression();
        }
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userContent);
        messages.add(userMsg);

        LlmClient.ChatResponse chatResponse = llmClient.chatWithReasoning(systemPrompt, messages);
        String rawResponse = chatResponse.getContent();
        String reasoningContent = chatResponse.getReasoningContent();
        int roundsUsed = 1;

        // 仅在尚未扩展过时才检测 NEED_MORE_CONTEXT（避免重复扩展）
        if (!contextExpanded && rawResponse != null && rawResponse.contains(NEED_MORE_CONTEXT_PREFIX)
                && properties.getMaxContextRounds() > 1) {
            log.info("第一轮返回 NEED_MORE_CONTEXT，开始加载全量函数/变量进行第二轮补充");

            String functionsContext = buildFunctionsContext(request, stats);
            String variablesContext = buildVariablesContext(request, stats);

            // 追加第一轮 AI 的回复到对话历史
            Map<String, String> assistantMsg = new HashMap<>();
            assistantMsg.put("role", "assistant");
            assistantMsg.put("content", rawResponse);
            messages.add(assistantMsg);

            // 追加全量上下文补充消息
            StringBuilder contextBuilder = new StringBuilder();
            contextBuilder.append("以下是补充的全量函数和变量上下文：\n");
            contextBuilder.append("## 全量可用函数（格式：示例 - 说明）\n").append(functionsContext).append("\n");
            contextBuilder.append("## 全量可用变量（格式：变量名 - 说明）\n").append(variablesContext).append("\n");
            contextBuilder.append("请基于以上完整的函数和变量信息，重新回答。");
            log.debug("第二轮提示词:{}", contextBuilder);
            Map<String, String> contextMsg = new HashMap<>();
            contextMsg.put("role", "user");
            contextMsg.put("content", contextBuilder.toString());
            messages.add(contextMsg);

            LlmClient.ChatResponse secondResponse = llmClient.chatWithReasoning(systemPrompt, messages);
            rawResponse = secondResponse.getContent();
            reasoningContent = secondResponse.getReasoningContent();
            roundsUsed = 2;

            log.info("第二轮完成，responseLength={}", rawResponse != null ? rawResponse.length() : 0);
        }

        String expression = extractExpression(rawResponse);

        AiExpressionResponse response = AiExpressionResponse.success(expression, rawResponse, reasoningContent);
        log.debug("llm : {}", response);
        stats.setRoundsUsed(roundsUsed);
        stats.setEstimatedTokens(stats.getEstimatedTokens() + (rawResponse != null ? rawResponse.length() / 2 : 0));
        response.setContextStats(stats);
        return response;
    }

    /**
     * 从 Redis 中加载所有服务的函数文档，按 serviceName 分组遍历 Hash
     */
    private List<ExpressionDocDto> loadAllDocsFromRedis(String serviceName) {
        List<ExpressionDocDto> allDocs = new ArrayList<>();
        final String serviceCacheKey = EnginCacheKeyEnums.EXPRESSION_DOC_KEY.generateKey(serviceName);
        Set<Object> keys = redisTemplate.keys(serviceCacheKey);
        if (keys == null || keys.isEmpty()) {
            return allDocs;
        }
        for (Object key : keys) {
            Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
            if (entries != null) {
                for (Object value : entries.values()) {
                    if (value instanceof ExpressionDocDto) {
                        allDocs.add((ExpressionDocDto) value);
                    }
                }
            }
        }
        return allDocs;
    }

    private String buildFunctionsContext(AiExpressionRequest request, AiExpressionResponse.ContextStats stats) {
        try {
            List<ExpressionDocDto> allDocs = loadAllDocsFromRedis(request.getServiceName());
            List<ExpressionDocDto> functions = allDocs.stream()
                    .filter(doc -> "fn".equals(doc.getType()))
                    .collect(Collectors.toList());

            if (functions.isEmpty()) {
                stats.setFunctionCount(0);
                return "暂无可用函数";
            }

            stats.setFunctionCount(functions.size());

            StringBuilder sb = new StringBuilder();
            for (ExpressionDocDto func : functions) {
                sb.append(func.getExample() != null ? func.getExample() : func.getName() + "()");
                sb.append(" - ").append(func.getDescribe() != null ? func.getDescribe() : "");
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("获取函数列表失败", e);
            stats.setFunctionCount(0);
            return "获取函数列表失败";
        }
    }

    private String buildVariablesContext(AiExpressionRequest request, AiExpressionResponse.ContextStats stats) {
        try {
            List<ExpressionDocDto> allDocs = loadAllDocsFromRedis(request.getServiceName());
            List<ExpressionDocDto> variables = allDocs.stream()
                    .filter(doc -> "var".equals(doc.getType()))
                    .collect(Collectors.toList());

            if (variables.isEmpty()) {
                stats.setVariableCount(0);
                return "暂无可用变量";
            }

            stats.setVariableCount(variables.size());

            StringBuilder sb = new StringBuilder();
            for (ExpressionDocDto var : variables) {
                sb.append(var.getName() != null ? var.getName() : "");
                sb.append(" - ").append(var.getDescribe() != null ? var.getDescribe() : "");
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("获取变量列表失败", e);
            stats.setVariableCount(0);
            return "获取变量列表失败";
        }
    }

    private String buildTraceContext(AiExpressionRequest request, AiExpressionResponse.ContextStats stats) {
        if (request.getExecutorId() == null) {
            stats.setSampleCount(0);
            return "";
        }

        try {
            QueryExpressionTraceRequest query = new QueryExpressionTraceRequest();
            query.setExecutorId(request.getExecutorId());
            query.setPageNum(1);
            query.setPageSize(properties.getMaxTraceSamples());
            query.setOrderByColumn("created");
            query.setIsAsc("desc");

            TraceLogPageResult<ExpressionTraceLogIndex> page = traceLogIndexService.queryExpressionTraceLogList(query);
            List<ExpressionTraceLogIndex> traces = page != null ? page.getRecords() : Collections.emptyList();

            if (traces.isEmpty()) {
                stats.setSampleCount(0);
                return "暂无追踪样本数据";
            }

            stats.setSampleCount(traces.size());

            StringBuilder sb = new StringBuilder();
            sb.append("以下是最近 ").append(traces.size()).append(" 条执行记录的入参样本:\n");
            for (int i = 0; i < traces.size(); i++) {
                ExpressionTraceLogIndex trace = traces.get(i);
                sb.append("样本").append(i + 1).append(": ");
                sb.append(trace.getEnvBody() != null ? trace.getEnvBody() : "{}");
                sb.append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            log.warn("获取追踪样本失败", e);
            stats.setSampleCount(0);
            return "";
        }
    }

    private String buildSystemPrompt(ExpressionExecutorBaseInfo baseInfo,
                                     String traceContext) {
        String template = promptLoader.loadBase();
        if (template == null) {
            template = buildDefaultSystemPrompt();
        }

        String traceSection = "";
        if (StringUtils.isNotBlank(traceContext)) {
            traceSection = "## 追踪样本数据（参考入参结构）\n" + traceContext + "\n";
        }

        String servicePrompt = "";
        String serviceContent = promptLoader.loadServicePrompt(baseInfo.getServiceName());
        if (serviceContent != null) {
            servicePrompt = serviceContent;
        }

        return template
                .replace("{{traceContext}}", traceSection)
                .replace("{{servicePrompt}}", servicePrompt);
    }

    /**
     * 构建包含全量函数/变量的 system prompt（用于已触发过扩展的会话）
     */
    private String buildExpandedSystemPrompt(ExpressionExecutorBaseInfo baseInfo,
                                             String traceContext,
                                             String functionsContext,
                                             String variablesContext) {
        String template = promptLoader.loadBase();
        if (template == null) {
            template = buildDefaultSystemPrompt();
        }

        String traceSection = "";
        if (StringUtils.isNotBlank(traceContext)) {
            traceSection = "## 追踪样本数据（参考入参结构）\n" + traceContext + "\n";
        }

        String servicePrompt = "";
        String serviceContent = promptLoader.loadServicePrompt(baseInfo.getServiceName());
        if (serviceContent != null) {
            servicePrompt = serviceContent;
        }

        // 在 servicePrompt 之后插入全量函数/变量上下文
        String fullContextSection = "\n## 全量可用函数（格式：示例 - 说明）\n" + functionsContext
                + "\n## 全量可用变量（格式：变量名 - 说明）\n" + variablesContext + "\n";

        return template
                .replace("{{traceContext}}", traceSection)
                .replace("{{servicePrompt}}", servicePrompt + fullContextSection);
    }

    /**
     * 扫描会话历史，判断是否曾经触发过全量上下文扩展
     */
    private boolean hasHistoryTriggeredContextExpansion(List<AiExpressionRequest.ChatMessage> conversationHistory) {
        if (conversationHistory == null || conversationHistory.isEmpty()) {
            return false;
        }
        for (AiExpressionRequest.ChatMessage msg : conversationHistory) {
            if ("assistant".equals(msg.getRole()) && msg.getContent() != null
                    && msg.getContent().contains(NEED_MORE_CONTEXT_PREFIX)) {
                return true;
            }
        }
        return false;
    }

    private static final String NEED_MORE_CONTEXT_PREFIX = "NEED_MORE_CONTEXT";

    private String buildDefaultSystemPrompt() {
        return "你是一个 Aviator 表达式助手，帮助用户编写、理解、优化和调试 Aviator 表达式。\n\n"
                + "## 可用函数（格式：示例 - 说明）\n{{functionsContext}}\n"
                + "## 可用变量（格式：变量名 - 说明）\n{{variablesContext}}\n"
                + "{{traceContext}}\n"
                + "## 输出格式\n"
                + "- 生成/修改表达式时：简要说明逻辑，表达式用 ```aviator 代码块包裹\n"
                + "- 解释表达式时：逐行解释含义，用列表和表格说明\n"
                + "- 优化/调试时：指出问题并给出代码块包裹的修正表达式\n"
                + "## 通用规则\n"
                + "- 参数值不确定时用'待补充'作为占位符\n"
                + "- 回复使用中文\n"
                + "- 严禁编造不存在的函数或变量。如果提示词中列出的函数和变量不足以完成用户需求，"
                + "在回复末尾单独一行输出：NEED_MORE_CONTEXT: 说明缺少哪些函数或变量信息\n";
    }

    private String extractExpression(String rawResponse) {
        if (rawResponse == null) {
            return "";
        }
        // 优先提取 ```aviator 代码块
        java.util.regex.Matcher aviatorMatcher = java.util.regex.Pattern.compile(
                "```aviator\\s*\\n([\\s\\S]*?)```", java.util.regex.Pattern.MULTILINE
        ).matcher(rawResponse);
        if (aviatorMatcher.find()) {
            return aviatorMatcher.group(1).trim();
        }

        // 其次提取通用 ``` 代码块（排除 ```aviator 已匹配的）
        java.util.regex.Matcher codeMatcher = java.util.regex.Pattern.compile(
                "```(?:\\w*)\\s*\\n([\\s\\S]*?)```", java.util.regex.Pattern.MULTILINE
        ).matcher(rawResponse);
        if (codeMatcher.find()) {
            return codeMatcher.group(1).trim();
        }

        // 没有代码块，说明是非表达式类回复（解释、建议等）
        return "";
    }
}
