package com.riverflow.ai.controller;

import com.alibaba.fastjson2.JSON;
import com.riverflow.ai.dto.AiGenerateConditionRequest;
import com.riverflow.ai.dto.AiGenerateConditionResponse;
import com.riverflow.ai.dto.AiGenerateFlowRequest;
import com.riverflow.ai.dto.AiGenerateFlowResponse;
import com.riverflow.ai.dto.AiGenerateMappingRequest;
import com.riverflow.ai.dto.AiGenerateMappingResponse;
import com.riverflow.ai.dto.AiGenerateScriptRequest;
import com.riverflow.ai.dto.AiGenerateScriptResponse;
import com.riverflow.ai.dto.AiParseApiDocRequest;
import com.riverflow.ai.dto.AiParseApiDocResponse;
import com.riverflow.ai.client.AiChatClient;
import com.riverflow.ai.config.AiProperties;
import com.riverflow.ai.dto.AiProviderInfo;
import com.riverflow.ai.service.AiAssistantService;
import com.riverflow.ai.service.ApiDocParseService;
import com.riverflow.ai.service.ConditionGenerationService;
import com.riverflow.ai.service.FlowGenerationService;
import com.riverflow.ai.service.MappingRecommendationService;
import com.riverflow.ai.service.ScriptGenerationService;
import com.riverflow.ai.provider.AiChatRequest;
import com.riverflow.ai.provider.AiMessage;
import com.riverflow.common.result.R;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * AI 智能助手接口
 *
 * <p>为 RiverFlow 主服务提供自然语言生成流程、SpEL 条件、数据映射、Groovy 脚本等 AI 能力。
 */
@Slf4j
@RestController
@RequestMapping("/ai")
public class AiAssistantController {

    private final AiChatClient aiChatClient;
    private final AiAssistantService aiAssistantService;
    private final FlowGenerationService flowGenerationService;
    private final ConditionGenerationService conditionGenerationService;
    private final MappingRecommendationService mappingRecommendationService;
    private final ScriptGenerationService scriptGenerationService;
    private final ApiDocParseService apiDocParseService;
    private final AiProperties aiProperties;

    @Autowired
    public AiAssistantController(AiChatClient aiChatClient,
                                 AiAssistantService aiAssistantService,
                                 FlowGenerationService flowGenerationService,
                                 ConditionGenerationService conditionGenerationService,
                                 MappingRecommendationService mappingRecommendationService,
                                 ScriptGenerationService scriptGenerationService,
                                 ApiDocParseService apiDocParseService,
                                 AiProperties aiProperties) {
        this.aiChatClient = aiChatClient;
        this.aiAssistantService = aiAssistantService;
        this.flowGenerationService = flowGenerationService;
        this.conditionGenerationService = conditionGenerationService;
        this.mappingRecommendationService = mappingRecommendationService;
        this.scriptGenerationService = scriptGenerationService;
        this.apiDocParseService = apiDocParseService;
        this.aiProperties = aiProperties;
    }

    /**
     * 通用 AI 对话
     */
    @PostMapping("/chat")
    public R<String> chat(@Valid @RequestBody com.riverflow.ai.dto.AiChatRequest request,
                          @RequestHeader(value = "X-User-Id", required = false) String userId) {
        String reply = aiAssistantService.chat(request.getMessage(), request.getHistory(),
                request.getProvider(), request.getModel(), defaultUserId(userId));
        return R.ok(reply);
    }

