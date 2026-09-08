package com.riverflow.admin.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息中心示例接口（模拟第三方消息中心）
 * 用于中转平台调试和演示。
 * <p>
 * 对应平台注册接口：messagecenter-send-sms（见 db/message_center_sms_api_init.sql），
 * 调试时将接口 URL 指向本模拟地址即可：
 * http://localhost:8080/example/messagecenter/sendSMSMsg
 * <p>
 * 请求方式：POST，Content-Type: application/x-www-form-urlencoded
 * 返回格式遵循接口文档（裸 JSON，不使用平台统一 R 包装）：
 * 成功 {"state":200, "SMSState":"200"}；
 * 部分号码失败 {"state":200, "SMSState":"300", "SMSError":..., "SMSErrorPhone":...}；
 * 调用失败 {"state":300, "error":...}
 */
@Slf4j
@RestController
@RequestMapping("/example/messagecenter")
public class ExampleMessageCenterController {

    /**
     * 发送短信接口
     * 入参（form 表单）：
     * toUsers      接收消息的用户id（与 phoneList 二选一必填其一）
     * appCode      应用编码
     * content      消息内容
     * subject      消息标题
     * sender       消息发送者（系统消息统一为 system）
     * itemId       业务id（可选）
     * level        消息级别：0-一般 1-紧急
     * smsChannelId 短信通道ID
     * phoneList    手机号码，多个以英文逗号隔开
     */
    @PostMapping("/sendSMSMsg")
    public Map<String, Object> sendSMSMsg(@RequestParam Map<String, String> params) {
        String toUsers = trim(params.get("toUsers"));
        String appCode = trim(params.get("appCode"));
        String content = trim(params.get("content"));
        String subject = trim(params.get("subject"));
        String sender = trim(params.get("sender"));
        String itemId = trim(params.get("itemId"));
        String level = trim(params.get("level"));
        String smsChannelId = trim(params.get("smsChannelId"));
        String phoneList = trim(params.get("phoneList"));

        log.info("[消息中心模拟] 收到发送短信请求: appCode={}, subject={}, toUsers={}, phoneList={}, level={}, itemId={}",
                appCode, subject, toUsers, phoneList, level, itemId);

        // 必填校验（按接口文档空值=N 的字段）
        if (isEmpty(appCode)) return fail("appCode 不能为空");
        if (isEmpty(content)) return fail("content 不能为空");
        if (isEmpty(subject)) return fail("subject 不能为空");
        if (isEmpty(sender)) return fail("sender 不能为空");
        if (isEmpty(level)) return fail("level 不能为空");
        if (isEmpty(smsChannelId)) return fail("smsChannelId 不能为空");
        if (isEmpty(toUsers) && isEmpty(phoneList)) {
            return fail("phoneList 和 toUsers 必须有一个不为空");
        }

        // 模拟逐号码发送：格式非法（非1开头的11位数字）的号码判定为发送失败
        List<String> failedPhones = new ArrayList<>();
        if (!isEmpty(phoneList)) {
            for (String phone : phoneList.split(",")) {
                String p = phone.trim();
                if (!p.matches("1\\d{10}")) {
                    failedPhones.add(p);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("state", 200);
        if (failedPhones.isEmpty()) {
            result.put("SMSState", "200");
            log.info("[消息中心模拟] 短信发送成功: subject={}, toUsers={}, phoneList={}", subject, toUsers, phoneList);
        } else {
            result.put("SMSState", "300");
            result.put("SMSError", "部分手机号码发送失败");
            result.put("SMSErrorPhone", String.join(",", failedPhones));
            log.warn("[消息中心模拟] 部分号码发送失败: SMSErrorPhone={}", failedPhones);
        }
        return result;
    }

    private Map<String, Object> fail(String error) {
        Map<String, Object> result = new HashMap<>();
        result.put("state", 300);
        result.put("error", error);
        log.warn("[消息中心模拟] 接口调用失败: {}", error);
        return result;
    }

    private String trim(String s) {
        return s == null ? null : s.trim();
    }

    private boolean isEmpty(String s) {
        return s == null || s.isEmpty();
    }
}
