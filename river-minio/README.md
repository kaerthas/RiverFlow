# river-minio 插件

MinIO文件存储插件，支持文件上传、下载、删除、查询等操作。

## 功能特性

- ✅ 文件上传到MinIO
- ✅ 从MinIO下载文件
- ✅ 删除MinIO对象
- ✅ 查询对象元数据
- ✅ 支持上下文变量
- ✅ 完整的前端配置界面

## 使用方法

### 1. 编译打包

```bash
mvn clean package
```

### 2. 部署插件

将生成的JAR包复制到RiverFlow的plugins目录：

```bash
cp target/river-minio-1.0.0-SNAPSHOT.jar ../plugins/
```

### 3. 重启服务

重启RiverFlow服务，插件会自动加载。

### 4. 使用节点

在流程设计器中，从节点面板拖拽"MinIO文件推送"节点到画布。

## 配置说明

### 连接配置

| 参数 | 说明 | 示例 |
|------|------|------|
| endpoint | MinIO服务地址 | http://localhost:9000 |
| accessKey | 访问密钥 | minioadmin |
| secretKey | 秘密密钥 | minioadmin |
| bucket | 存储桶名称 | materials |

### 操作类型

#### 上传文件 (upload)

| 参数 | 说明 | 是否必填 |
|------|------|---------|
| objectName | 对象名称 | ✅ |
| filePath | 本地文件路径 | ✅ |
| contentType | 内容类型 | ❌ |

#### 下载文件 (download)

| 参数 | 说明 | 是否必填 |
|------|------|---------|
| objectName | 对象名称 | ✅ |
| downloadPath | 下载保存路径 | ✅ |

#### 删除文件 (delete)

| 参数 | 说明 | 是否必填 |
|------|------|---------|
| objectName | 对象名称 | ✅ |

#### 查询文件 (stat)

| 参数 | 说明 | 是否必填 |
|------|------|---------|
| objectName | 对象名称 | ✅ |

## 上下文变量

支持使用 `${context.xxx}` 引用流程上下文变量：

```json
{
  "objectName": "${context.fileName}",
  "filePath": "${context.filePath}"
}
```

## 执行结果

节点执行成功后，会在上下文中输出结果：

### upload操作

```json
{
  "success": true,
  "bucket": "materials",
  "objectName": "report.pdf",
  "url": "http://localhost:9000/materials/report.pdf",
  "operation": "upload"
}
```

### download操作

```json
{
  "success": true,
  "bucket": "materials",
  "objectName": "report.pdf",
  "downloadPath": "/tmp/report.pdf",
  "operation": "download"
}
```

### stat操作

```json
{
  "success": true,
  "bucket": "materials",
  "objectName": "report.pdf",
  "size": 102400,
  "contentType": "application/pdf",
  "lastModified": "2024-01-01T12:00:00Z",
  "operation": "stat"
}
```

## 示例流程

### 示例1：上传材料文件

1. **Script节点**：准备文件信息
```groovy
context.filePath = "/tmp/application.pdf"
context.fileName = "application_${context.businessKey}.pdf"
```

2. **MinIO节点**：上传文件
```json
{
  "operation": "upload",
  "objectName": "${context.fileName}",
  "filePath": "${context.filePath}"
}
```

3. **DB节点**：保存文件URL到数据库
```sql
UPDATE business_form 
SET file_url = '${context.url}' 
WHERE business_key = '${context.businessKey}'
```

### 示例2：下载并处理文件

1. **MinIO节点**：下载文件
```json
{
  "operation": "download",
  "objectName": "${context.fileName}",
  "downloadPath": "/tmp/process/${context.fileName}"
}
```

2. **Script节点**：处理文件
```groovy
def file = new File("/tmp/process/${context.fileName}")
// 处理文件逻辑
```

## 前端集成

前端组件位于 `src/main/resources/ui/` 目录：

- `MinioNodeConfig.vue` - 配置表单组件
- `api/plugin.js` - API接口
- `index.js` - 导出配置

将组件复制到riverflow-ui项目的plugins目录即可使用。

## 注意事项

1. 确保MinIO服务可访问
2. 确保bucket已创建
3. 文件路径需要有读写权限
4. 建议配置失败重试策略

## 版本信息

- 插件版本：1.0.0-SNAPSHOT
- MinIO SDK版本：8.5.7
- 支持RiverFlow版本：1.0.0-SNAPSHOT
