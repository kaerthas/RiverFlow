package com.riverflow.api.modules.workflow.validate.rules;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.api.entity.FlowEdge;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import com.riverflow.api.modules.workflow.validate.FlowValidationResult;
import com.riverflow.api.modules.workflow.validate.FlowValidationRule;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 业务校验规则
 *
 * <p>校验流程节点与业务数据的关联关系，如 DB 节点 SQL 占位符、API 节点配置等。
 */
@Component
public class BusinessValidationRule implements FlowValidationRule {

    private static final Pattern SPEL_PATTERN = Pattern.compile("#\\{([^}]+)}");

    /**
     * 流程运行时会自动注入上下文的内置参数，无需在 inputMapping 中显式配置。
     */
    private static final Set<String> BUILT_IN_CONTEXT_KEYS = Set.of(
            "_businessKey",
            "_flowCode",
            "_instanceId",
            "itemCode"
    );

    @Override
    public FlowValidationResult validate(List<FlowNode> nodes, List<FlowEdge> edges) {
        FlowValidationResult result = new FlowValidationResult();
        result.setValid(true);

        if (nodes == null || nodes.isEmpty()) {
            return result;
        }

        for (FlowNode node : nodes) {
            if (FlowNodeTypeEnum.DB.getCode().equals(node.getNodeType())) {
                validateDbNode(node, result);
            } else if (FlowNodeTypeEnum.API.getCode().equals(node.getNodeType())) {
                validateApiNode(node, result);
            }
        }

        return result;
    }

    /**
     * 校验 DB 节点：SQL 中的占位符必须在输入映射中配置
     */
    private void validateDbNode(FlowNode node, FlowValidationResult result) {
        String configJson = node.getConfigJson();
        if (configJson == null || configJson.trim().isEmpty()) {
            result.addError("数据库节点 [" + getNodeName(node) + "] 缺少配置");
            return;
        }
        JSONObject config;
        try {
            config = JSON.parseObject(configJson);
        } catch (Exception e) {
            result.addError("数据库节点 [" + getNodeName(node) + "] 配置 JSON 解析失败");
            return;
        }
        String sql = config.getString("sql");
        if (sql == null || sql.trim().isEmpty()) {
            result.addError("数据库节点 [" + getNodeName(node) + "] 未配置 SQL");
            return;
        }

        Set<String> placeholders = extractPlaceholders(sql);
        if (placeholders.isEmpty()) {
            return;
        }

        Set<String> mappedTargets = new HashSet<>();
        String inputMapping = node.getInputMapping();
        if (inputMapping != null && !inputMapping.trim().isEmpty()) {
            try {
                JSONArray mappings = JSON.parseArray(inputMapping);
                for (int i = 0; i < mappings.size(); i++) {
                    JSONObject map = mappings.getJSONObject(i);
                    String target = map.getString("target");
                    if (target != null && !target.trim().isEmpty()) {
                        mappedTargets.add(target.trim());
                    }
                }
            } catch (Exception e) {
                result.addError("数据库节点 [" + getNodeName(node) + "] 输入映射解析失败: " + e.getMessage());
            }
        }

        for (String placeholder : placeholders) {
            if (BUILT_IN_CONTEXT_KEYS.contains(placeholder)) {
                continue;
            }
            if (!mappedTargets.contains(placeholder)) {
                result.addError("数据库节点 [" + getNodeName(node) + "] SQL 占位符 [#{" + placeholder
                        + "}] 未在输入映射中配置");
            }
        }
    }

    /**
     * 校验 API 节点：必须绑定接口编码，且配置 JSON 可解析
     */
    private void validateApiNode(FlowNode node, FlowValidationResult result) {
        String configJson = node.getConfigJson();
        if (configJson == null || configJson.trim().isEmpty()) {
            result.addError("接口节点 [" + getNodeName(node) + "] 缺少配置");
            return;
        }
        JSONObject config;
        try {
            config = JSON.parseObject(configJson);
        } catch (Exception e) {
            result.addError("接口节点 [" + getNodeName(node) + "] 配置 JSON 解析失败");
            return;
        }
        String apiCode = config.getString("apiCode");
        if (apiCode == null || apiCode.trim().isEmpty()) {
            result.addError("接口节点 [" + getNodeName(node) + "] 未配置 API 编码");
        }
    }

    /**
     * 提取 SQL 中的 #{...} 占位符名
     */
    private Set<String> extractPlaceholders(String sql) {
        Set<String> placeholders = new HashSet<>();
        Matcher matcher = SPEL_PATTERN.matcher(sql);
        while (matcher.find()) {
            placeholders.add(matcher.group(1).trim());
        }
        return placeholders;
    }

    private String getNodeName(FlowNode node) {
        String name = null;
        if (node.getConfigJson() != null && !node.getConfigJson().trim().isEmpty()) {
            try {
                JSONObject config = JSON.parseObject(node.getConfigJson());
                name = config.getString("name");
            } catch (Exception e) {
                // ignore
            }
        }
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        if (node.getNodeName() != null && !node.getNodeName().isEmpty()) {
            return node.getNodeName();
        }
        return node.getNodeId();
    }
}
