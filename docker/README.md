# Docker

容器与本地编排目录。

## 文件

- `docker-compose.yml`：本地编排（默认 MySQL / Redis；backend/frontend 服务块仍为注释，可按需启用）
- `.env.example`：环境变量模板（复制为 `.env` 后填写）
- 镜像构建上下文在仓库：`backend/Dockerfile`、`frontend/Dockerfile`（K8s / 推私有仓用）

## 启动

```bash
cp .env.example .env
docker compose up -d
```

K8s 部署见 `docs/deployment.md` §8 与 `k8s/`、`scripts/k8s/`。由 **@devops** 维护。
