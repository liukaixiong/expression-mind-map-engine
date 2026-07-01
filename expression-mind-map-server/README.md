# 表达式规则引擎服务端

整体服务体系：`SpringBoot + Mybatis plus + Mysql + Redis `，都是常用组件，逻辑简单，二开不费力。

## 源码启动

1. 编译项目

- jdk8

> mvn clean install -Pjdk8 -Djava.version=8
>
> 或者去主pom文件里面将jdk8相关的注释解开，把jdk17的注释掉

- jdk 17(默认)

> mvn clean install

2. 初始化表结构SQL : `script/expression_mysql.sql`
3. 配置文件

- 非nacos的话需要指定Mysql和Redis的地址，可以在启动参数中添加 `engine.xxx`部分，或者自行补全`application.yml`内的engine相关的参数

- nacos则参考`script/expression-mind-map-server.yml` ，参考`bootstrap.yml`；

  > -Dapp.nacos.enable=true -Dapp.nacos.ip=127.0.0.1:8848 

4. 启动类: `BootApplication`

## 案例使用

⚙️[服务端启动](../expression-mind-map-server/README.md)

1、下载代码

2、找到: `com.liukx.expression.engine.ClientServer#executorDemoExample`

```java
/**
     * 演示示例:
     * 1、启动服务端【注意redis要连接上喔~具体配置=> src/test/resources/application.yml】
     * 2、<a href="http://localhost:20888/template/executor-list.html">进入页面</a>
     * 3、导入规则：demo_example.json,以及下面的所有.json的规则都是简单的案例配置
     * 4、执行该用例
     * 5、<a href="http://localhost:20888/template/trace-list.html">查看追踪结果</a>
     *
     * @throws Exception
     */
@Test
public void executorDemoExample() throws Exception {
    // 直接去代码中看吧...
}
```

## 服务端拓展点

### 1、配置类

#### 登录鉴权

```yaml
spring:
  expression:
    server:
      enable-login: true  # 是否开启登录
      username: admin     # 单账号用户名（与 users 并存，见下）
      password: admin@123 # 单账号密码
      # token 签名密钥，缺省时回退到 apiKey，再缺省回退内置默认值；生产环境务必显式配置
      token-secret: change-me-in-production
      # 多用户列表（可选）。配置后可与单账号并存：登录时优先匹配 users，未命中再回退单账号
      users:
        - username: alice
          password: alice@123
          userId: u_alice   # 可选，缺省时用 username 作为身份标识
        - username: bob
          password: bob@123
          userId: u_bob
```

- **单账号模式**：仅配置 `username`/`password`，所有操作归属该账号。
- **多用户模式**：配置 `users` 列表，支持多人登录，操作记录（创建人/更新人）按 `userId` 区分操作人。
- **并存模式**：同时配置 `users` 和单账号时，两者都可用——登录优先匹配 `users`，未命中再校验单账号。便于在保留管理员账号的同时开放多人协作。
- **Token**：默认实现采用无状态签名格式 `{userId}.{expireAt}.{sign}`（`sign = md5(userId + expireAt + tokenSecret)`），支持多用户反解析、防伪造、可过期（默认 1 天，与登录 Cookie 过期一致）。更换 `token-secret` 或删除用户后，已签发的旧 token 会失效，需重新登录。

> ⚠️ YAML 缩进：`users` 列表中每个项的 `password`/`userId` 必须与同项的 `username` 对齐（`- ` 占两格，字段从第 3 列起）。缩进错位会导致字段解析为 null、登录静默失败。配置项 key 大小写不敏感（`Password`/`password` 均可）。

#### 登录过期回跳

登录态过期后会自动跳转登录页，并在登录成功后**回到过期前的页面**（而非固定回首页），规则配置页等带参数的页面也能正确恢复：

- 页面请求过期 → 由 `LoginHandler` 重定向到登录页，并携带原始地址作为 `redirect` 参数；
- Ajax 请求过期（返回 401）→ 由前端 `Utils.js` 统一拦截，跳转带 `redirect` 的登录页；
- 登录成功 → 校验 `redirect` 为站内相对路径后回跳，防开放重定向。

