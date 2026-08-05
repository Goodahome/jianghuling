# 职责交接与缺陷分流（Handoff）

> 产品改需求后如何接力；测试发现问题后由谁判断、交给谁修。  
> 配合 `.cursor/rules/workflow.mdc` 使用。

---

## 1. 产品更新后的标准接力

### 1.1 交接卡（复制填写）

```markdown
## 交接卡
- 需求版本：vX.Y
- 变更摘要：
  1. …
  2. …
- 影响端：文档 / 后端 / 前端 / 后台 / Docker
- 明确不做：…
- 下一棒：architect → backend → frontend → qa（按需 devops）
```

### 1.2 一键接力口令（小改动）

```text
按 .cursor/rules/workflow.mdc 与 docs/handoff.md 执行：
1) @architect 同步 architecture.md + api.md
2) @backend 落地后端
3) @frontend 落地前端
4) @qa 更新/回归验收
变更见 docs/requirements.md 文首「变更说明」。
约束：先文档后代码；不扩散范围外需求。
```

### 1.3 分步口令（大改动，推荐）

**架构**

```text
@architect
请读取 docs/requirements.md 最新变更，同步 docs/architecture.md 与 docs/api.md，列出相对上一版差异。
```

**后端**

```text
@backend
请按最新 docs/api.md（及 architecture）实现本次变更，完成后简述改动文件与接口。
```

**前端**

```text
@frontend
请按最新 docs/api.md / meta 落地本次 UI 与交互，对齐需求文案（含武侠展示名）。
```

**测试**

```text
@qa
请对照 requirements 验收标准做回归，输出结论与缺陷清单（用下文缺陷卡）。
```

**运维（仅当涉及环境/编排时）**

```text
@devops
请同步 docker/ 与 docs/deployment.md，保证本地可起。
```

---

## 2. 测试问题：谁判断？交给谁？

### 2.1 角色分工

| 角色 | 在分流中的职责 |
|------|----------------|
| **你（用户）** | 描述现象；可先 `@qa` 或贴缺陷卡 |
| **@qa** | **默认分流官**：复现 → 归层 → 指定执行角色 → 给出修复验收点 |
| **@pm** | **争议仲裁**：QA 与研发对「算不算缺陷 / 要不要改需求」意见不一致时，由 PM 定口径并改 requirements |
| **@architect** | 契约/模型/跨端方案错误时改 architecture + api |
| **@backend / @frontend / @devops** | 按 QA（或 PM）指定范围修复，不擅自改需求 |

**原则**：先 `@qa` 分流；只有「需求含糊 / 该不该做」才升级 `@pm`。

### 2.2 归层速查表

| 现象特征 | 归属 | 执行 |
|----------|------|------|
| 和需求不一致，或需求没写清「期望」 | 需求缺口 / 口径 | `@pm` 改 requirements → 再接力架构/研发 |
| 前后端字段/状态码/路径对不上 | 契约 | `@architect` 改 api（必要时 architecture）→ backend + frontend |
| 接口逻辑、校验、落库、鉴权、钱庄、状态机错误 | 后端实现 | `@backend` |
| 展示、文案、表单、路由、样式、仅前端校验 | 前端实现 | `@frontend` |
| 容器起不来、端口、环境变量、部署文档错 | 环境交付 | `@devops` |
| 两边都像有问题 | 先契约 | `@architect` 定正确契约 → 再修实现 |
| 无法判断 | — | `@qa` 写清复现与假设，必要时拉 `@pm` |

### 2.3 缺陷卡（测试填写 / QA 补全）

```markdown
## 缺陷卡
- 标题：
- 严重级别：P0阻断 / P1主要 / P2次要 / P3体验
- 复现步骤：
  1.
  2.
- 期望（引用需求/API 条款更佳）：
- 实际：
- QA 初判归属：pm / architect / backend / frontend / devops
- 初判理由（一层）：
- 建议执行口令：（见下）
```

### 2.4 分流后执行口令（QA 输出给用户直接粘贴）

**判给后端时：**

```text
@backend
请修复以下缺陷（见缺陷卡）。勿改需求口径；若发现是契约问题请停止并转 @architect。
（粘贴缺陷卡）
```

**判给前端时：**

```text
@frontend
请修复以下缺陷（见缺陷卡）。文案以 requirements 为准。
（粘贴缺陷卡）
```

**判给架构时：**

```text
@architect
请裁定并更新 api/architecture，再注明需 backend/frontend 跟随的点。
（粘贴缺陷卡）
```

**判给产品时：**

```text
@pm
请确认期望口径并更新 docs/requirements.md；确认后再按 handoff 接力研发。
（粘贴缺陷卡）
```

**有争议时：**

```text
@pm @qa
研发认为是需求如此，测试认为是缺陷。请 PM 仲裁期望，QA 记录结论。
（粘贴缺陷卡 + 双方观点）
```

---

## 3. 推荐日常节奏

```
你发现 Bug
  → @qa 填缺陷卡 + 归层
      → 明确执行角色口令
          → 对应角色修复
              → @qa 回归该缺陷
                  → 通过则关闭；失败则重新归层
```

产品主动改需求时：

```
@pm 更新 requirements
  → 填交接卡 / 用一键接力口令
      → architect（先锁 api.md）→ backend + frontend（同契约）→ qa（含字段比对）
```

---

## 4. 前后端契约关卡（防字段对不上）

### 4.1 根因约定

| 错误做法 | 正确做法 |
|----------|----------|
| 后端先写 DTO，前端再猜字段 | **先改 `docs/api.md`**，两边再实现 |
| 前端写死中文/另一套 key | 静态接口跟 api.md；令状等跟 **meta 模板 key** |
| 对不上就前端兼容多层 | 先 `@architect` 定稿，再改一端或两端 |

`docs/api.md` §0「契约纪律」为强制规则。

### 4.2 联调前字段比对表（QA / 研发自检）

```markdown
## 契约比对
- 接口：METHOD /path
- api.md 字段清单：
- 实际响应字段清单：
- 前端类型/使用字段清单：
- 差异：
- 归属：architect / backend / frontend
```

### 4.3 字段对不上时的口令

```text
@qa
前后端字段对不上：……请按 docs/handoff.md §4 分流（优先契约）。
```

```text
@architect
请裁定下列字段的最终命名与类型，更新 docs/api.md 变更记录，并注明 backend/frontend 各自改什么。
（粘贴比对表或响应 JSON + 前端类型）
```

```text
@backend @frontend
api.md 已更新（见变更记录）。请严格按新契约同步，禁止保留旧字段分叉（除非标注废弃期）。
```

### 4.4 产品改「展示名」时注意

只改展示文案时：优先改 **meta 模板 `label`**，**不要**改 `key`（如 `extra` 的 label 为「补充说明」），避免前后端存储字段断裂。
