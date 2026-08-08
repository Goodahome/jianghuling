# 江湖令 · QA 测试计划与用例

> 角色：测试工程师（@qa）  
> 对照：`docs/requirements.md`（**v1.6.3** §9 产品验收）、`docs/api.md`（v1.0.1）、`docs/architecture.md`（v1.0.1）  
> 基于前后端当前实现（2026-08-05，含令状展示修复与 ranks/offices/disputes/messages 落地）  
> **测试范围说明**：可执行项须勾选；依赖未实现或未联调的标 **待测/Blocked**。

---

## 0. 测试结论摘要

| 维度 | 结论 |
|------|------|
| 单元测试 | `mvn test` 通过（钱庄充值/幂等/冻结、自揭/重复揭榜） |
| 主交易闭环 | 后端接口齐；前端三区路由齐；**可端到端手测**（需人工授职执事） |
| 安全隔离 | 侠士 Token ↔ Admin Token 互调返回 `40300`（已冒烟） |
| 业务校验 | 赏银 `<200` → `43001`；令状缺字段 → `43002`；充值 `clientRequestId` 幂等（已冒烟） |
| 令状展示 | 详情须用 Meta `label` 中文展示（见 §3.3 / §11）；禁止裸英文 key（D-005 已修，须回归） |
| 原阻断模块 | 英雄谱 / 职司申请 / 纠纷 / 站内消息：**Controller 已落地**，升为 **可测·待执行** |
| MVP 完整度 | P0-A/B 主干可验；完整管理员 RBAC 等多角色仍可能缩水 |

**本批 Release Gate**：主闭环 + **租房令状中文展示** + 双审结算 + 后台主干；§11 清单全部勾选方可签本批。

---

## 1. 环境与账号

| 项 | 值 |
|----|-----|
| 后端 | `http://localhost:8080`，健康检查 `/actuator/health` |
| 前端 | Vite 开发服（见 `frontend` README） |
| 邀请码 | `JHOPEN1` / `JHOPEN2`（用尽后由后台生成） |
| 短信 Mock | `123456`（`MOCK_SMS_CODE`） |
| 管理员 | `admin` / `admin123`（仅 `/api/v1/admin/auth/login`） |
| 执事 | 注册侠士后 SQL 授职：`DECREE_REVIEWER` / `FEAT_REVIEWER`（见 `backend/README.md`） |

统一响应：`{ code, message, data }`；HTTP 多为 200，以业务 `code` 判定。

---

## 2. 实现对照（测什么 / 不测什么）

### 2.1 已实现可测

| 模块 | 前端 | 后端 |
|------|------|------|
| 邀请注册/登录/资料/邀请 | `/login` `/register` `/r/:code` `/profile` `/invites` | `/auth/**` `/user/**` |
| 钱庄 | `/wallet` | `/wallet/**` |
| Meta/告示 | 发令页、`/notices` | `/meta/**` `/notices/**` |
| 悬赏主链路 | `/` `/bounties/*` `/mine` | `/bounties/**` `/submissions/**` |
| 结算互评 | `/bounties/:id/settle` | `/bounties/{id}/settlement|evaluations|cancel` |
| 成长兑换 | `/growth` | `/growth/**` |
| 英雄谱 / 盟主申请 | `/ranks` | `/ranks/**` `/lord/**` |
| 职司申请 | `/offices` | `/offices/**` |
| 纠纷 | `/disputes` `/disputes/:id` | `/bounties/{id}/disputes` `/disputes/**` |
| 站内消息 | `/messages` | `/messages/**` |
| 执事堂 | `/hall/**` | `/hall/**` |
| 武林盟主干 | `/admin/**` | `/admin/**`（含告示、职司/盟主审批、清单/令状配置、系统参数、审计等） |

### 2.2 仍缩水 / 需抽检

| 能力 | 说明 |
|------|------|
| 管理员完整 RBAC | 多角色/菜单权限可能仍为 `permissions:["*"]` |
| 未实现路径错误码 | 若仍返回 `50000` 而非 `40400`，记为实现缺陷 |
| 后台「等级/英雄谱规则/兑换奖品」等配置完备度 | 对照 api.md §16 抽检是否齐 |

---

## 3. 功能用例（P0）

### 约定

- **优先级**：P0 必测 / P1 重要 / P2 回归  
- **判定**：期望与实际一致 → Pass；否则 Fail；依赖未实现 → Blocked  
- **严重级别**：S1 资金/安全阻断 · S2 主流程不可用 · S3 功能缺陷 · S4 体验/文案

---

### 3.1 鉴权与邀请

