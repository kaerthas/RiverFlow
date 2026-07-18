package com.riverflow.ai.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建/更新知识文档请求
 */
@Data
public class KnowledgeDocRequest {

    @NotBlank(message = "文档标题不能为空")
    private String title;

    @NotBlank(message = "来源类型不能为空")
    private String sourceType;

    private String sourceId;

    @NotBlank(message = "文档内容不能为空")
    private String content;

    /**
     * 指定向量集合，为空使用默认
     */
    private String collection;
}
