# 江湖互助平台 · Cursor AI 团队模板

方案四：项目级 AI 团队协作模板。用角色规则驱动「需求 → 架构 → 前后端 → 测试/部署」。

## 项目结构

```
Jinanghu_Ling
├── .cursor/rules/     # AI 角色规则
├── docs/              # requirements / architecture / api / deployment / handoff
├── backend/           # Spring Boot
├── frontend/          # Vue 3
├── docker/            # 本地 compose
├── k8s/               # 集群清单
└── scripts/k8s/       # 同步 / 构建推送 / 部署脚本
```

## 推荐工作流

1. **@pm** → `docs/requirements.md`
2. **@architect** → `docs/architecture.md` + `docs/api.md`（字段 SSOT）
3. **@backend / @frontend** → 只对照同一份 `api.md` 实现
4. **@qa / @devops** → 验收 + 部署

交接与缺陷分流见 **[docs/handoff.md](docs/handoff.md)**；本地 Docker 与完整运维说明见 **[docs/deployment.md](docs/deployment.md)**。

---

## K8s 部署（跳板机构建 → 集群）

### 环境

| 角色 | 地址 | 说明 |
|------|------|------|
| 开发机（Windows） | 本机仓库 | 改代码后同步到跳板机 |
| 跳板 / 构建机 | `kssrol@100.66.0.3` | 代码 `~/jianghu_ling`；Docker build + push |
| master | `kssrol@10.120.20.17` | `kubectl apply` / 滚动更新 |
| 私有仓库 | `10.120.20.201:5000` | HTTP，需 insecure-registry |
| Ingress | `10.120.20.200` | 域名 `jianghu.kssrol.com` |

- Namespace：`jianghu-ling`（独立 Redis；MySQL 用平台现有库，不部署本 ns MySQL）
- 镜像：`jianghu-ling-backend:latest` / `jianghu-ling-frontend:latest`
- TLS Secret：`kssrol-com-tls`（须在 `jianghu-ling` ns；可从 `ingress-nginx` 复制）
- 空库须手工执行：`schema.sql` → `data.sql` → 所需 `patch_*.sql`（应用启动**不会**自动灌基础数据）

### ① Windows → 跳板机同步代码

在仓库根目录：

```powershell
cd F:\Jinanghu_Ling
powershell -ExecutionPolicy Bypass -File scripts\k8s\sync-from-windows.ps1
```

默认目标：`kssrol@100.66.0.3:~/jianghu_ling`。

### ② 跳板机构建并推送镜像

```bash
ssh kssrol@100.66.0.3
cd ~/jianghu_ling

# 首次：Docker 需允许 HTTP 私有仓（/etc/docker/daemon.json）
#   "insecure-registries": ["10.120.20.201:5000"]
#   然后：sudo systemctl restart docker

bash scripts/k8s/02-build-push.sh
# 指定版本：TAG=v1 bash scripts/k8s/02-build-push.sh
```

脚本在 Docker 内完成前后端构建，一般不必在跳板机安装 JDK。若 `.sh` 报 `$'\r'`，先执行：`sed -i 's/\r$//' scripts/k8s/*.sh`。

### ③ 同步部署清单到 master 并 apply

构建机上把 `k8s/` 与部署脚本拷到 master（构建机通常无 kubeconfig）：

```bash
# 在跳板机执行
ssh kssrol@10.120.20.17 'mkdir -p ~/jianghu_ling/k8s ~/jianghu_ling/scripts/k8s'
scp -r ~/jianghu_ling/k8s/* kssrol@10.120.20.17:~/jianghu_ling/k8s/
scp ~/jianghu_ling/scripts/k8s/03-deploy.sh kssrol@10.120.20.17:~/jianghu_ling/scripts/k8s/
```

登录 master 部署：

```bash
ssh kssrol@10.120.20.17
cd ~/jianghu_ling
sed -i 's/\r$//' scripts/k8s/03-deploy.sh   # 若需要
bash scripts/k8s/03-deploy.sh
```

也可手动：

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-secrets.yaml
kubectl apply -f k8s/02-configmap.yaml
kubectl apply -f k8s/10-redis.yaml
kubectl apply -f k8s/20-backend.yaml
kubectl apply -f k8s/21-frontend.yaml
kubectl apply -f k8s/30-ingress.yaml
# 注意：默认不 apply k8s/11-mysql.yaml
```

仅更新镜像后滚动重启：

```bash
kubectl -n jianghu-ling rollout restart deploy/backend deploy/frontend
kubectl -n jianghu-ling rollout status deploy/backend
kubectl -n jianghu-ling rollout status deploy/frontend
```

### ④ TLS Secret（首次）

若 `jianghu-ling` 中没有证书：

```bash
kubectl get secret kssrol-com-tls -n ingress-nginx -o yaml \
  | sed -e 's/namespace: ingress-nginx/namespace: jianghu-ling/' \
        -e '/resourceVersion:/d' -e '/uid:/d' -e '/creationTimestamp:/d' \
  | kubectl apply -f -
```

Ingress 须带 `ingressClassName: nginx`（见 `k8s/30-ingress.yaml`），否则会落到默认后端 404。

### ⑤ 验证

```bash
kubectl -n jianghu-ling get pods,svc,ingress
kubectl -n jianghu-ling logs -f deploy/backend

# 健康检查
kubectl -n jianghu-ling exec deploy/backend -- \
  curl -fsS http://127.0.0.1:8080/actuator/health

# 浏览器：https://jianghu.kssrol.com/
# DNS：jianghu.kssrol.com → 10.120.20.200
# 或临时 hosts：10.120.20.200  jianghu.kssrol.com
```

### 流程速查

```text
Windows 改代码
    → sync-from-windows.ps1 → 跳板机 ~/jianghu_ling
    → 02-build-push.sh      → 10.120.20.201:5000
    → scp k8s/ + 03-deploy  → master 10.120.20.17
    → 03-deploy.sh / rollout restart
    → https://jianghu.kssrol.com/
```

更多排障（insecure registry、节点拉镜像、库初始化等）见 [docs/deployment.md §8](docs/deployment.md)。
