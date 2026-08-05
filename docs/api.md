# 接口文档（API）

> 由 **架构师 AI（@architect）** 定义；**后端 / 前端** 严格对齐本文件。  
> 对齐：`docs/requirements.md`（v1.6）P0、`docs/architecture.md`（v1.0）。

**版本**：v1.0  
**Base URL**：`/api/v1`  
**鉴权**：`Authorization: Bearer <accessToken>`（标注「公开」的除外）  
**最后更新**：2026-08-05

---

## 1. 统一约定

### 1.1 响应结构

```json
{
  "code": 0,
  "message": "ok",
  "data": {}
}
```

分页列表 `data`：

```json
{
  "list": [],
  "total": 0,
  "page": 1,
  "pageSize": 20
}
```

### 1.2 通用请求头

| Header | 说明 |
|--------|------|
| `Authorization` | `Bearer {token}` |
| `Content-Type` | `application/json`（上传除外） |
| `X-Request-Id` | 可选，链路追踪 |

### 1.3 错误码

| code | 含义 |
|------|------|
| 0 | 成功 |
| 40001 | 参数错误 / 校验失败 |
| 40002 | 业务规则不满足（状态不允许等） |
| 40100 | 未登录 / Token 无效 |
| 40101 | Token 过期 |
| 40300 | 无权限 |
| 40301 | 账号封禁 |
| 40310 | 职司无权 / 未授予 / 已暂停 |
| 40400 | 资源不存在 |
| 40900 | 冲突（重复揭榜、幂等冲突等） |
| 42900 | 限流（揭榜日限、提交冷却等） |
| 50000 | 服务内部错误 |

**钱包专项**

| code | 含义 |
|------|------|
| 42001 | 余额不足 |
| 42002 | 冻结/解冻失败 |
| 42003 | 结算分配未分完或不合法 |

**悬赏专项**

| code | 含义 |
|------|------|
| 43001 | 赏银低于最低限制 |
| 43002 | 令状字段不完整 |
| 43003 | 体力不足 |
| 43004 | 超出每日揭榜上限 |
| 43005 | 不可揭榜（本人/状态/已揭过） |
| 43006 | 成果提交被限流或内容非法 |

**邀请专项**

| code | 含义 |
|------|------|
| 44001 | 邀请码无效/已用尽/过期 |

金额单位：模拟银两（整数或两位小数，实现统一为分/厘整数存储亦可，API 对外用「两」数值）。  
时间：ISO-8601，时区 `Asia/Shanghai`。

### 1.4 角色与前缀

| 前缀 | 访问者 |
|------|--------|
| `/auth/**` | 公开或登录 |
| `/user/**`、`/wallet/**`、`/bounties/**` 等 | 侠士 L2 |
| `/hall/**` | 职司 L1 |
| `/admin/**` | 管理员 L0 |

---

## 2. 鉴权与邀请（P0）

### 2.1 发送短信验证码（公开，Mock）

- **POST** `/auth/sms/send`
- **Body**

```json
{
  "phone": "18800001111",
  "scene": "REGISTER | LOGIN"
}
```

- **Response data**：`{ "expireIn": 300 }`（Mock 环境可在日志打印验证码）

### 2.2 邀请码预校验（公开）

- **POST** `/auth/invite/validate`
- **Body**：`{ "inviteCode": "AB12CD" }`
- **Response data**：`{ "valid": true, "inviterNickname": "某侠" }`

### 2.3 注册（公开，须邀请）

- **POST** `/auth/register`
- **Body**（二选一：验证码 或 密码）

```json
{
  "inviteCode": "AB12CD",
  "phone": "18800001111",
  "smsCode": "123456",
  "username": "hero01",
  "password": "optional_if_sms",
  "nickname": "遵义某侠"
}
```

- **Response data**：同登录（含 token + 用户摘要）
- **错误**：`44001` 邀请无效

### 2.4 登录（公开）

- **POST** `/auth/login`
- **Body**

```json
{
  "loginType": "PASSWORD | SMS",
  "username": "hero01",
  "password": "******",
  "phone": "18800001111",
  "smsCode": "123456"
}
```

- **Response data**

```json
{
  "token": "eyJ...",
  "expiresIn": 7200,
  "user": {
    "id": 1,
    "nickname": "遵义某侠",
    "avatarUrl": "",
    "level": 1,
    "levelTitle": "初入江湖",
    "offices": []
  }
}
```

### 2.5 登出

- **POST** `/auth/logout`
- **Response data**：`null`

### 2.6 当前用户

