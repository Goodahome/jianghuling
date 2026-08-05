# 部署文档（Deployment）

> 由 **运维 AI（@devops）** 维护。对齐 `docs/architecture.md`（v1.0）与 `docker/`。

**版本**：v1.0  
**状态**：与架构对齐（Compose 骨架待 `@devops` / 研发补齐）  
**最后更新**：2026-08-05

---

## 1. 环境说明

| 环境 | 用途 | 备注 |
|------|------|------|
| local | 本地开发 / 演示 | Docker Compose 一键启动 |
| staging | 预发 | 复用 Compose 或同构单机；密钥独立 |
| prod | 生产（遵义试点） | MVP 可单机；须更换默认密码与 JWT |

架构形态：**模块化单体**（1×backend + 1×frontend + MySQL + Redis），无独立网关/MQ。

---

## 2. 依赖服务与端口

| 服务 | 容器名建议 | 端口（宿主机） | 说明 |
|------|------------|----------------|------|
| frontend | jhl-frontend | 80（或 dev 5173） | Vue；Nginx 反代 `/api` → backend |
| backend | jhl-backend | 8080 | Spring Boot 3 + Actuator |
| mysql | jhl-mysql | 3306 | MySQL 8；库名见环境变量 |
| redis | jhl-redis | 6379 | 验证码/限流/榜单/JWT 黑名单 |
| （卷）upload | — | — | 本地文件存储目录挂载 |

MVP **不部署**：Kafka/RabbitMQ、MinIO（文件走本地盘）、独立 API Gateway。

---

## 3. 快速启动（本地）

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

数据库迁移：建议后端启动时 Flyway/Liquibase 自动执行（实现阶段选定一种）。

---

## 4. 环境变量清单

| 变量名 | 说明 | 示例 |
|--------|------|------|
| `MYSQL_ROOT_PASSWORD` | MySQL root 密码 | （随机，勿提交） |
| `MYSQL_DATABASE` | 业务库名 | `jianghu_ling` |
| `MYSQL_USER` / `MYSQL_PASSWORD` | 应用账号 | `jhl` / （随机） |
| `SPRING_DATASOURCE_URL` | JDBC | `jdbc:mysql://mysql:3306/jianghu_ling?...` |
| `REDIS_HOST` / `REDIS_PORT` | Redis | `redis` / `6379` |
| `JWT_SECRET` | JWT 签名密钥 | ≥32 字节随机串 |
| `JWT_EXPIRE_SECONDS` | Access 有效期 | `7200` |
| `UPLOAD_DIR` | 本地上传目录 | `/data/uploads` |
| `SMS_MOCK_ENABLED` | 短信 Mock 开关 | `true` |
| `SMS_MOCK_FIXED_CODE` | 可选固定验证码（仅 local） | `123456` |
| `WALLET_SIMULATED` | 模拟钱庄标识 | `true` |
| `DEFAULT_CITY` | 试点城市 | `遵义` |
| `SERVER_PORT` | 后端端口 | `8080` |
| `CORS_ALLOWED_ORIGINS` | 跨域（若前后端分离调试） | `http://localhost:5173` |

完整示例见：`docker/.env.example`（由运维/研发在落地 Compose 时创建）。

**密钥规范**：真实 `.env` 不入库；生产禁止使用文档中的示例口令。

---

## 5. 前端反代约定（Nginx）

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
| `mysql_data` | MySQL 数据 |
| `redis_data` | Redis 持久化（可选 AOF） |
| `upload_data` | 探子清单等上传文件 |

备份建议：每日备份 MySQL；上传目录与库一并快照。账本表 `wallet_ledger` 禁止物理删除。

---

## 7. 日志与排查

```bash
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs -f backend
docker compose -f docker/docker-compose.yml logs -f frontend
```

| 现象 | 排查 |
|------|------|
| health 失败 | MySQL/Redis 是否就绪；`SPRING_DATASOURCE_*` / `REDIS_*` |
| 登录验证码收不到 | MVP 为 Mock，查 backend 日志或 `SMS_MOCK_FIXED_CODE` |
| 上传 404 | `UPLOAD_DIR` 卷是否挂载；Nginx `/files/` 反代 |
| 揭榜报限流 | Redis 是否通；系统参数日上限配置 |
| 结算失败 | 查 `wallet_ledger` 与悬赏状态；分配之和是否等于 90% 池 |

定时任务（超时退款、英雄谱刷新等）异常时：查 backend 日志；后台 `GET /api/v1/admin/jobs`（实现后）。

---

## 8. 发布检查清单

- [ ] 镜像构建成功
- [ ] `.env` 已替换默认密钥（JWT / DB）
- [ ] 数据库迁移完成
- [ ] `/actuator/health` 通过
- [ ] 模拟钱庄文案标识可见
- [ ] 邀请码冷启动已由武林盟预置
- [ ] 告示栏种子内容已配置
- [ ] 关键 P0 冒烟：注册登录 → 充值发令 → 审核 → 揭榜 → 成果审核 → 结算 → 英雄谱

---

## 9. 变更记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | — | 模板初始化 |
| v1.0 | 2026-08-05 | 对齐 architecture v1.0：四服务 Compose、Mock 短信/钱庄、上传卷与反代 |
