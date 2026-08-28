package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.infra.openapi.SqlCheckResult;
import com.riverflow.admin.infra.openapi.SqlSafetyChecker;
import com.riverflow.admin.mapper.ApiParamMapper;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.admin.service.ApiParamService;
import com.riverflow.admin.service.FlowDefinitionService;
import com.riverflow.api.entity.ApiCatalog;
import com.riverflow.api.entity.ApiParam;
import com.riverflow.api.entity.FlowDefinition;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 接口目录管理
 */
@Slf4j
@RestController
@RequestMapping("/api-catalog")
public class ApiCatalogController {

    @Autowired
    private ApiCatalogService apiCatalogService;
    @Autowired
    private ApiParamService apiParamService;
    @Autowired
    private ApiParamMapper apiParamMapper;

    @GetMapping("/list")
    public R<Page<ApiCatalog>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "apiCode", required = false) String apiCode,
            @RequestParam(value = "apiName", required = false) String apiName,
            @RequestParam(value = "appId", required = false) Long appId) {
        Page<ApiCatalog> pageParam = new Page<>(page, size);
        QueryWrapper<ApiCatalog> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (appId != null) qw.eq("app_id", appId);
        if (apiCode != null && !apiCode.isEmpty()) qw.like("api_code", apiCode);
        if (apiName != null && !apiName.isEmpty()) qw.like("api_name", apiName);
        qw.orderByDesc("create_time");
        return R.ok(apiCatalogService.page(pageParam, qw));
    }

    @Autowired
    private FlowDefinitionService flowDefinitionService;

    @GetMapping("/{id}")
    public R<ApiCatalog> getById(@PathVariable Long id) {
        ApiCatalog api = apiCatalogService.getById(id);
        // 兼容旧数据：如果 triggerFlowCode 为空但 triggerFlowId 有值，自动回填 flowCode
        if (api != null && (api.getTriggerFlowCode() == null || api.getTriggerFlowCode().isEmpty()) && api.getTriggerFlowId() != null) {
            FlowDefinition def = flowDefinitionService.getById(api.getTriggerFlowId());
            if (def != null) {
                api.setTriggerFlowCode(def.getFlowCode());
            }
        }
        return R.ok(api);
    }

    @GetMapping("/{id}/params")
    public R<List<ApiParam>> getParams(@PathVariable Long id) {
        return R.ok(apiParamService.getParamsByApiId(id));
    }

    @PostMapping
    public R<String> save(@RequestBody ApiCatalog apiCatalog) {
        R<String> validateResult = validateSqlApi(apiCatalog);
        if (validateResult != null) {
            return validateResult;
        }
        normalizeTriggerFlow(apiCatalog);
        apiCatalogService.saveOrUpdate(apiCatalog);
        return R.ok(String.valueOf(apiCatalog.getId()));
    }

    @PutMapping
    public R<String> update(@RequestBody ApiCatalog apiCatalog) {
        R<String> validateResult = validateSqlApi(apiCatalog);
        if (validateResult != null) {
            return validateResult;
        }
        normalizeTriggerFlow(apiCatalog);
        apiCatalogService.updateById(apiCatalog);
        return R.ok(String.valueOf(apiCatalog.getId()));
    }

    private R<String> validateSqlApi(ApiCatalog apiCatalog) {
        if ("sql".equalsIgnoreCase(apiCatalog.getApiType())) {
            String sql = apiCatalog.getUrl();
            if (sql != null && !sql.trim().isEmpty()) {
                SqlCheckResult result = SqlSafetyChecker.validate(sql);
                if (!result.isPassed()) {
                    return R.fail("SQL 校验失败: " + result.getMessage());
                }
            }
        }
        return null;
    }

    private void normalizeTriggerFlow(ApiCatalog api) {
        // 优先使用 triggerFlowCode，有值时清空旧 triggerFlowId，避免混淆
        if (api.getTriggerFlowCode() != null && !api.getTriggerFlowCode().isEmpty()) {
            api.setTriggerFlowId(null);
        }
    }

    @PostMapping("/{id}/params")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> saveParams(@PathVariable Long id, @RequestBody List<ApiParam> params) {
        // 物理删除该接口下的所有旧参数
        apiParamMapper.delete(new QueryWrapper<ApiParam>().eq("api_id", id));

        if (params == null || params.isEmpty()) {
            return R.ok();
        }

        // 过滤掉没有 paramKey 的参数
        List<ApiParam> validParams = params.stream()
                .filter(p -> p.getParamKey() != null && !p.getParamKey().trim().isEmpty())
                .collect(Collectors.toList());

        // 建立 clientId -> dbId 映射
        Map<String, Long> clientIdMap = new HashMap<>();

        // 第一轮：保存所有参数（parentId 先设为 0）
        for (ApiParam p : validParams) {
            p.setId(null);
            p.setApiId(id);
            p.setParentId(0L);
            p.setDelFlag(0);
            apiParamService.save(p);
            String clientId = p.getClientId();
            if (clientId != null && !clientId.isEmpty()) {
                clientIdMap.put(clientId, p.getId());
                log.debug("参数保存成功: clientId={}, dbId={}", clientId, p.getId());
            }
        }

        // 第二轮：更新子参数的 parentId
        for (ApiParam p : validParams) {
            String parentClientId = p.getParentClientId();
            if (parentClientId == null || parentClientId.isEmpty() || "0".equals(parentClientId)) {
                continue;
            }
            Long realParentId = clientIdMap.get(parentClientId);
            if (realParentId == null) {
                log.warn("未找到父参数映射: parentClientId={}, paramKey={}", parentClientId, p.getParamKey());
                continue;
            }
            // 使用 UpdateWrapper 明确只更新 parent_id，避免其他字段干扰
            apiParamService.update(new UpdateWrapper<ApiParam>()
                    .eq("id", p.getId())
                    .set("parent_id", realParentId));
            log.debug("更新子参数 parentId: id={}, paramKey={}, parentId={}", p.getId(), p.getParamKey(), realParentId);
        }

        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        apiCatalogService.removeById(id);
        apiParamService.remove(new QueryWrapper<ApiParam>().eq("api_id", id));
        return R.ok();
    }
}