- **GET** `/auth/me`
- **Response data**：用户资料 + 资产摘要（余额、冻结、侠义、体力、今日揭榜次数/上限、声望、完成单、好评率、是否现任盟主、职司列表）

### 2.7 生成邀请码

- **POST** `/user/invites`
- **Response data**：`{ "code": "XY99", "link": "https://.../r/XY99", "remainQuotaToday": 2 }`
- **说明**：受每日额度配置限制

### 2.8 我的邀请记录

- **GET** `/user/invites?page=1&pageSize=20`

---

## 3. 用户资料与实名（P0）

### 3.1 更新资料

- **PUT** `/user/profile`
- **Body**：`{ "nickname", "avatarUrl", "bio" }`

### 3.2 提交实名（占位，非硬门槛）

- **POST** `/user/real-name`
- **Body**：`{ "realName", "idNumber" }`
- **Response data**：`{ "status": "PENDING | VERIFIED | REJECTED" }`

### 3.3 上传文件

- **POST** `/files/upload`（`multipart/form-data`，字段 `file`）
- **Response data**：`{ "url": "/files/xxx", "fileId": "..." }`

---

## 4. 模拟钱庄（P0）

### 4.1 账户概览

- **GET** `/wallet/account`
- **Response data**：`{ "balance": 1000, "frozen": 200, "currency": "两", "simulated": true }`

### 4.2 模拟充值

- **POST** `/wallet/recharge`
- **Body**：`{ "amount": 500, "clientRequestId": "idem-001" }`
- **Response data**：账户 + 流水号

### 4.3 模拟提现

- **POST** `/wallet/withdraw`
- **Body**：`{ "amount": 100, "clientRequestId": "idem-002" }`
- **错误**：`42001` 余额不足

### 4.4 流水分页

- **GET** `/wallet/ledgers?page=1&pageSize=20&type=`

---

## 5. 配置读取（C 端，P0）

### 5.1 赏银建议档位

- **GET** `/meta/reward-suggest`
- **Response data**

```json
{
  "minReward": 200,
  "difficulties": [
    { "code": "EASY", "name": "简易", "suggestMin": 200, "suggestMax": 300 },
    { "code": "NORMAL", "name": "普通", "suggestMin": 300, "suggestMax": 500 },
    { "code": "HARD", "name": "艰辛", "suggestMin": 500, "suggestMax": 800 },
    { "code": "EXTREME", "name": "超难", "suggestMin": 800, "suggestMax": 1500 }
  ]
}
```

### 5.2 令状模板元数据

- **GET** `/meta/warrant-templates`
- **Response data**：求租 / 出租字段定义（必填、类型、选项）

### 5.3 探子清单模板（按标签预勾）

- **GET** `/meta/checklist-templates?tags=帮带看,帮验房`

### 5.4 成长相关公开配置

- **GET** `/meta/growth-config`  
  日揭榜上限、每日免费体力、单次耗体、侠义兑体力汇率、等级表摘要等

---

## 6. 告示栏（P0）

### 6.1 告示列表（公开/登录均可）

- **GET** `/notices?category=&page=1&pageSize=20`
- **category**：`RULES | ANTI_FRAUD | ZUNYI_RENT | ANNOUNCE`

### 6.2 告示详情

- **GET** `/notices/{id}`

### 6.3 置顶摘要（发令/揭榜路径露出）

- **GET** `/notices/top?category=ANTI_FRAUD&limit=3`

---

## 7. 悬赏令（P0）

### 7.1 广场列表

- **GET** `/bounties?type=&district=&status=OPEN,IN_COLLAB&page=1&pageSize=20&keyword=`
- **说明**：仅遵义试点；默认不展示 `PENDING_REVIEW`

### 7.2 详情

- **GET** `/bounties/{id}`
- **Response data**：主信息 + 令状字段 + 探子清单 + 揭榜人数 + 本人是否已揭榜 + 截止时间等  
  （出租令「精确位置」等字段按配置对未揭榜人脱敏）

### 7.3 创建悬赏（发令）

- **POST** `/bounties`
- **Body**

```json
{
  "type": "RENT_SEEK | RENT_OUT",
  "title": "红花岗两室求租核验",
  "difficulty": "NORMAL",
  "rewardAmount": 350,
  "confirmLowReward": false,
  "deadlineAt": "2026-08-20T23:59:59+08:00",
  "taskTags": ["帮寻房", "帮带看"],
  "warrantFields": {
    "district": "红花岗",
    "rentBudgetMin": 800,
    "rentBudgetMax": 1200,
    "layout": "两室一厅",
    "expectMoveInDate": "2026-09-01",
    "acceptAgency": false,
    "extra": ""
  },
  "checklistItemCodes": ["VERIFY_AUTHENTIC", "SITE_VISIT_RECORD"]
}
```

