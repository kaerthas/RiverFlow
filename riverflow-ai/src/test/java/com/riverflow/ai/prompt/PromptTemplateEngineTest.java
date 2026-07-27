package com.riverflow.ai.prompt;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Prompt 模板引擎测试
 */
class PromptTemplateEngineTest {

    private final PromptTemplateEngine engine = new PromptTemplateEngine();

    @Test
    void renderSimpleVariable() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("userPrompt", "测试需求");
        String result = engine.render("用户输入：${userPrompt}", vars);
        assertEquals("用户输入：测试需求", result);
    }

    @Test
    void renderSpelExpression() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("count", 3);
        String result = engine.render("数量加一：#{#count + 1}", vars);
        assertEquals("数量加一：4", result);
    }

    @Test
    void keepUnknownSpelPlaceholder() {
        // Prompt 示例中经常包含 SQL 或条件表达式占位符，如 #{businessId}，
        // 这些不是模板变量，解析失败时应保留原文，避免整个生成流程中断。
        Map<String, Object> vars = new HashMap<>();
        vars.put("availableApis", "[]");
        String template = "示例 SQL：SELECT * FROM t_order WHERE business_id = #{businessId}，可用 API：${availableApis}";
        String result = engine.render(template, vars);
        assertEquals("示例 SQL：SELECT * FROM t_order WHERE business_id = #{businessId}，可用 API：[]", result);
    }

    @Test
    void keepEscapedPlaceholder() {
        Map<String, Object> vars = new HashMap<>();
        vars.put("x", "value");
        String result = engine.render("保留转义：\\#{x} 和 \\${x}，替换：${x}", vars);
        assertEquals("保留转义：#{x} 和 ${x}，替换：value", result);
    }
}
