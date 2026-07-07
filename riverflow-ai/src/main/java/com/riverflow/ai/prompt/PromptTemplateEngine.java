package com.riverflow.ai.prompt;

import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Prompt 模板引擎
 *
 * <p>支持两种占位符：
 * <ul>
 *   <li><code>${variableName}</code>：简单变量替换，从 variables Map 中取值</li>
 *   <li><code>#{spelExpression}</code>：Spring SpEL 表达式渲染</li>
 * </ul>
 */
@Component
public class PromptTemplateEngine {

    private static final Pattern SIMPLE_PLACEHOLDER = Pattern.compile("\\$\\{([^}]+)\\}");
    private static final Pattern SPEL_PLACEHOLDER = Pattern.compile("#\\{([^}]+)\\}");
    private final SpelExpressionParser spelParser = new SpelExpressionParser();

    /**
     * 渲染模板
     */
    public String render(String template, Map<String, Object> variables) {
        if (template == null) {
            return null;
        }
        // 保护被反斜杠转义的占位符，避免示例中的 #{...} / ${...} 被当成模板表达式解析
        String escapedSpel = template.replace("\\#{", "\u0000{");
        String escapedSimple = escapedSpel.replace("\\${", "\u0001{");
        String result = renderSimple(escapedSimple, variables);
        result = renderSpel(result, variables);
        result = result.replace("\u0000{", "#{");
        result = result.replace("\u0001{", "${");
        return result;
    }

    private String renderSimple(String template, Map<String, Object> variables) {
        Matcher matcher = SIMPLE_PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = variables.get(key);
            matcher.appendReplacement(sb, value == null ? "" : Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String renderSpel(String template, Map<String, Object> variables) {
        Matcher matcher = SPEL_PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        StandardEvaluationContext context = new StandardEvaluationContext();
        if (variables != null) {
            variables.forEach(context::setVariable);
        }
        while (matcher.find()) {
            String expr = matcher.group(1);
            Object value = spelParser.parseExpression(expr).getValue(context);
            matcher.appendReplacement(sb, value == null ? "" : Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