| ID | 优先级 | 步骤 | 期望 | 实测/状态 |
|----|--------|------|------|-----------|
| F-AUTH-01 | P0 | 无效邀请码 `POST /auth/invite/validate` | `44001` 或 valid=false | 待全量 |
| F-AUTH-02 | P0 | 有效码 `inviteCode=JHOPEN*` | `code=0`，`valid=true` | **Pass**（冒烟） |
| F-AUTH-03 | P0 | Body 用错字段 `code` 而非 `inviteCode` | `40001` | **Pass**（冒烟） |
| F-AUTH-04 | P0 | 无邀请码注册 | 拒绝，不可创建用户 | 待测 |
| F-AUTH-05 | P0 | 邀请码+短信 `123456`+username/nickname 注册 | 返回 token + user | **Pass** |
| F-AUTH-06 | P0 | 密码登录 `loginType=PASSWORD` | 返回 token | **Pass** |
| F-AUTH-07 | P0 | 已注册用户无需邀请码登录 | 成功 | **Pass** |
| F-AUTH-08 | P0 | 未登录访问 `/wallet/account` | `40100` | **Pass** |
| F-AUTH-09 | P0 | 侠士 Token 调 `/admin/dashboard/overview` | `40300` | **Pass** |
| F-AUTH-10 | P0 | Admin Token 调 `/auth/me` | `40300` | **Pass** |
| F-AUTH-11 | P0 | 前端未登录访问 `/wallet` | 跳转 `/login` | 待 UI |
| F-AUTH-12 | P0 | 无职司访问 `/hall` | 跳转 `/offices` | 待 UI（职司申请页 Blocked） |
| F-AUTH-13 | P0 | 管理端未登录访问 `/admin` | 跳转 `/admin/login` | 待 UI |
| F-AUTH-14 | P0 | 后台禁用/封禁用户后登录或请求 | `40301` 或登录失败 | 待测 |
| F-AUTH-15 | P1 | 用尽邀请码再注册 | `44001` | 待测 |

---

### 3.2 钱庄

| ID | 优先级 | 步骤 | 期望 | 实测/状态 |
|----|--------|------|------|-----------|
| F-WAL-01 | P0 | `POST /wallet/recharge`，`amount`+`clientRequestId` | 余额增加 | **Pass** |
| F-WAL-02 | P0 | 同一 `clientRequestId` 再充 | 幂等，余额不翻倍 | **Pass** |
| F-WAL-03 | P0 | 提现超过余额 | `42001` | 待测 |
| F-WAL-04 | P0 | 发令成功后余额↓、冻结↑ | 账本有 FREEZE | 待 E2E |
| F-WAL-05 | P0 | 驳回/超时取消 | 全额解冻退回 | 待测 |
| F-WAL-06 | P1 | 流水分页 `GET /wallet/ledgers` | 列表与金额一致 | 待测 |

---

### 3.3 发令 / 广场 / 揭榜 / 成果

| ID | 优先级 | 步骤 | 期望 | 实测/状态 |
|----|--------|------|------|-----------|
| F-BN-01 | P0 | Meta：赏银建议/令状/清单模板 | `code=0`，最低 200；令状字段含 `key`+`label` | **Pass**（meta 结构） |
| F-BN-02 | P0 | `rewardAmount=100` 发令 | `43001` | **Pass** |
| F-BN-03 | P0 | 缺令状必填字段 | `43002` | **Pass** |
| F-BN-04 | P0 | 低于建议下限且未 `confirmLowReward` | `40002` | 待测 |
| F-BN-05 | P0 | 合法发令（求租/出租） | 状态 `PENDING_REVIEW`，赏银冻结 | 待 E2E |
| F-BN-06 | P0 | 令审使/管理员 APPROVE | 状态 `OPEN`，出现在广场 | 待 E2E |
| F-BN-07 | P0 | 令审 REJECT | 退款，状态驳回 | 待测 |
| F-BN-08 | P0 | 本人揭榜 | `43005` 或业务拒绝 | 单测有规则，待接口 |
| F-BN-09 | P0 | 同令重复揭榜 | `40900`/`43005` | 单测有 |
| F-BN-10 | P0 | ≥2 人揭同一令 | 均成功，会话可进 | 待 E2E |
| F-BN-11 | P0 | 日揭榜第 11 次 | `43004`/`42900` | 待测 |
| F-BN-12 | P0 | 体力不足揭榜 | `43003` | 待测 |
| F-BN-13 | P0 | 非参与人读会话消息 | `40300` | 待测 |
| F-BN-14 | P0 | 按清单提交成果 | 进入待验功 | 待 E2E |
| F-BN-15 | P0 | 冷却期内重复提交 | `43006` | 待测 |
| F-BN-16 | P1 | 出租令精确地址对未揭榜脱敏 | 详情字段脱敏 | 待测 |
| F-BN-17 | P0 | **详情「租房令状」标签为中文**，与 `GET /meta/warrant-templates` 的 `label` 一致（如区域/户型/是否接受中介），**禁止**显示 `district`/`layout` 等英文 key | 见 §11.2 | **须回归**（D-005） |
| F-BN-18 | P0 | `extra` 为空时详情**不展示**该行；有值时标签为「补充说明」（禁止「令外叮嘱」「其他要求」作独立 key） | 对齐 api.md §0 / §5.2 | **须回归** |
| F-BN-19 | P0 | 发令页字段 label 与详情页同源（Meta），boolean 展示「是/否」 | 发令↔详情一致 | 待测 |
| F-BN-20 | P0 | 契约三方比对：api.md §5.2 示例 key ⊆ Meta 响应 ⊆ 发令提交 ⊆ 详情 `warrantFields` | 无多余同义 key（remark/note 等） | 待测 |
| F-BN-21 | P1 | 后台改令状字段 label 后，C 端发令/详情立即或刷新后反映（`extra` 服务端仍强制「补充说明」） | `/admin/warrant-config` | 待测 |
---

### 3.4 执事堂双审

