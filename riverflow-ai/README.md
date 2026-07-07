# RiverFlow AI 智能助手服务（riverflow-ai）

## 简介

`riverflow-ai` 是 RiverFlow 的独立 AI 微服务，为流程编排平台提供自然语言生成流程、SpEL 条件表达式、数据映射推荐、Groovy 脚本生成等 AI 能力。

采用独立微服务架构，便于：
- 独立升级模型和 Prompt
- 独立扩缩容
- 避免 AI SDK 依赖污染核心流程引擎

## 核心模块

```
com.riverflow.ai
├── config          # 配置类
├── provider        # LLM Provider SPI（OpenAI 协议兼容）
├── client          # AI 调用统一客户端
├── prompt          # Prompt 模板引擎
├── parser          # 响应解析与安全过滤
├── audit           # 调用审计日志
├── service         # 四个 AI 助手业务服务
├── controller      # REST API
└── dto             # 请求/响应 DTO
```

## 快速启动

### 1. 准备 LLM 服务

推荐本地部署 Ollama：

```bash
ollama run qwen2.5:14b
```

### 2. 启动 AI 服务

```bash
mvn spring-boot:run -pl riverflow-ai
```

默认端口：`8081`

### 3. 启动主服务

主服务（riverflow-admin）会自动把 `/api/ai/**` 请求转发到 `http://localhost:8081`。

```bash
mvn spring-boot:run -pl riverflow-admin
```

### 4. 配置说明

编辑 `riverflow-ai/src/main/resources/application.yml`：

```yaml
riverflow:
  ai:
    default-provider: ollama
    providers:
      - name: ollama
        type: openai
        base-url: http://localhost:11434/v1
        default-model: qwen2.5:14b
```

如果使用 OpenAI：

```yaml
riverflow:
  ai:
    default-provider: openai
    providers:
      - name: openai
        type: openai
        base-url: https://api.openai.com/v1
        api-key: ${OPENAI_API_KEY}
        default-model: gpt-4o-mini
```

## API 列表

| 接口 | 说明 |
|---|---|
| `POST /ai/chat` | 通用 AI 对话 |
| `POST /ai/generate-flow` | 自然语言生成流程 |
| `POST /ai/generate-condition` | 自然语言生成 SpEL 条件 |
| `POST /ai/generate-mapping` | 智能推荐数据映射 |
| `POST /ai/generate-script` | 自然语言生成 Groovy 脚本 |

前端统一通过主服务 `/api/ai/**` 访问。

## 支持的 LLM Provider

| Provider | type | 说明 |
|---|---|---|
| OpenAI 协议 | `openai` | OpenAI、智谱、通义千问、Ollama（/v1 端点）等 |

后续可扩展 `ollama`、`zhipu`、`qwen` 等独立实现。

## 安全与审计

- 所有 LLM 调用通过 `AiChatClient` 统一入口
- 支持调用审计日志（默认开启）
- 支持敏感信息脱敏（默认开启）
- 生成内容经过安全过滤（Groovy 黑名单、SpEL 白名单）

## 与主服务集成

```
前端 (5173)
  │
  ▼ /api/ai/generate-flow
主服务 (8080)  ── 认证鉴权
  │
  ▼ 转发到 http://localhost:8081/ai/generate-flow
AI 服务 (8081)
  │
  ▼ 调用 LLM
```

## 后续扩展

1. 增加流式输出（SSE）
2. 增加 Prompt 结果缓存
3. 增加更多 Provider（Claude、文心一言、讯飞星火）
4. 增加运行时 AI 节点（workflow 中直接调用 LLM）
5. 持久化审计日志到数据库
