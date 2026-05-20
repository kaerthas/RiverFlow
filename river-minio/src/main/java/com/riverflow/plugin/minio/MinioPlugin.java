package com.riverflow.plugin.minio;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.riverflow.api.entity.FlowNode;
import com.riverflow.api.plugin.NodePlugin;
import com.riverflow.api.plugin.NodePluginResult;
import com.riverflow.api.plugin.ValidationResult;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.DownloadObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.GetObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import io.minio.Result;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MinIO文件存储插件
 * 支持文件上传、下载、删除、查询等操作
 */
public class MinioPlugin implements NodePlugin {

    @Override
    public String getNodeType() {
        return "minio";
    }

    @Override
    public String getNodeName() {
        return "MinIO文件推送";
    }

    @Override
    public String getIcon() {
        return "CloudUpload";
    }

    @Override
    public String getCategory() {
        return "storage";
    }

    @Override
    public String getDescription() {
        return "MinIO对象存储操作，支持文件上传、下载、删除、查询等";
    }

    @Override
    public String getConfigTemplate() {
        JSONObject template = new JSONObject();
        template.put("endpoint", "http://localhost:9000");
        template.put("accessKey", "minioadmin");
        template.put("secretKey", "minioadmin");
        template.put("bucket", "materials");
        template.put("operation", "upload");
        template.put("filePath", "${context.filePath}");
        template.put("objectName", "${context.fileName}");
        template.put("contentType", "application/octet-stream");
        return template.toJSONString();
    }

    @Override
    public String getConfigSchema() {
        JSONObject schema = new JSONObject();
        JSONObject[] fields = new JSONObject[8];
        
        fields[0] = new JSONObject();
        fields[0].put("name", "endpoint");
        fields[0].put("label", "服务地址");
        fields[0].put("type", "text");
        fields[0].put("required", true);
        fields[0].put("placeholder", "http://localhost:9000");
        fields[0].put("defaultValue", "http://localhost:9000");
        fields[0].put("tip", "MinIO服务的访问地址");
        
        fields[1] = new JSONObject();
        fields[1].put("name", "accessKey");
        fields[1].put("label", "访问密钥");
        fields[1].put("type", "text");
        fields[1].put("required", true);
        fields[1].put("placeholder", "minioadmin");
        fields[1].put("defaultValue", "minioadmin");
        
        fields[2] = new JSONObject();
        fields[2].put("name", "secretKey");
        fields[2].put("label", "密钥");
        fields[2].put("type", "password");
        fields[2].put("required", true);
        fields[2].put("placeholder", "minioadmin");
        fields[2].put("defaultValue", "minioadmin");
        
        fields[3] = new JSONObject();
        fields[3].put("name", "bucket");
        fields[3].put("label", "存储桶");
        fields[3].put("type", "text");
        fields[3].put("required", true);
        fields[3].put("placeholder", "materials");
        fields[3].put("defaultValue", "materials");
        fields[3].put("tip", "存储桶名称，需要先在MinIO中创建");
        
        fields[4] = new JSONObject();
        fields[4].put("name", "operation");
        fields[4].put("label", "操作类型");
        fields[4].put("type", "select");
        fields[4].put("required", true);
        fields[4].put("defaultValue", "upload");
        JSONObject[] options = new JSONObject[4];
        options[0] = new JSONObject(); options[0].put("label", "上传"); options[0].put("value", "upload");
        options[1] = new JSONObject(); options[1].put("label", "下载"); options[1].put("value", "download");
        options[2] = new JSONObject(); options[2].put("label", "删除"); options[2].put("value", "delete");
        options[3] = new JSONObject(); options[3].put("label", "查询元数据"); options[3].put("value", "stat");
        fields[4].put("options", options);
        
        fields[5] = new JSONObject();
        fields[5].put("name", "filePath");
        fields[5].put("label", "文件路径");
        fields[5].put("type", "text");
        fields[5].put("required", true);
        fields[5].put("placeholder", "${context.filePath}");
        fields[5].put("defaultValue", "${context.filePath}");
        fields[5].put("tip", "支持本地路径或HTTP/HTTPS URL，使用 ${context.xxx} 引用上下文变量");
        
        fields[6] = new JSONObject();
        fields[6].put("name", "objectName");
        fields[6].put("label", "对象名称");
        fields[6].put("type", "text");
        fields[6].put("required", true);
        fields[6].put("placeholder", "${context.fileName}");
        fields[6].put("defaultValue", "${context.fileName}");
        fields[6].put("tip", "MinIO中的对象名称（key）");
        
        fields[7] = new JSONObject();
        fields[7].put("name", "contentType");
        fields[7].put("label", "文件类型");
        fields[7].put("type", "text");
        fields[7].put("required", false);
        fields[7].put("placeholder", "application/octet-stream");
        fields[7].put("defaultValue", "application/octet-stream");
        fields[7].put("tip", "文件的MIME类型");
        
        schema.put("fields", fields);
        return schema.toJSONString();
    }