| ID | 优先级 | 步骤 | 期望 | 实测/状态 |
|----|--------|------|------|-----------|
| F-HALL-01 | P0 | 无职司调 `/hall/bounty-reviews` | `40310` | 待测 |
| F-HALL-02 | P0 | 仅令审使审发令队列 | 可 APPROVE/REJECT | 待测 |
| F-HALL-03 | P0 | 令审使调验功接口 | `40310` | 待测 |
| F-HALL-04 | P0 | 审自己发的令（回避） | 拒绝 | 待测 |
| F-HALL-05 | P0 | 验功通过成果 | 成果有效，可结算 | 待 E2E |
| F-HALL-06 | P1 | `GET /hall/my-actions` | 有操作记录 | 待测 |

---

### 3.5 结算 / 取消 / 互评

| ID | 优先级 | 步骤 | 期望 | 实测/状态 |
|----|--------|------|------|-----------|
| F-SET-01 | P0 | 预览结算：服务费 10%，可分配池 90% | 金额正确 | 待测 |
| F-SET-02 | P0 | 分配未分完提交 | `42003` | 待测 |
| F-SET-03 | P0 | 分完提交 | 状态 `COMPLETED`，各方到账 | 待 E2E |
| F-SET-04 | P0 | 允许 0 两+可选侠义值 | 规则符合需求 | 待测 |
| F-SET-05 | P0 | 无有效成果不可「空分」 | 仅能取消退款 | 待测 |
| F-SET-06 | P0 | 互评后好评率/完成单量更新 | 资料/资产一致 | 待测（英雄谱 Blocked） |
| F-SET-07 | P1 | 令主取消（规则允许时） | 解冻退款 | 待测 |

---

### 3.6 成长

| ID | 优先级 | 步骤 | 期望 | 实测/状态 |
|----|--------|------|------|-----------|
| F-GR-01 | P0 | `GET /growth/level` | 等级/进度 | **Pass** |
| F-GR-02 | P0 | 侠义值兑体力，仍受日揭榜≤10 | 规则生效 | 待测 |
| F-GR-03 | P1 | 兑换配置奖品 | 订单可查 | 待测 |
| F-GR-04 | P1 | 侠义值达标晋升头衔 | 资料页展示 | 待 UI |

---

### 3.7 后台（武林盟）

| ID | 优先级 | 步骤 | 期望 | 实测/状态 |
|----|--------|------|------|-----------|
| F-ADM-01 | P0 | 管理员登录 | token + permissions | **Pass** |
| F-ADM-02 | P0 | 工作台 overview | 有统计数据 | 待测 |
| F-ADM-03 | P0 | 用户启停/封禁/备注/调账 | 生效且有审计 | 待测 |
| F-ADM-04 | P0 | 邀请码生成/失效 | C 端可用/不可用 | 待测 |
| F-ADM-05 | P0 | 悬赏强制关闭 | 状态关闭+资金正确 | 待测 |
| F-ADM-06 | P0 | 管理员代审发令/成果 | 与执事堂等效 | 待测 |
| F-ADM-07 | P0 | 流水与手续费汇总 | 可查 | 待测 |
| F-ADM-08 | P0 | 告示 CRUD | C 端可见 | 待测 |
| F-ADM-09 | P1 | 职司/盟主申请审批 | 批准后权限生效 | 申请入口 Blocked |
| F-ADM-10 | P1 | 系统参数读写 | 影响业务阈值 | 待测 |
| F-ADM-11 | P1 | 审计日志 | 敏感操作可追溯 | 待测 |
| F-ADM-12 | P1 | 完整 RBAC/多管理员 | 按需求 | **Blocked**（固定 `*`） |

---

### 3.8 英雄谱 / 职司 / 纠纷 / 消息（Controller 已落地 · 待执行）

| ID | 优先级 | 步骤 | 期望 | 状态 |
|----|--------|------|------|------|
| F-RNK-01 | P0 | `GET /ranks/reputation|chivalry|completed` + 页 `/ranks` | 三榜有数据或空态正常；与资产口径一致 | 可测·待执行 |
| F-RNK-02 | P0 | `GET /ranks/me` | 本人排名摘要 | 可测·待执行 |
| F-LRD-01 | P0 | 非声望第 1 申请盟主 | 业务拒绝 | 可测·待执行 |
| F-LRD-02 | P0 | 声望第 1 申请 → 后台批准 | 全局仅 1 名盟主；荣耀位更新 | 可测·待执行 |
| F-OFC-01 | P0 | `GET /offices/defs`；等级不足申请 | 拒绝 | 可测·待执行 |
| F-OFC-02 | P0 | 达标申请 → 后台批准 → 可进 `/hall` 对应队列 | 未批无审核权 | 可测·待执行 |
| F-DSP-01 | P0 | 结算后 7 日内 `POST /bounties/{id}/disputes` | 建单成功；`/disputes` 可见 | 可测·待执行 |
| F-DSP-02 | P0 | 超窗或未结算发起 | 拒绝 | 可测·待执行 |
| F-DSP-03 | P0 | Admin 终裁 | 资金/状态正确 | 可测·待执行 |
| F-MSG-01 | P0 | 审核/揭榜等触发后 `GET /messages` | 有站内信；已读接口生效 | 可测·待执行 |

---

## 4. 接口用例（抽样矩阵）

