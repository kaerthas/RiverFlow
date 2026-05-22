---
title: RiverFlow 插件系统使用指南
---

# RiverFlow 插件系统使用指南

## 一、插件系统概述

RiverFlow支持通过前端上传JAR包的方式动态扩展流程节点，**无需重启服务**，实现真正的热插拔。

### 核心特性

- ✅ **前端上传**：通过Web界面上传插件JAR包
- ✅ **自动加载**：上传后自动加载并注册节点
- ✅ **热插拔**：支持运行时启用、禁用、重载插件
- ✅ **可视化管理**：完整的插件管理界面
- ✅ **持久化存储**：插件信息存储在数据库中
- ✅ **零侵入**：无需修改核心代码

## 二、快速开始

### 2.1 初始化数据库

执行SQL脚本创建插件管理表：

```bash
mysql -u root -p riverflow < db/sys_plugin.sql
```

### 2.2 配置插件目录

在 `application.yml` 中配置：

```yaml
riverflow:
  plugin:
    enabled: true        # 启用插件系统
    dir: plugins         # 插件存储目录
```

### 2.3 访问插件管理页面

启动服务后，访问：

```
http://localhost:8080/#/plugin
```

### 2.4 上传插件

1. 点击"上传插件"按钮
2. 选择JAR包文件
3. 等待上传和自动加载
4. 在流程设计器中使用新节点

## 三、MinIO插件使用示例

### 3.1 编译MinIO插件

```bash
cd river-minio
mvn clean package
```

编译后生成：`target/river-minio-1.0.0-SNAPSHOT.jar`

### 3.2 上传插件

1. 访问插件管理页面
2. 点击"上传插件"
3. 选择 `river-minio-1.0.0-SNAPSHOT.jar`
4. 上传成功后自动加载

### 3.3 验证插件

在插件列表中查看：

| 插件名称 | 类型标识 | 分类 | 状态 | 加载状态 |
|---------|---------|------|------|---------|
| MinIO文件推送 | minio | storage | 已启用 | 已加载 |

### 3.4 使用插件节点

在流程设计器中：

1. 从节点面板找到"MinIO文件推送"
2. 拖拽到画布
3. 配置参数：
   - MinIO地址：`http://192.168.1.100:9000`
   - Access Key：`minioadmin`
   - Secret Key：`minioadmin`
   - Bucket：`materials`
   - 操作类型：`upload`
   - 对象名称：`${context.fileName}`
   - 文件路径：`${context.filePath}`

## 四、插件管理功能

### 4.1 插件列表

- 分页展示所有插件
- 支持按名称、分类、状态筛选
- 显示文件大小、上传时间等信息

### 4.2 插件操作

| 操作 | 说明 |
|------|------|
| 启用 | 启用已禁用的插件，自动加载到内存 |
| 禁用 | 禁用插件，从内存卸载但保留文件 |
| 重载 | 重新加载插件，用于更新后生效 |
| 删除 | 删除插件，同时删除JAR文件 |

### 4.3 热加载流程

```
上传JAR → 保存文件 → 加载到内存 → 注册节点 → 写入数据库
```

### 4.4 热卸载流程

```
禁用插件 → 卸载节点 → 关闭ClassLoader → 更新数据库
```

## 五、开发自定义插件

### 5.1 创建插件项目

参考 `river-minio` 项目结构：

```
my-plugin/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/mycompany/plugin/
│       │       └── MyPlugin.java
│       └── resources/
│           └── META-INF/
│               └── services/
│                   └── com.riverflow.api.plugin.NodePlugin
```

### 5.2 实现NodePlugin接口

```java
public class MyPlugin implements NodePlugin {
    
    @Override
    public String getNodeType() {
        return "my-node";
    }
    
    @Override
    public String getNodeName() {
        return "我的自定义节点";
    }
    
    @Override
    public String getIcon() {
        return "Star";
    }
    
    @Override
    public String getCategory() {
        return "custom";
    }
    
    @Override
    public String getDescription() {
        return "自定义节点描述";
    }
    
    @Override
    public String getConfigTemplate() {
        return "{\"param1\": \"value1\"}";
    }
    
    @Override
    public NodePluginResult execute(FlowNode node, Map<String, Object> context) {
        try {
            JSONObject config = JSON.parseObject(node.getConfigJson());
            
            // 实现业务逻辑
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            
            return NodePluginResult.success(result);
        } catch (Exception e) {
            return NodePluginResult.fail(e.getMessage());
        }
    }
}
```

