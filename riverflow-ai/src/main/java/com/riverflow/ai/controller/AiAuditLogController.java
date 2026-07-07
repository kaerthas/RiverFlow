package com.riverflow.ai.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.ai.audit.entity.AiAuditLog;
import com.riverflow.ai.audit.mapper.AiAuditLogMapper;
import com.riverflow.ai.dto.AiAuditLogQuery;
import com.riverflow.ai.dto.AiCallStats;
import com.riverflow.ai.service.AiStatsService;
import com.riverflow.common.result.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * AI 审计日志查询接口
 */
@RestController
@RequestMapping("/ai/audit")
public class AiAuditLogController {

    private final AiAuditLogMapper auditLogMapper;
    private final AiStatsService aiStatsService;

    @Autowired
    public AiAuditLogController(AiAuditLogMapper auditLogMapper, AiStatsService aiStatsService) {
        this.auditLogMapper = auditLogMapper;
        this.aiStatsService = aiStatsService;
    }

    /**
     * 分页查询审计日志
     */
    @GetMapping("/list")
    public R<Page<AiAuditLog>> list(
            @RequestParam(required = false) String scene,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Integer success,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        LambdaQueryWrapper<AiAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(AiAuditLog::getCreateTime);

        if (hasText(scene)) {
            wrapper.eq(AiAuditLog::getScene, scene);
        }
        if (hasText(userId)) {
            wrapper.eq(AiAuditLog::getUserId, userId);
        }
        if (hasText(provider)) {
            wrapper.eq(AiAuditLog::getProvider, provider);
        }
        if (success != null) {
            wrapper.eq(AiAuditLog::getSuccess, success);
        }
        if (startTime != null) {
            wrapper.ge(AiAuditLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(AiAuditLog::getCreateTime, endTime);
        }

        Page<AiAuditLog> page = new Page<>(pageNum, pageSize);
        auditLogMapper.selectPage(page, wrapper);
        return R.ok(page);
    }

    /**
     * 综合统计看板数据
     */
    @GetMapping("/stats")
    public R<AiCallStats> stats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return R.ok(aiStatsService.getStats(startTime, endTime));
    }

    /**
     * 按场景分组统计
     */
    @GetMapping("/stats/scene")
    public R<Long> countByScene(@RequestParam String scene,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
                                @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        LambdaQueryWrapper<AiAuditLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiAuditLog::getScene, scene);
        if (startTime != null) wrapper.ge(AiAuditLog::getCreateTime, startTime);
        if (endTime != null) wrapper.le(AiAuditLog::getCreateTime, endTime);
        return R.ok(auditLogMapper.selectCount(wrapper));
    }

    private boolean hasText(String str) {
        return str != null && !str.isBlank();
    }
}
