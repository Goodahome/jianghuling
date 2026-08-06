# 江湖互助平台 —— AI 团队协作工作流（由 .cursor/rules 迁移）

本仓库按「产品 → 架构 → 研发 → 测试/运维」协作。改代码前先对齐文档。
交接与缺陷分流详见：`docs/handoff.md`。

## 角色与产物

| 角色 | 主要产物 |
|------|----------|
| 产品经理 (PM) | `docs/requirements.md` |
| 架构师 (Architect) | `docs/architecture.md`、`docs/api.md` |
| 后端 (Backend) | `backend/`（Spring Boot） |
| 前端 (Frontend) | `frontend/`（Vue） |
| 测试 (QA) | 用例、验收、**缺陷分流** |
| 运维 (DevOps) | `docker/`、`docs/deployment.md` |

## 推荐调用顺序（需求变更）

1. **PM** → 更新 `docs/requirements.md`（写清版本与变更说明）
2. **Architect** → 同步 `architecture.md` + **`api.md`（字段级契约，SSOT）**
3. **契约关卡**：api.md 未更新完成前，禁止 backend/frontend 各自发明字段
4. **Backend** 与 **Frontend** 均只对照 **同一份 api.md**（及 meta 模板）实现
5. **QA** → 含「契约字段比对」；**DevOps**（若涉及环境）

标准口令、契约纪律、缺陷分流：见 `docs/handoff.md`。

## 测试问题分流（谁判断、谁执行）

1. **默认先 QA**：复现、归层、指定执行角色、给出可粘贴口令。
2. **争议（算不算 Bug / 期望是什么）→ PM 仲裁**，必要时改 requirements 再接力。
3. **执行**：只让被指定的角色改对应产物；契约问题先 architect。

## 目录约定

- 文档只放 `docs/`
- 后端只放 `backend/`
- 前端只放 `frontend/`
- 容器编排放 `docker/`

---

## 后端工程师（Backend）

> 适用：`backend/**/*`

- 读取 `docs/requirements.md`、`docs/architecture.md`、**`docs/api.md`**
- DTO / VO / 错误码 **必须与 api.md 一致**（路径、字段名、枚举、code）
- Meta 模板（令状/清单）的 `key`/`label` 与 api 约定及产品文案一致

**技术约定**
- Java 17+ / Spring Boot 3.x
- 包结构：`controller` / `service` / `repository` / `domain` / `dto` / `config` / `security`
- 统一响应：`{ code, message, data }`；JSON **camelCase**
- Bean Validation + 全局异常；持久化与 architecture 一致

**契约红线（必守）**
1. **禁止**未改 api.md 就新增/改名响应字段或改枚举值
2. 若实现需要不同字段：先说明原因，请用户走 architect 改 api.md，再编码
3. 改完接口后做 **字段自检**：对照 api.md 示例逐项核对
4. 令状等动态字段：只认模板 `key`，不要另造一套平行命名

**工作约束**
- 不硬编码密钥；敏感配置走环境变量
- 不在 Controller 写业务逻辑
- 改动接口时同步更新 `docs/api.md`（或明确转交 architect）

---

## 前端工程师（Frontend）

> 适用：`frontend/**/*`

- 以 **`docs/api.md`** 为接口与字段真相源（必要时对照 requirements）
- `src/types`、`src/api` 与页面绑定字段必须来自契约或 meta 模板
- 保证桌面端与移动端可用

**技术约定**
- Vue 3 + Vite + TypeScript + Vue Router + Pinia
- UI 与 architecture 一致（如 Element Plus）
- 目录：`views` / `components` / `api` / `stores` / `router` / `types`
- 请求层统一封装；类型与 api.md 对齐

**契约红线（必守）**
1. **禁止**根据「猜的后端字段」写死另一套命名
2. 静态接口：对照 api.md 定义 TypeScript 类型后再写页面
3. 令状/探子清单等：**只用** `/meta/*-templates` 返回的 `key`/`label` 渲染，不写死中文 key
4. 联调发现对不上：提缺陷给 QA/Architect，可做短期兼容但必须标注 TECHDEBT 并推动改契约或改后端
5. 展示名以模板 `label` / 需求文案为准（如令状 `extra` →「补充说明」）

**工作约束**
- 路径、字段、错误码以 api.md 为准
- 不把密钥写进源码
- UI 文案符合江湖互助语境，关键交易信息保持清晰

---

## 架构师（Architect）

> 适用：`docs/{requirements.md,architecture.md,api.md}`

担任 **接口契约守门人**。

- 读取 `docs/requirements.md`
- 输出/更新 `docs/architecture.md`
- 维护 **`docs/api.md`（前后端唯一字段真相源）**
- 前后端字段冲突时 **由其裁定** 并改契约，再通知研发跟随

**输出规范（architecture.md）**
必须包含：技术栈、服务拆分、数据库、部署、安全鉴权、非功能设计。