| 接口 | 鉴权 | 正向 | 负向 | 幂等/限流 |
|------|------|------|------|-----------|
| `POST /auth/invite/validate` | 公开 | 有效码 | 缺字段 `40001` | — |
| `POST /auth/register` | 公开 | 完整字段 | 无码 `44001`；缺 username `40001` | — |
| `POST /auth/login` | 公开 | PASSWORD/SMS | 错密 | — |
| `POST /wallet/recharge` | 侠士 | 加余额 | 缺 `clientRequestId` | 同 id 幂等 **Pass** |
| `POST /bounties` | 侠士 | 合法令状 | `43001`/`43002`/`42001` | — |
| `POST /bounties/{id}/claims` | 侠士 | 他号揭榜 | 自揭/重复/体力/日限 | — |
| `POST /hall/*-reviews/{id}` | 职司 | APPROVE | 无职司 `40310`；回避 | — |
| `POST .../settlement` | 令主 | 分完 | `42003` | — |
| `POST /admin/auth/login` | 公开 | admin | 侠士账号不可用此口 | — |
| `GET /ranks/*` 等缺口 | — | — | 现状 `50000`（缺陷） | — |

**缺陷登记**：未实现路径在已登录时应返回 `40400`，当前落入全局 `Exception` → `50000`（见 D-001）。

---

## 5. 主闭环手工脚本（Release Gate）

准备：2 个侠士账号 A（令主）、B（揭榜）；1 个执事 C（或管理员代审）。

1. A/B 用邀请码注册并登录（前端或 API）。  
2. A 钱庄充值 ≥200。  
3. A 发令（结构化令状 + 清单）→ `PENDING_REVIEW`，冻结赏银。  
4. C/Admin 通过发令 → `OPEN`，广场可见。  
5. B 揭榜 → 进入会话发消息 → 按清单提交成果。  
6. C/Admin 验功通过。  
7. A 结算预览确认 10% 服务费 → 分配分完 → `COMPLETED`。  
8. A/B 互评；A 查看成长等级。  
9. Admin：用户列表、流水、强制关闭另测一单。  

**通过标准**：步骤 1–8 无阻断；资金账平；关键状态机正确。

---

## 6. 回归清单（发布前）

### P0 必测

- [ ] 邀请注册 + 双方式登录  
- [ ] Token 类型隔离（侠士/管理员）  
- [ ] 充值幂等 + 发令冻结 + 结算到账  
- [ ] 赏银下限 / 令状必填  
- [ ] **租房令状详情中文 label（F-BN-17/18）**  
- [ ] 发令审 + 成果审（执事或管理员）  
- [ ] 揭榜：自揭拒绝、重复拒绝  
- [ ] 结算未分完拒绝、分完成功  
- [ ] 后台登录、封禁、调账、邀请码  

### P1

- [ ] 日揭榜上限 / 体力 / 提交冷却  
- [ ] 超时 Job 退款（造截止数据或调时间）  
- [ ] 回避规则  
- [ ] 告示前后台一致  
- [ ] 成长兑体力  
- [ ] 英雄谱三榜 + 盟主申请  
- [ ] 职司自助申请 → 执事堂  
- [ ] 纠纷 7 日链路  
- [ ] 站内消息已读  
- [ ] 后台令状/清单配置  

### 兼容

- [ ] Chrome / Edge 最新  
- [ ] 移动端宽度 375 / 768 主路径可操作（含令状 descriptions）  

---

## 7. 缺陷报告

### 模板

```text
标题：[{模块}] 一句话问题
严重级别：S1/S2/S3/S4
归属层：需求 / 接口契约 / 后端实现 / 前端实现
环境：…
复现步骤：
1.
2.
期望：
实际：
附件：请求/响应、截图
```

### 已发现缺陷

| ID | 标题 | 级别 | 归属 | 说明 |
|----|------|------|------|------|
| D-001 | 未实现接口返回 `50000` 而非 `40400` | S3 | 后端 | 历史上缺口路径曾出现；现 Controller 已补，抽检是否仍有漏网 |
| D-002 | （历史）英雄谱/职司/纠纷/消息后端缺失 | S2 | 后端 | **Controller 已补** → 改走 §3.8 验收；本条关闭前须 E2E Pass |
| D-003 | 后台 RBAC 固定 `permissions:["*"]` | S3/P2 | 后端 | **关闭**：四角色种子 + 权限码；超管 `*` 合契约 |
| D-004 | `backend/README.md` 范围说明过时 | S4 | 文档 | 核对是否已同步 |
| D-005 | 悬赏详情「租房令状」展示英文 key | S3 | 前端 | **关闭**（AC-W* Pass） |
| D-006 | AC-S4 职司回避未测 | P1 | qa | **关闭**：补测 Pass（本人审本人令 `40310`） |

### 修复后最小验收（D-005）— 已执行 Pass

1. Meta `extra.label=补充说明` — Pass  
2. 详情/发令用 Meta 中文 label — Pass  
3. 空 extra 隐藏 — Pass  

---

## 8. 单元测试现状

| 用例类 | 覆盖 | 结果 |
|--------|------|------|
| `WalletServiceTest` | 充值、bizNo 幂等、余额不足冻结、冻结划转 | Pass |
| `BountyClaimRulesTest` | 本人不可揭榜、同令重复揭榜 | Pass |

**建议补测（交 @backend）**：结算分完/`42003`、日限/体力、审核回避、`JwtAuthFilter` 公私路径与 Token 类型隔离。

---

## 9. MVP 验收标准对照（requirements §9 · v1.6.3）

