# AI 表达式生成功能

通过 LLM 将自然语言描述转化为 Aviator 表达式，支持多轮对话、自动注入函数/变量/样本上下文。

## 一、配置

### 完整配置

```yaml
spring:
  expression:
    server:
      ai:
        enabled: true                              # 启用（默认 false）
        api-url: https://open.bigmodel.cn/api/paas/v4/chat/completions
        api-key: "your-api-key"
        model: glm-5.1                             # 模型名称
        max-tokens: 10240                          # 最大生成 token
        temperature: 0.3                           # 采样温度
        connect-timeout: 10000                     # 连接超时 ms
        read-timeout: 120000                       # 读取超时 ms
        max-trace-samples: 3                       # 注入历史执行样本数
        max-conversation-turns: 10                 # 最大对话轮数
        max-context-rounds: 2                      # 最大上下文补充轮次（精简→全量自动重试）
```

### 切换其他模型

`LlmClient` 兼容 OpenAI Chat Completions API 格式，改 `api-url` / `api-key` / `model` 即可：

```yaml
# OpenAI
api-url: "https://api.openai.com/v1/chat/completions"
api-key: "sk-xxx"
model: "gpt-4o"

# Ollama 本地
api-url: "http://localhost:11434/v1/chat/completions"
api-key: "ollama"
model: "qwen2.5:7b"

# DeepSeek
api-url: "https://api.deepseek.com/v1/chat/completions"
api-key: "sk-xxx"
model: "deepseek-chat"
```

## 二、使用

### 接口

**POST** `/expression-engine/ai/generate`

### 请求

```json
{
  "executorId": 1,
  "expressionType": "condition",
  "serviceName": "order-service",
  "currentExpression": "amount > 100",
  "newUserMessage": "把条件改为金额大于500且用户是VIP",
  "conversationHistory": [
    { "role": "user", "content": "金额大于100" },
    { "role": "assistant", "content": "amount > 100" }
  ]
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `newUserMessage` | 是 | 自然语言描述 |
| `executorId` | 否 | 提供时注入历史执行样本，帮助 AI 理解入参结构 |
| `expressionType` | 否 | `action` / `condition` / `trigger` / `callback` |
| `currentExpression` | 否 | 当前表达式，AI 会在此基础上修改而非重写 |
| `serviceName` | 否 | 用于加载该服务的函数、变量和专属提示词 |
| `conversationHistory` | 否 | 多轮对话历史，`role` 为 `user` 或 `assistant` |

### 响应

```json
{
  "expression": "amount > 500 && userLevel == 'VIP'",
  "rawResponse": "amount > 500 && userLevel == 'VIP'",
  "reasoningContent": "用户要求修改条件...",
  "error": false,
  "errorMessage": null,
  "contextStats": {
    "functionCount": 15,
    "variableCount": 8,
    "sampleCount": 3,
    "estimatedTokens": 3200
  }
}
```

| 字段 | 说明 |
|------|------|
| `expression` | 生成的表达式（已去除代码块标记） |
| `rawResponse` | LLM 原始返回内容 |
| `reasoningContent` | 推理模型的思考过程 |
| `error` / `errorMessage` | 错误标识和信息 |
| `contextStats` | 本次注入的函数数、变量数、样本数、估算 token |

### 上下文自动注入

系统自动构建以下上下文到系统提示词中：

| 上下文 | 来源 | 触发条件 |
|--------|------|---------|
| 函数列表 | Redis（`ExpressionDocService` 维护） | `serviceName` 非空 |
| 变量列表 | Redis | `serviceName` 非空 |
| 执行样本 | 数据库 `expression_trace_log_index` | `executorId` 非空 |
| 服务专属提示词 | Redis Hash | `serviceName` 非空且对应模板存在 |

### 提示词模板占位符

| 占位符 | 内容 |
|--------|------|
| `{{functionsContext}}` | 函数列表 |
| `{{variablesContext}}` | 变量列表 |
| `{{traceContext}}` | 历史样本 |
| `{{servicePrompt}}` | 服务专属提示词 |

## 三、提示词管理

提示词模板存储在 **Redis Hash** 中，通过提示词管理页面进行可视化编辑，实时生效无需重启。

**管理页面**: `/template/ai-prompt-list.html` — 支持 CRUD 操作 + 实时 Markdown 预览。

### API 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/expression-engine/ai/prompt/list` | POST | 列出所有提示词 |
| `/expression-engine/ai/prompt/get?promptKey=xxx` | POST | 获取指定提示词 |
| `/expression-engine/ai/prompt/save` | POST | 保存/更新提示词 |
| `/expression-engine/ai/prompt/delete?promptKey=xxx` | POST | 删除提示词 |

