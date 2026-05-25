package com.riverflow.admin.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.infra.dynamicds.DynamicDataSourceService;
import com.riverflow.admin.infra.groovy.GroovySandboxExecutor;
import com.riverflow.admin.infra.http.HttpRequestService;
import com.riverflow.admin.infra.openapi.NestedParamResolver;
import com.riverflow.admin.modules.workflow.engine.FlowEngine;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.admin.service.ApiScriptService;
import com.riverflow.admin.service.DatasourceService;
import com.riverflow.admin.service.FlowDefinitionService;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.admin.service.FlowNodeService;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.ApiCatalog;
import com.riverflow.api.entity.ApiScript;
import com.riverflow.api.entity.Datasource;
import com.riverflow.api.entity.FlowDefinition;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import com.riverflow.api.enums.FlowTaskStatusEnum;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用开放接口执行器
 * 根据 wf_api_catalog 配置动态暴露接口，支持 sql / proxy 类型
 * 调用方式：POST /open/{apiCode} 或 GET /open/{apiCode}
 */
@Slf4j
@RestController
@RequestMapping("/open")
public class OpenApiController {

    @Autowired
    private ApiCatalogService apiCatalogService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private DatasourceService datasourceService;
    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;
    @Autowired
    private HttpRequestService httpRequestService;
    @Autowired
    private FlowEngine flowEngine;
    @Autowired
    private FlowDefinitionService flowDefinitionService;
    @Autowired
    private FlowNodeService flowNodeService;
    @Autowired
    private FlowInstanceService flowInstanceService;
    @Autowired
    private FlowTaskService flowTaskService;
    @Autowired
    private ApiScriptService apiScriptService;
    @Autowired
    private GroovySandboxExecutor groovyExecutor;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/flow/start")
    public R<Map<String, Object>> startFlow(@RequestBody(required = false) Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        
        String flowCode = (String) params.get("flowCode");
        String businessKey = (String) params.get("businessKey");
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) params.get("variables");
        
        if (flowCode == null || flowCode.isEmpty()) {
            return R.fail("flowCode不能为空");
        }
        
        FlowDefinition def = flowDefinitionService.getLatestPublished(flowCode);
        if (def == null) {
            return R.fail("流程不存在或未发布: " + flowCode);
        }
        
        if (businessKey == null || businessKey.isEmpty()) {
            businessKey = String.valueOf(System.currentTimeMillis());
        }
        
        FlowInstance instance = flowEngine.startInstance(
            def.getId(),
            def.getFlowCode(),
            def.getVersion(),
            businessKey,
            def.getItemCode()
        );
        
        if (variables != null && !variables.isEmpty()) {
            try {
                String existingContext = instance.getContextJson();
                Map<String, Object> contextMap;
                if (existingContext != null && !existingContext.isEmpty()) {
                    contextMap = JSON.parseObject(existingContext, Map.class);
                } else {
                    contextMap = new HashMap<>();
                }
                contextMap.putAll(variables);
                instance.setContextJson(JSON.toJSONString(contextMap));
                flowInstanceService.updateById(instance);
            } catch (Exception e) {
                log.warn("保存流程上下文失败", e);
            }
        }
        
        List<FlowNode> nodes = flowNodeService.list(
            new QueryWrapper<FlowNode>()
                .eq("flow_id", def.getId())
                .eq("del_flag", 0)
        );
        
        FlowNode startNode = nodes.stream()
            .filter(n -> FlowNodeTypeEnum.START.getCode().equals(n.getNodeType()))
            .findFirst()
            .orElse(null);
        
        if (startNode != null) {
            FlowTask task = new FlowTask();
            task.setInstanceId(instance.getId());
            task.setNodeId(startNode.getNodeId());
            task.setNodeName(startNode.getNodeName());
            task.setNodeType(startNode.getNodeType());
            task.setStatus(FlowTaskStatusEnum.PENDING.getCode());
            task.setExecuteCount(0);
            task.setCreateTime(java.time.LocalDateTime.now());
            flowTaskService.save(task);
            
            instance.setCurrentNodeId(startNode.getNodeId());
            flowInstanceService.updateById(instance);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("instanceId", instance.getId());
        result.put("flowCode", instance.getFlowCode());
        result.put("businessKey", instance.getBusinessKey());
        result.put("status", instance.getStatus());
        
        return R.ok(result);
    }

