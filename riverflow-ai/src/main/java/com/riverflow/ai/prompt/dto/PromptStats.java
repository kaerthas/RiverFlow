package com.riverflow.ai.prompt.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Prompt 版本调用统计
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptStats {

    /**
     * Prompt 版本标识：scene:model:version
     */
    private String promptVersion;

    /**
     * 场景
     */
    private String scene;

    /**
     * 模型
     */
    private String model;

    /**
     * 版本
     */
    private String version;

    /**
     * 总调用次数
     */
    private Long totalCount;

    /**
     * 成功次数
     */
    private Long successCount;

    /**
     * 失败次数
     */
    private Long failCount;

    /**
     * 成功率（0-100）
     */
    private Double successRate;

    /**
     * 平均响应耗时（毫秒）
     */
    private Double avgResponseTimeMs;
}
