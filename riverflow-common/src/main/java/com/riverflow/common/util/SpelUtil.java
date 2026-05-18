package com.riverflow.common.util;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import java.util.Map;

@Slf4j
public class SpelUtil {
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    public static boolean evaluateBoolean(String expression, Map<String, Object> variables) {
        if (expression == null || StrUtil.isBlank(expression)) return true;

        String expr = expression.trim();
        if (expr.startsWith("#{")) expr = expr.substring(2, expr.length() - 1);

        try {
            StandardEvaluationContext context = new StandardEvaluationContext();

            // 将整个 variables Map 设为根对象
            context.setRootObject(variables);

            // 同时将 variables 中的每个 key 注册为变量（可选）
            variables.forEach(context::setVariable);

            // 如果表达式包含 context.，转换为 #context['xxx']
            if (expr.contains("context.")) {
                // 关键修复：转换为 #context['xxx'] 而不是 #root['xxx']
                String processedExpr = expr.replaceAll("\\bcontext\\.(\\w+)", "#context['$1']");
                log.debug("表达式转换: {} -> {}", expr, processedExpr);
                return PARSER.parseExpression(processedExpr).getValue(context, boolean.class);
            }

            return PARSER.parseExpression(expr).getValue(context, boolean.class);
        } catch (Exception e) {
            log.error("SpEL 表达式解析失败: [{}], 变量: {}", expression, variables, e);
            return false;
        }
    }
}