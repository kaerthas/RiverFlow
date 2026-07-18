package com.riverflow.ai.prompt.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.riverflow.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * AI Prompt 模板
 *
 * <p>按 scene + model + version 管理 Prompt 模板，数据库中的 Prompt 优先级高于 classpath 文件。</p>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("wf_ai_prompt")
public class AiPrompt extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 场景：flow-generation / condition-generation / mapping-recommendation /
     * script-generation / api-doc-parse
     */
    private String scene;

    /**
     * 模型：qwen2.5:14b / gpt-4o-mini / default
     */
    private String model;

    /**
     * 版本：v1 / v2
     */
    private String version;

    /**
     * Prompt 模板
     */
    private String template;

    /**
     * 系统 Prompt（可选，为空时使用默认系统 Prompt）
     */
    private String systemPrompt;

    /**
     * Few-shot 示例 JSON 数组
     */
    private String examples;

    /**
     * 输出 JSON Schema
     */
    private String outputSchema;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否启用：0-停用 1-启用
     */
    private Integer enabled;

    /**
     * 排序号
     */
    private Integer sortNo;
}
