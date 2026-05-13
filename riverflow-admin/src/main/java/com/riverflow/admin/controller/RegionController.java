package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.service.RegionService;
import com.riverflow.api.entity.Region;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 行政区划管理
 */
@Slf4j
@RestController
@RequestMapping("/region")
public class RegionController {

    @Autowired
    private RegionService regionService;

    @GetMapping("/tree")
    public R<List<Region>> tree() {
        return R.ok(regionService.buildRegionTree());
    }

    @GetMapping("/list")
    public R<List<Region>> list(@RequestParam(required = false) String parentCode) {
        QueryWrapper<Region> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (parentCode != null) qw.eq("parent_code", parentCode);
        qw.orderByAsc("sort_no");
        return R.ok(regionService.list(qw));
    }

    @PostMapping
    public R<Long> save(@RequestBody Region region) {
        regionService.saveOrUpdate(region);
        return R.ok(region.getId());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        regionService.removeById(id);
        return R.ok();
    }
}