> 产品签收口径见 `requirements.md` §9.1（A～F）。下表为执行态摘要；勾选以 §11 为准。

| 分组 | 验收项 | 状态 |
|------|--------|------|
| A 主闭环 | 邀请注册/登录/充值幂等/发令待审/双审结算/互评成长 | **Pass**（§11.1） |
| B 令状展示 | Meta 中文 label；`extra`=**补充说明**；禁裸英文 key | **Pass**（§11.2） |
| C 告示清单 | 告示栏；后台配告示/清单/令状/赏银档 | **Pass** |
| D 扩展冒烟 | 英雄谱/职司/盟主/纠纷/消息；职司回避 | **Pass**（含 AC-S4、纠纷裁决） |
| E 后台安全 | 主干后台；侠士↔Admin 隔离；RBAC 四角色 | **Pass**（D-003 关闭） |
| F 契约 | api ↔ 响应 ↔ 前端类型；令状只认 Meta key | **Pass** |

---

## 10. 下一步

1. ~~勾选 §11 / 补测 AC-S4 / 回归 D-003~~ → **已完成**（2026-08-05）。  
2. 产品在 `requirements.md` §9.3 填写同意放行。  
3. 可选：`@frontend` 抽检后台邀请页对 `data.codes[]` 的展示（P3）。  

---

## 11. 本批可勾选验收清单（补）

> 用途：发布/修复签收。对照 `requirements.md` v1.6.3 §9.1 + `api.md` v1.0.1 §0/§5.2。  
> **实测批次**：2026-08-05 · 主闭环 `run_e2e.ps1` + 补测 `run_followup_v2.ps1`（admin/admin123）  
> 原始结果：`e2e_results.json` / `followup_v2_results.json`  

### 11.1 主闭环

- [x] AC-01 邀请码注册成功；无码失败 — 无码 `44001`；A/B 注册 `code=0`（userId=3/4）
- [x] AC-02 密码/短信登录成功 — `PASSWORD` 登录 `code=0`
- [x] AC-03 充值成功；同 `clientRequestId` 幂等 — 两次充值后余额仍 `1000.00`
- [x] AC-04 求租令合法发令 → `PENDING_REVIEW` + 赏银冻结 — bountyId=2；余额 800 / 冻结 200
- [x] AC-05 赏银 &lt;200 → `43001`；缺必填令状字段 → `43002` — 实测 `43001`/`43002`
- [x] AC-06 审核通过 → `OPEN`；广场可见 — Admin `APPROVE` 后 status=`OPEN`
- [x] AC-07 第二侠士揭榜成功；本人/重复揭榜失败 — 自揭 `43005`；揭榜 `0`；重复 `40900`
- [x] AC-08 会话可收发；成果按清单提交 — messages/submissions `code=0`；submissionId=1
- [x] AC-09 验功通过 → 结算预览 10% → 分完 → `COMPLETED` — fee=`20.00` dist=`180.00`；B 余额 `180.00`；status=`COMPLETED`
- [x] AC-10 互评成功；成长等级可查 — 双向评价 `0`；`GET /growth/level` `code=0`

### 11.2 租房令状展示（针对 D-005 / 用户反馈）

- [x] AC-W1 `GET /meta/warrant-templates` 求租含约定 key，均有中文 `label`；**`extra.label=补充说明`** — 见 `warrant-templates.raw.json`
- [x] AC-W2 详情映射中文标签 + 空 `extra` 不展示 — Meta label 齐全；前端 `warrantRows`+`isWarrantValueEmpty`；数据侧 `extra=""`  
  > 浏览器肉眼抽检建议保留：打开 `/bounties/2` 确认无英文 key
- [x] AC-W3 页面上**不出现**裸英文 key 作标签 — 已移除 `:label="String(key)"`，改用 `row.label`
- [x] AC-W4 发令页同套字段中文与详情一致（同源 Meta） — `PublishBountyView` `:label="field.label"`
- [x] AC-W5 契约自检：无 forbidden key /「令外叮嘱」 — Meta 无 `remark/note/...`；extra=补充说明

### 11.3 安全与治理

- [x] AC-S1 未登录敏感接口 → `40100`
- [x] AC-S2 侠士 Token 调 `/admin/**` → `40300`；Admin Token 调侠士接口 → `40300`
- [x] AC-S3 无职司调 `/hall/**` → `40310`
- [x] AC-S4 职司回避（审自己的令）失败 — **补测 Pass**（2026-08-05 16:01）：持 `DECREE_REVIEWER` 审本人发令 → `40310`「不可审核本人发布的令」；验功侧审本人揭榜令同拒；他职可审过（见 `followup_v2_results.json`）

### 11.4 扩展模块（Controller 已有 · 本批建议至少冒烟）

- [x] AC-X1 `/ranks` 三榜可打开无 500 — `GET /ranks/reputation` `code=0`
- [x] AC-X2 `/offices` 可看 defs — `GET /offices/defs` `code=0`
- [x] AC-X3 结算后可发起纠纷；管理员裁决 — 发起 + `POST .../verdict` `action=KEEP` 均 `code=0`（补测）
- [x] AC-X4 `/messages` 列表可打开 — `code=0`
- [x] AC-X5 `/admin/warrant-field-configs`、checklist 可读 — 两者 `code=0`（写保存未压测）

### 11.5 签收

