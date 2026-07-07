package com.riverflow.ai.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI 审计日志查询条件
 */
@Data
public class AiAuditLogQuery {

    private String scene;
    private String userId;
    private String provider;
    private Integer success;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer pageNum = 1;
    private Integer pageSize = 20;
}