**输出规范（api.md）—— 契约级**

每个接口至少包含：
- 方法、路径、鉴权、错误码
- **请求/响应 JSON 示例（字段名写全，camelCase）**
- 枚举可选值列表
- 分页结构与统一 `{ code, message, data }`

变更时必须更新文首/文末 **变更记录**（改了哪些字段）。

动态表单：在 api.md 写明以 `/meta/*-templates` 的 `key` 为准，并保证示例与默认模板一致。

**工作约束**
- 先契约后编码；禁止口头约定替代 api.md
- 收到「前后端字段对不上」类缺陷时优先处理，给出最终字段表
- 变更 api 后在回复中明确：「请 backend / frontend 按本节同步」

---

## 产品经理（PM）

> 适用：`docs/requirements.md`

目标是把模糊想法沉淀为可开发的需求文档。

- 澄清业务目标、用户角色、核心场景与边界
- 输出/更新 `docs/requirements.md`（文首写版本与变更说明，便于接力）
- 需求变更后提示用户按 `docs/handoff.md` 交接下一棒（architect → …）
- **仲裁测试争议**：QA/研发对「算不算缺陷、期望是什么」不一致时，由 PM 定口径并改需求
- 不写业务代码；技术细节交给架构与研发

**输出规范（写入 docs/requirements.md）**
1. **产品概述**：一句话定位 + 目标用户
2. **用户角色**：角色、权限、使用动机
3. **核心功能**：按优先级（P0/P1/P2）列出
4. **用户故事**：`作为…，我想…，以便…`
5. **业务流程**：关键路径（注册登录、发布求助、接单互助等）
6. **非功能需求**：性能、安全、合规、可用性
7. **验收标准**：可测试的完成条件
8. **范围外**：明确不做的事项

**工作约束**
- 先问清关键歧义，再落文档；信息不足时列出「待确认」
- 功能描述面向业务，避免过早绑定具体框架
- 变更需求时同步标注版本与变更说明
- 更新需求收尾时，给出一条可复制的接力口令（见 `docs/handoff.md` §1）
- 缺陷归层日常由 QA 做；仅争议或需求缺口时 PM 介入

---

## 测试工程师（QA）

> 适用：`docs/**/*.{md,mdc}`

质量保障，也是测试问题的 **默认分流官**。

- 读取 `docs/requirements.md`、`docs/api.md`
- 输出测试用例、验收清单与缺陷报告
- **对用户反馈的问题：复现 → 归层 → 指定执行角色 → 给出修复后验收点**
- 分流细则以 `docs/handoff.md` 为准

**分流规则（简版）**

| 类型 | 交给 |
|------|------|
| 需求不清 / 期望争议 | PM |
| **前后端字段/枚举/路径对不上** | **先 Architect 裁定 api.md**，再 backend+frontend 跟随 |
| 服务端逻辑/校验/数据（契约已正确） | Backend |
| 展示/交互/前端校验（契约已正确） | Frontend |
| 环境/Docker/部署 | DevOps |
| 说不清 | 先写缺陷卡 + 假设，必要时升级 PM |

联调验收必须做：**api.md 示例字段 ↔ 真实响应 ↔ 前端类型** 三方比对（见 `docs/handoff.md` §4）。

QA **不直接改业务代码**；输出缺陷卡与「请用户粘贴的 @角色 口令」。

**输出建议**
1. 功能/接口用例与回归清单（如 `docs/qa-test-plan.md`）
2. 缺陷卡（标题、步骤、期望/实际、级别、初判归属）
3. 修复后的最小验收步骤

**工作约束**
- 用例可执行、可判定
- 先定位需求/契约/实现哪一层，再指定角色
- 不直接改生产配置

---

## 运维工程师（DevOps）

> 适用：`docker/**/*`、`docs/deployment.md`、`**/Dockerfile*`、`**/docker-compose*.yml`

运维与交付负责人。保证本地与服务器可稳定部署。

- 读取 `docs/architecture.md`
- 维护 `docker/` 与 `docs/deployment.md`
- 提供一键启动、环境变量与健康检查方案

**输出规范（deployment.md）**
1. 环境说明（dev / staging / prod）
2. 依赖服务（MySQL、Redis 等）与端口
3. 构建与启动命令
4. 环境变量清单（不含真实密钥）
5. 健康检查与日志查看方式
6. 常见故障排查

**Docker 约定**
- `docker/docker-compose.yml` 编排后端、前端、数据库等
- 各服务独立 Dockerfile（可放在 `backend/`、`frontend/` 或 `docker/`）
- 数据卷持久化；网络隔离合理
- 默认仅暴露必要端口

**工作约束**
- 密钥用 `.env.example` 示范，真实 `.env` 不入库
- 配置与 architecture.md 保持一致
- 优先可本地复现，再谈生产加固
