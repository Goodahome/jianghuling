# 部署文档（Deployment）

> 由 **运维 AI（@devops）** 维护。对齐 `docs/architecture.md` 与 `docker/`、`k8s/`。

**版本**：v1.1  
**状态**：本地 Compose + K8s 一键部署说明已落地  
**最后更新**：2026-08-07

---

## 1. 环境说明

| 环境 | 用途 | 备注 |
|------|------|------|
| local | 本地开发 / 演示 | Docker Compose 一键启动 |
| staging | 预发 | 复用 Compose 或同构单机；密钥独立 |
| prod / k8s | 集群部署 | 独立 namespace `jianghu-ling`；独立 Redis + 可选同 ns MySQL |

架构形态：**模块化单体**（1×backend + 1×frontend + MySQL + Redis），无独立网关/MQ。

---

## 2. 依赖服务与端口

| 服务 | 容器名建议 | 端口（宿主机 / ClusterIP） | 说明 |
|------|------------|----------------------------|------|
| frontend | jhl-frontend / `frontend` | 80 | Vue；Nginx 反代 `/api/`、`/files/` → backend |
| backend | jhl-backend / `backend` | 8080 | Spring Boot 3 + Actuator |
| mysql | jhl-mysql / `mysql` | 3306 | MySQL 8；库名见环境变量 |
| redis | jhl-redis / `redis` | 6379 | 验证码/限流/榜单/JWT 黑名单 |
| （卷）upload | — | — | 本地文件存储目录挂载 |

MVP **不部署**：Kafka/RabbitMQ、MinIO（文件走本地盘）、独立 API Gateway。

---

## 3. 快速启动（本地 Compose）

```bash
# 1. 复制环境变量（勿提交真实 .env）
cp docker/.env.example docker/.env

# 2. 启动
cd docker
docker compose up -d --build

# 3. 健康检查
curl http://localhost:8080/actuator/health

# 4. 访问
# 侠士端 / 执事堂 / 后台：http://localhost/
# API：http://localhost/api/v1 或 http://localhost:8080/api/v1
```

> 当前 `docker/docker-compose.yml` 默认启 MySQL/Redis；backend/frontend 服务块仍为注释，可按需启用并对接仓库根下 `backend/Dockerfile`、`frontend/Dockerfile`。

数据库初始化：本地可用 `backend/src/main/resources/db/schema.sql` + `data.sql` 手工导入；补丁脚本按版本按需执行。

---

## 4. 环境变量清单

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `MYSQL_HOST` / `MYSQL_PORT` / `MYSQL_DB` | 业务库连接 | `mysql` / `3306` / `jianghu_ling` |
| `MYSQL_USER` / `MYSQL_PASSWORD` | 应用账号 | `jhl` / （随机，Secret） |
| `MYSQL_ROOT_PASSWORD` | MySQL root（仅 DB 容器） | （随机，Secret） |
| `REDIS_HOST` / `REDIS_PORT` | Redis | `redis` / `6379` |
| `JWT_SECRET` | JWT 签名密钥 | ≥32 字节随机串 |
| `JWT_EXPIRE_SECONDS` / `JWT_EXPIRES_IN` | Access 有效期 | `7200` |
| `UPLOAD_DIR` | 本地上传目录 | `/data/uploads` |
| `SPRING_PROFILES_ACTIVE` | Spring Profile | `dev`（可改） |
| `MOCK_SMS_CODE` / `SMS_MOCK_*` | 短信 Mock | `123456` |
| `SERVER_PORT` | 后端端口 | `8080` |

Compose 示例见：`docker/.env.example`。K8s 非敏感项见 `k8s/02-configmap.yaml`，密钥见 `k8s/01-secrets.yaml`（`CHANGE_ME_*`，**勿提交真实密码**）。

**密钥规范**：真实 `.env` / 生产 Secret 不入库。

---

## 5. 前端反代约定（Nginx）

生产镜像使用 `frontend/nginx.conf`：

```nginx
location /api/ {
  proxy_pass http://backend:8080/api/;
  proxy_set_header Host $host;
  proxy_set_header X-Real-IP $remote_addr;
  proxy_set_header X-Request-Id $request_id;
}

location /files/ {
  proxy_pass http://backend:8080/files/;
}
```

