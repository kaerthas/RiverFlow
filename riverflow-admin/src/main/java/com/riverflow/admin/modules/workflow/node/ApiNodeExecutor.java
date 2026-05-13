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

        // 解析输入映射，从上下文组装参数
        Map<String, String> headers = new HashMap<>();
        Object body = null;

        String inputMapping = node.getInputMapping();
        if (inputMapping != null && !inputMapping.isEmpty()) {
            JSONArray mappings = JSON.parseArray(inputMapping);
            for (int i = 0; i < mappings.size(); i++) {
                JSONObject map = mappings.getJSONObject(i);
                String source = map.getString("source");
                String target = map.getString("target");
                Object value = context.getByPath(source);
                if (value == null) continue;

                // target 格式：header.xxx / body.xxx / query.xxx
                if (target.startsWith("header.")) {
                    headers.put(target.substring(7), String.valueOf(value));
                } else if (target.startsWith("body.")) {
                    if (body == null) body = new JSONObject();
                    ((JSONObject) body).put(target.substring(5), value);
                }
            }
        }

        try {
            // 执行 HTTP 请求
            JSONObject result = httpRequestService.execute(apiCatalog, headers, body);
            log.info("[流程实例:{}] 接口调用完成: status={}",
                    context.getInstanceId(), result.getIntValue("statusCode"));

            // 解析输出映射，将结果写回上下文
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
     * 从结果对象中解析路径
     */
    private Object resolvePath(JSONObject result, String path) {
        if (path == null || path.isEmpty()) return null;
        String trimmed = path.trim();
        if (trimmed.startsWith("result.")) trimmed = trimmed.substring(7);
        if (trimmed.startsWith("body.")) trimmed = trimmed.substring(5);
        return result.getByPath(trimmed);
    }
}
