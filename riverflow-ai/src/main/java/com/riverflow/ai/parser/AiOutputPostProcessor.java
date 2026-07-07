package com.riverflow.ai.parser;

import com.riverflow.ai.dto.AiGenerateConditionResponse;
import com.riverflow.ai.dto.AiGenerateScriptResponse;
import org.springframework.stereotype.Component;

/**
 * AI 输出后处理器
 *
 * <p>对 LLM 生成的结果进行规范化处理，提升可用性。
 */
@Component
public class AiOutputPostProcessor {

    /**
     * 规范化 SpEL 表达式
     */
    public void postProcess(AiGenerateConditionResponse response) {
        if (response == null || response.getExpression() == null) {
            return;
        }
        String expr = response.getExpression().trim();
        // 如果缺少 #{} 包裹，自动补上
        if (!expr.startsWith("#{")) {
            expr = "#{" + expr;
        }
        if (!expr.endsWith("}")) {
            expr = expr + "}";
        }
        // 把 context.xxx 统一转为 #context['xxx'] 形式（可选，根据 SpelUtil 兼容处理）
        response.setExpression(expr);
    }

    /**
     * 规范化 Groovy 脚本
     */
    public void postProcess(AiGenerateScriptResponse response) {
        if (response == null || response.getScriptContent() == null) {
            return;
        }
        String script = response.getScriptContent().trim();
        // 如果脚本中缺少 return 语句，但最后一行是赋值，自动补 return
        if (!script.toLowerCase().contains("return") && !script.isEmpty()) {
            // 简单判断最后一行是否是赋值语句
            String[] lines = script.split("\n");
            String lastLine = lines[lines.length - 1].trim();
            if (lastLine.startsWith("def ") && lastLine.contains("=")) {
                String varName = lastLine.substring(4, lastLine.indexOf('=')).trim();
                script = script + "\nreturn [" + varName + ": " + varName + "]";
                response.setScriptContent(script);
            }
        }
    }
}
