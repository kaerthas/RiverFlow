package com.riverflow.admin.modules.workflow.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.riverflow.admin.infra.dynamicds.DynamicDataSourceService;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.api.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
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
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    /**
     * 必须绑定动态路由数据源，@Autowired 直接注入的 JdbcTemplate 绑死主库，
     * DynamicDataSourceContextHolder 的数据源切换对其无效
     */
    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        this.jdbcTemplate = new JdbcTemplate(dynamicRoutingDataSource);
    }

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

        // 解析 SQL 中的 SpEL 占位符（必须从输入映射取值）
        PreparedSql prepared = resolvePreparedSql(sql, node, context);
        String resolvedSql = prepared.getSql();
        Object[] args = prepared.getArgs();
        log.debug("[流程实例:{}] 解析后SQL: {}", context.getInstanceId(), resolvedSql);

        try {
            Object result;
            if (dsCode == null || dsCode.isEmpty() || "master".equals(dsCode)) {
                // 使用默认数据源（参与流程事务）
                result = executeSql(operation, resolvedSql, args);
            } else {
                // 显式校验数据源已注册，防止路由不到时静默回退主库，把 SQL 打到错误的库
                if (!dynamicDataSourceService.hasDataSource(dsCode)) {
                    return NodeExecuteResult.fail("数据源未注册或不在线: " + dsCode
                            + "（请检查数据源状态、驱动JAR与连接URL，或到数据源管理页测试连接）");
                }
                // 外层流程事务已绑定主库连接，JdbcTemplate 会优先复用该连接导致路由失效，
                // 故外部数据源使用裸连接执行，绕开事务上下文
                result = executeSqlOnExternalDs(dsCode, operation, resolvedSql, args);
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
     * 解析 SQL 中的 SpEL 占位符，转为 PreparedStatement 参数化SQL
     * 占位符必须从输入映射（inputMapping）中取值，不允许黑盒读取上下文
     */
    private PreparedSql resolvePreparedSql(String sql, FlowNode node, FlowContext context) {
        Map<String, Object> paramMap = buildInputParamMap(node, context);
        Matcher matcher = SPEL_PATTERN.matcher(sql);
        StringBuffer sb = new StringBuffer();
        List<Object> args = new ArrayList<>();
        while (matcher.find()) {
            String expression = matcher.group(1).trim();
            if (!paramMap.containsKey(expression)) {
                throw new IllegalArgumentException("SQL占位符 [#{" + expression + "}] 未在输入映射中配置");
            }
            Object value = paramMap.get(expression);
            if (value instanceof Map || value instanceof List) {
                value = JSON.toJSONString(value);
            }
            args.add(value);
            matcher.appendReplacement(sb, "?");
        }
        matcher.appendTail(sb);
        return new PreparedSql(sb.toString(), args.toArray());
    }

    /**
     * 根据输入映射构建参数表
     */
    private Map<String, Object> buildInputParamMap(FlowNode node, FlowContext context) {
        Map<String, Object> paramMap = new HashMap<>();
        String inputMapping = node.getInputMapping();
        if (inputMapping == null || inputMapping.isEmpty()) {
            return paramMap;
        }
        try {
            JSONArray mappings = JSON.parseArray(inputMapping);
            for (int i = 0; i < mappings.size(); i++) {
                JSONObject map = mappings.getJSONObject(i);
                String target = map.getString("target");
                String source = map.getString("source");
                String type = map.getString("type");
                if (target == null || target.isEmpty()) {
                    continue;
                }
                Object value;
                if ("const".equals(type)) {
                    value = source;
                } else {
                    value = context.getByPath(source);
                }
                paramMap.put(target, value);
            }
        } catch (Exception e) {
            log.warn("输入映射解析失败: {}", e.getMessage());
        }
        return paramMap;
    }

    private static class PreparedSql {
        private final String sql;
        private final Object[] args;

        public PreparedSql(String sql, Object[] args) {
            this.sql = sql;
            this.args = args;
        }

        public String getSql() {
            return sql;
        }

        public Object[] getArgs() {
            return args;
        }
    }

    /**
     * 执行 SQL（参数化查询）
     */
    private Object executeSql(String operation, String sql, Object[] args) {
        switch (operation != null ? operation.toLowerCase() : "select") {
            case "select":
                return jdbcTemplate.queryForList(sql, args);
            case "insert":
            case "update":
            case "delete":
                return jdbcTemplate.update(sql, args);
            default:
                throw new IllegalArgumentException("不支持的操作类型: " + operation);
        }
    }

    /**
     * 在外部数据源上执行 SQL：使用目标数据源的裸连接。
     * 外层流程事务（@Transactional）开启时已把主库连接绑定到线程，
     * JdbcTemplate 会优先复用该绑定连接导致数据源切换失效，故这里绕开事务上下文。
     * 外部数据源操作独立提交，不随流程事务回滚（跨库本地事务本就无法保证原子性）。
     */
    private Object executeSqlOnExternalDs(String dsCode, String operation, String sql, Object[] args) throws Exception {
        javax.sql.DataSource ds = dynamicRoutingDataSource.getDataSource(dsCode);
        String op = operation != null ? operation.toLowerCase() : "select";
        try (java.sql.Connection conn = ds.getConnection();
             java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            if ("select".equals(op)) {
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    java.sql.ResultSetMetaData md = rs.getMetaData();
                    int cols = md.getColumnCount();
                    List<Map<String, Object>> rows = new ArrayList<>();
                    while (rs.next()) {
                        Map<String, Object> row = new java.util.LinkedHashMap<>();
                        for (int c = 1; c <= cols; c++) {
                            row.put(md.getColumnLabel(c), rs.getObject(c));
                        }
                        rows.add(row);
                    }
                    return rows;
                }
            } else if ("insert".equals(op) || "update".equals(op) || "delete".equals(op)) {
                return ps.executeUpdate();
            } else {
                throw new IllegalArgumentException("不支持的操作类型: " + operation);
            }
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
