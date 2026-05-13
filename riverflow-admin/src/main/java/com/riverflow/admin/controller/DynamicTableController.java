package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.DynamicTableColumnService;
import com.riverflow.admin.service.DynamicTableService;
import com.riverflow.api.entity.DynamicTable;
import com.riverflow.api.entity.DynamicTableColumn;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 动态表管理
 */
@Slf4j
@RestController
@RequestMapping("/dynamic-table")
public class DynamicTableController {

    @Autowired
    private DynamicTableService dynamicTableService;
    @Autowired
    private DynamicTableColumnService dynamicTableColumnService;

    @GetMapping("/list")
    public R<Page<DynamicTable>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Page<DynamicTable> pageParam = new Page<>(page, size);
        QueryWrapper<DynamicTable> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        qw.orderByDesc("create_time");
        return R.ok(dynamicTableService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    public R<DynamicTable> getById(@PathVariable Long id) {
        return R.ok(dynamicTableService.getById(id));
    }

    @GetMapping("/{id}/columns")
    public R<List<DynamicTableColumn>> getColumns(@PathVariable Long id) {
        return R.ok(dynamicTableColumnService.getColumnsByTableId(id));
    }

    @PostMapping
    public R<Long> save(@RequestBody DynamicTable table) {
        dynamicTableService.saveOrUpdate(table);
        return R.ok(table.getId());
    }

    @PostMapping("/{id}/columns")
    public R<Void> saveColumns(@PathVariable Long id, @RequestBody List<DynamicTableColumn> columns) {
        // 删除旧字段
        dynamicTableColumnService.remove(new QueryWrapper<DynamicTableColumn>().eq("table_id", id));
        // 保存新字段
        for (DynamicTableColumn col : columns) {
            col.setTableId(id);
        }
        dynamicTableColumnService.saveBatch(columns);
        return R.ok();
    }

    @PostMapping("/{id}/gen-api")
    public R<String> generateApi(@PathVariable Long id) {
        DynamicTable table = dynamicTableService.getById(id);
        if (table == null) return R.fail("表不存在");
        // TODO: 动态生成CRUD接口
        return R.ok("API生成成功");
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        dynamicTableService.removeById(id);
        dynamicTableColumnService.remove(new QueryWrapper<DynamicTableColumn>().eq("table_id", id));
        return R.ok();
    }
}
