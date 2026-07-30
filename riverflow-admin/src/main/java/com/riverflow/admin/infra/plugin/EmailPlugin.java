package com.riverflow.admin.infra.plugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.plugin.NodePlugin;
import com.riverflow.api.plugin.NodePluginResult;
import com.riverflow.api.plugin.ValidationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 邮件发送节点插件
 * 支持发送文本邮件和HTML邮件
 */
@Component
public class EmailPlugin implements NodePlugin {

    @Value("${mail.smtp.host:}")
    private String defaultHost;

    @Value("${mail.smtp.port:25}")
    private int defaultPort;

    @Value("${mail.smtp.username:}")
    private String defaultUsername;

    @Value("${mail.smtp.password:}")
    private String defaultPassword;

    @Value("${mail.smtp.ssl.enable:false}")
    private boolean defaultSslEnable;

    @Value("${mail.from:}")
    private String defaultFrom;

    @Override
    public String getNodeType() {
        return "email";
    }

    @Override
    public String getNodeName() {
        return "邮件发送";
    }

    @Override
    public String getIcon() {
        return "Message";
    }

    @Override
    public String getCategory() {
        return "communication";
    }

    @Override
    public String getDescription() {
        return "发送邮件通知，支持文本和HTML格式，可用于错误告警、流程通知等场景";
    }

    @Override
    public String getConfigTemplate() {
        JSONObject template = new JSONObject();
        template.put("host", "");
        template.put("port", 465);
        template.put("username", "");
        template.put("password", "");
        template.put("sslEnable", true);
        template.put("from", "");
        template.put("to", "");
        template.put("cc", "");
        template.put("subject", "流程通知: ${flowName}");
        template.put("content", "流程 ${flowName} 执行${status}，实例ID: ${instanceId}");
        template.put("contentType", "text");
        return template.toJSONString();
    }

    @Override
    public String getConfigSchema() {
        JSONObject schema = new JSONObject();
        JSONArray fields = new JSONArray();
        
        // SMTP服务器
        JSONObject hostField = new JSONObject();
        hostField.put("name", "host");
        hostField.put("label", "SMTP服务器");
        hostField.put("type", "text");
        hostField.put("required", true);
        hostField.put("placeholder", "smtp.qq.com");
        fields.add(hostField);
        
        // 端口
        JSONObject portField = new JSONObject();
        portField.put("name", "port");
        portField.put("label", "端口");
        portField.put("type", "number");
        portField.put("defaultValue", 465);
        fields.add(portField);
        
        // 用户名
        JSONObject usernameField = new JSONObject();
        usernameField.put("name", "username");
        usernameField.put("label", "用户名");
        usernameField.put("type", "text");
        usernameField.put("required", true);
        usernameField.put("placeholder", "xxx@qq.com");
        fields.add(usernameField);
        
        // 密码
        JSONObject passwordField = new JSONObject();
        passwordField.put("name", "password");
        passwordField.put("label", "密码/授权码");
        passwordField.put("type", "password");
        passwordField.put("required", true);
        fields.add(passwordField);
        
        // SSL
        JSONObject sslField = new JSONObject();
        sslField.put("name", "sslEnable");
        sslField.put("label", "启用SSL");
        sslField.put("type", "switch");
        sslField.put("defaultValue", true);
        fields.add(sslField);
        
        // 发件人
        JSONObject fromField = new JSONObject();
        fromField.put("name", "from");
        fromField.put("label", "发件人");
        fromField.put("type", "text");
        fromField.put("required", true);
        fromField.put("placeholder", "xxx@qq.com");
        fields.add(fromField);
        
        // 收件人
        JSONObject toField = new JSONObject();
        toField.put("name", "to");
        toField.put("label", "收件人");
        toField.put("type", "textarea");
        toField.put("required", true);
        toField.put("placeholder", "多个邮箱用逗号分隔");
        fields.add(toField);
        
        // 抄送
        JSONObject ccField = new JSONObject();
        ccField.put("name", "cc");
        ccField.put("label", "抄送");
        ccField.put("type", "textarea");
        ccField.put("placeholder", "多个邮箱用逗号分隔");
        fields.add(ccField);
        
        // 主题
        JSONObject subjectField = new JSONObject();
        subjectField.put("name", "subject");
        subjectField.put("label", "邮件主题");
        subjectField.put("type", "text");
        subjectField.put("required", true);
        subjectField.put("placeholder", "支持变量: ${flowName}, ${instanceId}, ${status}");
        fields.add(subjectField);
        
        // 内容
        JSONObject contentField = new JSONObject();
        contentField.put("name", "content");
        contentField.put("label", "邮件内容");
        contentField.put("type", "textarea");
        contentField.put("required", true);
        contentField.put("rows", 5);
        contentField.put("placeholder", "支持变量: ${flowName}, ${instanceId}, ${status}, ${errorMsg}");
        fields.add(contentField);
        
        // 内容类型
        JSONObject contentTypeField = new JSONObject();
        contentTypeField.put("name", "contentType");
        contentTypeField.put("label", "内容类型");
        contentTypeField.put("type", "select");
        contentTypeField.put("defaultValue", "text");
        JSONArray options = new JSONArray();
        options.add(createOption("纯文本", "text"));
        options.add(createOption("HTML", "html"));
        contentTypeField.put("options", options);
        fields.add(contentTypeField);
        
        schema.put("fields", fields);
        return schema.toJSONString();
    }

