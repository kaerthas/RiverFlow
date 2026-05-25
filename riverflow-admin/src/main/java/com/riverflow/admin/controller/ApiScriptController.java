package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.ApiScriptService;
import com.riverflow.api.entity.ApiScript;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 脚本库管理
 */
@Slf4j
@RestController
@RequestMapping("/api-script")
public class ApiScriptController {

    @Autowired
    private ApiScriptService apiScriptService;

    @GetMapping("/list")
    public R<Page<ApiScript>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String scriptType,
            @RequestParam(required = false) String scriptCode,
            @RequestParam(required = false) String scriptName) {
        Page<ApiScript> pageParam = new Page<>(page, size);
        QueryWrapper<ApiScript> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (scriptType != null && !scriptType.isEmpty()) qw.eq("script_type", scriptType);
        if (scriptCode != null && !scriptCode.isEmpty()) qw.like("script_code", scriptCode);
        if (scriptName != null && !scriptName.isEmpty()) qw.like("script_name", scriptName);
        qw.orderByDesc("create_time");
        return R.ok(apiScriptService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    public R<ApiScript> getById(@PathVariable Long id) {
        return R.ok(apiScriptService.getById(id));
    }

    @PostMapping
    public R<Long> save(@RequestBody ApiScript script) {
        apiScriptService.saveOrUpdate(script);
        return R.ok(script.getId());
    }

    @PutMapping
    public R<Long> update(@RequestBody ApiScript script) {
        apiScriptService.updateById(script);
        return R.ok(script.getId());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        apiScriptService.removeById(id);
        return R.ok();
    }
}
