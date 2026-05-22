# API 参考

## 流程管理 API

### 启动流程

```http
POST /open/flow/start
Content-Type: application/json

{
  "flowId": "流程定义ID",
  "businessKey": "业务主键",
  "variables": {
    "userId": "123"
  }
}
```

### 查询流程实例

```http
GET /workflow/instance/{flowId}
```

### 查询任务列表

```http
GET /workflow/task/list?pageNum=1&pageSize=10
```

## 插件管理 API

### 上传插件

```http
POST /plugin/upload
Content-Type: multipart/form-data

file: [JAR文件]
```

### 查询已加载插件

```http
GET /plugin/loaded
```

### 重载插件

```http
POST /plugin/reload/{id}
```

### 删除插件

```http
DELETE /plugin/delete/{id}
```

## 数据源 API

### 查询数据源列表

```http
GET /datasource/list
```

### 测试连接

```http
POST /datasource/test
Content-Type: application/json

{
  "url": "jdbc:mysql://localhost:3306/test",
  "username": "root",
  "password": "123456"
}
```

## API 目录 API

### 注册接口

```http
POST /api/catalog/register
Content-Type: application/json

{
  "name": "用户查询",
  "url": "http://localhost:8081/api/user/get",
  "method": "GET",
  "headers": [
    { "key": "Authorization", "value": "Bearer xxx" }
  ]
}
```

### 调用接口

```http
POST /api/catalog/invoke/{id}
Content-Type: application/json

{
  "params": {
    "userId": "123"
  }
}
```