### 存储结构

| Hash Key | Hash Field | 内容 |
|----------|------------|------|
| `{ENGINE_SERVER_ID}:ai_prompt` | `base` | 基础系统提示词 |
| `{ENGINE_SERVER_ID}:ai_prompt` | `{serviceName}` | 服务专属提示词片段 |

**基础提示词**定义了 Aviator 语法规则、公共变量、内置函数列表等通用知识。

**服务专属提示词**在基础模板的 `{{servicePrompt}}` 占位符处注入，可写入特定服务的业务规则、变量约束、表达式风格要求等。

## 四、核心流程

```
AiExpressionController  POST /expression-engine/ai/generate
  → AiExpressionService.generate()
    ├─ buildTraceContext()           从 DB 加载最近执行样本
    ├─ buildSystemPrompt()           模板占位符替换（基础上下文）
    ├─ buildExpandedSystemPrompt()   两阶段扩展（全量上下文）
    │   ├─ buildFunctionsContext()   从 Redis 加载函数文档
    │   └─ buildVariablesContext()   从 Redis 加载变量文档
    ├─ LlmClient.chatWithReasoning() 调用 LLM API
    └─ extractExpression()           清理响应提取表达式
```

### 两阶段上下文扩展

1. **第一阶段（精简上下文）**：仅注入执行样本和基础提示词，减少 token 消耗
2. **第二阶段（全量上下文）**：当 LLM 返回 `NEED_MORE_CONTEXT` 时，自动补充完整函数列表和变量列表重试
3. 最多重试 `max-context-rounds` 次（默认 2 次）

## 五、核心类

| 类 | 职责 |
|----|------|
| `AiExpressionController` | REST 入口，`POST /expression-engine/ai/generate` |
| `AiPromptController` | 提示词 CRUD 入口，`/expression-engine/ai/prompt/*` |
| `AiExpressionService` | 上下文构建、两阶段扩展、提示词组装、表达式提取 |
| `AiPromptService` | 提示词 Redis CRUD 操作 |
| `LlmClient` | HTTP 调用 LLM，OpenAI 兼容协议，解析 `reasoning_content` |
| `SystemPromptLoader` | 提示词加载接口（`loadBase()` + `loadServicePrompt()`） |
| `RedisPromptLoader` | 默认实现，从 Redis Hash 实时加载，每次请求直接读 Redis |
| `AiExpressionProperties` | 配置绑定，前缀 `spring.expression.server.ai` |

## 六、开发扩展

### 换 LLM 服务

只需改配置，`LlmClient` 基于 OpenAI Chat Completions API 格式，无需改代码。

### 换提示词来源

实现 `SystemPromptLoader` 接口并注册为 Spring Bean，默认的 `RedisPromptLoader` 会通过 `@ConditionalOnMissingBean` 自动跳过：

```java
@Component
public class MyPromptLoader implements SystemPromptLoader {

    @Override
    public String loadBase() {
        // 从自定义来源加载基础提示词
        return "你的基础提示词内容...";
    }

    @Override
    public String loadServicePrompt(String serviceName) {
        // 加载服务专属提示词，不存在返回 null
        return null;
    }
}
```

### 增加上下文

在 `AiExpressionService` 中新增 `build*Context()` 方法，并在 `buildSystemPrompt()` / `buildExpandedSystemPrompt()` 中加入对应占位符。
