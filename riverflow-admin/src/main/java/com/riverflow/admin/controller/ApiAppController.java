package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.ApiAppService;
import com.riverflow.admin.service.ApiCatalogService;
import com.riverflow.api.entity.ApiApp;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 接口应用/目录管理
 */
@Slf4j
@RestController
@RequestMapping("/api-app")
public class ApiAppController {

    @Autowired
    private ApiAppService apiAppService;
    @Autowired
    private ApiCatalogService apiCatalogService;

    @GetMapping("/list")
    public R<Page<ApiApp>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "100") Integer size,
            @RequestParam(required = false) String keyword) {
        Page<ApiApp> pageParam = new Page<>(page, size);
        QueryWrapper<ApiApp> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (keyword != null && !keyword.isEmpty()) {
            qw.and(w -> w.like("app_code", keyword).or().like("app_name", keyword));
        }
        qw.orderByAsc("sort_no").orderByDesc("create_time");
        Page<ApiApp> result = apiAppService.page(pageParam, qw);
        return R.ok(result);
    }

    @GetMapping("/list-all")
    public R<List<ApiApp>> listAll(@RequestParam(required = false) Integer status) {
        QueryWrapper<ApiApp> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (status != null) {
            qw.eq("status", status);
        }
        qw.orderByAsc("sort_no").orderByDesc("create_time");
        return R.ok(apiAppService.list(qw));
    }

    @GetMapping("/{id}")
    public R<ApiApp> getById(@PathVariable Long id) {
        return R.ok(apiAppService.getById(id));
    }

    @PostMapping
    public R<String> save(@RequestBody ApiApp apiApp) {
        apiAppService.saveOrUpdate(apiApp);
        return R.ok(String.valueOf(apiApp.getId()));
    }

    @PutMapping
    public R<String> update(@RequestBody ApiApp apiApp) {
        apiAppService.updateById(apiApp);
        return R.ok(String.valueOf(apiApp.getId()));
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 检查是否有关联的接口
        long count = apiCatalogService.count(
                new QueryWrapper<com.riverflow.api.entity.ApiCatalog>()
                        .eq("app_id", id)
                        .eq("del_flag", 0));
        if (count > 0) {
            return R.fail("该应用下存在 " + count + " 个接口，请先移除或迁移接口后再删除应用");
        }
        apiAppService.removeById(id);
        return R.ok();
    }

    /**
     * 批量获取应用下的接口数量
     */
    @GetMapping("/api-counts")
    public R<Map<Long, Long>> getApiCounts(@RequestParam String appIds) {
        if (appIds == null || appIds.isEmpty()) {
            return R.ok(new HashMap<>());
        }
        List<Long> idList = Arrays.stream(appIds.split(","))
                .map(Long::valueOf)
                .collect(Collectors.toList());
        List<Map<String, Object>> list = apiCatalogService.listMaps(
                new QueryWrapper<com.riverflow.api.entity.ApiCatalog>()
                        .select("app_id, count(*) as cnt")
                        .in("app_id", idList)
                        .eq("del_flag", 0)
                        .groupBy("app_id"));
        Map<Long, Long> result = list.stream()
                .collect(Collectors.toMap(
                        m -> Long.valueOf(String.valueOf(m.get("app_id"))),
                        m -> Long.valueOf(String.valueOf(m.get("cnt"))),
                        (a, b) -> a));
        return R.ok(result);
    }
}
