# 江湖互助平台 · Cursor AI 团队模板

方案四：项目级 AI 团队协作模板。用角色规则驱动「需求 → 架构 → 前后端 → 测试/部署」。

## 项目结构

```
Jinanghu_Ling
├── .cursor
│   └── rules
│       ├── workflow.mdc    # 总工作流（始终生效）
│       ├── pm.mdc
│       ├── architect.mdc
│       ├── backend.mdc
│       ├── frontend.mdc
│       ├── qa.mdc
│       └── devops.mdc
├── docs
│   ├── requirements.md
│   ├── architecture.md
│   ├── api.md
│   └── deployment.md
├── backend
├── frontend
└── docker
```

## 推荐工作流

1. **产品经理 AI（@pm）**  
   输入：「我要做一个江湖互助平台」  
   输出：`docs/requirements.md`

2. **架构 AI（@architect）**  
   读取：`docs/requirements.md`  
   输出：`docs/architecture.md`（技术栈 / 服务拆分 / 数据库 / 部署）+ `docs/api.md`

3. **后端 AI（@backend）**  
   读取：requirements + architecture + api  
   生成：`backend/` Spring Boot 代码

4. **前端 AI（@frontend）**  
   读取：`docs/api.md`  
   生成：`frontend/` Vue 页面

5. **测试（@qa）/ 运维（@devops）**  
   验收用例 + `docker/` 与 `docs/deployment.md`

## 快速开始

在 Cursor 对话中按角色依次调用，例如：

```text
@pm 我要做一个江湖互助平台，请生成 docs/requirements.md
```

```text
@architect 请基于 docs/requirements.md 输出 architecture.md 和 api.md
```

```text
@backend 请按 docs 生成 Spring Boot 工程骨架与 P0 接口
```

```text
@frontend 请按 docs/api.md 生成 Vue 页面
```