| 项 | 填写 |
|----|------|
| 测试人 | QA Agent（@qa） |
| 环境（前后端版本/commit） | 本地 backend `localhost:8080`；联调管理员 `admin` / `admin123`；git 工作区有未提交改动 |
| 对照需求 | `requirements.md` **v1.6.3** §9.1 |
| 结论 | **Pass** |
| 遗留缺陷 ID | 无阻断项（D-003/D-006 已关闭） |
| 日期 | 2026-08-05（主闭环 15:40 + 补测 16:01） |
| 放行依据 | §9.3：A+B+C+D+E+F 均已覆盖；AC-S4/纠纷裁决/RBAC 种子角色补测通过 |

#### 相对产品 §9.1 分组结论

| §9.1 | 结论 | 说明 |
|------|------|------|
| A 主闭环 | **Pass** | AC-01～10；结算 fee 20 / 池 180 |
| B 令状展示 | **Pass** | Meta `extra=补充说明`；前端中文 label |
| C 告示/清单/模拟标识 | **Pass** | 告示 API + 模拟银两文案 |
| D 扩展冒烟 | **Pass** | X1～X5 + **AC-S4** + 纠纷 `KEEP` 裁决 Pass |
| E 后台安全 | **Pass** | S1～S3；RBAC 四角色种子齐；超管 `*` 符合契约 |
| F 契约 | **Pass** | 令状 Meta；邀请生成响应字段见备注 |

#### 缺陷卡（本批）

##### D-003（关闭）

- 原问题：permissions 固定 `["*"]`
- 复测：`GET /admin/roles` 含 `SUPER_ADMIN` / `OPS_ADMIN`(35 权) / `ARBITER` / `OBSERVER`(19 权)；OPS/OBSERVER **不含** `*`；`admin/me` 为 SUPER → `permissions:["*"]` + `roles:[{code:SUPER_ADMIN}]`（api.md §16.0 约定）
- 结论：**已修复 / 关闭**

##### D-006（关闭）

- 原问题：AC-S4 未测
- 复测：SQL 授职后，本人审本人令 `40310`；非当事人令审/验功成功；见 `docs/_qa_run/followup_v2_results.json`
- 结论：**补测 Pass / 关闭**

##### D-005（关闭）

- 令状英文 key 展示 — 前期回归已 Pass

##### 备注（非阻断）

- Admin 生成邀请响应为 `data.codes[]`（非 `code`）；联调脚本须取 `codes[0]`。前端 `adminCreateInvites` 类型标为 `null`，若 UI 未刷新列表可能导致「生成后看不见码」——建议 `@frontend` 抽检后台邀请页（P3）。

---

## 12. 原始证据索引

| 文件 | 内容 |
|------|------|
| `docs/_qa_run/e2e_summary.txt` | 主闭环 PASS/FAIL |
| `docs/_qa_run/e2e_results.json` | 主闭环明细 |
| `docs/_qa_run/warrant-templates.raw.json` | Meta 令状 UTF-8 |
| `docs/_qa_run/followup_v2_summary.txt` | 补测一览（全 PASS） |
| `docs/_qa_run/followup_v2_results.json` | AC-S4 / D-003 / 纠纷裁决 |
| `docs/_qa_run/admin_roles.json` | 四角色权限种子 |
| `docs/_qa_run/run_e2e.ps1` / `run_followup_v2.ps1` | 可复现脚本 |
| `docs/_qa_run/v17_summary.txt` / `v17_results.json` | **§9.5 v1.7** 增量验收 |
| `docs/_qa_run/run_v17.ps1` | v1.7 可复现脚本 |
| `docs/_qa_run/v18_summary.txt` / `v18_results.json` | **§9.6 / §9.7** 再发一令 + 执事堂 |
| `docs/_qa_run/run_v18.ps1` | v1.8 / v1.8.1 可复现脚本 |

---

## 13. requirements §9.5（v1.7）增量验收

> 对照 `requirements.md` §9.5 / api.md v1.0.3。实测 2026-08-05。管理员 `admin`/`admin123`。

| ID | 验收项 | 结果 | 实测要点 |
|----|--------|------|----------|
| V17-01 | 注册赠银 500 + `REGISTER_GRANT` 流水 | **Pass** | 新用户余额 `500.00`；流水含 `REGISTER_GRANT` |
| V17-02 | 邀请人 +100（每被邀请人一次） | **Pass** | 好友注册后邀请人 `600`；第二人后 `700`；流水 `INVITE_REWARD`×2 |
| V17-03 | C 端不可见/不可用充值提现 | **Pass** | 账户 flags=false；`POST` 充/提 → `42004`；前端 `v-if="showOps"` |
| V17-04 | 能力未删除、可配置再开 | **Pass** | 接口保留返 `42004`；`GET /meta/wallet-features` 含开关与 500/100 |
| V17-05 | 管理员仍可调账/发放 | **Pass** | `assets/adjust` 成功，余额增加 |
| V17-06 | 未读角标 / 未读样式 / 已读 | **Pass** | `unread-count`≥1 → 标已读后减少；HeroLayout 角标 + MessagesView 未读样式 |

**§9.5 结论：Pass**（无阻断缺陷）  
已同步勾选 `requirements.md` §9.5 判定列。

---

## 14. requirements §9.6 / §9.7（v1.8 / v1.8.1）验收

> 对照 `requirements.md` §9.6（再发一令）+ §9.7（执事堂）+ 主闭环回归。实测 2026-08-05。脚本 `run_v18.ps1`。

