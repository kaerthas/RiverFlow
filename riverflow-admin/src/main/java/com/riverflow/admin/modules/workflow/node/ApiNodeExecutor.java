package com.riverflow.admin.modules.workflow.node;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.admin.infra.http.HttpRequestService;
import com.riverflow.admin.modules.workflow.context.FlowContext;
import com.riverflow.admin.modules.workflow.engine.NodeExecuteResult;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.admin.service.ApiParamService;
import com.riverflow.api.entity.ApiCatalog;
import com.riverflow.api.entity.ApiParam;
import com.riverflow.api.entity.FlowNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 接口调用节点执行器
 * 调用已注册的第三方接口，支持输入映射和输出映射
 */
@Slf4j
@Component
public class ApiNodeExecutor implements NodeExecutor {

    @Autowired
    private HttpRequestService httpRequestService;
    @Autowired
    private ApiCatalogService apiCatalogService;
    @Autowired
    private ApiParamService apiParamService;

    @Override
    public String getNodeType() {
        return "api";
    }

    @Override
    public NodeExecuteResult execute(FlowNode node, FlowContext context) {
        log.info("[流程实例:{}] 执行接口节点: {}", context.getInstanceId(), node.getNodeName());

        String configJson = node.getConfigJson();
        if (configJson == null || configJson.trim().isEmpty()) {
            return NodeExecuteResult.fail("接口节点缺少配置");
        }

        JSONObject config = JSON.parseObject(configJson);
        String apiCode = config.getString("apiCode");

        if (apiCode == null || apiCode.isEmpty()) {
            return NodeExecuteResult.fail("接口节点未绑定API");
        }

        // 查询接口配置
        ApiCatalog apiCatalog = apiCatalogService.getOne(
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ApiCatalog>()
                        .eq("api_code", apiCode)
                        .eq("del_flag", 0)
        );

        if (apiCatalog == null) {
            return NodeExecuteResult.fail("接口不存在: " + apiCode);
        }

        // 解析输入映射，从上下文组装参数（支持变量/常量）
        Map<String, String> headers = new HashMap<>();
        Map<String, String> queryParams = new HashMap<>();
        Object body = null;

        String inputMapping = node.getInputMapping();
        log.info("[流程实例:{}] API节点 inputMapping={}, context={}",
                context.getInstanceId(), inputMapping, context.toJsonString());
        if (inputMapping != null && !inputMapping.isEmpty()) {
            JSONArray mappings = JSON.parseArray(inputMapping);
            for (int i = 0; i < mappings.size(); i++) {
                JSONObject map = mappings.getJSONObject(i);
                String source = map.getString("source");
                String target = map.getString("target");
                String type = map.getString("type");
                // type = "const" 时直接使用 source 作为常量值，否则从上下文取值
                Object value = "const".equals(type) ? source : context.getByPath(source);
                log.info("[流程实例:{}] 映射解析: source={}, target={}, type={}, value={}",
                        context.getInstanceId(), source, target, type, value);
                if (value == null) continue;

                // target 格式：header.xxx / body.xxx / body.xxx.yyy / query.xxx
                if (target.startsWith("header.")) {
                    headers.put(target.substring(7), String.valueOf(value));
                } else if (target.startsWith("body.")) {
                    if (body == null) body = new JSONObject();
                    String bodyPath = target.substring(5);
                    setNestedValue((JSONObject) body, bodyPath, value);
                } else if (target.startsWith("query.")) {
                    queryParams.put(target.substring(6), String.valueOf(value));
                }
            }
        }
        log.info("[流程实例:{}] 组装请求参数: headers={}, body={}, queryParams={}",
                context.getInstanceId(), headers, body, queryParams);

        try {
            // 执行 HTTP 请求
            JSONObject result = httpRequestService.execute(apiCatalog, headers, body, queryParams);
            int statusCode = result.getIntValue("statusCode");
            log.info("[流程实例:{}] 接口调用完成: status={}",
                    context.getInstanceId(), statusCode);

            // 检查 HTTP 状态码，非 2xx 视为失败
            if (statusCode < 200 || statusCode >= 300) {
                String errorMsg = result.getString("body");
                return NodeExecuteResult.fail("接口调用失败, HTTP状态码: " + statusCode + ", 响应: " + errorMsg);
            }

            // 检查业务响应码（如果响应体是统一包装格式 R<T>）
            String bodyStr = result.getString("body");
            if (bodyStr != null && !bodyStr.isEmpty()) {
                try {
                    JSONObject bodyJson = JSON.parseObject(bodyStr);
                    if (bodyJson.containsKey("code")) {
                        int bizCode = bodyJson.getIntValue("code");
                        if (bizCode != 200) {
                            String bizMsg = bodyJson.getString("msg");
                            return NodeExecuteResult.fail("接口调用失败, 业务码: " + bizCode + ", 错误: " + bizMsg);
                        }
                    }
                } catch (Exception e) {
                    // body 不是 JSON，忽略业务码检查
                }
            }

            // 查询接口定义的返回参数（只取最上层，parent_id = 0）
            List<ApiParam> responseParams = apiParamService.list(
                    new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<ApiParam>()
                            .eq("api_id", apiCatalog.getId())
                            .eq("param_type", "response")
                            .eq("del_flag", 0)
                            .and(qw -> qw.eq("parent_id", 0).or().isNull("parent_id"))
                            .orderByAsc("sort_no")
            );

            // 解析响应体为 JSON（如果可能）
            JSONObject responseBody = null;
            if (bodyStr != null && !bodyStr.isEmpty()) {
                try {
                    responseBody = JSON.parseObject(bodyStr);
                } catch (Exception e) {
                    // body 不是 JSON，忽略自动映射
                }
            }

            // 自动将返回参数写入上下文（无需手动配置输出映射）
            if (responseBody != null && responseParams != null && !responseParams.isEmpty()) {
                for (ApiParam param : responseParams) {
                    String paramKey = param.getParamKey();
                    if (paramKey == null || paramKey.isEmpty()) continue;
                    Object value = responseBody.getByPath(paramKey);
                    if (value != null) {
                        context.set(paramKey, value);
                    }
                }
            }

            // 解析自定义输出映射，将结果写回上下文（作为补充和覆盖）
            String outputMapping = node.getOutputMapping();
            if (outputMapping != null && !outputMapping.isEmpty()) {
                JSONArray mappings = JSON.parseArray(outputMapping);
                for (int i = 0; i < mappings.size(); i++) {
                    JSONObject map = mappings.getJSONObject(i);
                    String source = map.getString("source"); // result.xxx
                    String target = map.getString("target"); // context.xxx
                    Object value = resolvePath(result, source);
                    if (value != null) {
                        context.set(target.replace("context.", ""), value);
                    }
                }
            }

            return NodeExecuteResult.success(result);
        } catch (Exception e) {
            log.error("[流程实例:{}] 接口调用失败: {}", context.getInstanceId(), apiCode, e);
            return NodeExecuteResult.fail("接口调用失败: " + e.getMessage());
        }
    }

