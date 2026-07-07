package com.riverflow.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.ai.audit.entity.AiAuditLog;
import com.riverflow.ai.audit.mapper.AiAuditLogMapper;
import com.riverflow.ai.dto.AiCallStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * AI 调用统计服务
 */
@Service
public class AiStatsService {

    private final AiAuditLogMapper auditLogMapper;

    @Autowired
    public AiStatsService(AiAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 获取综合统计看板数据
     */
    public AiCallStats getStats(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null) {
            startTime = LocalDateTime.now().minusDays(7).withHour(0).withMinute(0).withSecond(0);
        }
        if (endTime == null) {
            endTime = LocalDateTime.now();
        }

        QueryWrapper<AiAuditLog> wrapper = new QueryWrapper<>();
        wrapper.ge("create_time", startTime)
                .le("create_time", endTime);
        List<AiAuditLog> logs = auditLogMapper.selectList(wrapper);

        AiCallStats stats = new AiCallStats();
        stats.setTotalCalls((long) logs.size());
        stats.setSuccessCalls(logs.stream().filter(l -> l.getSuccess() != null && l.getSuccess() == 1).count());
        stats.setFailCalls(logs.stream().filter(l -> l.getSuccess() == null || l.getSuccess() != 1).count());
        stats.setTotalTokens(logs.stream()
                .filter(l -> l.getTotalTokens() != null)
                .mapToLong(AiAuditLog::getTotalTokens)
                .sum());
        double avg = logs.stream()
                .filter(l -> l.getResponseTimeMs() != null)
                .mapToLong(AiAuditLog::getResponseTimeMs)
                .average()
                .orElse(0.0);
        stats.setAvgResponseTime(avg);

        // 按场景分组
        Map<String, Long> sceneMap = logs.stream()
                .collect(Collectors.groupingBy(l -> defaultIfEmpty(l.getScene(), "unknown"), Collectors.counting()));
        stats.setByScene(sceneMap.entrySet().stream()
                .map(e -> new AiCallStats.NameValue(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));

        // 按日期分组 (yyyy-MM-dd)
        Map<String, Long> dateMap = logs.stream()
                .collect(Collectors.groupingBy(l -> l.getCreateTime() != null ? l.getCreateTime().toLocalDate().toString() : "unknown", Collectors.counting()));
        stats.setByDate(dateMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> new AiCallStats.NameValue(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));

        // 按 Provider 分组
        Map<String, Long> providerMap = logs.stream()
                .collect(Collectors.groupingBy(l -> defaultIfEmpty(l.getProvider(), "unknown"), Collectors.counting()));
        stats.setByProvider(providerMap.entrySet().stream()
                .map(e -> new AiCallStats.NameValue(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));

        return stats;
    }

    private String defaultIfEmpty(String value, String defaultValue) {
        return value == null || value.trim().isEmpty() ? defaultValue : value;
    }
}
