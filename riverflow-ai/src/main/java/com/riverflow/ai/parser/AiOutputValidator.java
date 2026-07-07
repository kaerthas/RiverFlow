package com.riverflow.ai.parser;

import com.riverflow.ai.dto.AiGenerateConditionResponse;
import com.riverflow.ai.dto.AiGenerateFlowResponse;
import com.riverflow.ai.dto.AiGenerateMappingResponse;
import com.riverflow.ai.dto.AiGenerateScriptResponse;
import com.riverflow.ai.dto.AiParseApiDocResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * AI 输出结果校验器
 *
 * <p>对 LLM 生成的结构化结果做业务级校验，确保结果可用。
 */
@Component
public class AiOutputValidator {

    public void validate(AiGenerateConditionResponse response) {
        if (response == null || !StringUtils.hasText(response.getExpression())) {
            throw new IllegalArgumentException("生成的条件表达式为空");
        }
        String expr = response.getExpression().trim();
        if (!expr.startsWith("#{") || !expr.endsWith("}")) {
            throw new IllegalArgumentException("条件表达式格式错误，必须以 #{ 开头、以 } 结尾");
        }
    }

    public void validate(AiGenerateScriptResponse response) {
        if (response == null || !StringUtils.hasText(response.getScriptContent())) {
            throw new IllegalArgumentException("生成的脚本为空");
        }
        String script = response.getScriptContent();
        if (script.contains("Runtime.exec") || script.contains("ProcessBuilder")
                || script.contains("System.exit") || script.contains("ClassLoader")) {
            throw new SecurityException("生成的脚本包含危险关键字");
        }
    }

    public void validate(AiGenerateFlowResponse response) {
        if (response == null || response.getGraphJson() == null) {
            throw new IllegalArgumentException("生成的流程图数据为空");
        }
        if (response.getNodes() == null || response.getNodes().isEmpty()) {
            throw new IllegalArgumentException("生成的流程节点为空");
        }
        if (response.getEdges() == null) {
            throw new IllegalArgumentException("生成的流程边为空");
        }
        boolean hasStart = response.getNodes().stream().anyMatch(n -> "start".equals(n.getNodeType()));
        boolean hasEnd = response.getNodes().stream().anyMatch(n -> "end".equals(n.getNodeType()));
        if (!hasStart) {
            throw new IllegalArgumentException("生成的流程缺少开始节点");
        }
        if (!hasEnd) {
            throw new IllegalArgumentException("生成的流程缺少结束节点");
        }
        // 语义校验：script 节点内容中不允许包含循环控制流，否则说明模型未使用 while 节点
        for (AiGenerateFlowResponse.FlowNodeDraft node : response.getNodes()) {
            if ("script".equals(node.getNodeType()) && StringUtils.hasText(node.getConfigJson())) {
                String cfg = node.getConfigJson().toLowerCase();
                if (cfg.contains("scriptcontent") && containsLoopControl(cfg)) {
                    throw new IllegalArgumentException("生成的脚本节点中包含了 while/for/break/end_while 等循环控制流。请使用 while/foreach 节点实现循环，不要把循环逻辑写在 scriptContent 里");
                }
            }
        }
    }

    private boolean containsLoopControl(String cfg) {
        // 匹配常见循环控制关键字。前后留空格/换行/括号，避免误伤普通英文单词。
        String[] patterns = {
                " while ", " while(",
                " for ", " for(",
                "break", "end_while", "end_foreach",
                "\\nwhile ", "\\nwhile(", "\\nfor ", "\\nfor("
        };
        for (String p : patterns) {
            if (cfg.contains(p)) return true;
        }
        return false;
    }

    public void validate(AiGenerateMappingResponse response) {
        if (response == null || response.getMappings() == null) {
            throw new IllegalArgumentException("生成的映射结果为空");
        }
    }

    public void validate(AiParseApiDocResponse response) {
        if (response == null) {
            throw new IllegalArgumentException("接口文档解析结果为空");
        }
        if (!StringUtils.hasText(response.getApiName()) || !StringUtils.hasText(response.getApiCode())) {
            throw new IllegalArgumentException("解析结果缺少 API 名称或编码");
        }
        if (!StringUtils.hasText(response.getMethod()) || !StringUtils.hasText(response.getPath())) {
            throw new IllegalArgumentException("解析结果缺少 HTTP 方法或路径");
        }
    }
}
