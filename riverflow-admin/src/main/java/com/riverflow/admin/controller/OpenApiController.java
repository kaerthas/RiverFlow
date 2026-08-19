package com.riverflow.admin.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.dynamic.datasource.DynamicRoutingDataSource;
import com.riverflow.admin.infra.dynamicds.DynamicDataSourceService;
import com.riverflow.admin.infra.groovy.GroovySandboxExecutor;
import com.riverflow.admin.infra.http.HttpRequestService;
import com.riverflow.admin.infra.openapi.NestedParamResolver;
import com.riverflow.admin.infra.openapi.SqlCheckResult;
import com.riverflow.admin.infra.openapi.SqlSafetyChecker;
import com.riverflow.admin.infra.plugin.ApiPluginLoader;
import com.riverflow.admin.modules.workflow.engine.FlowEngine;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.admin.service.ApiParamService;
import com.riverflow.admin.service.ApiScriptService;
import com.riverflow.admin.service.DatasourceService;
import com.riverflow.admin.service.FlowDefinitionService;
import com.riverflow.admin.service.FlowInstanceService;
import com.riverflow.admin.service.FlowNodeService;
import com.riverflow.admin.service.FlowTaskService;
import com.riverflow.api.entity.ApiCatalog;
import com.riverflow.api.entity.ApiParam;
import com.riverflow.api.entity.ApiScript;
import com.riverflow.api.entity.Datasource;
import com.riverflow.api.entity.FlowDefinition;
import com.riverflow.api.entity.FlowInstance;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.entity.FlowTask;
import com.riverflow.api.enums.FlowNodeTypeEnum;
import com.riverflow.api.enums.FlowTaskStatusEnum;
import com.riverflow.api.plugin.FileResponse;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用开放接口执行器
 * 根据 wf_api_catalog 配置动态暴露接口，支持 sql / proxy / script 类型
 * 调用方式：{openMethod} /open{openPath}（由用户自定义路径和请求方式）
 */
@Slf4j
@RestController
@RequestMapping("/open")
public class OpenApiController {

    @Autowired
    private ApiCatalogService apiCatalogService;
    @Autowired
    private ApiParamService apiParamService;
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
    @Autowired
    private ApiPluginLoader apiPluginLoader;
    @Autowired
    private DynamicRoutingDataSource dynamicRoutingDataSource;

    private JdbcTemplate dynamicJdbcTemplate;

    /**
     * 传统同步流程接口的固定字段（body 内）
     */
    private static final Set<String> SYNC_FIXED_KEYS = new HashSet<>(
            Arrays.asList("flowCode", "businessKey", "variables", "timeoutMs"));

    /**
     * 自定义同步流程接口保留字段（body 内不参与业务变量）
     */
    private static final Set<String> SYNC_RESERVED_KEYS = new HashSet<>(
            Arrays.asList("businessKey", "timeoutMs"));

    @PostConstruct
    public void init() {
        this.dynamicJdbcTemplate = new JdbcTemplate(dynamicRoutingDataSource);
    }

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

        // 合并流程默认入参与外部传入参数（外部参数优先级高）
        Map<String, Object> mergedVars = mergeFlowInputParams(def.getInputParams(), variables);

