# Docker

容器与本地编排目录。

## 文件

- `docker-compose.yml`：服务编排（当前含 MySQL / Redis；后端前端待工程生成后启用）
- `.env.example`：环境变量模板（复制为 `.env` 后填写）

## 启动

```bash
cp .env.example .env
docker compose up -d
```

详情见 `docs/deployment.md`。由 **@devops** 维护。