### 14.1 §9.6 再发一令

| ID | 验收项 | 结果 | 实测要点 |
|----|--------|------|----------|
| V18-01 | 终态可见「再发一令」 | **Pass** | 驳回后 `canRepublish=true`；完结后亦可；FE 详情/列表入口 |
| V18-02 | 新 ID；原单不变 | **Pass** | `7`→`8`；原单仍 `REJECTED` |
| V18-03 | 重新冻结进待审 | **Pass** | 新单 `PENDING_REVIEW`；冻结 `200`；余额 `300` |
| V18-05 | `sourceBountyId` | **Pass** | 详情/列表均为 `7` |
| V18-06 | 非终态/非令主拒绝 | **Pass** | `43007` |
| V18-DRAFT | 预填草稿 | **Pass** | `GET .../republish-draft` 含标题/赏银/`sourceBountyId`；`deadlineAt=null` |
| REGRESS | 主闭环回归 | **Pass** | 审→揭榜→成果→结算 `COMPLETED`；fee=`20`；dist=`180` |

**备注**：再发提交须带未来 `deadlineAt`（draft 刻意清空）。

### 14.2 §9.7 执事堂体验

| ID | 验收项 | 结果 | 实测要点 |
|----|--------|------|----------|
| V181-01 | 武侠视觉非 Admin 皮 | **Pass** | hall 视图 + `jh-*` / `HallBackBar` |
| V181-02 | 列表形态对齐「我的悬赏」 | **Pass** | 静态对齐 |
| V181-03 | 可进详情 | **Pass** | 列表→详情路由 |
| V181-04 | 返回上一页 | **Pass** | `HallBackBar` |
| V181-05 | 通过/驳回；驳回须原因 | **Pass** | 原因必填 |

**§9.6 / §9.7 结论：Pass**（无阻断缺陷）  
已同步勾选 `requirements.md` §9.6 / §9.7 判定列与 P0 清单（再发一令、执事堂）。产品放行栏待 `@pm`。

---

## 15. requirements §9.8（v1.8.3）视觉 + 主路径回归（合并前）

> 对照 `requirements.md` §9.8 / §6.20。分支 `trial/wuxia-notice-board-ui`。实测 2026-08-05。

| ID | 验收项 | 结果 |
|----|--------|------|
| V183-01～07 | 告示板壳 / 首页·广场拆分 / 纸贴 / 页眉英雄榜 / 防骗箴言 / 文案芯片 / 执事堂返回 | **Pass**（静态代码+结构核对） |
| 主路径 | 发令→审→揭榜→成果→结算 | **Pass**（`run_e2e`：COMPLETED；fee=20；dist=180） |
| 广场 API | `GET /bounties?status=OPEN,IN_COLLAB` | **Pass** |

**说明**：旧 `run_e2e` 的 AC-03（充值）因 v1.7 默认关返回 `42004`，不计入本批失败；AC-S4 脚本仍 deferred。

**结论：Pass** → 合并 `main`。证据 `docs/_qa_run/v183_visual_summary.txt`。

---

## 16. 全站搜索 / 筛选功能验收

> 范围：页面上**实际提供**搜索或筛选控件的列表。实测 2026-08-05。脚本 `docs/_qa_run/run_filter_qa.ps1`。

### 16.1 有筛选 UI 的页面

| 页面 | 筛选项 | 结果 | 说明 |
|------|--------|------|------|
| 悬赏榜 `/plaza` | type / district / keyword | **Pass** | 与 api `GET /bounties` 一致；无匹配 keyword→0 |
| 告示栏 `/notices` | category | **Pass** | 分类互斥正确 |
| 英雄榜 `/ranks` | type 三榜 | **Pass** | |
| 站内消息 `/messages` | 仅未读 | **Pass** | `unreadOnly=true` |
| 我的悬赏 `/mine` | 发布 / 揭榜 Tab | **Pass** | |
| Admin 侠士 | keyword | **Pass** | 用户名/手机/昵称均可（D-FILTER-001 已关闭） |
| Admin 悬赏 | status | **Pass** | |
| Admin 令状配置 | templateCode | **Pass** | |

### 16.2 无筛选 UI（本批 N/A）

首页、钱庄流水、邀请、成长、纠纷、职司、资料；执事堂队列（固定待审）；Admin 邀请/纠纷/告示/奖品/盟主/职司/清单/运营/系统/流水等。

### 16.3 缺陷

**D-FILTER-001**（P2）Admin「侠士管理」查询 — **已关闭**（2026-08-05 复测）

| 项 | 内容 |
|----|------|
| 修复 | `@backend` keyword 含 `user_profile.nickname`；`api.md` §16.3；`@frontend` placeholder「用户名/手机号/昵称」 |
| 复测 | `KW-USERNAME/PHONE/NICKNAME` 均 Pass（`docs/_qa_run/dfilter_retest.txt`） |

**本批结论：有筛选页主功能 Pass；D-FILTER-001 已关闭**

---

## 17. requirements §9.11 子页返回抽查（v1.8.6）

> 对照 §6.22 / §9.11。2026-08-06 静态抽查。证据 `docs/_qa_run/nav_back_spotcheck.txt`。