    /**
     * 通用 AI 对话（SSE 流式输出）
     */
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@Valid @RequestBody com.riverflow.ai.dto.AiChatRequest request,
                                  @RequestHeader(value = "X-User-Id", required = false) String userId) {
        // 流式接口超时设置为 10 分钟，避免 Ollama 首次加载大模型时超时
        SseEmitter emitter = new SseEmitter(600000L);
        emitter.onTimeout(() -> {
            log.warn("AI 流式调用 SSE 超时");
            try {
                emitter.send(SseEmitter.event().data("[ERROR]AI 流式调用超时，请稍后重试"));
            } catch (Exception ignored) {
            }
            emitter.complete();
        });
        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.system("你是 RiverFlow 流程编排平台的 AI 助手，帮助用户理解、设计、优化流程。"));
        if (request.getHistory() != null && !request.getHistory().isBlank()) {
            messages.add(AiMessage.user(request.getHistory()));
        }
        messages.add(AiMessage.user(request.getMessage()));

        AiChatRequest chatRequest = AiChatRequest.builder()
                .model(request.getModel())
                .messages(messages)
                .scene("chat-stream")
                .build();

        String provider = request.getProvider() != null && !request.getProvider().isBlank()
                ? request.getProvider() : null;

        java.util.function.Consumer<Throwable> errorHandler = err -> {
            log.error("AI 流式调用失败", err);
            try {
                emitter.send(SseEmitter.event().data("[ERROR]" + err.getMessage()));
            } catch (Exception e) {
                log.error("发送 SSE 错误事件失败", e);
            }
            emitter.complete();
        };

        if (provider != null) {
            aiChatClient.stream(provider, chatRequest,
                    data -> sendEvent(emitter, data),
                    errorHandler,
                    emitter::complete);
        } else {
            aiChatClient.stream(chatRequest,
                    data -> sendEvent(emitter, data),
                    errorHandler,
                    emitter::complete);
        }
        return emitter;
    }

    private void sendEvent(SseEmitter emitter, String data) {
        try {
            emitter.send(SseEmitter.event().data(data));
        } catch (Exception e) {
            log.error("SSE 发送失败", e);
        }
    }

    /**
     * 自然语言生成流程
     */
    @PostMapping("/generate-flow")
    public R<AiGenerateFlowResponse> generateFlow(@Valid @RequestBody AiGenerateFlowRequest request,
                                                   @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return R.ok(flowGenerationService.generate(request, defaultUserId(userId)));
    }

    /**
     * 自然语言生成流程（SSE 流式输出）
     */
    @PostMapping(value = "/generate-flow/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter generateFlowStream(@Valid @RequestBody AiGenerateFlowRequest request,
                                          @RequestHeader(value = "X-User-Id", required = false) String userId) {
        SseEmitter emitter = new SseEmitter(600000L);
        emitter.onTimeout(() -> {
            try {
                emitter.send(SseEmitter.event().data("[ERROR]AI 流程生成超时，请稍后重试"));
            } catch (Exception ignored) {
            }
            emitter.complete();
        });

        Consumer<String> thinkSender = text -> sendEvent(emitter, "[THINK]" + text);
        Consumer<AiGenerateFlowResponse> resultSender = result -> {
            try {
                emitter.send(SseEmitter.event().data("[JSON]" + JSON.toJSONString(result)));
            } catch (Exception e) {
                log.error("发送流程生成结果失败", e);
            }
        };
        Consumer<Throwable> errorSender = err -> {
            log.error("AI 流程生成流式调用失败", err);
            try {
                emitter.send(SseEmitter.event().data("[ERROR]" + err.getMessage()));
            } catch (Exception e) {
                log.error("发送流程生成错误事件失败", e);
            }
            emitter.complete();
        };
        Runnable completeSender = emitter::complete;

        flowGenerationService.generateStream(request, defaultUserId(userId),
                thinkSender, resultSender, errorSender, completeSender);
        return emitter;
    }

    /**
     * 自然语言生成 SpEL 条件表达式
     */
    @PostMapping("/generate-condition")
    public R<AiGenerateConditionResponse> generateCondition(@Valid @RequestBody AiGenerateConditionRequest request,
                                                             @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return R.ok(conditionGenerationService.generate(request, defaultUserId(userId)));
    }

    /**
     * 智能推荐数据映射
     */
    @PostMapping("/generate-mapping")
    public R<AiGenerateMappingResponse> generateMapping(@Valid @RequestBody AiGenerateMappingRequest request,
                                                         @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return R.ok(mappingRecommendationService.recommend(request, defaultUserId(userId)));
    }

    /**
     * 自然语言生成 Groovy 脚本
     */
    @PostMapping("/generate-script")
    public R<AiGenerateScriptResponse> generateScript(@Valid @RequestBody AiGenerateScriptRequest request,
                                                       @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return R.ok(scriptGenerationService.generate(request, defaultUserId(userId)));
    }

    /**
     * 接口文档智能解析
     */
    @PostMapping("/parse-api-doc")
    public R<AiParseApiDocResponse> parseApiDoc(@Valid @RequestBody AiParseApiDocRequest request,
                                                 @RequestHeader(value = "X-User-Id", required = false) String userId) {
        return R.ok(apiDocParseService.parse(request, defaultUserId(userId)));
    }

    /**
     * 查询可用的 LLM Provider 列表（脱敏）
     */
    @GetMapping("/providers")
    public R<List<AiProviderInfo>> providers() {
        List<AiProviderInfo> list = aiProperties.getProviders().stream()
                .map(p -> {
                    AiProviderInfo info = new AiProviderInfo();
                    info.setName(p.getName());
                    info.setType(p.getType());
                    info.setDefaultModel(p.getDefaultModel());
                    return info;
                })
                .toList();
        return R.ok(list);
    }

    private String defaultUserId(String userId) {
        return userId != null && !userId.isBlank() ? userId : "anonymous";
    }
}
