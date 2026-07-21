# Milvus 向量库 Docker 安装与初始化指南

本文档用于在本地或服务器通过 Docker / Docker Compose 部署 Milvus standalone，并与 RiverFlow 知识库管理完成对接。

## 1. 环境要求

- Docker Engine >= 20.10
- Docker Compose >= 2.0
- 至少 4GB 可用内存（建议 8GB）

## 2. 拉取镜像

Milvus standalone 依赖三个镜像：etcd、minio、milvus。可以直接用 `docker pull` 拉取，避免启动时因网络失败：

```bash
# 拉取 Milvus 依赖镜像
docker pull quay.io/coreos/etcd:v3.5.5
docker pull minio/minio:RELEASE.2023-03-20T20-16-18Z

# 拉取 Milvus 主镜像（v2.4.x 长期支持版本）
docker pull milvusdb/milvus:v2.4.5
```

> 国内网络如果拉取慢，可以先配置 Docker 镜像加速（阿里云/中科大镜像），或者使用代理。

## 3. 准备目录与配置文件

```bash
mkdir -p /opt/milvus/{etcd,minio,milvus}
cd /opt/milvus
```

创建 `docker-compose.yml`：

```yaml
version: '3.5'

services:
  etcd:
    container_name: milvus-etcd
    image: quay.io/coreos/etcd:v3.5.5
    environment:
      - ETCD_AUTO_COMPACTION_MODE=revision
      - ETCD_AUTO_COMPACTION_RETENTION=1000
      - ETCD_QUOTA_BACKEND_BYTES=4294967296
      - ETCD_SNAPSHOT_COUNT=50000
    volumes:
      - ./etcd:/etcd
    command: etcd -advertise-client-urls http://127.0.0.1:2379 -listen-client-urls http://0.0.0.0:2379 --data-dir /etcd
    healthcheck:
      test: ["CMD", "etcdctl", "endpoint", "health"]
      interval: 30s
      timeout: 20s
      retries: 3

  minio:
    container_name: milvus-minio
    image: minio/minio:RELEASE.2023-03-20T20-16-18Z
    environment:
      MINIO_ACCESS_KEY: minioadmin
      MINIO_SECRET_KEY: minioadmin
    ports:
      - "9001:9001"
      - "9000:9000"
    volumes:
      - ./minio:/minio_data
    command: minio server /minio_data --console-address ":9001"
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
      interval: 30s
      timeout: 20s
      retries: 3

  standalone:
    container_name: milvus-standalone
    image: milvusdb/milvus:v2.4.5
    command: ["milvus", "run", "standalone"]
    environment:
      ETCD_ENDPOINTS: etcd:2379
      MINIO_ADDRESS: minio:9000
    volumes:
      - ./milvus:/var/lib/milvus
    ports:
      - "19530:19530"
      - "9091:9091"
    depends_on:
      - etcd
      - minio
```

## 4. 启动 Milvus

```bash
cd /opt/milvus
docker compose up -d
```

等待约 30 秒，确认三个容器都健康：

```bash
docker compose ps
```

输出示例：

```
NAME                STATUS
milvus-etcd         healthy
milvus-minio        healthy
milvus-standalone   healthy
```

## 5. 验证服务

```bash
# 查看 Milvus 日志
docker logs -f milvus-standalone

# 测试 gRPC 端口是否监听
nc -zv localhost 19530
```

如果安装了 [milvus-cli](https://github.com/zilliztech/milvus-cli)：`milvus-cli --uri http://localhost:19530`。

没有 milvus-cli 也没关系，可以直接在 RiverFlow 后台 **向量库管理 → 测试连接** 验证。

## 6. 在 RiverFlow 中配置 Milvus

编辑 `riverflow-ai/src/main/resources/application.yml`（或外层 `application-*.yml`）：

```yaml
riverflow:
  ai:
    knowledge:
      vector-store:
        type: milvus
        default-collection: riverflow_default
        milvus:
          host: localhost
          port: 19530
          database: default
          # standalone 默认无认证，token 留空即可；启用认证后填写 root:Milvus
          token:
          secure: false
          index-type: HNSW
```

> 如果 RiverFlow 后端也在 Docker 中运行，`host` 应填写宿主机的可访问地址，如 `host.docker.internal` 或服务器 IP，不能填 `localhost`。

## 7. 初始化默认向量集合

执行数据库初始化 SQL：

```sql
INSERT INTO `wf_ai_vector_collection` (`id`, `collection`, `store_type`, `dimension`, `distance_metric`, `embedding_type`, `description`, `enabled`)
VALUES (1, 'riverflow_default', 'milvus', 1536, 'COSINE', 'openai', 'RiverFlow 默认知识库向量集合', 1)
ON DUPLICATE KEY UPDATE `update_time` = CURRENT_TIMESTAMP;
```

首次新增知识文档时，RiverFlow 会自动在 Milvus 中创建对应的 collection。

## 8. 可选：部署 Attu 可视化工具

Attu 是 Milvus 的 Web 管理界面，需要用另一个容器独立启动：

```bash
docker pull zilliz/attu:v2.4

docker run -d \
  --name attu \
  -p 8000:3000 \
  zilliz/attu:v2.4
```

访问：`http://localhost:8000`

连接信息：

- Host：`localhost`
- Port：`19530`
- Database：`default`
- Token：留空（未启用认证时）或 `root:Milvus`

## 9. 常用运维命令

```bash
# 停止 Milvus
docker compose down

# 停止并删除数据（谨慎操作）
docker compose down -v
rm -rf /opt/milvus/etcd /opt/milvus/minio /opt/milvus/milvus

# 查看日志
docker logs -f milvus-standalone
```

## 10. 常见问题

### 10.1 连接超时

- 检查防火墙是否放行 `19530` 端口。
- 容器间访问应使用宿主机 IP 或 `host.docker.internal`，而不是 `localhost`。

### 10.2 集合维度不匹配

Milvus 创建 collection 后维度不可修改。如果修改了 `dimension` 或 Embedding 模型，需要：

1. 删除 Milvus 中的旧 collection（会丢失索引数据）。
2. 在 RiverFlow 中重新创建向量集合配置。
3. 在 **知识库管理** 页面执行 **重建索引**。

### 10.3 镜像拉取失败

如果 `docker pull` 失败，先检查 Docker 镜像加速配置：

```bash
# 查看当前镜像配置
cat /etc/docker/daemon.json
```

可以配置阿里云镜像加速器后重启 Docker：

```json
{
  "registry-mirrors": ["https://<your-code>.mirror.aliyuncs.com"]
}
```

## 11. 与 RiverFlow 知识库的关联关系

改造后，知识库文档通过 `collection_id` 字段关联到 `wf_ai_vector_collection` 表。索引和检索时会根据该配置选择：

- 向量库类型（`store_type`）
- 向量维度（`dimension`）
- 距离度量（`distance_metric`）
- Embedding 类型及模型（`embedding_type` / `embedding_model` / `embedding_base_url`）

这意味着同一个 RiverFlow 实例可以支持多组向量集合，例如：

- `riverflow_default`：Milvus + OpenAI Embedding，1536 维
- `riverflow_ollama`：Milvus + Ollama Embedding，768 维
- `riverflow_pg`：PGVector + 本地 Embedding

在 **知识库管理 → 新增文档** 时选择对应的向量集合即可。
