package com.riverflow.admin.modules.workflow.validate;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.api.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 流程定义校验器
 */
@Slf4j
@Component
public class FlowValidator {

    private static final Pattern SPEL_PATTERN = Pattern.compile("#\\{([^}]+)}");

    /**
     * 校验流程节点列表，返回所有错误信息（空表示通过）
     */
    public List<String> validate(List<FlowNode> nodes) {
        List<String> errors = new ArrayList<>();
        if (nodes == null || nodes.isEmpty()) {
            return errors;
        }
        for (FlowNode node : nodes) {
            if ("db".equals(node.getNodeType())) {
                validateDbNode(node, errors);
            }
        }
        return errors;
    }

    /**
     * 校验 DB 节点：SQL 中的占位符必须在输入映射中配置
     */
    private void validateDbNode(FlowNode node, List<String> errors) {
        String configJson = node.getConfigJson();
        if (configJson == null || configJson.trim().isEmpty()) {
            return;
        }
        JSONObject config = JSON.parseObject(configJson);
        String sql = config.getString("sql");
        if (sql == null || sql.trim().isEmpty()) {
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
                log.warn("DB节点 [{}] 输入映射解析失败: {}", getNodeDisplayName(node, config), e.getMessage());
            }
        }

        for (String placeholder : placeholders) {
            if (!mappedTargets.contains(placeholder)) {
                errors.add("数据库节点 [" + getNodeDisplayName(node, config) + "] SQL占位符 [#{" + placeholder + "}] 未在输入映射中配置");
            }
        }
    }

    private String getNodeDisplayName(FlowNode node, JSONObject config) {
        String name = config != null ? config.getString("name") : null;
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        name = node.getNodeName();
        if (name != null && !name.trim().isEmpty()) {
            return name.trim();
        }
        return node.getNodeId();
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
}
