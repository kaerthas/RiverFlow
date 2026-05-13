package com.riverflow.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.Map;

/**
 * Spring SpEL 表达式工具类
 * 用于流程条件判断、动态参数解析
 */
@Slf4j
public class SpelUtil {

    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    /**
     * 解析布尔表达式
     *
     * @param expression SpEL 表达式，如 #{context.code == 200}
     * @param variables  变量上下文
     * @return 表达式结果
     */
    public static boolean evaluateBoolean(String expression, Map<String, Object> variables) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }
        // 去除首尾 #{} 包裹（如果存在）
        String expr = expression.trim();
        if (expr.startsWith("#{")) {
            expr = expr.substring(2);
        }
        if (expr.endsWith("}")) {
            expr = expr.substring(0, expr.length() - 1);
        }

        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            if (variables != null) {
                variables.forEach(context::setVariable);
                // 同时支持直接以 root 对象方式访问
                context.setRootObject(variables);
            }
            return Boolean.TRUE.equals(PARSER.parseExpression(expr).getValue(context, Boolean.class));
        } catch (Exception e) {
            log.error("SpEL 表达式解析失败: [{}], 变量: {}", expression, variables, e);
            return false;
        }
    }

    /**
     * 解析对象表达式
     */
    @SuppressWarnings("unchecked")
    public static <T> T evaluate(String expression, Map<String, Object> variables, Class<T> clazz) {
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }
        String expr = expression.trim();
        if (expr.startsWith("#{")) {
            expr = expr.substring(2);
        }
        if (expr.endsWith("}")) {
            expr = expr.substring(0, expr.length() - 1);
        }

        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            if (variables != null) {
                variables.forEach(context::setVariable);
                context.setRootObject(variables);
            }
            return (T) PARSER.parseExpression(expr).getValue(context, clazz);
        } catch (Exception e) {
            log.error("SpEL 表达式解析失败: [{}]", expression, e);
            return null;
        }
    }
}
