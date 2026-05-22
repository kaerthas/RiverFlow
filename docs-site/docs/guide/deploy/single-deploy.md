# 单机部署

## 环境准备

- JDK 1.8+
- MySQL 8.0+
- Redis 6.0+
- Node.js 16+（前端构建需要）

## 数据库初始化

```bash
mysql -u root -p < db/riverflow_init.sql
mysql -u root -p < db/init_data.sql
mysql -u root -p < db/workflow_example_init.sql
```

## 后端部署

### 1. 编译打包

```bash
cd RiverFlow
mvn clean install -DskipTests
```

### 2. 配置文件

复制 `application.yml` 并根据环境修改：

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/riverflow?useUnicode=true&characterEncoding=utf-8
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
    database: 0

riverflow:
  plugin:
    enabled: true
    dir: ${user.home}/riverflow/plugins
```

### 3. 启动服务

```bash
cd riverflow-admin/target
java -jar riverflow-admin-1.0.0-SNAPSHOT.jar
```

## 前端部署

### 1. 构建

```bash
cd riverflow-ui
npm install
npm run build
```

### 2. 部署

将 `dist/` 目录中的文件部署到 Nginx 或静态资源服务器：

```nginx
server {
    listen 80;
    server_name riverflow.example.com;
    root /var/www/riverflow-ui/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080/;
    }
}
```

## 验证

访问 `http://localhost:8080` 或配置好的域名，看到登录页面即部署成功。

默认账号：`admin` / `admin123`