        if (mergedVars != null && !mergedVars.isEmpty()) {
            try {
                String existingContext = instance.getContextJson();
                Map<String, Object> contextMap;
                if (existingContext != null && !existingContext.isEmpty()) {
                    contextMap = JSON.parseObject(existingContext, Map.class);
                } else {
                    contextMap = new HashMap<>();
                }
                contextMap.putAll(mergedVars);
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

    /**
     * 同步执行流程（传统格式）
     * 支持两种入参方式：
     * 1. { flowCode: "xxx", variables: { a: 1 } }
     * 2. { flowCode: "xxx", a: 1 }
     *
     * @param params flowCode-流程编码（必填）, businessKey-业务主键（可选）,
     *               variables-初始上下文变量（可选）, timeoutMs-超时毫秒（可选，默认30000）
     */
    @PostMapping("/flow/executeSync")
    public R<Map<String, Object>> executeSync(@RequestBody(required = false) Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }

        String flowCode = (String) params.get("flowCode");
        String businessKey = (String) params.get("businessKey");
        Object timeoutObj = params.get("timeoutMs");

        // 兼容 variables 和扁平两种格式
        Map<String, Object> variables = new HashMap<>();
        Object varObj = params.get("variables");
        if (varObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> varMap = (Map<String, Object>) varObj;
            variables.putAll(varMap);
        }
        params.forEach((k, v) -> {
            if (!SYNC_FIXED_KEYS.contains(k)) {
                variables.put(k, v);
            }
        });

        return doExecuteSync(flowCode, businessKey, variables, timeoutObj);
    }

    /**
     * 同步执行流程（自定义 body 格式）
     * flowCode 通过 URL 路径传递，body 完全由业务方自定义
     * 例如：POST /open/flow/executeSync/FLOW_XXX
     * body: { "applicant": {...}, "baseInfo": {...} }
     */
    @PostMapping("/flow/executeSync/{flowCode}")
    public R<Map<String, Object>> executeSyncCustom(
            @PathVariable String flowCode,
            @RequestBody(required = false) Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }

        String businessKey = (String) params.get("businessKey");
        Object timeoutObj = params.get("timeoutMs");

        // body 中除保留字段外，全部作为业务变量注入上下文
        Map<String, Object> variables = new HashMap<>();
        params.forEach((k, v) -> {
            if (!SYNC_RESERVED_KEYS.contains(k)) {
                variables.put(k, v);
            }
        });

        return doExecuteSync(flowCode, businessKey, variables, timeoutObj);
    }

    /**
     * 同步执行流程公共逻辑
     */
    private R<Map<String, Object>> doExecuteSync(String flowCode, String businessKey,
                                                 Map<String, Object> variables, Object timeoutObj) {
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

        long timeoutMs = 30000L; // 默认30秒
        if (timeoutObj != null) {
            try {
                timeoutMs = Long.parseLong(timeoutObj.toString());
                if (timeoutMs < 1000L) {
                    timeoutMs = 1000L;
                }
                if (timeoutMs > 120000L) {
                    timeoutMs = 120000L; // 最大120秒
                }
            } catch (NumberFormatException e) {
                log.warn("timeoutMs格式错误，使用默认值30000: {}", timeoutObj);
            }
        }

        // 同步执行只允许 SYNC 模式的流程
        if (!"SYNC".equals(def.getExecutionMode())) {
            return R.fail("该流程不是同步流程，请使用 /open/flow/start 异步启动");
        }

        // 合并流程默认入参与外部传入参数（外部参数优先级高）
        Map<String, Object> mergedVars = mergeFlowInputParams(def.getInputParams(), variables);

        try {
            Map<String, Object> result = flowEngine.executeSync(
                    def.getId(),
                    def.getFlowCode(),
                    def.getVersion(),
                    businessKey,
                    def.getItemCode(),
                    mergedVars,
                    timeoutMs
            );
            return R.ok(result);
        } catch (com.riverflow.common.exception.BusinessException e) {
            log.warn("同步流程执行失败: flowCode={}, error={}", flowCode, e.getMessage());
            return R.fail(e.getMessage());
        } catch (Exception e) {
            log.error("同步流程执行异常: flowCode={}", flowCode, e);
            return R.fail("同步流程执行异常: " + e.getMessage());
        }
    }