前端路由为 History 模式时，其余路径回退 `index.html`（侠士端 `/`、执事堂 `/hall`、后台 `/admin` 同一 SPA）。

---

## 6. 数据持久化

| 卷 | 用途 |
|----|------|
| `mysql_data` / STS PVC `data` | MySQL 数据 |
| `redis_data` / PVC `redis-data` | Redis AOF |
| `upload_data` / PVC `backend-uploads` | 探子清单等上传文件 |

备份建议：每日备份 MySQL；上传目录与库一并快照。账本表 `wallet_ledger` 禁止物理删除。

---

## 7. 日志与排查（Compose）

```bash
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs -f backend
docker compose -f docker/docker-compose.yml logs -f frontend
```

| 现象 | 排查 |
|------|------|
| health 失败 | MySQL/Redis 是否就绪；`MYSQL_*` / `REDIS_*` |
| 登录验证码收不到 | MVP 为 Mock，查 backend 日志或 `MOCK_SMS_CODE` |
| 上传 404 | `UPLOAD_DIR` 卷是否挂载；Nginx `/files/` 反代 |
| 揭榜报限流 | Redis 是否通；系统参数日上限配置 |
| 结算失败 | 查 `wallet_ledger` 与悬赏状态；分配之和是否等于 90% 池 |

---

## 8. K8s 部署

### 8.1 集群与入口

| 角色 | 地址 | 说明 |
|------|------|------|
| 跳板 / 构建机 | `kssrol@100.66.0.3` | 代码目录 `~/jianghu_ling`；Docker 构建推送 |
| master | `kssrol@10.120.20.17` | `kubectl` |
| 私有仓库 | `10.120.20.201:5000` | **HTTP insecure** |
| Ingress 入口 | `10.120.20.200` | 浏览器访问入口 |

- Namespace：`jianghu-ling`
- 同 ns：独立 Redis（**MySQL 使用平台现有库，不部署本 ns MySQL**）
- 镜像：
  - `10.120.20.201:5000/jianghu-ling-backend:latest`
  - `10.120.20.201:5000/jianghu-ling-frontend:latest`
- Ingress Host：`jianghu.kssrol.com`（TLS Secret：`kssrol-com-tls`，须在 `jianghu-ling` ns）
- MySQL：`mysql.ks-platform-dev.svc.cluster.local` / 库 `jianghu_ling`
- 访问：`https://jianghu.kssrol.com/`（解析到 `10.120.20.200`）

### 8.2 清单与脚本

| 路径 | 说明 |
|------|------|
| `k8s/00-namespace.yaml` … `30-ingress.yaml` | ns、Secret、ConfigMap、Redis、backend、frontend、Ingress（`11-mysql.yaml` 备用默认不 apply） |
| `k8s/kustomization.yaml` | 可选：`kubectl apply -k k8s/` |
| `scripts/k8s/sync-from-windows.ps1` | Windows → 构建机 scp/rsync |
| `scripts/k8s/sync.sh` | Linux/macOS/Git Bash 同步 |
| `scripts/k8s/01-setup-temp-jdk.sh` | 临时 Temurin **17** → `~/tools/jdk-17-tmp`，只打印 `export` |
| `scripts/k8s/02-build-push.sh` | Docker build + push（Dockerfile 内 Maven，构建机可不装 Java） |
| `scripts/k8s/03-deploy.sh` | `kubectl apply` + rollout 等待 |
| `scripts/k8s/03-deploy.sh` | `kubectl apply` + rollout 等待 |

### 8.3 insecure registry

构建机 `/etc/docker/daemon.json`：

```json
{
  "insecure-registries": ["10.120.20.201:5000"]
}
```

```bash
sudo systemctl restart docker
```

**集群节点**（containerd / CRI-O）也需允许拉取该 HTTP 仓库，否则 Pod `ImagePullBackOff`。按集群实际改 `containerd` 的 `registries` / `config_path` 后重启 kubelet/containerd。

### 8.4 推荐执行顺序（Windows → 构建机 → 集群）

**① Windows 改密钥并同步**

```powershell
# 编辑 k8s\01-secrets.yaml：将 CHANGE_ME_* 改为强随机值（勿提交真实密码）
cd F:\Jinanghu_Ling
powershell -ExecutionPolicy Bypass -File scripts\k8s\sync-from-windows.ps1
```