- **规则**：`rewardAmount < minReward` → `43001`；低于建议下限且未 `confirmLowReward` → `40002`；余额不足 → `42001`；字段缺 → `43002`
- **成功**：创建后状态 `PENDING_REVIEW`，赏银已冻结

### 7.4 我发布的 / 我揭榜的

- **GET** `/bounties/mine/published?status=&page=&pageSize=`
- **GET** `/bounties/mine/claimed?status=&page=&pageSize=`

### 7.5 一键揭榜

- **POST** `/bounties/{id}/claims`
- **Body**：`{}`
- **错误**：`43003` 体力不足；`43004` 日上限；`43005` 不可揭榜；`40900` 已揭过

### 7.6 会话消息列表

- **GET** `/bounties/{id}/messages?page=1&pageSize=50`
- **权限**：令主或已揭榜侠士

### 7.7 发送会话消息

- **POST** `/bounties/{id}/messages`
- **Body**：`{ "content": "今晚可带看" }`

---

## 8. 成果提交（P0）

### 8.1 提交成果（可多次）

- **POST** `/bounties/{id}/submissions`
- **Body**

```json
{
  "summary": "已完成两套房带看",
  "items": [
    {
      "itemCode": "SITE_VISIT_RECORD",
      "done": true,
      "text": "2026-08-06 15:00 某某小区，接待方中介王某",
      "mediaUrls": ["https://.../1.jpg"]
    }
  ]
}
```

- **错误**：`43006` 冷却/日限/空内容/必验项未覆盖

### 8.2 某揭榜关系的成果版本列表

- **GET** `/bounties/{id}/claims/{claimId}/submissions`

### 8.3 成果详情

- **GET** `/submissions/{submissionId}`

---

## 9. 结算与评价（P0）

### 9.1 预览可分配池

- **GET** `/bounties/{id}/settlement/preview`
- **Response data**：`{ "rewardB": 350, "feeRate": 0.1, "fee": 35, "distributable": 315, "claimants": [ { "userId", "nickname", "approvedSubmissionCount" } ] }`
- **权限**：令主

### 9.2 提交结算（全额分完）

- **POST** `/bounties/{id}/settlement`
- **Body**

```json
{
  "items": [
    { "userId": 2, "amount": 200, "chivalryBonus": 10 },
    { "userId": 3, "amount": 115, "chivalryBonus": 0 }
  ]
}
```

- **规则**：金额之和必须等于可分配池；允许 0 两；自动到账  
- **前置**：至少 1 名揭榜人且 ≥1 条审核通过成果（否则仅可走取消）  
- **错误**：`42003`

### 9.3 令主主动取消（未结算且规则允许时）

- **POST** `/bounties/{id}/cancel`
- **Body**：`{ "reason": "已租到" }`  
- **说明**：托管全额退回；与超时取消同类资金效果

### 9.4 提交互评

- **POST** `/bounties/{id}/evaluations`
- **Body**：`{ "toUserId": 2, "score": 5, "content": "靠谱" }`  
- **说明**：结算完成后开放；更新好评率与声望

### 9.5 评价列表

- **GET** `/bounties/{id}/evaluations`

---

## 10. 成长：体力 / 侠义 / 兑换（P0）

### 10.1 侠义值兑体力

- **POST** `/growth/stamina/exchange`
- **Body**：`{ "staminaPoints": 1 }`  
- **说明**：按配置汇率扣侠义值；不突破日揭榜上限

### 10.2 奖品列表

- **GET** `/growth/products?page=&pageSize=`

### 10.3 兑换奖品

- **POST** `/growth/products/{productId}/redeem`
- **Body**：`{ "quantity": 1 }`

### 10.4 我的兑换订单

- **GET** `/growth/redeem-orders?page=&pageSize=`

### 10.5 等级进度

- **GET** `/growth/level`
- **Response data**：当前头衔、累计侠义、下一级阈值与进度、是否盟主

---

## 11. 英雄谱与武林盟主（P0）

### 11.1 英雄谱

- **GET** `/ranks/{type}?page=1&pageSize=50`  
  `type` = `REPUTATION | CHIVALRY | COMPLETED`
- **Response data**：排名列表 + `lord` 荣耀位信息

### 11.2 我的排名

- **GET** `/ranks/me`

### 11.3 申请武林盟主

