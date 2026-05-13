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

import java.util.List;

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
        for (ApiParam p : params) {
            p.setApiId(id);
        }
        apiParamService.saveBatch(params);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        apiCatalogService.removeById(id);
        apiParamService.remove(new QueryWrapper<ApiParam>().eq("api_id", id));
        return R.ok();
    }
}