    @Override
    public NodePluginResult execute(FlowNode node, Map<String, Object> context) {
        try {
            String configJson = node.getConfigJson();
            if (configJson == null || configJson.trim().isEmpty()) {
                return NodePluginResult.fail("MinIO节点缺少配置");
            }

            JSONObject config = JSON.parseObject(configJson);
            
            System.out.println("=== MinIO Plugin Debug ===");
            System.out.println("Config: " + configJson);
            System.out.println("Context: " + context);
            System.out.println("filePath config: " + config.getString("filePath"));
            System.out.println("objectName config: " + config.getString("objectName"));
            
            String endpoint = resolveValue(config.getString("endpoint"), context);
            String accessKey = resolveValue(config.getString("accessKey"), context);
            String secretKey = resolveValue(config.getString("secretKey"), context);
            String bucket = resolveValue(config.getString("bucket"), context);
            String operation = config.getString("operation");
            
            System.out.println("Resolved endpoint: " + endpoint);
            System.out.println("Resolved bucket: " + bucket);
            System.out.println("Operation: " + operation);

            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();

            Map<String, Object> result = new HashMap<>();

            switch (operation) {
                case "upload":
                    result = doUpload(minioClient, endpoint, bucket, config, context);
                    break;
                case "download":
                    result = doDownload(minioClient, bucket, config, context);
                    break;
                case "delete":
                    result = doDelete(minioClient, bucket, config, context);
                    break;
                case "stat":
                    result = doStat(minioClient, bucket, config, context);
                    break;
                default:
                    return NodePluginResult.fail("不支持的操作类型: " + operation);
            }

            return NodePluginResult.success(result);

        } catch (Exception e) {
            return NodePluginResult.fail("MinIO操作失败: " + e.getMessage());
        }
    }

    private Map<String, Object> doUpload(MinioClient client, String endpoint, String bucket, 
                                         JSONObject config, Map<String, Object> context) throws Exception {
        String filePath = resolveValue(config.getString("filePath"), context);
        String objectName = resolveValue(config.getString("objectName"), context);
        String contentType = config.getString("contentType");

        if (filePath == null || objectName == null) {
            throw new IllegalArgumentException("文件路径和对象名称不能为空");
        }
        
        filePath = filePath.trim();

        Path uploadFile;
        boolean isTempFile = false;
        
        if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
            uploadFile = downloadFromUrl(filePath);
            isTempFile = true;
        } else {
            uploadFile = Paths.get(filePath);
            if (!Files.exists(uploadFile)) {
                throw new IllegalArgumentException("文件不存在: " + filePath);
            }
        }