    @PostMapping("/{apiCode}")
    public R<Object> executePost(@PathVariable("apiCode") String apiCode,
                                 HttpServletRequest request) {
        String contentType = request.getContentType();
        Map<String, Object> params;
        if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
            params = NestedParamResolver.resolve(request);
        } else {
            params = readBodyAsMap(request);
        }
        return execute(apiCode, params);
    }

    @GetMapping("/{apiCode}")
    public R<Object> executeGet(@PathVariable("apiCode") String apiCode, @RequestParam Map<String, Object> params) {
        return execute(apiCode, params);
    }

    /**
     * 从请求体中读取 JSON 并解析为 Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> readBodyAsMap(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString().trim();
            if (body.isEmpty()) {
                return new HashMap<>();
            }
            Object parsed = JSON.parse(body);
            if (parsed instanceof Map) {
                return (Map<String, Object>) parsed;
            }
            // 如果不是 Map（如 JSONArray），包装一下
            Map<String, Object> wrap = new HashMap<>();
            wrap.put("data", parsed);
            return wrap;
        } catch (IOException e) {
            log.warn("读取请求体失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private R<Object> execute(String apiCode, Map<String, Object> params) {
        ApiCatalog api = apiCatalogService.getOne(
                new QueryWrapper<ApiCatalog>()
                        .eq("api_code", apiCode)
                        .eq("del_flag", 0)
        );
        if (api == null) {
            return R.fail("接口不存在: " + apiCode);
        }
        if (api.getStatus() == null || api.getStatus() != 1) {
            return R.fail("接口未发布: " + apiCode);
        }

        String apiType = api.getApiType();
        if ("sql".equals(apiType)) {
            return executeSql(api, params);
        } else if ("proxy".equals(apiType)) {
            return executeProxy(api, params);
        } else if ("script".equals(apiType)) {
            return executeScript(api, params);
        } else {
            return R.fail("不支持的接口类型: " + apiType);
        }
    }

    /**
     * 执行 SQL 类型接口
     * SQL 语句存储在 ApiCatalog.url 字段中
     */
    private R<Object> executeSql(ApiCatalog api, Map<String, Object> params) {
        String sql = api.getUrl();
        if (sql == null || sql.trim().isEmpty()) {
            return R.fail("SQL 未配置");
        }

        String resolvedSql = resolveSql(sql, params);
        Long dsId = api.getDsId();

        try {
            Object result;
            String sqlLower = resolvedSql.trim().toLowerCase();
            boolean isSelect = sqlLower.startsWith("select");

            if (dsId == null || dsId == 0) {
                // 使用主库
                result = isSelect ? jdbcTemplate.queryForList(resolvedSql)
                        : jdbcTemplate.update(resolvedSql);
            } else {
                // 切换到动态数据源
                Datasource ds = datasourceService.getById(dsId);
                if (ds == null) {
                    return R.fail("数据源不存在: dsId=" + dsId);
                }
                result = dynamicDataSourceService.executeWithDs(ds.getDsCode(), () -> {
                    return isSelect ? jdbcTemplate.queryForList(resolvedSql)
                            : jdbcTemplate.update(resolvedSql);
                });
            }

            JSONObject resultData = new JSONObject();
            if (result instanceof List) {
                resultData.put("data", result);
                resultData.put("count", ((List<?>) result).size());
            } else if (result instanceof Number) {
                resultData.put("affectedRows", result);
            }

            // 配置化流程触发（SQL执行成功后，隔离异常不影响接口返回）
            triggerFlowIfNeeded(api, params, resultData);

            return R.ok(resultData);
        } catch (Exception e) {
            log.error("SQL执行失败: apiCode={}, sql={}", api.getApiCode(), resolvedSql, e);
            return R.fail("SQL执行失败: " + e.getMessage());
        }
    }

    /**
     * 执行 PROXY 类型接口（调用外部HTTP）
     */
    private R<Object> executeProxy(ApiCatalog api, Map<String, Object> params) {
        Map<String, String> headers = new HashMap<>();
        Object body = null;

        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                String key = entry.getKey();
                if (key.startsWith("header.")) {
                    headers.put(key.substring(7), String.valueOf(entry.getValue()));
                } else if (key.startsWith("body.")) {
                    if (body == null) body = new JSONObject();
                    ((JSONObject) body).put(key.substring(5), entry.getValue());
                }
            }
            if (body == null) {
                body = new JSONObject(params);
            }
        }

        try {
            JSONObject result = httpRequestService.execute(api, headers, body);
            return R.ok(result);
        } catch (Exception e) {
            log.error("接口调用失败: apiCode={}", api.getApiCode(), e);
            return R.fail("接口调用失败: " + e.getMessage());
        }
    }

    /**
     * 解析 SQL 中的 #{xxx} 占位符
     */
    private String resolveSql(String sql, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return sql;
        }
        Pattern pattern = Pattern.compile("#\\{([^}]+)}");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            // 支持嵌套路径取值，如 baseInfo.person.name
            Object value = NestedParamResolver.getValueByPath(params, key);
            String replacement = formatSqlValue(value);
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String formatSqlValue(Object value) {
        if (value == null) {
            return "NULL";
        }
        // Map/List 自动序列化为 JSON 字符串（用于 JSON 类型字段）
        if (value instanceof Map || value instanceof List) {
            return "'" + JSON.toJSONString(value).replace("'", "''") + "'";
        }
        String str = String.valueOf(value);
        return "'" + str.replace("'", "''") + "'";
    }

    /**
     * 配置化流程触发
     * 当接口配置了 triggerEnabled=1 且 triggerFlowCode 不为空时，
     * 自动查找最新发布的版本并启动流程实例
     */
    private void triggerFlowIfNeeded(ApiCatalog api, Map<String, Object> params, JSONObject resultData) {
        if (api.getTriggerEnabled() == null || api.getTriggerEnabled() != 1) {
            return;
        }

        try {
            FlowDefinition def = null;
            // 优先使用 triggerFlowCode（绑定编码，自动指向最新版本）
            if (api.getTriggerFlowCode() != null && !api.getTriggerFlowCode().isEmpty()) {
                def = flowDefinitionService.getLatestPublished(api.getTriggerFlowCode());
                if (def == null) {
                    log.warn("流程触发失败：流程编码 {} 无已发布版本", api.getTriggerFlowCode());
                    return;
                }
            } else if (api.getTriggerFlowId() != null) {
                // 兼容旧数据：使用 triggerFlowId（绑定具体版本）
                def = flowDefinitionService.getById(api.getTriggerFlowId());
                if (def == null) {
                    log.warn("流程触发失败：流程定义不存在, triggerFlowId={}", api.getTriggerFlowId());
                    return;
                }
                if (def.getStatus() == null || def.getStatus() != 1) {
                    log.warn("流程触发失败：流程未发布, triggerFlowId={}, status={}", api.getTriggerFlowId(), def.getStatus());
                    return;
                }
            } else {
                return;
            }

            // 提取业务主键
            String bizKeyField = api.getTriggerBizKeyField();
            String businessKey = null;
            if (bizKeyField != null && !bizKeyField.isEmpty()) {
                Object bizVal = NestedParamResolver.getValueByPath(params, bizKeyField);
                if (bizVal != null) {
                    businessKey = String.valueOf(bizVal);
                }
            }
            if (businessKey == null || businessKey.isEmpty() || "null".equals(businessKey)) {
                businessKey = String.valueOf(System.currentTimeMillis());
                log.warn("业务主键字段未配置或参数中不存在，使用时间戳作为 businessKey: apiCode={}", api.getApiCode());
            }

            // 启动流程实例（绑定到具体版本）
            FlowInstance instance = flowEngine.startInstance(
                    def.getId(),
                    def.getFlowCode(),
                    def.getVersion(),
                    businessKey,
                    def.getItemCode()
            );

            // 创建开始节点的 pending 任务，让 FlowScheduler 能扫描并自动推进
            List<FlowNode> nodes = flowNodeService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<FlowNode>()
                            .eq("flow_id", def.getId())
                            .eq("del_flag", 0)
            );
            FlowNode startNode = nodes.stream()
                    .filter(n -> FlowNodeTypeEnum.START.getCode().equals(n.getNodeType()))
                    .findFirst()
                    .orElse(null);

            if (startNode != null) {
                FlowTask task = new FlowTask();
                task.setInstanceId(instance.getId());
                task.setNodeId(startNode.getNodeId());
                task.setNodeName(startNode.getNodeName());
                task.setNodeType(startNode.getNodeType());
                task.setStatus(FlowTaskStatusEnum.PENDING.getCode());
                task.setExecuteCount(0);
                task.setCreateTime(java.time.LocalDateTime.now());
                flowTaskService.save(task);

                instance.setCurrentNodeId(startNode.getNodeId());
                flowInstanceService.updateById(instance);
            }

            resultData.put("instanceId", instance.getId());
            resultData.put("businessKey", businessKey);
            log.info("流程触发成功: apiCode={}, instanceId={}, businessKey={}",
                    api.getApiCode(), instance.getId(), businessKey);

        } catch (Exception e) {
            log.error("流程触发异常: apiCode={}, triggerFlowId={}", api.getApiCode(), api.getTriggerFlowId(), e);
            // 流程触发异常不影响 SQL 接口返回，仅记录日志
        }
    }

    /**
     * 执行脚本类型接口
     */
    private R<Object> executeScript(ApiCatalog api, Map<String, Object> params) {
        Long scriptId = api.getScriptId();
        if (scriptId == null) {
            return R.fail("脚本接口未绑定脚本: apiCode=" + api.getApiCode());
        }
        ApiScript script = apiScriptService.getById(scriptId);
        if (script == null || script.getScriptContent() == null || script.getScriptContent().trim().isEmpty()) {
            return R.fail("脚本内容不存在或为空: scriptId=" + scriptId);
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("params", params);
            variables.put("ctx", params);
            variables.put("context", params);
            variables.put("instanceId", "script-api-" + api.getApiCode());
            variables.put("redis", redisTemplate);

            JSONObject result = groovyExecutor.execute(script.getScriptContent(), variables);
            return R.ok(result);
        } catch (Exception e) {
            log.error("脚本接口执行失败: apiCode={}, scriptId={}", api.getApiCode(), scriptId, e);
            return R.fail("脚本执行失败: " + e.getMessage());
        }
    }
}
