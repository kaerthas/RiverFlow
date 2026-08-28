package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.ItemService;
import com.riverflow.api.entity.Item;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 事项管理
 */
@Slf4j
@RestController
@RequestMapping("/item")
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping("/list")
    public R<Page<Item>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "regionCode", required = false) String regionCode,
            @RequestParam(value = "itemCode", required = false) String itemCode,
            @RequestParam(value = "itemName", required = false) String itemName) {
        Page<Item> pageParam = new Page<>(page, size);
        QueryWrapper<Item> qw = new QueryWrapper<>();
        qw.eq("del_flag", 0);
        if (regionCode != null && !regionCode.isEmpty()) qw.eq("region_code", regionCode);
        if (itemCode != null && !itemCode.isEmpty()) qw.like("item_code", itemCode);
        if (itemName != null && !itemName.isEmpty()) qw.like("item_name", itemName);
        qw.orderByDesc("create_time");
        return R.ok(itemService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    public R<Item> getById(@PathVariable Long id) {
        return R.ok(itemService.getById(id));
    }

    @PostMapping
    public R<Long> save(@RequestBody Item item) {
        itemService.saveOrUpdate(item);
        return R.ok(item.getId());
    }

    @PutMapping
    public R<Long> update(@RequestBody Item item) {
        itemService.updateById(item);
        return R.ok(item.getId());
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        itemService.removeById(id);
        return R.ok();
    }
}