        try {
            if (contentType == null || contentType.isEmpty()) {
                contentType = Files.probeContentType(uploadFile);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
            }

            client.uploadObject(
                UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(uploadFile.toString())
                    .contentType(contentType)
                    .build()
            );

            String url = endpoint + "/" + bucket + "/" + objectName;
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("bucket", bucket);
            result.put("objectName", objectName);
            result.put("url", url);
            result.put("operation", "upload");
            result.put("sourceType", filePath.startsWith("http") ? "url" : "local");
            
            return result;
        } finally {
            if (isTempFile && Files.exists(uploadFile)) {
                Files.deleteIfExists(uploadFile);
            }
        }
    }

    private Path downloadFromUrl(String url) throws Exception {
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(createInsecureSslSocketFactory());
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        
        java.net.URL downloadUrl = new java.net.URL(url);
        String fileName = url.substring(url.lastIndexOf('/') + 1);
        if (fileName.isEmpty() || fileName.contains("?")) {
            fileName = "download_" + System.currentTimeMillis();
        }
        
        Path tempDir = Files.createTempDirectory("minio-download");
        Path tempFile = tempDir.resolve(fileName);
        
        try (java.io.InputStream in = downloadUrl.openStream();
             java.io.OutputStream out = Files.newOutputStream(tempFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        }
        
        return tempFile;
    }
    
    private javax.net.ssl.SSLSocketFactory createInsecureSslSocketFactory() {
        try {
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(null, new javax.net.ssl.TrustManager[] {
                new javax.net.ssl.X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
            }, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException("创建SSL上下文失败", e);
        }
    }

    private Map<String, Object> doDownload(MinioClient client, String bucket,
                                           JSONObject config, Map<String, Object> context) throws Exception {
        String objectName = resolveValue(config.getString("objectName"), context);
        String downloadPath = resolveValue(config.getString("downloadPath"), context);

        if (objectName == null || downloadPath == null) {
            throw new IllegalArgumentException("对象名称和下载路径不能为空");
        }

        client.downloadObject(
            DownloadObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .filename(downloadPath)
                .build()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("bucket", bucket);
        result.put("objectName", objectName);
        result.put("downloadPath", downloadPath);
        result.put("operation", "download");
        
        return result;
    }

    private Map<String, Object> doDelete(MinioClient client, String bucket,
                                         JSONObject config, Map<String, Object> context) throws Exception {
        String objectName = resolveValue(config.getString("objectName"), context);

        if (objectName == null) {
            throw new IllegalArgumentException("对象名称不能为空");
        }

        client.removeObject(
            RemoveObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("bucket", bucket);
        result.put("objectName", objectName);
        result.put("operation", "delete");
        
        return result;
    }

    private Map<String, Object> doStat(MinioClient client, String bucket,
                                       JSONObject config, Map<String, Object> context) throws Exception {
        String objectName = resolveValue(config.getString("objectName"), context);

        if (objectName == null) {
            throw new IllegalArgumentException("对象名称不能为空");
        }

        StatObjectResponse stat = client.statObject(
            StatObjectArgs.builder()
                .bucket(bucket)
                .object(objectName)
                .build()
        );

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("bucket", bucket);
        result.put("objectName", objectName);
        result.put("size", stat.size());
        result.put("contentType", stat.contentType());
        result.put("lastModified", stat.lastModified());
        result.put("operation", "stat");
        
        return result;
    }

    private String resolveValue(String value, Map<String, Object> context) {
        if (value == null) {
            return null;
        }

        if (value.startsWith("${") && value.endsWith("}")) {
            String expression = value.substring(2, value.length() - 1);
            
            if (expression.startsWith("context.")) {
                String key = expression.substring(8);
                Object obj = context.get(key);
                return obj != null ? obj.toString() : null;
            }
        }

        return value;
    }

    @Override
    public ValidationResult validateConfig(String configJson) {
        ValidationResult result = ValidationResult.success();
        
        if (configJson == null || configJson.trim().isEmpty()) {
            return result.addError("配置不能为空");
        }

        try {
            JSONObject config = JSON.parseObject(configJson);
            
            if (config.getString("endpoint") == null) {
                result.addError("endpoint不能为空");
            }
            if (config.getString("accessKey") == null) {
                result.addError("accessKey不能为空");
            }
            if (config.getString("secretKey") == null) {
                result.addError("secretKey不能为空");
            }
            if (config.getString("bucket") == null) {
                result.addError("bucket不能为空");
            }
            if (config.getString("operation") == null) {
                result.addError("operation不能为空");
            }
            
        } catch (Exception e) {
            result.addError("配置JSON格式错误: " + e.getMessage());
        }

        return result;
    }
}
