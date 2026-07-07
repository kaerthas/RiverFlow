package com.riverflow.ai.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI 模型配置
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_ai_model")
public class AiModel extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 模型编码（唯一，作为 provider 名称使用）
     */
    private String modelCode;

    /**
     * 模型显示名称
     */
    private String modelName;

    /**
     * provider 类型：openai / ollama / qwen / zhipu
     */
    private String providerType;

    /**
     * provider 分组名称（用于前端分组展示）
     */
    private String providerName;

    /**
     * 基础 URL
     */
    private String baseUrl;

    /**
     * API Key（建议加密存储）
     */
    private String apiKey;

    /**
     * 温度
     */
    private Float temperature;

    /**
     * 最大 token
     */
    private Integer maxTokens;

    /**
     * 超时毫秒
     */
    private Integer timeout;

    /**
     * 是否默认模型：0-否 1-是
     */
    private Integer isDefault;

    /**
     * 状态：0-停用 1-启用
     */
    private Integer status;

    /**
     * 排序号
     */
    private Integer sortNo;

    /**
     * 备注
     */
    private String remark;
}
