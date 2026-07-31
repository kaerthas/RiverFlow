package com.riverflow.admin.controller;

import com.riverflow.common.result.R;
import com.riverflow.common.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * B系统示例接口（模拟第三方系统）
 * 用于中转平台调试和演示
 */
@Slf4j
@RestController
@RequestMapping("/example/b-sys")
public class ExampleBSystemController {

    // 记录每个HTBH的完税查询次数，模拟异步处理效果
    private static final Map<String, Integer> WS_QUERY_COUNTER = new ConcurrentHashMap<>();

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

        // 重置该项目的完税查询计数（模拟新提交）
        WS_QUERY_COUNTER.remove(projectNo);

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

    /**
     * B系统订单单条同步写入接口（模拟写入B库）
     * 入参: { "orderNo": "...", "userName": "...", "amount": "...", "sourceId": 1 }
     * 出参: { "code": 200, "data": { "orderNo": "...", "userName": "...", "amount": "...", "sourceId": 1 } }
     */
    @PostMapping("/insertOrder")
    public R<Map<String, Object>> insertOrder(@RequestBody(required = false) Map<String, Object> params) {
        log.info("[B系统模拟] 收到订单同步请求, params={}", params);
        if (params == null || params.isEmpty()) {
            log.warn("[B系统模拟] 订单同步请求体为空");
            return R.fail(ResultCode.PARAM_ERROR.getCode(), "请求体不能为空");
        }
        String orderNo = String.valueOf(params.getOrDefault("orderNo", ""));
        String userName = String.valueOf(params.getOrDefault("userName", ""));
        Object amountObj = params.get("amount");
        String amount = amountObj != null ? amountObj.toString() : "0.00";
        Object sourceIdObj = params.get("sourceId");
        Long sourceId = sourceIdObj != null ? Long.valueOf(sourceIdObj.toString()) : null;

        log.info("[B系统模拟] 订单同步解析: orderNo={}, userName={}, amount={}, sourceId={}",
                orderNo, userName, amount, sourceId);

        // 使用 INSERT IGNORE 实现幂等写入，避免重复同步时唯一键冲突
        int affected = jdbcTemplate.update(
                "INSERT IGNORE INTO example_order_target (order_no, user_name, amount, source_id, sync_time) VALUES (?, ?, ?, ?, NOW())",
                orderNo, userName, new BigDecimal(amount), sourceId);
        if (affected == 0) {
            log.warn("[B系统模拟] 订单已存在，忽略重复写入: orderNo={}", orderNo);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("orderNo", orderNo);
        data.put("userName", userName);
        data.put("amount", amount);
        data.put("sourceId", sourceId);
        return R.ok(data);
    }

    /**
     * B系统完税状态信息查询接口（C接口）
     * 接口文档：SNSW.FCSB.GETWSQKXX
     *
     * 入参:
     * {
     *   "appid": "XT000",
     *   "password": "SZRZYT",
     *   "intrfaceid": "SNSW.FCSB.GETWSQKXX",
     *   "HTBH": "合同编号",
     *   "QXDM": "区县代码"
     * }
     *
     * 出参:
     * {
     *   "code": 200,
     *   "msg": "success",
     *   "data": {
     *     "RetCode": "000",
     *     "RetMsg": "查询成功",
     *     "Wsbz": "Y" / "N",
     *     "Nsrmc": "纳税人名称",
     *     "Nsrsbh": "纳税人识别号",
     *     "Tdfwdz": "土地房屋地址",
     *     "Ybtse": "税额",
     *     "Pgjyjg": "评估金额",
     *     "Htje": "合同金额",
     *     "Mj": "面积",
     *     "Dzsphm": "电子税票号码",
     *     "Htqdsj": "合同签订时间",
     *     "Htbh": "合同编号",
     *     "WspzPdf": "完税凭证(Base64)"
     *   }
     * }
     */
    @PostMapping("/getWsqkxx")
    public R<Map<String, Object>> getWsqkxx(@RequestBody Map<String, Object> params) {
        String appid = String.valueOf(params.getOrDefault("appid", ""));
        String password = String.valueOf(params.getOrDefault("password", ""));
        String intrfaceid = String.valueOf(params.getOrDefault("intrfaceid", ""));
        String htbh = String.valueOf(params.getOrDefault("HTBH", ""));
        String qxdm = String.valueOf(params.getOrDefault("QXDM", ""));

        log.info("[B系统模拟] 收到完税状态查询: appid={}, intrfaceid={}, HTBH={}, QXDM={}",
                appid, intrfaceid, htbh, qxdm);

        // 参数校验（模拟真实系统的鉴权）
        if (!"XT000".equals(appid) || !"SZRZYT".equals(password) || !"SNSW.FCSB.GETWSQKXX".equals(intrfaceid)) {
            Map<String, Object> errData = new HashMap<>();
            errData.put("RetCode", "999");
            errData.put("RetMsg", "接口鉴权失败：appid、password或intrfaceid不正确");
            log.warn("[B系统模拟] 鉴权失败: appid={}, password={}, intrfaceid={}", appid, password, intrfaceid);
            return R.ok(errData);
        }

        // 模拟异步处理：前2次查询返回未完税(N)，第3次起返回已完税(Y)
        int count = WS_QUERY_COUNTER.merge(htbh, 1, Integer::sum);
        String wsbz = count < 3 ? "N" : "Y";

        Map<String, Object> data = new HashMap<>();
        data.put("RetCode", "000");
        data.put("RetMsg", "查询成功");
        data.put("Wsbz", wsbz);
        data.put("Nsrmc", "模拟纳税人名称");
        data.put("Nsrsbh", "91350000M0001XXXX1");
        data.put("Tdfwdz", "模拟土地房屋地址");
        data.put("Ybtse", "15000.00");
        data.put("Pgjyjg", "2000000.00");
        data.put("Htje", "1800000.00");
        data.put("Mj", "120.50");
        data.put("Dzsphm", "SP" + System.currentTimeMillis());
        data.put("Htqdsj", "2026-05-16");
        data.put("Htbh", htbh);
        // 未完税时返回空，已完税时返回模拟PDF Base64片段
        data.put("WspzPdf", "Y".equals(wsbz)
                ? "JVBERi0xLjQKJeLjz9MKMyAwIG9iago8PAovVHlwZSAvUGFnZQo+PgplbmRvYmoK"
                : "");

        log.info("[B系统模拟] 返回完税状态: HTBH={}, Wsbz={}, 查询次数={}", htbh, wsbz, count);
        return R.ok(data);
    }

    /**
     * 接收纯文本请求接口（测试 application/text）
     * 支持任意 Content-Type，将请求体作为纯文本读取
     *
     * 测试方式：
     * curl -X POST -H "Content-Type: application/text" -d "你好，这是一段测试文本" http://localhost:8080/example/b-sys/text/receive
     *
     * 返回：接收到的文本内容和元数据
     */
    @PostMapping("/text/receive")
    public R<Map<String, Object>> receiveText(HttpServletRequest request) {
        try {
            // 读取请求体内容
            StringBuilder requestBody = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            String content = requestBody.toString();
            String contentType = request.getContentType();
            int contentLength = request.getContentLength();

            log.info("[B系统模拟] 接收纯文本请求: Content-Type={}, Length={}, Content={}",
                    contentType, contentLength, content);

            Map<String, Object> data = new HashMap<>();
            data.put("receivedText", content);
            data.put("contentType", contentType);
            data.put("contentLength", contentLength);
            data.put("timestamp", LocalDateTime.now().toString());
            data.put("message", "纯文本请求接收成功");

            return R.ok(data);
        } catch (Exception e) {
            log.error("[B系统模拟] 读取纯文本请求失败", e);
            return R.fail("读取请求失败: " + e.getMessage());
        }
    }

    /**
     * 返回纯文本响应接口（测试 application/text 响应）
     * 返回格式化的纯文本信息
     *
     * 测试方式：
     * curl -X GET http://localhost:8080/example/b-sys/text/send
     *
     * 返回：纯文本格式的响应
     */
    @GetMapping(value = "/text/send", produces = "application/text;charset=UTF-8")
    @ResponseBody
    public String sendText() {
        String response = "=== 纯文本响应测试 ===\n" +
                "时间: " + LocalDateTime.now() + "\n" +
                "状态: 成功\n" +
                "消息: 这是来自B系统的纯文本响应\n" +
                "提示: Content-Type=application/text\n" +
                "==================";

        log.info("[B系统模拟] 发送纯文本响应: length={}", response.length());
        return response;
    }

    /**
     * 纯文本双向测试接口
     * 接收纯文本请求并返回纯文本响应
     *
     * 测试方式：
     * curl -X POST -H "Content-Type: application/text" -d "测试数据123" http://localhost:8080/example/b-sys/text/echo
     *
     * 返回：将接收到的文本原样返回
     */
    @PostMapping(value = "/text/echo", produces = "application/text;charset=UTF-8")
    @ResponseBody
    public String echoText(HttpServletRequest request) {
        try {
            // 读取请求体
            StringBuilder requestBody = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            String receivedText = requestBody.toString();
            log.info("[B系统模拟] Echo测试: 接收={}, 返回={}", receivedText, receivedText);

            // 原样返回
            return receivedText;
        } catch (Exception e) {
            log.error("[B系统模拟] Echo测试失败", e);
            return "错误: " + e.getMessage();
        }
    }
}
