package com.riverflow.ai.audit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 调用审计日志实体
 */
@Data
@TableName("wf_ai_audit_log")
public class AiAuditLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 功能场景：flow / condition / mapping / script / chat / chat-stream
     */
    private String scene;

    /**
     * 用户 ID
     */
    private String userId;

    /**
     * provider 名称
     */
    private String provider;

    /**
     * 模型名称
     */
    private String model;

    /**
     * Prompt token 数
     */
    private Integer promptTokens;

    /**
     * 生成 token 数
     */
    private Integer completionTokens;

    /**
     * 总 token 数
     */
    private Integer totalTokens;

    /**
     * 响应耗时（毫秒）
     */
    private Long responseTimeMs;

    /**
     * 用户输入摘要（已脱敏）
     */
    private String inputSummary;

    /**
     * AI 输出摘要（已脱敏）
     */
    private String outputSummary;

    /**
     * 是否成功：0-失败 1-成功
     */
    private Integer success;

    /**
     * 失败原因
     */
    private String errorMsg;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