### 5.3 注册SPI服务

在 `META-INF/services/com.riverflow.api.plugin.NodePlugin` 文件中：

```
com.mycompany.plugin.MyPlugin
```

### 5.4 打包上传

```bash
mvn clean package
```

将生成的JAR包通过前端上传即可。

## 六、API接口说明

### 6.1 上传插件

```http
POST /plugin/upload
Content-Type: multipart/form-data

file: [JAR文件]
```

### 6.2 查询插件列表

```http
GET /plugin/list?pageNum=1&pageSize=10&pluginName=&category=&status=
```

### 6.3 启用插件

```http
POST /plugin/enable/{id}
```

### 6.4 禁用插件

```http
POST /plugin/disable/{id}
```

### 6.5 重载插件

```http
POST /plugin/reload/{id}
```

### 6.6 删除插件

```http
DELETE /plugin/delete/{id}
```

### 6.7 获取已加载插件

```http
GET /plugin/loaded
```

### 6.8 获取配置模板

```http
GET /plugin/template?nodeType=minio
```

## 七、最佳实践

### 7.1 插件命名规范

- 类型标识：小写字母+连字符，如 `minio`、`email-sender`
- 插件名称：简洁明了，如 `MinIO文件推送`
- 分类：统一分类，如 `storage`、`communication`

### 7.2 版本管理

- 插件版本存储在数据库中
- 更新插件时上传新JAR包会覆盖旧版本
- 建议在JAR包名称中包含版本号

### 7.3 错误处理

- 插件加载失败会记录日志并标记为未加载
- 可以通过"重载"按钮重新加载
- 查看日志排查加载失败原因

### 7.4 性能优化

- 插件按需加载，不会影响系统启动速度
- 禁用的插件不会占用内存
- 定期清理不用的插件

### 7.5 安全考虑

- 插件运行在独立的ClassLoader中（父ClassLoader为主项目ClassLoader）
- 插件可以获取主项目的 Spring Bean，因此插件拥有与主项目同等的权限
- 建议对上传权限进行控制，生产环境建议审核插件代码
- `init(ApplicationContext)` 没有引入新的安全漏洞，因为插件本身就能执行任意代码（Java 字节码）

## 八、常见问题

### Q1: 插件上传后没有出现在列表中？

检查：
1. JAR包是否包含正确的SPI配置
2. 实现类是否正确实现NodePlugin接口
3. 查看后台日志错误信息

### Q2: 插件加载失败？

可能原因：
1. JAR包依赖冲突
2. 实现类实例化异常
3. SPI配置文件格式错误

解决方法：
1. 检查pom.xml依赖配置
2. 查看详细错误日志
3. 使用maven-shade-plugin正确打包

### Q3: 如何更新插件？

方法一：
1. 删除旧插件
2. 上传新版本JAR包

方法二：
1. 上传同名JAR包（会自动覆盖）
2. 点击"重载"按钮

### Q4: 插件可以使用 Spring 管理的 Bean 吗？

可以。从 v1.0.0 开始，插件实现 `init(ApplicationContext)` 方法后，可通过 `applicationContext.getBean()` 获取主项目的任意 Spring Bean：

```java
@Override
public void init(ApplicationContext applicationContext) {
    // 获取 RedisTemplate
    this.redisTemplate = applicationContext.getBean(StringRedisTemplate.class);
    // 获取 JdbcTemplate
    this.jdbcTemplate = applicationContext.getBean(JdbcTemplate.class);
    // 获取任意自定义 Service
    this.myService = applicationContext.getBean(MyService.class);
}
```

常用可获取的 Bean：
- `StringRedisTemplate` / `RedisTemplate` — Redis 操作
- `JdbcTemplate` — 数据库操作
- 主项目中自定义的 Service / Mapper / DAO

> ⚠️ 所有 Spring 相关依赖在插件 pom.xml 中必须使用 `<scope>provided</scope>`，避免类加载冲突。

### Q5: 如何开发前端配置组件？

前端组件开发步骤：
1. 创建Vue组件
2. 放入riverflow-ui的plugins目录
3. 在流程设计器中动态加载

## 九、示例项目

完整的MinIO插件示例：`river-minio`

包含：
- ✅ 后端NodePlugin实现
- ✅ SPI注册配置
- ✅ Maven打包配置
- ✅ 使用文档

---

**技术支持**：如有问题，请提交Issue或联系开发团队。
