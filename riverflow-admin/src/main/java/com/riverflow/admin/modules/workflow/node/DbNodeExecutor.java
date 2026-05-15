package com.riverflow.admin.modules.workflow.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.infra.dynamicds.DynamicDataSourceService;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.api.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 数据库操作节点执行器
 * 支持：查询/插入/更新/删除，SQL中可使用 SpEL 占位符
 */
@Slf4j
@Component
public class DbNodeExecutor implements NodeExecutor {

    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Pattern SPEL_PATTERN = Pattern.compile("#\\{([^}]+)}");

    @Override
    public String getNodeType() {
        return "db";
    }

    @Override
    public NodeExecuteResult execute(FlowNode node, FlowContext context) {
        log.info("[流程实例:{}] 执行数据库节点: {}", context.getInstanceId(), node.getNodeName());

        String configJson = node.getConfigJson();
        if (configJson == null || configJson.trim().isEmpty()) {
            return NodeExecuteResult.fail("数据库节点缺少配置");
        }

        JSONObject config = JSON.parseObject(configJson);
        String dsCode = config.getString("dsCode");
        String operation = config.getString("operation"); // select/insert/update/delete
        String sql = config.getString("sql");

        if (sql == null || sql.isEmpty()) {
            return NodeExecuteResult.fail("数据库节点未配置SQL");
        }

        // 解析 SQL 中的 SpEL 占位符
        String resolvedSql = resolveSql(sql, context);
        log.debug("[流程实例:{}] 解析后SQL: {}", context.getInstanceId(), resolvedSql);

        try {
            Object result;
            if (dsCode == null || dsCode.isEmpty() || "master".equals(dsCode)) {
                // 使用默认数据源
                result = executeSql(operation, resolvedSql);
            } else {
                // 切换到动态数据源
                result = dynamicDataSourceService.executeWithDs(dsCode, () -> executeSql(operation, resolvedSql));
            }

            log.info("[流程实例:{}] SQL执行完成: op={}, result={}",
                    context.getInstanceId(), operation, result);

            JSONObject resultData = new JSONObject();
            resultData.put("operation", operation);
            resultData.put("sql", resolvedSql);

            if (result instanceof List) {
                resultData.put("data", result);
                resultData.put("count", ((List<?>) result).size());
            } else if (result instanceof Number) {
                resultData.put("affectedRows", result);
            }

            // 如果配置了结果变量名，自动将查询结果写入上下文（方便后续节点直接使用）
            String resultVarName = config.getString("resultVarName");
            if (resultVarName != null && !resultVarName.isEmpty() && resultData.containsKey("data")) {
                context.set(resultVarName, resultData.get("data"));
                log.info("[流程实例:{}] 查询结果已自动写入上下文变量: {}", context.getInstanceId(), resultVarName);
            }

            // 输出映射（支持更精细的字段映射）
            applyOutputMapping(node, context, resultData);

            return NodeExecuteResult.success(resultData);
        } catch (Exception e) {
            log.error("[流程实例:{}] SQL执行失败: {}", context.getInstanceId(), resolvedSql, e);
            return NodeExecuteResult.fail("SQL执行失败: " + e.getMessage());
        }
    }

    /**
     * 解析 SQL 中的 SpEL 占位符
     */
    private String resolveSql(String sql, FlowContext context) {
        Matcher matcher = SPEL_PATTERN.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String expression = matcher.group(1);
            Object value = context.getByPath(expression);
            String replacement = value != null ? escapeSql(String.valueOf(value)) : "NULL";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String escapeSql(String value) {
        if (value == null) return "NULL";
        // 简单转义单引号
        return "'" + value.replace("'", "''") + "'";
    }

    /**
     * 执行 SQL
     */
    private Object executeSql(String operation, String sql) {
        switch (operation != null ? operation.toLowerCase() : "select") {
            case "select":
                return jdbcTemplate.queryForList(sql);
            case "insert":
            case "update":
            case "delete":
                return jdbcTemplate.update(sql);
            default:
                throw new IllegalArgumentException("不支持的操作类型: " + operation);
        }
    }

    /**
     * 应用输出映射
     */
    private void applyOutputMapping(FlowNode node, FlowContext context, JSONObject resultData) {
        String outputMapping = node.getOutputMapping();
        if (outputMapping == null || outputMapping.isEmpty()) return;
        try {
            JSONArray mappings = JSON.parseArray(outputMapping);
            for (int i = 0; i < mappings.size(); i++) {
                JSONObject map = mappings.getJSONObject(i);
                String source = map.getString("source");
                String target = map.getString("target");
                Object value = resolveResultPath(resultData, source);
                if (value != null) {
                    context.set(target.replace("context.", ""), value);
                }
            }
        } catch (Exception e) {
            log.warn("输出映射解析失败: {}", e.getMessage());
        }
    }

    private Object resolveResultPath(JSONObject result, String path) {
        if (path == null || path.isEmpty()) return null;
        String trimmed = path.trim();
        if (trimmed.startsWith("result.")) trimmed = trimmed.substring(7);
        return result.getByPath(trimmed);
    }
}