- **POST** `/lord/applications`
- **Body**：`{ "statement": "愿行侠仗义..." }`
- **规则**：默认声望榜第 1；驳回后冷却期内不可重复

### 11.4 我的盟主申请状态

- **GET** `/lord/applications/mine`

---

## 12. 职司申请（侠士端，P0）

### 12.1 可申请职司列表

- **GET** `/offices/defs`

### 12.2 申请职司

- **POST** `/offices/applications`
- **Body**：`{ "officeCode": "DECREE_REVIEWER | FEAT_REVIEWER", "statement": "..." }`

### 12.3 我的职司与申请

- **GET** `/offices/mine`
- **GET** `/offices/applications/mine`

---

## 13. 纠纷（P0）

### 13.1 发起纠纷

- **POST** `/bounties/{id}/disputes`
- **Body**：`{ "reason": "...", "evidenceUrls": [], "evidenceText": "..." }`
- **规则**：结算成功起 7 日内

### 13.2 纠纷详情

- **GET** `/disputes/{id}`

### 13.3 我的纠纷

- **GET** `/disputes/mine?page=&pageSize=`

---

## 14. 站内消息（P0）

### 14.1 消息列表

- **GET** `/messages?page=&pageSize=&unreadOnly=`

### 14.2 标记已读

- **POST** `/messages/{id}/read`
- **POST** `/messages/read-all`

---

## 15. 执事堂（L1，P0）

> 需有效职司；回避规则服务端强制。

### 15.1 待审发令队列（令审使）

- **GET** `/hall/bounty-reviews?status=PENDING&page=&pageSize=`

### 15.2 审核发令

- **POST** `/hall/bounty-reviews/{bountyId}`
- **Body**：`{ "result": "APPROVE | REJECT", "reason": "赏银与难度不符" }`

### 15.3 待审成果队列（验功使）

- **GET** `/hall/submission-reviews?status=PENDING&page=&pageSize=`

### 15.4 审核成果

- **POST** `/hall/submission-reviews/{submissionId}`
- **Body**：`{ "result": "APPROVE | REJECT", "reason": "...", "itemComments": [] }`

### 15.5 本人操作记录

- **GET** `/hall/my-actions?page=&pageSize=`

---

## 16. 武林盟后台（L0，P0）

> 管理员 Token；菜单/按钮级 RBAC。下列为模块接口清单（请求体字段实现时按表结构补全，语义以需求为准）。

### 16.1 管理员登录

- **POST** `/admin/auth/login`  
  Body：`{ "username", "password" }` → token + 角色权限码列表
- **POST** `/admin/auth/logout`
- **GET** `/admin/auth/me`

### 16.2 工作台

- **GET** `/admin/dashboard/overview`  
  用户数、待审发令/成果、纠纷数、今日揭榜、托管汇总等

### 16.3 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/users` | 列表筛选 |
| GET | `/admin/users/{id}` | 详情（等级/声望/资产/邀请） |
| POST | `/admin/users/{id}/disable` | 禁用 |
| POST | `/admin/users/{id}/enable` | 启用 |
| POST | `/admin/users/{id}/ban` | 封禁 |
| POST | `/admin/users/{id}/unban` | 解封 |
| PUT | `/admin/users/{id}/remark` | 备注 |
| POST | `/admin/users/{id}/assets/adjust` | 手工调整侠义/体力/余额（审计） |
| GET | `/admin/users/{id}/login-logs` | 登录日志 |
| GET | `/admin/users/{id}/real-name` | 实名查看/状态维护 PUT |

资产调整 Body 示例：`{ "assetType": "BALANCE|CHIVALRY|STAMINA", "delta": 10, "reason": "..." }`

### 16.4 邀请管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/invites` | 邀请码/关系查询 |
| POST | `/admin/invites` | 批量生成 |
| POST | `/admin/invites/{id}/invalidate` | 失效 |

### 16.5 悬赏与双审核

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/bounties` | 列表 |
| GET | `/admin/bounties/{id}` | 详情含揭榜/提交 |
| POST | `/admin/bounties/{id}/force-close` | 强制关闭/下架 |
| POST | `/admin/bounty-reviews/{bountyId}` | 发令审核（可改判） |
| POST | `/admin/submission-reviews/{submissionId}` | 成果审核（可改判） |
| GET | `/admin/bounties/{id}/messages` | 会话抽检 |

### 16.6 钱庄与流水

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/wallet/ledgers` | 托管/结算/退款/提现流水 |
| GET | `/admin/wallet/fee-summary` | 服务费汇总 |
| POST | `/admin/wallet/ledgers/{id}/flag` | 异常标记 |