    @Override
    public String getOutputSchema() {
        JSONObject schema = new JSONObject();
        JSONArray fields = new JSONArray();
        
        JSONObject successField = new JSONObject();
        successField.put("name", "success");
        successField.put("label", "发送是否成功");
        successField.put("type", "boolean");
        fields.add(successField);
        
        JSONObject messageIdField = new JSONObject();
        messageIdField.put("name", "messageId");
        messageIdField.put("label", "邮件消息ID");
        messageIdField.put("type", "string");
        fields.add(messageIdField);
        
        JSONObject errorMsgField = new JSONObject();
        errorMsgField.put("name", "errorMsg");
        errorMsgField.put("label", "错误信息");
        errorMsgField.put("type", "string");
        fields.add(errorMsgField);
        
        schema.put("fields", fields);
        return schema.toJSONString();
    }

    @Override
    public NodePluginResult execute(FlowNode node, Map<String, Object> context) {
        NodePluginResult result = new NodePluginResult();
        
        try {
            // 解析配置
            String configJson = node.getConfigJson();
            JSONObject config = configJson != null ? JSON.parseObject(configJson) : new JSONObject();
            
            // 优先使用节点配置，节点配置为空时才使用默认配置
            boolean useDefault = config.getBooleanValue("useDefaultConfig");
            String host = config.getString("host");
            if (host == null || host.isEmpty()) {
                host = defaultHost;
            }
            int port = config.getIntValue("port", defaultPort);
            String username = config.getString("username");
            if (username == null || username.isEmpty()) {
                username = defaultUsername;
            }
            String password = config.getString("password");
            if (password == null || password.isEmpty()) {
                password = defaultPassword;
            }
            boolean sslEnable = config.containsKey("sslEnable") ? config.getBooleanValue("sslEnable") : defaultSslEnable;
            String from = config.getString("from");
            if (from == null || from.isEmpty()) {
                from = defaultFrom;
            }
            
            // 验证配置
            if (host == null || host.isEmpty()) {
                return errorResult("SMTP服务器未配置");
            }
            
            // 最终变量用于内部类
            final String finalUsername = username;
            final String finalPassword = password;
            
            // 获取邮件内容
            String to = resolveVariables(config.getString("to"), context);
            String cc = resolveVariables(config.getString("cc"), context);
            String subject = resolveVariables(config.getString("subject"), context);
            String content = resolveVariables(config.getString("content"), context);
            String contentType = config.getString("contentType");
            if (contentType == null) contentType = "text";
            
            if (to == null || to.isEmpty()) {
                return errorResult("收件人不能为空");
            }
            
            // 发送邮件
            Properties props = new Properties();
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", String.valueOf(port));
            props.put("mail.smtp.auth", "true");
            
            // SSL 配置
            if (port == 465) {
                // 465端口使用 SMTPS（直接SSL连接）
                props.put("mail.smtp.ssl.enable", "true");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                props.put("mail.smtp.socketFactory.port", "465");
                props.put("mail.smtp.socketFactory.fallback", "false");
            } else if (port == 587) {
                // 587端口使用 STARTTLS
                props.put("mail.smtp.starttls.enable", "true");
            }
            
            props.put("mail.smtp.timeout", "10000");
            props.put("mail.smtp.connectiontimeout", "10000");
            
            // 开启调试模式（生产环境可关闭）
            // props.put("mail.debug", "true");
            
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(finalUsername, finalPassword);
                }
            });
            
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            
            // 设置收件人
            String[] toAddresses = to.split("[,;\\s]+");
            InternetAddress[] toIntAddresses = new InternetAddress[toAddresses.length];
            for (int i = 0; i < toAddresses.length; i++) {
                toIntAddresses[i] = new InternetAddress(toAddresses[i].trim());
            }
            message.setRecipients(Message.RecipientType.TO, toIntAddresses);
            
            // 设置抄送
            if (cc != null && !cc.isEmpty()) {
                String[] ccAddresses = cc.split("[,;\\s]+");
                InternetAddress[] ccIntAddresses = new InternetAddress[ccAddresses.length];
                for (int i = 0; i < ccAddresses.length; i++) {
                    ccIntAddresses[i] = new InternetAddress(ccAddresses[i].trim());
                }
                message.setRecipients(Message.RecipientType.CC, ccIntAddresses);
            }
            
            message.setSubject(subject, StandardCharsets.UTF_8.name());
            
            if ("html".equals(contentType)) {
                message.setContent(content, "text/html;charset=UTF-8");
            } else {
                message.setText(content, StandardCharsets.UTF_8.name());
            }
            
            message.setSentDate(new Date());
            
            Transport.send(message);
            
            // 成功
            result.setSuccess(true);
            Map<String, Object> output = new HashMap<>();
            output.put("success", true);
            output.put("messageId", message.getMessageID());
            result.setData(output);
            
        } catch (Exception e) {
            return errorResult("邮件发送失败: " + e.getMessage());
        }
        
        return result;
    }

    @Override
    public ValidationResult validateConfig(String configJson) {
        ValidationResult result = ValidationResult.success();
        
        if (configJson == null || configJson.trim().isEmpty()) {
            return result.addError("配置不能为空");
        }
        
        try {
            JSONObject config = JSON.parseObject(configJson);
            
            if (config.getString("to") == null || config.getString("to").trim().isEmpty()) {
                result.addError("收件人不能为空");
            }
            if (config.getString("subject") == null || config.getString("subject").trim().isEmpty()) {
                result.addError("邮件主题不能为空");
            }
            if (config.getString("content") == null || config.getString("content").trim().isEmpty()) {
                result.addError("邮件内容不能为空");
            }
            
        } catch (Exception e) {
            result.addError("配置 JSON 格式错误: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 解析变量
     */
    private String resolveVariables(String template, Map<String, Object> context) {
        if (template == null) return null;
        
        String result = template;
        for (Map.Entry<String, Object> entry : context.entrySet()) {
            String placeholder = "${" + entry.getKey() + "}";
            String value = entry.getValue() != null ? String.valueOf(entry.getValue()) : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
    
    private NodePluginResult errorResult(String errorMsg) {
        NodePluginResult result = new NodePluginResult();
        result.setSuccess(false);
        result.setErrorMsg(errorMsg);
        Map<String, Object> output = new HashMap<>();
        output.put("success", false);
        output.put("errorMsg", errorMsg);
        result.setData(output);
        return result;
    }
    
    private JSONObject createOption(String label, String value) {
        JSONObject opt = new JSONObject();
        opt.put("label", label);
        opt.put("value", value);
        return opt;
    }
}