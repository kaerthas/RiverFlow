package com.riverflow.ai.audit;

import com.riverflow.ai.audit.entity.AiAuditLog;
import com.riverflow.ai.audit.mapper.AiAuditLogMapper;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * AI 调用审计日志服务
 *
 * <p>同时输出 Slf4j 日志并异步持久化到数据库 wf_ai_audit_log 表。
 */
@Slf4j
@Service
public class AiAuditLogService {

    private static final Pattern ID_CARD_PATTERN = Pattern.compile("\\b\\d{17}[\\dXx]\\b");
    private static final Pattern MOBILE_PATTERN = Pattern.compile("\\b1[3-9]\\d{9}\\b");
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("\\b\\d{16,19}\\b");

    private final AiAuditLogMapper auditLogMapper;

    @Autowired
    public AiAuditLogService(AiAuditLogMapper auditLogMapper) {
        this.auditLogMapper = auditLogMapper;
    }

    /**
     * 记录 AI 调用审计日志（兼容旧接口）
     */
    @Async("aiAuditExecutor")
    public void log(String scene, String userId, AiChatRequest request, AiChatResponse response) {
        log(scene, userId, request, response, null);
    }

    /**
     * 记录 AI 调用审计日志
     */
    @Async("aiAuditExecutor")
    public void log(String scene, String userId, AiChatRequest request, AiChatResponse response, String promptVersion) {
        try {
            String inputText = summarizeInput(request);
            String outputText = response != null ? summarizeOutput(response) : "";

            // Slf4j 日志
            log.info("[AI_AUDIT] scene={}, userId={}, provider={}, model={}, promptTokens={}, completionTokens={}, " +
                            "responseTimeMs={}, inputLen={}, outputLen={}",
                    scene, userId, getProvider(response), getModel(response),
                    getPromptTokens(response), getCompletionTokens(response), getResponseTime(response),
                    inputText.length(), outputText.length());

            // 数据库持久化
            AiAuditLog entity = new AiAuditLog();
            entity.setScene(scene);
            entity.setUserId(userId);
            entity.setProvider(getProvider(response));
            entity.setModel(getModel(response));
            entity.setPromptTokens(getPromptTokens(response));
            entity.setCompletionTokens(getCompletionTokens(response));
            entity.setTotalTokens(getTotalTokens(response));
            entity.setResponseTimeMs(getResponseTime(response));
            entity.setInputSummary(maskSensitive(inputText));
            entity.setOutputSummary(maskSensitive(outputText));
            entity.setSuccess(response != null ? 1 : 0);
            entity.setPromptVersion(promptVersion);
            entity.setCreateTime(LocalDateTime.now());
            auditLogMapper.insert(entity);
        } catch (Exception e) {
            log.error("AI 审计日志持久化失败", e);
        }
    }

    /**
     * 记录失败审计日志
     */
    @Async("aiAuditExecutor")
    public void logError(String scene, String userId, AiChatRequest request, String errorMsg) {
        logError(scene, userId, request, errorMsg, null);
    }

    /**
     * 记录失败审计日志
     */
    @Async("aiAuditExecutor")
    public void logError(String scene, String userId, AiChatRequest request, String errorMsg, String promptVersion) {
        try {
            AiAuditLog entity = new AiAuditLog();
            entity.setScene(scene);
            entity.setUserId(userId);
            entity.setInputSummary(maskSensitive(summarizeInput(request)));
            entity.setOutputSummary("");
            entity.setSuccess(0);
            entity.setPromptVersion(promptVersion);
            entity.setErrorMsg(errorMsg != null && errorMsg.length() > 500 ? errorMsg.substring(0, 500) : errorMsg);
            entity.setCreateTime(LocalDateTime.now());
            auditLogMapper.insert(entity);
        } catch (Exception e) {
            log.error("AI 失败审计日志持久化失败", e);
        }
    }

    private String summarizeInput(AiChatRequest request) {
        if (request == null || request.getMessages() == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (var msg : request.getMessages()) {
            if (msg.getContent() != null) {
                sb.append(msg.getContent()).append("\n");
            }
        }
        return sb.toString().trim();
    }

    private String summarizeOutput(AiChatResponse response) {
        return response.getContent() != null ? response.getContent() : "";
    }

    private String getProvider(AiChatResponse response) {
        return response != null ? response.getProvider() : "unknown";
    }

    private String getModel(AiChatResponse response) {
        return response != null ? response.getModel() : "unknown";
    }

    private Integer getPromptTokens(AiChatResponse response) {
        return response != null ? response.getPromptTokens() : null;
    }

    private Integer getCompletionTokens(AiChatResponse response) {
        return response != null ? response.getCompletionTokens() : null;
    }

    private Integer getTotalTokens(AiChatResponse response) {
        return response != null ? response.getTotalTokens() : null;
    }

    private Long getResponseTime(AiChatResponse response) {
        return response != null ? response.getResponseTimeMs() : null;
    }

    /**
     * 敏感信息脱敏
     */
    private String maskSensitive(String text) {
        if (text == null) {
            return "";
        }
        String result = ID_CARD_PATTERN.matcher(text).replaceAll(m -> m.group().substring(0, 6) + "********" + m.group().substring(14));
        result = MOBILE_PATTERN.matcher(result).replaceAll(m -> m.group().substring(0, 3) + "****" + m.group().substring(7));
        result = BANK_CARD_PATTERN.matcher(result).replaceAll(m -> m.group().substring(0, 4) + " **** **** " + m.group().substring(m.group().length() - 4));
        // 超长截断
        return result.length() > 4000 ? result.substring(0, 4000) : result;
    }
}
