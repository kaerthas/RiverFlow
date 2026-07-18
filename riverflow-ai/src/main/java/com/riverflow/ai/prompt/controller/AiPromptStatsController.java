package com.riverflow.ai.prompt.controller;

import com.riverflow.ai.prompt.dto.PromptStats;
import com.riverflow.ai.prompt.service.AiPromptStatsService;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AI Prompt 版本统计
 */
@Slf4j
@RestController
@RequestMapping("/ai/prompt/stats")
public class AiPromptStatsController {

    private final AiPromptStatsService promptStatsService;

    @Autowired
    public AiPromptStatsController(AiPromptStatsService promptStatsService) {
        this.promptStatsService = promptStatsService;
    }

    /**
     * 按 Prompt 版本统计成功率
     */
    @GetMapping("/version")
    public R<List<PromptStats>> statsByVersion(
            @RequestParam(required = false) String scene,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return R.ok(promptStatsService.statsByPromptVersion(scene, startTime, endTime));
    }
}