| ID | 验收项 | 结果 |
|----|--------|------|
| NAV-01 | chat / submit / settle「返回悬赏详情」→ `/bounties/:id` | **Pass** |
| NAV-02 | 告示详情 / 发令 / 条款 / 纠纷详情有返回 | **Pass** |
| NAV-03 | 一级页未误加返回条 | **Pass** |
| NAV-04 | 执事堂令审/验功详情 → 回队列 | **Pass** |
| NAV-05 | Admin 纠纷详情弹层可关闭 | **Pass** |

**结论：抽查项 Pass**（无阻断缺陷；已回填 `requirements.md` §9.11）

---

## 18. requirements §9.13（v1.8.9）页眉 / 会话 / 令种 / 告示

> 对照 §6.24–§6.27 / §9.13。实测 2026-08-07。证据 `docs/_qa_run/v189_summary.txt`。

| ID | 验收项 | 结果 |
|----|--------|------|
| V189-01 | 金榜页眉无 subtitle；分割线统一固定长 | **Fail**（11 处仍传 subtitle；底边随容器宽） |
| V189-02 | 令主↔揭榜侠会话互见 | **Pass**（实现对齐共享流；建议补双账号烟测） |
| V189-03 | 令种名：租房/出租/转租悬赏 | **Pass**（筛/发令/标签） |
| V189-04 | 告示 N1–N3 正文完整；防骗行非空 | **Fail**（库内短摘要；防骗行非空） |

### 缺陷

| ID | 级别 | 标题 | 归属 |
|----|------|------|------|
| D-V189-01 | P1 | 多页 `JhPageHeader` 仍展示 subtitle | `@frontend` |
| D-V189-02 | P2 | 页眉底部分割线随容器宽度变化 | `@frontend` |
| D-V189-03 | P1 | N1–N3 未按 `docs/notices/standard-notices.md` 入库全文 | `@backend`（或后台发布） |

**§9.13 结论：Fail**（阻断 D-V189-01 / D-V189-03）

```text
@frontend
D-V189-01/02：去掉所有 JhPageHeader 的 subtitle 传参（说明改普通段落）；分割线改为固定长度（勿随容器宽）。
验收：各金榜页无副题；悬赏榜/钱庄/告示栏等分割线视觉等长。

@backend
D-V189-03：将 standard-notices.md 的 N1–N3（建议含 N4–N6）写入 notice 表 content（patch 或后台发布）。
验收：告示详情可见「一、张贴悬赏须按令状…」等完整条款；广场防骗行仍非空。
```

---

## 19. requirements §9.14（v1.8.10）全生命周期 / api §7.9

> 对照 §6.28 / §9.14 / `api.md` v1.0.7 §7.9。实测 2026-08-07。证据 `docs/_qa_run/v1810_summary.txt`、`v1810_results.json`。

| ID | 验收项 | 结果 |
|----|--------|------|
| V1810-01 | 令主 IN_COLLAB/PENDING_SETTLE：会话/成果/完结/取消入口 | **Fail**（无 `capabilities` → 按钮全隐；FE 接线静态 Pass） |
| V1810-02 | 揭榜侠互发可见；可提交；可退出；退出后禁发禁交 | **部分**（互发 Pass；提交/退出未闭环） |
| V1810-03 | CANCELLED/COMPLETED/REJECTED → messages `43008`、submissions `43009`；历史只读 | **Fail**（取消/驳回后 messages=`0`） |
| V1810-04 | 无成果取消全额退；有成果取消进分配；之后禁写 | **Fail**（有成果仍全额退+CANCELLED） |
| V1810-05 | PENDING_SETTLE 令主可取消 | **Fail**（未稳定验完） |
| V1810-06 | 写接口非法状态业务错误码（非仅藏按钮） | **Fail** |
| V1810-07 | capabilities 三方比对（api ↔ 响应 ↔ FE types） | **Fail**（响应缺字段） |

### 缺陷

| ID | 级别 | 标题 | 归属 |
|----|------|------|------|
| D-V1810-01 | P0 | 有成果取消未进分配，仍 `CANCELLED`+全额退 | `@backend`（`SettleService.cancel`） |
| D-V1810-02 | P0 | 详情缺 `capabilities`；终态 messages 未稳定 `43008` | `@backend`（部署/拦截复测） |
| D-V1810-03 | P1 | `capabilities` 缺失时详情入口全隐 | `@frontend`（依赖 02；可选提示） |
| D-V1810-04 | P2 | 空/无效 checklist 提交成果返回 `50000` | `@backend` |

**§9.14 结论：Fail**（阻断 D-V1810-01 / D-V1810-02）

```text
@backend
请修 D-V1810-01/02/04（对照 docs/api.md v1.0.7 §7.9 与 requirements §6.23/§6.28/§9.14）：
1) SettleService.cancel：无成果 → CANCELLED+全额退；有成果 → 进分配（托管不退光），之后禁聊禁交（43008/43009）
2) GET /bounties/{id} 必返 capabilities（9 键）；POST messages/submissions 非法状态硬拦 43008/43009
3) 提交成果参数/清单非法时返回业务错误，勿 50000
验收：重跑 docs/_qa_run/run_v1810_lifecycle.ps1，V1810-01~07 全 Pass。

@frontend
D-V1810-03：确认详情按钮仅认 capabilities；后端修复后抽查令主四入口与揭榜退出/禁发。
（可选）capabilities 缺失时提示「能力状态加载失败」而非静默藏光所有入口。
```