**② 构建机（可选临时 JDK 17）**

后端镜像多阶段已含 Maven + Temurin 17，**一般不必装 Java**。若需机上 `mvn package`：

```bash
ssh kssrol@100.66.0.3
cd ~/jianghu_ling
bash scripts/k8s/01-setup-temp-jdk.sh
eval "$(bash scripts/k8s/01-setup-temp-jdk.sh --print-exports)"
java -version   # 应为 17；不改系统默认 /usr/bin/java
# 可选：HOST_MVN_PACKAGE=1 bash scripts/k8s/02-build-push.sh
```

**③ 构建并推送镜像**

```bash
cd ~/jianghu_ling
bash scripts/k8s/02-build-push.sh
```

**④ 部署（构建机已配 kubeconfig，或在 master 上执行）**

```bash
# 若清单只在构建机：先同步到 master，或从构建机 kubectl
bash scripts/k8s/03-deploy.sh

# 等价：
# kubectl apply -f k8s/
# 或：kubectl apply -k k8s/
```

**⑤ MySQL**

已使用平台现有库，**无需**本 ns 内 MySQL / init。若库未建表，在有权限的环境对 `jianghu_ling` 执行 `schema.sql` / `data.sql` 及所需 `patch_*.sql`。

### 8.5 健康检查与日志（K8s）

```bash
kubectl -n jianghu-ling get pods,svc,ingress
kubectl -n jianghu-ling logs -f deploy/backend
kubectl -n jianghu-ling logs -f deploy/frontend

# 后端健康
kubectl -n jianghu-ling exec deploy/backend -- \
  curl -fsS http://127.0.0.1:8080/actuator/health

# 经 Ingress
curl -fsSk https://jianghu.kssrol.com/api/v1/ || true
```

前端 Nginx 仅反代 `/api/` 与 `/files/`；Actuator 建议在集群内探针或 `kubectl exec` 检查。

TLS：`kssrol-com-tls` 必须在 `jianghu-ling` namespace。若不在：

```bash
kubectl get secret kssrol-com-tls -n <源ns> -o yaml \
  | sed 's/namespace: .*/namespace: jianghu-ling/' \
  | kubectl apply -f -
```

### 8.6 K8s 常见排障

| 现象 | 排查 |
|------|------|
| `ImagePullBackOff` | 节点是否允许 insecure `10.120.20.201:5000`；镜像是否已 push；`imagePullPolicy` |
| backend CrashLoop / DB 连不上 | `MYSQL_*` ConfigMap/Secret；mysql Pod Ready；库是否已建表 |
| Redis 超时 | Service 名是否为 `redis`；同 namespace |
| Ingress 404 / 打不开 | hosts 是否指向 `10.120.20.200`；Ingress Controller 是否就绪；`ingressClassName` |
| 上传失败 | PVC `backend-uploads`；`UPLOAD_DIR=/data/uploads` |
| push 报 HTTPS | 构建机未配 `insecure-registries` |
| Java 版本不对 | 项目为 **17**；临时 JDK 用 `01-setup-temp-jdk.sh`，勿装成 21 当默认运行时却与 pom 不一致 |

---

## 9. 发布检查清单

- [ ] 镜像构建并 push 成功
- [ ] Secret / `.env` 已替换默认密钥（JWT / DB）
- [ ] 数据库 schema（及必要 seed/patch）已执行
- [ ] `/actuator/health` 通过
- [ ] Ingress / hosts 可访问前端与 `/api/`
- [ ] 模拟钱庄文案标识可见
- [ ] 邀请码冷启动已由武林盟预置
- [ ] 告示栏种子内容已配置
- [ ] 关键 P0 冒烟：注册登录 → 充值发令 → 审核 → 揭榜 → 成果审核 → 结算 → 英雄谱

---

## 10. 变更记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | — | 模板初始化 |
| v1.0 | 2026-08-05 | 对齐 architecture v1.0：四服务 Compose、Mock 短信/钱庄、上传卷与反代 |
| v1.1 | 2026-08-07 | 新增 K8s：独立 ns / Redis / MySQL、前后端 Dockerfile、scripts/k8s、私有仓与临时 JDK17 说明 |
