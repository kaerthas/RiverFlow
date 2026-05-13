package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.infra.dynamicds.DynamicDataSourceService;
import com.riverflow.admin.service.DatasourceService;
import com.riverflow.api.entity.Datasource;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据源管理
 */
@Slf4j
@RestController
@RequestMapping("/datasource")
public class DatasourceController {

    @Autowired
    private DatasourceService datasourceService;
    @Autowired
    private DynamicDataSourceService dynamicDataSourceService;

    @GetMapping("/list")
    public R<Page<Datasource>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<Datasource> pageParam = new Page<>(page, size);
        QueryWrapper<Datasource> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        qw.orderByDesc("create_time");
        return R.ok(datasourceService.page(pageParam, qw));
    }

    @PostMapping
    public R<Long> save(@RequestBody Datasource datasource) {
        datasourceService.saveOrUpdate(datasource);
        // 如果状态为启用，动态加载
        if (datasource.getStatus() != null && datasource.getStatus() == 1) {
            try {
                dynamicDataSourceService.addDataSource(datasource);
            } catch (Exception e) {
                log.error("动态加载数据源失败: {}", e.getMessage());
            }
        }
        return R.ok(datasource.getId());
    }

    @PutMapping
    public R<Long> update(@RequestBody Datasource datasource) {
        datasourceService.updateById(datasource);
        return R.ok(datasource.getId());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Datasource ds = datasourceService.getById(id);
        if (ds != null) {
            dynamicDataSourceService.removeDataSource(ds.getDsCode());
        }
        datasourceService.removeById(id);
        return R.ok();
    }

    @GetMapping("/{id}/test")
    public R<String> testConnection(@PathVariable Long id) {
        Datasource ds = datasourceService.getById(id);
        if (ds == null) return R.fail("数据源不存在");
        boolean success = dynamicDataSourceService.testConnection(ds);
        return success ? R.ok("连接成功") : R.fail("连接失败");
    }

    @PostMapping("/{id}/reload")
    public R<Void> reload(@PathVariable Long id) {
        Datasource ds = datasourceService.getById(id);
        if (ds == null) return R.fail("数据源不存在");
        dynamicDataSourceService.removeDataSource(ds.getDsCode());
        dynamicDataSourceService.addDataSource(ds);
        return R.ok();
    }
}
