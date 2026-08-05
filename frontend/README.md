# 江湖令 · 前端

Vue 3 + Vite + TypeScript 单工程三区：

| 区 | 路径 | UI |
|----|------|-----|
| 侠士端 | `/` | 自定义江湖主题 |
| 执事堂 | `/hall` | Element Plus |
| 武林盟后台 | `/admin` | Element Plus |

接口契约以 `docs/api.md` 为准；开发代理将 `/api`、`/files` 转发至 `http://localhost:8080`。

## 启动

```bash
npm install
npm run dev
```

环境变量见 `.env.example` / `.env.development`。

## 目录

```
src/
  api/        # 请求封装与接口
  stores/     # Pinia 鉴权
  router/     # 三区路由与守卫
  layouts/    # 布局
  views/      # hero / hall / admin 页面
  types/      # 与 API 对齐的类型
  components/ # 通用组件
  styles/     # 侠士端主题
```

## 联调说明

- 侠士 Token：`localStorage.jh_token`
- 管理员 Token：`localStorage.jh_admin_token`
- 统一响应：`{ code, message, data }`，`code !== 0` 由请求层提示