> 本地 / 测试环境若使用 HTTP，登录 Cookie 的 `Secure` 标志会自动跟随请求协议（仅 HTTPS 下启用），避免 HTTP 下浏览器丢弃 Cookie 导致无法登录。

### 2、关键类

| 名称                         | 作用                         | 默认实现类                       | 默认实现作用                                                 |
| ---------------------------- | ---------------------------- | -------------------------------- | ------------------------------------------------------------ |
| IExpressionLoginService      | 登录鉴权                     | ExpressionLoginServiceImpl       | 从配置校验用户名密码。优先匹配 `users` 列表，未命中回退单账号 `username`/`password`，支持多用户并存 |
| IExpressionTokenService      | token生成器                  | ExpressionMd5TokenServiceImpl    | 无状态签名 token `{userId}.{expireAt}.{sign}`，可反解析出操作人、防伪造、可过期。可替换为 JWT 等实现 |
| IdentityExtractor            | 身份提取策略                 | CookieIdentityExtractor / HeaderIdentityExtractor / ApiKeyIdentityExtractor | 按优先级从 Cookie（控制台登录）、Header、API Key 中提取用户身份，支持扩展自定义来源 |
| AutoFillMetaObjectHandler    | MyBatis Plus 自动填充        | AutoFillMetaObjectHandler        | 插入/更新时自动填充 `created`/`updated`/`creator`/`updater`/`createBy`/`updateBy`，操作人取自当前登录用户上下文 |
| TraceLogStorageService       | 追踪日志存储                 | DefaultMysqlTraceLogStorageService | 默认存储到 MySQL（含按月分表），可替换为 ES、ClickHouse 等  |
| ClientExecutorController     | 客户端与服务端交互的路由入口 |                                  |                                                              |
| ExecutorManagerController    | 执行器增删改入口             |                                  |                                                              |
| ExecutorExpressionController | 表达式增删改入口             |                                  |                                                              |
| ExecutorTraceController      | 执行器表达式的追踪入口       |                                  |                                                              |
| ComponentsController         | 执行器导入导出入口           |                                  |                                                              |

### 3、追踪日志存储扩展点（TraceLogStorageService）

默认情况下追踪日志存储在 MySQL 中（`expression_trace_log_index` + `expression_trace_log_info`），并通过 `MysqlTableManager` 实现按月分表。
如果日志量大或需要更强大的检索能力，可以实现 `TraceLogStorageService` 接口，将日志存储到 ES、ClickHouse 等引擎。

#### 接口方法

| 方法签名 | 说明 |
| --- | --- |
| `saveTraceLog(ExpressionExecutorResultDTO)` | 保存一条追踪日志（索引 + 明细） |
| `queryTraceLogList(QueryExpressionTraceRequest)` → `TraceLogPageResult` | 分页查询追踪日志列表 |
| `getTraceInfo(Long id)` → `ExpressionTraceInfoDTO` | 查询追踪详情（索引 + 明细） |
| `getExpressionSampleBody(Long expressionId)` → `ExpressionTraceLogIndex` | 获取表达式最近一次成功执行的样本请求体 |
| `getInfoListByTraceLogId(Long traceLogId)` → `List<ExpressionTraceLogInfo>` | 查询追踪日志明细列表 |
| `hasRecentlySuccessLog(Long expressionId, Date startDate)` → `boolean` | 判断表达式在指定日期后是否有成功记录 |

#### 替换方式

注册一个自定义的 Spring Bean 即可，框架通过 `@ConditionalOnMissingBean` 自动跳过默认的 MySQL 实现：

```java
@Service
public class ElasticsearchTraceStorageService implements TraceLogStorageService {

    @Override
    public void saveTraceLog(ExpressionExecutorResultDTO traceLog) {
        // 存储到 ES
    }

    @Override
    public TraceLogPageResult<ExpressionTraceLogIndex> queryTraceLogList(QueryExpressionTraceRequest queryRequest) {
        // 从 ES 查询
        return new TraceLogPageResult<>(records, total);
    }

    // ... 实现其余方法
}
```

