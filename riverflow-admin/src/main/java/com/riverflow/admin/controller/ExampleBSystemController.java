package com.riverflow.admin.controller;

import com.riverflow.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * B系统示例接口（模拟第三方系统）
 * 用于中转平台调试和演示
 */
@Slf4j
@RestController
@RequestMapping("/example/b-sys")
public class ExampleBSystemController {

    /**
     * B系统提交接口
     * 入参: { "projectName": "...", "projectNo": "..." }
     * 出参: { "code": 200, "data": { "orderId": "...", "projectName": "...", "projectNo": "...", "status": "processing" } }
     */
    @PostMapping("/submit")
    public R<Map<String, Object>> submit(@RequestBody Map<String, Object> params) {
        String projectName = params.get("projectName") != null ? String.valueOf(params.get("projectName")) : "";
        String projectNo = params.get("projectNo") != null ? String.valueOf(params.get("projectNo")) : "";

        log.info("[B系统模拟] 收到提交请求: projectName={}, projectNo={}", projectName, projectNo);

        // 模拟生成B系统订单号
        String orderId = "B" + System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("projectName", projectName);
        data.put("projectNo", projectNo);
        data.put("status", "processing");
        data.put("createTime", LocalDateTime.now().toString());

        log.info("[B系统模拟] 返回结果: orderId={}", orderId);
        return R.ok(data);
    }
}
