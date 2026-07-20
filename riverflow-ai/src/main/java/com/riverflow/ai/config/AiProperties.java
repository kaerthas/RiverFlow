package com.riverflow.ai.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 服务配置属性
 *
 * <p>示例配置：
 * <pre>
 * riverflow:
 *   ai:
 *     enabled: true
 *     audit-enabled: true
 *     default-provider: ollama
 *     timeout: 30000
 *     retry: 1
 *     providers:
 *       - name: ollama
 *         type: ollama
 *         base-url: http://localhost:11434
 *         default-model: qwen2.5:14b
 *       - name: openai
 *         type: openai
 *         base-url: https://api.openai.com/v1
 *         api-key: ${OPENAI_API_KEY}
 *         default-model: gpt-4o-mini
 * </pre>
 */
@Data
@Component
@Validated
@ConfigurationProperties(prefix = "riverflow.ai")
public class AiProperties {

    /**
     * 是否启用 AI 服务
     */
    private boolean enabled = true;

    /**
     * 是否记录调用审计日志
     */
    private boolean auditEnabled = true;

    /**
     * 是否对输入输出进行敏感信息脱敏
     */
    private boolean sensitiveMaskEnabled = true;

    /**
     * 默认使用的 provider 名称
     */
    private String defaultProvider;

    /**
     * LLM HTTP 调用超时（毫秒）
     */
    private int timeout = 30000;

    /**
     * 失败重试次数（不含首次）
     */
    private int retry = 1;

    /**
     * 知识库与 RAG 配置
     */
    private KnowledgeConfig knowledge = new KnowledgeConfig();

    /**
     * Provider 列表（配置文件中的静态配置，数据库无可用配置时作为兜底）
     */
    private List<Provider> providers = new ArrayList<>();

    @Data
    public static class KnowledgeConfig {

        /**
         * 向量库配置
         */
        private VectorStoreConfig vectorStore = new VectorStoreConfig();

        /**
         * Embedding 配置
         */
        private EmbeddingConfig embedding = new EmbeddingConfig();

        /**
         * 分块配置
         */
        private ChunkConfig chunk = new ChunkConfig();

        /**
         * RAG 检索配置
         */
        private RagConfig rag = new RagConfig();
    }

    @Data
    public static class VectorStoreConfig {

        /**
         * 向量库类型：milvus / pgvector / memory
         */
        private String type = "milvus";

        /**
         * 默认向量集合/表名
         */
        private String defaultCollection = "riverflow_default";

        /**
         * Milvus 配置
         */
        private MilvusConfig milvus = new MilvusConfig();

        /**
         * PGVector 配置
         */
        private PgVectorConfig pgvector = new PgVectorConfig();
    }

    @Data
    public static class MilvusConfig {

        private String host = "localhost";
        private int port = 19530;
        private String database = "default";
        private String token;
        private boolean secure = false;
        /**
         * 向量索引类型：IVF_FLAT / HNSW
         */
        private String indexType = "HNSW";
    }

    @Data
    public static class PgVectorConfig {

        private String url;
        private String username;
        private String password;
        private String schema = "public";
    }

    @Data
    public static class EmbeddingConfig {

        /**
         * Embedding 类型：openai / ollama / qwen / zhipu / memory
         * memory 仅用于测试，不依赖外部服务，返回随机向量
         */
        private String type = "openai";

        /**
         * 基础 URL（OpenAI 协议兼容）
         */
        private String baseUrl;

        /**
         * API Key
         */
        private String apiKey;

        /**
         * 模型名称
         */
        private String model = "text-embedding-3-small";

        /**
         * 向量维度，需与 collection 配置一致
         */
        private int dimension = 1536;

        /**
         * 调用超时（毫秒）
         */
        private int timeout = 30000;
    }

    @Data
    public static class ChunkConfig {

        /**
         * 分块大小（字符数）
         */
        private int size = 512;

        /**
         * 分块重叠字符数
         */
        private int overlap = 64;

        /**
         * 单个文档最大分块数
         */
        private int maxChunks = 100;
    }

    @Data
    public static class RagConfig {

        /**
         * 是否启用 RAG 检索增强
         */
        private boolean enabled = true;

        /**
         * 检索 Top-K
         */
        private int topK = 5;

        /**
         * 相似度最低阈值（0~1，COSINE）
         */
        private double minScore = 0.7;

        /**
         * 默认检索集合
         */
        private String collection = "riverflow_default";
    }

    @Data
    public static class Provider {

        /**
         * provider 唯一标识
         */
        @NotBlank
        private String name;

        /**
         * provider 类型：openai / ollama / zhipu / qwen 等
         */
        @NotBlank
        private String type;

        /**
         * 基础 URL
         */
        @NotBlank
        private String baseUrl;

        /**
         * API Key（部分 provider 不需要）
         */
        private String apiKey;

        /**
         * 默认模型
         */
        @NotBlank
        private String defaultModel;

        /**
         * 默认温度
         */
        private Float temperature = 0.2f;

        /**
         * 默认最大 token
         */
        private Integer maxTokens = 4096;

        /**
         * 上下文窗口大小（仅 Ollama 有效，对应 num_ctx）
         */
        private Integer contextSize = 8192;
    }
}
