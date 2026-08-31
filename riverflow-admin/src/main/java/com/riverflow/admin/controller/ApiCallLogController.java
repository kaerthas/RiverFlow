package com.riverflow.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.riverflow.admin.service.ApiCallLogService;
import com.riverflow.api.entity.ApiCallLog;
import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 接口调用日志 Controller
 */
@Slf4j
@RestController
@RequestMapping("/api-call-log")
public class ApiCallLogController {

    @Autowired
    private ApiCallLogService apiCallLogService;

    @GetMapping("/list")
    public R<Page<ApiCallLog>> list(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "apiCode", required = false) String apiCode,
            @RequestParam(value = "callStatus", required = false) Integer callStatus,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "startTime", required = false) String startTime,
            @RequestParam(value = "endTime", required = false) String endTime) {
        Page<ApiCallLog> pageParam = new Page<>(page, size);
        QueryWrapper<ApiCallLog> qw = new QueryWrapper<>();
        // 列表页不查询大文本字段，详情接口单独获取
        qw.select(ApiCallLog.class, field -> !"request_headers".equals(field.getColumn())
                && !"request_body".equals(field.getColumn())
                && !"response_body".equals(field.getColumn()));
        qw.eq("del_flag", 0);
        if (apiCode != null && !apiCode.isEmpty()) qw.like("api_code", apiCode);
        if (callStatus != null) qw.eq("call_status", callStatus);
        if (source != null && !source.isEmpty()) qw.eq("source", source);
        if (startTime != null && !startTime.isEmpty()) qw.ge("create_time", startTime);
        if (endTime != null && !endTime.isEmpty()) qw.le("create_time", endTime);
        qw.orderByDesc("create_time");
        return R.ok(apiCallLogService.page(pageParam, qw));
    }

    @GetMapping("/{id}")
    public R<ApiCallLog> getById(@PathVariable Long id) {
        ApiCallLog callLog = apiCallLogService.getById(id);
        if (callLog == null) return R.fail("日志不存在");
        return R.ok(callLog);
    }

    @DeleteMapping("/{id}")
    public R<Void> delete(@PathVariable Long id) {
        ApiCallLog callLog = new ApiCallLog();
        callLog.setId(id);
        callLog.setDelFlag(1);
        apiCallLogService.updateById(callLog);
        return R.ok();
    }
}