> **注意：** `TraceLogStorageService` 接口不依赖 MyBatis 或任何 ORM 框架。分页返回值 `TraceLogPageResult` 是纯 POJO，接入方无需引入 MyBatis Plus。

> 服务端的职责：
>
> 1、维护执行器、规则等配置数据，
>
> 2、收集客户端的函数和变量定义以及表达式执行日志数据。
>
> 后半部分如果不太需要，可以直接让客户端直接连Redis即可，都不需要服务端，前提就是服务端要把数据更新到Redis中。具体参考客户端接入

---

## AI 表达式生成

通过 LLM 将自然语言描述转化为 Aviator 表达式，支持多轮对话、自动注入函数/变量/执行样本上下文。

- 关于提示词：

默认的系统提示词模版: [ai-system-prompt.md](src/main/resources/ai-system-prompt.md)
> 这个可直接在提示词管理页面中添加，可自行优化。

**服务的提示词模版: 可以基于自己的服务特性创建一些常用的使用方式和最佳实践，然后添加到提示词服务管理中即可生效。**


### 启用配置

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

系统兼容 OpenAI Chat Completions API 格式，改 `api-url` / `api-key` / `model` 即可：

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

### API 接口

**POST** `/expression-engine/ai/generate`

#### 请求

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

#### 响应

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

### 上下文自动注入

系统自动构建以下上下文到系统提示词中：

| 上下文 | 来源 | 触发条件 |
|--------|------|---------|
| 函数列表 | Redis（`ExpressionDocService` 维护） | `serviceName` 非空 |
| 变量列表 | Redis | `serviceName` 非空 |
| 执行样本 | 数据库 `expression_trace_log_index` | `executorId` 非空 |
| 服务专属提示词 | Redis | `serviceName` 非空且对应模板存在 |

### 提示词管理

提示词模板存储在 **Redis Hash** 中，通过提示词管理页面（执行器列表页顶部入口）进行可视化编辑，实时生效无需重启。

| Hash Key | Hash Field | 内容 |
|----------|------------|------|
| `{ENGINE_SERVER_ID}:ai_prompt` | `base` | 基础系统提示词 |
| `{ENGINE_SERVER_ID}:ai_prompt` | `{serviceName}` | 服务专属提示词片段 |

**基础提示词**定义了 Aviator 语法规则、公共变量、内置函数列表等通用知识。

**服务专属提示词**在基础模板的 `{{servicePrompt}}` 占位符处注入，可写入特定服务的业务规则、变量约束、表达式风格要求等。

### AI 扩展点

#### 换 LLM 服务

只需改配置，`LlmClient` 基于 OpenAI API 格式，无需改代码。

#### 换提示词来源

实现 `SystemPromptLoader` 接口并注册为 Spring Bean：

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

默认实现为 `RedisPromptLoader`（从 Redis Hash 读取），通过 `@ConditionalOnMissingBean` 自动注册。

#### 增加上下文

在 `AiExpressionService` 中新增 `build*Context()` 方法，并在 `buildSystemPrompt()` 中加入对应占位符。

### 核心类

| 类 | 职责 |
|----|------|
| `AiExpressionController` | REST 入口，`POST /expression-engine/ai/generate` |
| `AiExpressionService` | 上下文构建、提示词组装、表达式提取 |
| `LlmClient` | HTTP 调用 LLM，解析响应（含 `reasoning_content`） |
| `SystemPromptLoader` | 提示词加载接口 |
| `RedisPromptLoader` | 默认实现，从 Redis Hash 加载，实时读取 |
| `AiExpressionProperties` | 配置绑定，前缀 `spring.expression.server.ai` |

---

## 其他

nacos相关版本介绍 : https://github.com/alibaba/spring-cloud-alibaba/wiki/%E7%89%88%E6%9C%AC%E8%AF%B4%E6%98%8E
