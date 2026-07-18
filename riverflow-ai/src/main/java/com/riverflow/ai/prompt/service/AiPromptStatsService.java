package com.riverflow.ai.prompt.service;

import com.riverflow.ai.audit.mapper.AiAuditLogMapper;
import com.riverflow.ai.prompt.dto.PromptStats;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * AI Prompt 版本统计服务
 */
@Slf4j
@Service
public class AiPromptStatsService {

    private final AiAuditLogMapper auditLogMapper;

    @Autowired
    public AiPromptStatsService(AiAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 按 Prompt 版本统计成功率
     */
    public List<PromptStats> statsByPromptVersion(String scene, LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> rows = auditLogMapper.groupByPromptVersion(scene, startTime, endTime);
        List<PromptStats> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            String promptVersion = String.valueOf(row.get("promptVersion"));
            String[] parts = promptVersion.split(":", 3);
            String scenePart = parts.length > 0 ? parts[0] : "";
            String modelPart = parts.length > 1 ? parts[1] : "";
            String versionPart = parts.length > 2 ? parts[2] : "";

            PromptStats stats = PromptStats.builder()
                    .promptVersion(promptVersion)
                    .scene(scenePart)
                    .model(modelPart)
                    .version(versionPart)
                    .totalCount(toLong(row.get("totalCount")))
                    .successCount(toLong(row.get("successCount")))
                    .failCount(toLong(row.get("failCount")))
                    .successRate(toDouble(row.get("successRate")))
                    .avgResponseTimeMs(toDouble(row.get("avgResponseTimeMs")))
                    .build();
            result.add(stats);
        }
        return result;
    }

    private Long toLong(Object obj) {
        if (obj == null) return 0L;
        if (obj instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(obj.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private Double toDouble(Object obj) {
        if (obj == null) return 0.0;
        if (obj instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(obj.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
