# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

An expression rule engine with mind-map visualization. Rules are configured on a server (web UI), synced to embedded client SDKs, and executed locally using the Aviator expression engine. Supports real-time rule updates and full execution tracing.

## Build Commands

```bash
# Build (JDK 17, default)
mvn clean install

# Build (JDK 8)
mvn clean install -Pjdk8 -Djava.version=8

# Build specific module with dependencies
mvn clean install -pl expression-mind-map-server -am

# Run tests (disabled by default via maven.test.skip=true in parent POM)
mvn test -Dmaven.test.skip=false

# Run a single test class
mvn test -Dmaven.test.skip=false -Dtest=ClientServer#executorDemoExample

# Start server (requires MySQL + Redis)
cd expression-mind-map-server && mvn spring-boot:run
```

Server starts on port 20888 by default. Override with `-Dapplication.port=PORT`.

## Module Structure

- **expression-mind-map-core** - Shared models, APIs, enums, utility classes. No Spring dependency.
- **expression-mind-map-server** - Spring Boot web app serving the mind-map UI (jsMind + Layui) and REST APIs. Uses MySQL (MyBatis Plus) + Redis.
- **expression-mind-map-client-starter** - Client SDK auto-configured via `ExpressionConfiguration`. Applications import this to execute expressions.
- **expression-mind-map-jdk8/jdk17** - Thin modules selected by Maven profile to handle JDK/Spring Boot version differences.

## Architecture

### Execution Flow

1. Business code calls `ExpressionExecutorFactory.executor(businessCode, executorCode, request)`
2. `LocalEngineServiceImpl` fetches config tree via `ExpressionConfigCallManager` (HTTP, Redis, or HTTP+cache)
3. Config tree (`ExpressionConfigTreeModel`) is traversed recursively
4. Each node passes through `ExpressionFilterChain` (interceptors)
5. Individual expressions evaluated by `AviatorEvaluatorServiceImpl` using the Aviator script engine
6. Results and traces collected and returned

### Tree Model

`ExpressionConfigTreeModel` is the recursive node structure. Each node has:
- `expressionType`: `trigger` | `condition` | `action` | `callback`
- `expression`: The Aviator expression string
- `nodeExpression`: List of child nodes
- `configurabilityMap`: Per-node extension properties

Execution priority: left-to-right (action -> condition -> trigger -> callback), then top-to-bottom.

### Config Sync Modes

Configured via `spring.plugin.express.expression-config-call`:
- `http` - Direct HTTP call to server each time
- `redis` - Read from Redis cache (server publishes updates via Spring events)
- `http_cache` - HTTP with local caching

### Metrics

Micrometer + Prometheus. Key metrics defined in `MetricKeyEnum`:
- `expression_config_query_call` (timer) - Config fetch latency
- `expression_executor_local_call` (timer) - Executor call latency
- `expression_slow_execution` (summary) - Slow expressions (threshold: `slow-expression-threshold-ms`, default 1000ms)
- `function_slow_execution` (summary) - Slow functions (threshold: `slow-function-threshold-ms`, default 500ms)

Grafana dashboard at `script/grafana/grafana-expression-engine.json`.

### Trace System

Trace logs stored in MySQL with monthly table sharding (`expression_trace_log_index` + `expression_trace_log_info`). Table splitting managed by server config in `application.yml`.

### AI Expression Assistant (Server-side, optional)

AI-powered expression generation via LLM (default: Zhipu AI GLM-5.1). Disabled by default, enable via `spring.expression.server.ai.enabled=true`.

**Core flow:**
1. `AiExpressionController` receives `POST /expression-engine/ai/generate`
2. `AiExpressionService` builds context: functions (Redis), variables (Redis), trace samples (DB)
3. System prompt assembled from template with `{{functionsContext}}`, `{{variablesContext}}`, `{{traceContext}}`, `{{servicePrompt}}` placeholders
4. Two-phase context expansion: basic context first, full context on `NEED_MORE_CONTEXT`
5. `LlmClient` calls OpenAI-compatible Chat Completions API
6. Expression extracted from response, returned with reasoning content

**Config:** `spring.expression.server.ai.*` (api-url, api-key, model, max-tokens, temperature, max-trace-samples, max-conversation-turns, max-context-rounds)

**Prompt storage:** Redis Hash `{ENGINE_SERVER_ID}:ai_prompt` with fields `base` (base template) and `{serviceName}` (per-service prompts). Managed via `RedisPromptLoader`.

**Prompt management UI:** `/template/ai-prompt-list.html` — CRUD with real-time Markdown preview.

## Key Extension Points

### Client-side

| Interface / Base Class | Purpose |
|------------------------|---------|
| `AbstractSimpleFunction` | Custom functions. Override `processor()` and register via `documentRegister()` enum. |
| `AbstractExpressionVariableContextProcessor` | Custom variables. Override `processor()` and register via `variableName()` enum. |
| `ExpressionExecutorPostProcessor` | Pre/post hooks around entire executor execution |
| `ExpressionConfigExecutorIntercept` | Intercept individual expression execution |
| `ExpressionFunctionPostProcessor` | Pre/post hooks around function execution |
| `ExpressionFunctionFilter` | Chain to modify function results |

Custom functions and variables are auto-discovered via component scanning on packages `com.liukx.expression.engine.client.function` and `com.liukx.expression.engine.client.variable`.

### Server-side

| Interface | Default Implementation |
|-----------|----------------------|
| `IExpressionLoginService` | `ExpressionLoginServiceImpl` (config-file based) |
| `IExpressionTokenService` | `ExpressionMd5TokenServiceImpl` |
| `DataRender` | `DefaultDataRender` (trace log rendering) |
| `SystemPromptLoader` | `RedisPromptLoader` (Redis Hash based, real-time) |
| `TraceLogStorageService` | `DefaultMysqlTraceLogStorageService` (MySQL with monthly sharding) |

## Test Framework

JUnit 4 with `SpringRunner`. Tests live in `src/test/java` under each module. Integration tests use `@SpringBootTest`.

## Database

Schema at `script/expression_mysql.sql`. 5 tables:
- `expression_executor_base_info` - Executor metadata
- `expression_executor_info_config` - Expression configs with `parent_id` tree structure
- `expression_trace_log_index` / `expression_trace_log_info` - Trace logs (monthly sharding)
- `expression_history_version` - Expression version history

## Frontend

- Static files: `expression-mind-map-server/src/main/resources/static/`
- Templates: `expression-mind-map-server/src/main/resources/template/`
- Stack: jQuery + Layui 2.x + jsMind (with custom draggable-node plugin)
- Use Layui API for any UI component work

## Important Conventions

- Expression evaluation uses Aviator syntax (not SpEL or MVEL)
- All execution happens client-side; server only stores and serves configurations
- Redis is used for config caching and distributed locks (via Redisson)
- Version management uses `${revision}` property with flatten-maven-plugin
- JDK compatibility: default is JDK 17 + Spring Boot 3.1.7; use `-Pjdk8` for JDK 8 + Spring Boot 2.7.18
