package com.riverflow.admin.controller;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.infra.dynamicds.DynamicDataSourceService;
import com.riverflow.admin.infra.http.HttpRequestService;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.admin.service.DatasourceService;
import com.riverflow.api.entity.ApiCatalog;
import com.riverflow.api.entity.Datasource;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用开放接口执行器
 * 根据 wf_api_catalog 配置动态暴露接口，支持 sql / proxy 类型
 * 调用方式：POST /api/open/{apiCode} 或 GET /api/open/{apiCode}
 */
@Slf4j
@RestController
@RequestMapping("/api/open")
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

    @PostMapping("/{apiCode}")
    public R<Object> executePost(@PathVariable String apiCode, @RequestBody(required = false) Map<String, Object> params) {
        return execute(apiCode, params);
    }

    @GetMapping("/{apiCode}")
    public R<Object> executeGet(@PathVariable String apiCode, @RequestParam Map<String, Object> params) {
        return execute(apiCode, params);
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
            Object value = params.get(key);
            String replacement = value != null ? escapeSql(String.valueOf(value)) : "NULL";
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private String escapeSql(String value) {
        if (value == null) return "NULL";
        return "'" + value.replace("'", "''") + "'";
    }
}