    /**
     * 动态开放接口执行器
     * 根据用户配置的 open_path + open_method 匹配并执行
     */
    @RequestMapping(value = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE})
    public Object executeDynamic(HttpServletRequest request, HttpServletResponse response) {
        String path = extractOpenPath(request);
        String method = request.getMethod();

        ApiCatalog api = apiCatalogService.getOne(
                new QueryWrapper<ApiCatalog>()
                        .eq("open_path", path)
                        .eq("open_method", method)
                        .eq("status", 1)
                        .eq("del_flag", 0)
        );
        if (api == null) {
            return R.fail("接口不存在: " + method + " " + path);
        }

        Map<String, Object> params = readRequestParams(request);
        return execute(api, params, response);
    }

    private String extractOpenPath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        return uri.substring((contextPath + "/open").length());
    }

    private Map<String, Object> readRequestParams(HttpServletRequest request) {
        String method = request.getMethod();
        if ("GET".equals(method) || "DELETE".equals(method)) {
            return NestedParamResolver.resolve(request);
        }
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
            return NestedParamResolver.resolve(request);
        }
        // 支持 application/text 类型，直接作为字符串传递
        if (contentType != null && contentType.contains("text")) {
            return readBodyAsString(request);
        }
        return readBodyAsMap(request);
    }

    /**
     * 从请求体中读取纯文本内容
     * 用于 application/text 等非 JSON 格式的请求体
     */
    private Map<String, Object> readBodyAsString(HttpServletRequest request) {
        try (BufferedReader reader = request.getReader()) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            String body = sb.toString();
            
            Map<String, Object> params = new HashMap<>();
            params.put("body", body);
            return params;
        } catch (IOException e) {
            log.warn("读取纯文本请求体失败: {}", e.getMessage());
            return new HashMap<>();
        }
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

    /**
     * 合并流程默认入参与外部传入参数（外部参数优先级高）
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> mergeFlowInputParams(String inputParams, Map<String, Object> variables) {
        Map<String, Object> merged = new HashMap<>();
        if (inputParams != null && !inputParams.isEmpty()) {
            try {
                Map<String, Object> defaultVars = JSON.parseObject(inputParams, Map.class);
                if (defaultVars != null) {
                    merged.putAll(defaultVars);
                }
            } catch (Exception e) {
                log.warn("解析流程默认入参失败: {}", inputParams, e);
            }
        }
        if (variables != null && !variables.isEmpty()) {
            merged.putAll(variables);
        }
        return merged;
    }

    private Object execute(ApiCatalog api, Map<String, Object> params, HttpServletResponse response) {
        if (api.getStatus() == null || api.getStatus() != 1) {
            return R.fail("接口未发布: " + api.getApiCode());
        }

        String apiType = api.getApiType();
        Object result;
        if ("sql".equals(apiType)) {
            result = executeSql(api, params);
        } else if ("proxy".equals(apiType)) {
            result = executeProxy(api, params);
        } else if ("script".equals(apiType)) {
            result = executeScript(api, params);
        } else if ("plugin".equals(apiType)) {
            result = executePlugin(api, params);
        } else {
            return R.fail("不支持的接口类型: " + apiType);
        }
        
        // 处理文件流响应
        if (result instanceof R) {
            Object data = ((R<?>) result).getData();
            if (data instanceof FileResponse) {
                return writeFileSync((FileResponse) data, response);
            }
        }
        
        return result;
    }
    
    /**
     * 写入文件流响应
     */
    private Object writeFileSync(FileResponse fileResponse, HttpServletResponse response) {
        try (InputStream is = fileResponse.getInputStream();
             OutputStream os = response.getOutputStream()) {
            
            // 设置响应头
            response.setContentType(fileResponse.getContentType() != null ? 
                fileResponse.getContentType() : "application/octet-stream");
            
            String fileName = fileResponse.getFileName();
            if (fileName != null) {
                String encodedFileName = URLEncoder.encode(fileName, "UTF-8");
                response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            }
            
            if (fileResponse.getContentLength() > 0) {
                response.setContentLengthLong(fileResponse.getContentLength());
            }
            
            // 写入流
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            
            return null; // 返回 null 表示已直接写入响应
        } catch (Exception e) {
            log.error("文件流写入失败", e);
            return R.fail("文件下载失败: " + e.getMessage());
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

        // SQL 安全校验
        SqlCheckResult checkResult = SqlSafetyChecker.validate(sql);
        if (!checkResult.isPassed()) {
            log.warn("SQL 安全校验失败: apiCode={}, reason={}", api.getApiCode(), checkResult.getMessage());
            return R.fail("SQL 安全校验失败: " + checkResult.getMessage());
        }

        // 按接口参数定义的数据类型转换绑定值（query/form 参数均为字符串，数值型参数不转换会导致 LIMIT 等场景报错）
        Map<String, String> dataTypes = loadParamDataTypes(api.getId());
        PreparedSql prepared = resolvePreparedSql(sql, params, dataTypes);
        String resolvedSql = prepared.getSql();
        Object[] args = prepared.getArgs();
        Long dsId = api.getDsId();

        try {
            Object result;
            String sqlLower = resolvedSql.trim().toLowerCase();
            boolean isSelect = sqlLower.startsWith("select");

            if (dsId == null || dsId == 0) {
                // 使用主库
                result = isSelect ? jdbcTemplate.queryForList(resolvedSql, args)
                        : jdbcTemplate.update(resolvedSql, args);
            } else {
                // 切换到动态数据源
                Datasource ds = datasourceService.getById(dsId);
                if (ds == null) {
                    return R.fail("数据源不存在: dsId=" + dsId);
                }
                final String execSql = resolvedSql;
                final Object[] execArgs = args;
                result = dynamicDataSourceService.executeWithDs(ds.getDsCode(), () -> {
                    return isSelect ? dynamicJdbcTemplate.queryForList(execSql, execArgs)
                            : dynamicJdbcTemplate.update(execSql, execArgs);
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
        Map<String, String> queryParams = new HashMap<>();
        Object body = null;

        List<ApiParam> apiParams = apiParamService.list(
                new QueryWrapper<ApiParam>()
                        .eq("api_id", api.getId())
                        .eq("del_flag", 0)
                        .orderByAsc("sort_no")
        );

        for (ApiParam p : apiParams) {
            if (p.getParamKey() == null || p.getParamKey().isEmpty()) continue;
            if (p.getDefaultValue() == null || p.getDefaultValue().isEmpty()) continue;
            if ("header".equals(p.getParamType())) {
                headers.put(p.getParamKey(), p.getDefaultValue());
            } else if ("query".equals(p.getParamType())) {
                queryParams.put(p.getParamKey(), p.getDefaultValue());
            } else if ("body".equals(p.getParamType())) {
                if (body == null) body = new JSONObject();
                ((JSONObject) body).put(p.getParamKey(), p.getDefaultValue());
            }
        }

        if (params != null && !params.isEmpty()) {
            // 特殊处理：如果 params 直接包含 "body" 键且值为字符串，说明是纯文本请求
            if (params.containsKey("body") && params.get("body") instanceof String) {
                body = params.get("body");
            } else {
                // 常规处理：解析 header/query/body. 前缀参数
                for (Map.Entry<String, Object> entry : params.entrySet()) {
                    String key = entry.getKey();
                    if (key.startsWith("header.")) {
                        headers.put(key.substring(7), String.valueOf(entry.getValue()));
                    } else if (key.startsWith("query.")) {
                        queryParams.put(key.substring(6), String.valueOf(entry.getValue()));
                    } else if (key.startsWith("body.")) {
                        if (body == null) body = new JSONObject();
                        ((JSONObject) body).put(key.substring(5), entry.getValue());
                    }
                }
                boolean hasPrefixed = params.keySet().stream().anyMatch(k ->
                        k.startsWith("header.") || k.startsWith("query.") || k.startsWith("body."));
                if (!hasPrefixed) {
                    if (body == null) {
                        body = new JSONObject(params);
                    } else {
                        JSONObject bodyJson = (JSONObject) body;
                        for (Map.Entry<String, Object> entry : params.entrySet()) {
                            bodyJson.put(entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        }

        try {
            JSONObject result = httpRequestService.execute(api, headers, body, queryParams);
            return R.ok(result);
        } catch (Exception e) {
            log.error("接口调用失败: apiCode={}", api.getApiCode(), e);
            return R.fail("接口调用失败: " + e.getMessage());
        }
    }

    /**
     * 解析 SQL 中的 #{xxx} 占位符，返回预编译SQL和参数列表
     * 绑定值按接口参数定义的数据类型转换（见 {@link #convertByDataType}）
     */
    private PreparedSql resolvePreparedSql(String sql, Map<String, Object> params, Map<String, String> dataTypes) {
        if (params == null || params.isEmpty()) {
            return new PreparedSql(sql, new Object[0]);
        }
        Pattern pattern = Pattern.compile("#\\{([^}]+)}");
        Matcher matcher = pattern.matcher(sql);
        StringBuffer sb = new StringBuffer();
        List<Object> args = new ArrayList<>();
        while (matcher.find()) {
            String key = matcher.group(1).trim();
            // 支持嵌套路径取值，如 baseInfo.person.name
            Object value = NestedParamResolver.getValueByPath(params, key);
            if (value instanceof Map || value instanceof List) {
                value = JSON.toJSONString(value);
            }
            value = convertByDataType(value, lookupDataType(dataTypes, key));
            args.add(value);
            matcher.appendReplacement(sb, "?");
        }
        matcher.appendTail(sb);
        return new PreparedSql(sb.toString(), args.toArray());
    }

    /**
     * 加载接口参数定义的数据类型：paramKey -> dataType（小写）
     */
    private Map<String, String> loadParamDataTypes(Long apiId) {
        Map<String, String> dataTypes = new HashMap<>();
        if (apiId == null) {
            return dataTypes;
        }
        List<ApiParam> apiParams = apiParamService.getParamsByApiId(apiId);
        if (apiParams == null) {
            return dataTypes;
        }
        for (ApiParam p : apiParams) {
            if (p.getParamKey() == null || p.getParamKey().isEmpty()
                    || p.getDataType() == null || p.getDataType().isEmpty()) {
                continue;
            }
            dataTypes.putIfAbsent(p.getParamKey(), p.getDataType().toLowerCase());
        }
        return dataTypes;
    }

    /**
     * 查找占位符对应的数据类型，嵌套路径（如 baseInfo.page）先按全路径匹配，再按末段键名匹配
     */
    private String lookupDataType(Map<String, String> dataTypes, String key) {
        if (dataTypes == null || dataTypes.isEmpty()) {
            return null;
        }
        String dataType = dataTypes.get(key);
        if (dataType == null) {
            int dot = key.lastIndexOf('.');
            if (dot >= 0) {
                dataType = dataTypes.get(key.substring(dot + 1));
            }
        }
        return dataType;
    }

    /**
     * 按参数定义的数据类型转换字符串值：query/form 传入的参数一律为字符串，
     * 数值、布尔类型若仍以字符串绑定会导致 LIMIT 等场景报错，这里按声明类型转换；
     * 未声明类型或非字符串值原样返回，转换失败保留原值
     */
    private Object convertByDataType(Object value, String dataType) {
        if (!(value instanceof String) || dataType == null) {
            return value;
        }
        String str = ((String) value).trim();
        if (str.isEmpty()) {
            return null;
        }
        try {
            switch (dataType) {
                case "int":
                    return Integer.valueOf(str);
                case "long":
                    return Long.valueOf(str);
                case "double":
                    return Double.valueOf(str);
                case "boolean":
                    return "true".equalsIgnoreCase(str) || "1".equals(str);
                default:
                    return value;
            }
        } catch (NumberFormatException e) {
            log.warn("参数值 [{}] 无法转换为 {}，按原字符串绑定", str, dataType);
            return value;
        }
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

            // 注入流程默认入参
            Map<String, Object> defaultVars = mergeFlowInputParams(def.getInputParams(), null);
            if (defaultVars != null && !defaultVars.isEmpty()) {
                try {
                    String existingContext = instance.getContextJson();
                    Map<String, Object> contextMap;
                    if (existingContext != null && !existingContext.isEmpty()) {
                        contextMap = JSON.parseObject(existingContext, Map.class);
                    } else {
                        contextMap = new HashMap<>();
                    }
                    contextMap.putAll(defaultVars);
                    instance.setContextJson(JSON.toJSONString(contextMap));
                    flowInstanceService.updateById(instance);
                } catch (Exception e) {
                    log.warn("注入流程默认入参失败", e);
                }
            }

            // 创建开始节点的 pending 任务，让 FlowScheduler 能扫描并自动推进
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
     * 执行 PLUGIN 类型接口（调用 SDK 插件）
     */
    private R<Object> executePlugin(ApiCatalog api, Map<String, Object> params) {
        String pluginType = api.getPluginType();
        if (pluginType == null || pluginType.isEmpty()) {
            return R.fail("插件接口未绑定插件类型: apiCode=" + api.getApiCode());
        }

        if (!apiPluginLoader.hasPlugin(pluginType)) {
            return R.fail("插件未加载: " + pluginType);
        }

        try {
            Object result = apiPluginLoader.getPlugin(pluginType).execute(api, params);
            return R.ok(result);
        } catch (Exception e) {
            log.error("插件接口执行失败: apiCode={}, pluginType={}", api.getApiCode(), pluginType, e);
            return R.fail("插件接口执行失败: " + e.getMessage());
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