### 16.7 纠纷仲裁（终裁）

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/disputes` | 队列 |
| GET | `/admin/disputes/{id}` | 详情举证 |
| POST | `/admin/disputes/{id}/verdict` | 裁决执行 |

裁决 Body 示例：`{ "action": "KEEP|REALLOCATE|REFUND|PUNISH", "reallocations": [], "punishments": [], "comment": "..." }`

### 16.8 成长与运营配置

| 方法 | 路径 | 说明 |
|------|------|------|
| GET/PUT | `/admin/configs/levels` | 等级配置 |
| GET | `/admin/lord` | 现任盟主 |
| GET | `/admin/lord/applications` | 申请队列 |
| POST | `/admin/lord/applications/{id}/approve` | 任命 |
| POST | `/admin/lord/applications/{id}/reject` | 驳回 |
| POST | `/admin/lord/dismiss` | 罢免（原因+审计） |
| GET/PUT | `/admin/offices/defs` | 职司定义/名额/门槛 |
| GET | `/admin/offices/applications` | 职司申请 |
| POST | `/admin/offices/applications/{id}/approve` | 授予 |
| POST | `/admin/offices/applications/{id}/reject` | 驳回 |
| POST | `/admin/offices/holders/{id}/suspend` | 暂停 |
| POST | `/admin/offices/holders/{id}/revoke` | 撤销 |
| GET/PUT | `/admin/configs/ranks` | 英雄谱规则 |
| GET/PUT | `/admin/configs/growth` | 体力/日限/提交频控/汇率 |
| CRUD | `/admin/products`、`/admin/redeem-orders` | 奖品与兑换单 |

### 16.9 内容与风控

| 方法 | 路径 | 说明 |
|------|------|------|
| CRUD | `/admin/notices` | 告示发布/下架/置顶/分类 |
| CRUD | `/admin/checklist-templates` | 探子清单模板 |
| CRUD | `/admin/warrant-field-configs` | 令状字段配置 |
| GET/PUT | `/admin/configs/reward-suggest` | 赏银建议档位 |
| GET | `/admin/audit-logs` | 审计日志 |
| GET | `/admin/office-metrics` | 职司驳回率/改判率等 |
| GET | `/admin/reports` | 举报列表（若有）处理 POST |

### 16.10 系统管理

| 方法 | 路径 | 说明 |
|------|------|------|
| CRUD | `/admin/admins` | 后台账号 |
| CRUD | `/admin/roles` | 角色与权限 |
| CRUD | `/admin/menus` | 菜单 |
| GET/PUT | `/admin/dicts` | 字典枚举 |
| GET/PUT | `/admin/configs/system` | 系统参数（费率等） |
| GET | `/admin/jobs` | 定时任务状态查看 |
| GET | `/admin/login-logs` | 管理员登录日志 |

---

## 17. P0 需求 ↔ 接口映射

| 需求验收 / 功能 | 主要接口 |
|-----------------|----------|
| 邀请注册 + 双登录 | 2.1–2.4 |
| 邀请生成与追溯 | 2.7–2.8，16.4 |
| 资料与实名入口 | 3.1–3.2 |
| 模拟钱庄 | 4.x，16.6 |
| 结构化令状 + 赏银建议 ≥200 | 5.1–5.2，7.3 |
| 探子清单 | 5.3，7.3，8.x，16.9 |
| 告示栏 | 6.x，16.9 |
| 广场/详情/揭榜 | 7.1–7.5 |
| 体力与日限、兑体力 | 5.4，7.5，10.1 |
| 协作会话 | 7.6–7.7 |
| 成果多次提交防刷 | 8.x |
| 双审核（职司/管理） | 15.x，16.5 |
| 结算 10% + 分完 + 0 两 | 9.1–9.2 |
| 超时提醒与自动退款 | job（无直接 C 端写接口）；站内消息 14.x |
| 互评与声望 | 9.4–9.5，11.x |
| 等级进度 | 10.5，16.8 |
| 英雄谱 | 11.1–11.2 |
| 盟主申请/审批 | 11.3–11.4，16.8 |
| 职司申请/执事堂 | 12.x，15.x，16.8 |
| 纠纷 7 日 | 13.x，16.7 |
| 完整后台 | 16.x |

---

## 18. 变更记录

| 版本 | 日期 | 说明 |
|------|------|------|
| v0.1 | — | 模板初始化 |
| v1.0 | 2026-08-05 | 按 MVP P0 全量定义接口契约 |
