package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.riverflow.admin.service.SysDeptService;
import com.riverflow.api.entity.SysDept;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统部门管理
 */
@Slf4j
@RestController
@RequestMapping("/system/dept")
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;

    @GetMapping("/list")
    @PreAuthorize("@ss.hasPerm('system:dept:list')")
    public R<List<SysDept>> list(@RequestParam(required = false) String deptName,
                                 @RequestParam(required = false) Integer status) {
        QueryWrapper<SysDept> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (status != null) qw.eq("status", status);
        if (deptName != null && !deptName.isEmpty()) qw.like("dept_name", deptName);
        qw.orderByAsc("sort_no");
        List<SysDept> depts = sysDeptService.list(qw);
        return R.ok(buildTree(depts));
    }

    @GetMapping("/all")
    @PreAuthorize("@ss.hasPerm('system:dept:list')")
    public R<List<SysDept>> all() {
        List<SysDept> depts = sysDeptService.getAllEnabled();
        return R.ok(buildTree(depts));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('system:dept:list')")
    public R<SysDept> getById(@PathVariable Long id) {
        return R.ok(sysDeptService.getById(id));
    }

    @PostMapping
    @PreAuthorize("@ss.hasPerm('system:dept:add')")
    public R<Long> save(@RequestBody SysDept dept) {
        sysDeptService.save(dept);
        return R.ok(dept.getId());
    }

    @PutMapping
    @PreAuthorize("@ss.hasPerm('system:dept:edit')")
    public R<Long> update(@RequestBody SysDept dept) {
        sysDeptService.updateById(dept);
        return R.ok(dept.getId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.hasPerm('system:dept:delete')")
    public R<Void> delete(@PathVariable Long id) {
        // 检查是否存在子部门
        QueryWrapper<SysDept> qw = new QueryWrapper<>();
        qw.eq("parent_id", id);
        qw.eq("del_flag", 0);
        long count = sysDeptService.count(qw);
        if (count > 0) {
            return R.fail("存在子部门，无法删除");
        }
        sysDeptService.removeById(id);
        return R.ok();
    }

    private List<SysDept> buildTree(List<SysDept> depts) {
        if (depts == null || depts.isEmpty()) {
            return new ArrayList<>();
        }
        List<SysDept> sorted = depts.stream()
                .sorted(Comparator.comparing(SysDept::getSortNo, Comparator.nullsLast(Integer::compareTo))
                        .thenComparing(SysDept::getId))
                .collect(Collectors.toList());
        Map<Long, SysDept> deptMap = new HashMap<>();
        sorted.forEach(d -> deptMap.put(d.getId(), d));
        List<SysDept> tree = new ArrayList<>();
        sorted.forEach(d -> {
            Long parentId = d.getParentId();
            if (parentId == null || parentId == 0L) {
                tree.add(d);
            } else {
                SysDept parent = deptMap.get(parentId);
                if (parent != null) {
                    parent.getChildren().add(d);
                }
            }
        });
        return tree;
    }
}
