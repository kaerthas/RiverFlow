package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.admin.service.ApiParamService;
import com.riverflow.api.entity.ApiCatalog;
import com.riverflow.api.entity.ApiParam;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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

    @GetMapping("/list")
    public R<Page<ApiCatalog>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String apiCode,
            @RequestParam(required = false) String apiName) {
        Page<ApiCatalog> pageParam = new Page<>(page, size);
        QueryWrapper<ApiCatalog> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (apiCode != null && !apiCode.isEmpty()) qw.like("api_code", apiCode);
        if (apiName != null && !apiName.isEmpty()) qw.like("api_name", apiName);
        qw.orderByDesc("create_time");
        return R.ok(apiCatalogService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    public R<ApiCatalog> getById(@PathVariable Long id) {
        return R.ok(apiCatalogService.getById(id));
    }

    @GetMapping("/{id}/params")
    public R<List<ApiParam>> getParams(@PathVariable Long id) {
        return R.ok(apiParamService.getParamsByApiId(id));
    }

    @PostMapping
    public R<Long> save(@RequestBody ApiCatalog apiCatalog) {
        apiCatalogService.saveOrUpdate(apiCatalog);
        return R.ok(apiCatalog.getId());
    }

    @PutMapping
    public R<Long> update(@RequestBody ApiCatalog apiCatalog) {
        apiCatalogService.updateById(apiCatalog);
        return R.ok(apiCatalog.getId());
    }

    @PostMapping("/{id}/params")
    public R<Void> saveParams(@PathVariable Long id, @RequestBody List<ApiParam> params) {
        apiParamService.remove(new QueryWrapper<ApiParam>().eq("api_id", id));

        // 建立 clientId -> dbId 映射
        Map<String, Long> clientIdMap = new HashMap<>();

        // 第一轮：保存所有参数（parentId 先设为 0，id 置空让数据库自动生成）
        for (ApiParam p : params) {
            p.setId(null);
            p.setApiId(id);
            p.setParentId(0L);
            apiParamService.save(p);
            if (p.getClientId() != null && !p.getClientId().isEmpty()) {
                clientIdMap.put(p.getClientId(), p.getId());
            }
        }

        // 第二轮：更新子参数的 parentId
        for (ApiParam p : params) {
            if (p.getParentClientId() != null && !p.getParentClientId().isEmpty()
                    && !"0".equals(p.getParentClientId())) {
                Long realParentId = clientIdMap.get(p.getParentClientId());
                if (realParentId != null) {
                    p.setParentId(realParentId);
                    apiParamService.updateById(p);
                }
            }
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