    /**
     * 设置嵌套 JSON 值，支持 body.params.a0188 这样的嵌套路径
     */
    private void setNestedValue(JSONObject json, String path, Object value) {
        String[] keys = path.split("\\.");
        JSONObject current = json;
        for (int i = 0; i < keys.length - 1; i++) {
            String key = keys[i];
            if (!current.containsKey(key) || !(current.get(key) instanceof JSONObject)) {
                current.put(key, new JSONObject());
            }
            current = current.getJSONObject(key);
        }
        current.put(keys[keys.length - 1], value);
    }

    /**
     * 从结果对象中解析路径
     * 支持 result.xxx、body.xxx 等前缀，其中 body.xxx 会从响应体 JSON 中解析
     */
    private Object resolvePath(JSONObject result, String path) {
        if (path == null || path.isEmpty()) return null;
        String trimmed = path.trim();
        if (trimmed.startsWith("result.")) trimmed = trimmed.substring(7);
        
        // 如果路径以 body. 开头，尝试从响应体 JSON 中解析
        if (trimmed.startsWith("body.")) {
            String innerPath = trimmed.substring(5);
            String bodyStr = result.getString("body");
            if (bodyStr != null && !bodyStr.isEmpty()) {
                try {
                    JSONObject bodyJson = JSON.parseObject(bodyStr);
                    return bodyJson.getByPath(innerPath);
                } catch (Exception e) {
                    // body 不是 JSON，回退到从 result 取
                }
            }
        }
        
        return result.getByPath(trimmed);
    }
}
