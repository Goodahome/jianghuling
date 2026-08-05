# 接口文档（API）

> 由 **架构师 AI（@architect）** 定义；**后端 / 前端** 严格对齐本文件。  
> 对齐：`docs/requirements.md`（**v1.8.0**）P0、`docs/architecture.md`（**v1.0.4**）。

**版本**：v1.0.5  
**Base URL**：`/api/v1`  
**鉴权**：`Authorization: Bearer <accessToken>`（标注「公开」的除外）  
**最后更新**：2026-08-05

---

## 0. 契约纪律（前后端必须遵守）

1. **`docs/api.md` 是唯一字段真相源（SSOT）**。后端 DTO、前端 `types`/`api` 层不得各自发明字段名。  
2. **先改契约，再改代码**：任何新增/改名/删字段/改枚举，必须先由 `@architect` 更新本文件（含变更记录），再 `@backend` / `@frontend` 同步。  
3. **命名**：JSON 一律 **camelCase**；枚举值与本文件示例字符串完全一致（含大小写）。  
4. **动态表单例外**：租房令状等「模板驱动」字段以 **`GET /meta/warrant-templates` 返回的 `key`/`label`** 为准；前端禁止写死另一套 key。  
   - 自由文本：**key=`extra`**，**label=`补充说明`**（选填）。只改 `label` 不改 `key`。  
   - **禁止**把展示名做成「令外叮嘱」；**禁止**再增加 `remark`/`note`/`description`/`otherRequirements` 等同义自由文本 key。  
5. **实现自检**：后端改接口后对照本文件勾选请求/响应字段；前端联调前对照本文件与真实响应做一次字段表比对。  
6. **对不上时**：先开缺陷给 `@architect` 裁定契约，禁止前端「兼容瞎猜」或后端「先上车后补票」长期分叉。

分流与接力口令见 `docs/handoff.md` §4。

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
| 42004 | 充值/提现暂未开放（开关关闭） |

**悬赏专项**

| code | 含义 |
|------|------|
| 43001 | 赏银低于最低限制 |
| 43002 | 令状字段不完整 |
| 43003 | 体力不足 |
| 43004 | 超出每日揭榜上限 |
| 43005 | 不可揭榜（本人/状态/已揭过） |
| 43006 | 成果提交被限流或内容非法 |
| 43007 | 不可再发一令（非令主 / 非允许终态 / 账号异常） |

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
- **副作用（v1.7，同一事务）**：
  1. 为新用户入账 **注册赠银**（默认 **500** 两，`sys_config`：`wallet.registerGrantAmount`）；流水 `type=REGISTER_GRANT`；`biz_no` 建议 `REG_GRANT:{userId}`（幂等）
  2. 若邀请码绑定有效邀请人：为邀请人入账 **邀新奖励**（默认 **100** 两，`wallet.inviteRewardAmount`）；流水 `type=INVITE_REWARD`；`biz_no` 建议 `INV_REWARD:{inviteeId}`（**同一被邀请人仅一次**）
  3. 向新用户、邀请人（若有）各发站内消息告知到账

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

## 4. 模拟钱庄（P0 / v1.7）

> MVP **银两来源**：注册赠银、邀新奖励、管理员发放/调账、悬赏结算/退款。  
> 用户 **充值/提现**：能力保留，默认 **关闭**（暂时隐藏，非删除）。

### 4.1 账户概览

- **GET** `/wallet/account`
- **Response data**

```json
{
  "balance": 500,
  "frozen": 0,
  "currency": "两",
  "simulated": true,
  "rechargeEnabled": false,
  "withdrawEnabled": false
}
```

| 字段 | 说明 |
|------|------|
| `rechargeEnabled` / `withdrawEnabled` | 来自 `sys_config`（`wallet.rechargeEnabled` / `wallet.withdrawEnabled`，默认 `false`）；前端据此隐藏入口 |

### 4.2 模拟充值（能力保留，默认关）

- **POST** `/wallet/recharge`
- **Body**：`{ "amount": 500, "clientRequestId": "idem-001" }`
- **Response data**：`{ "balance", "frozen", "ledgerBizNo" }`
- **规则**：
  - `wallet.rechargeEnabled≠true` → **`42004`**（暂未开放）；**不得删接口**
  - 开启后：`type=RECHARGE`；`clientRequestId` 幂等
- **错误**：`42004` 未开放；`42001` 不适用（入账）

### 4.3 模拟提现（能力保留，默认关）

- **POST** `/wallet/withdraw`
- **Body**：`{ "amount": 100, "clientRequestId": "idem-002" }`
- **规则**：
  - `wallet.withdrawEnabled≠true` → **`42004`**
  - 开启后：余额不足 → `42001`；流水 `type=WITHDRAW`
- **说明**：接口保留；C 端默认不可见、不可用

### 4.4 流水分页

- **GET** `/wallet/ledgers?page=1&pageSize=20&type=`
- **Query `type`**（可选，枚举）：

| type | 含义 | 方向 |
|------|------|------|
| `REGISTER_GRANT` | 注册赠银 | +余额 |
| `INVITE_REWARD` | 邀新奖励（入邀请人账） | +余额 |
| `RECHARGE` | 模拟充值（开关开启后） | +余额 |
| `WITHDRAW` | 模拟提现（开关开启后） | -余额 |
| `FREEZE` | 发令托管 | 余额→冻结 |
| `UNFREEZE_REFUND` | 驳回/超时退款 | 冻结→余额 |
| `SETTLE_PAY` | 结算扣托管 | -冻结 |
| `SETTLE_INCOME` | 结算入账 | +余额 |
| `PLATFORM_FEE` | 平台服务费（平台侧，用户流水可不展示） | — |
| `ADJUST` | 管理员调账/发放 | ± |

- **列表项示例**

```json
{
  "id": 12,
  "bizNo": "REG_GRANT:1001",
  "type": "REGISTER_GRANT",
  "amount": 500,
  "balanceAfter": 500,
  "frozenAfter": 0,
  "refType": "USER",
  "refId": 1001,
  "remark": "注册赠银",
  "createdAt": "2026-08-05T16:00:00+08:00"
}
```

### 4.5 钱庄公开开关（亦可并入账户概览）

- **GET** `/meta/wallet-features`（公开或登录均可）
- **Response data**

```json
{
  "rechargeEnabled": false,
  "withdrawEnabled": false,
  "registerGrantAmount": 500,
  "inviteRewardAmount": 100,
  "currencyLabel": "两",
  "simulated": true,
  "hint": "模拟银两，由平台发放与悬赏流转"
}
```

> 前端钱庄页：**不得**在 `rechargeEnabled/withdrawEnabled=false` 时展示充值/提现按钮；后台改配置后无需发版即可再开。

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
- **鉴权**：公开（可匿名）或登录均可
- **Response data**：数组；默认两套模板。**以下为默认契约示例，须与实现种子/Meta 默认一致**。

```json
[
  {
    "type": "RENT_SEEK",
    "code": "RENT_SEEK",
    "name": "求租令状",
    "fields": [
      { "key": "district", "label": "区域", "type": "text", "required": true },
      { "key": "rentBudgetMin", "label": "预算下限(元/月)", "type": "number", "required": true },
      { "key": "rentBudgetMax", "label": "预算上限(元/月)", "type": "number", "required": true },
      { "key": "layout", "label": "户型", "type": "text", "required": true },
      { "key": "expectMoveInDate", "label": "期望入住", "type": "date", "required": true },
      { "key": "acceptAgency", "label": "是否接受中介", "type": "boolean", "required": true },
      { "key": "extra", "label": "补充说明", "type": "textarea", "required": false }
    ]
  },
  {
    "type": "RENT_OUT",
    "code": "RENT_OUT",
    "name": "出租令状",
    "fields": [
      { "key": "district", "label": "区域", "type": "text", "required": true },
      { "key": "exactAddress", "label": "精确位置", "type": "text", "required": true, "maskUntilClaimed": true },
      { "key": "rentPrice", "label": "租金(元/月)", "type": "number", "required": true },
      { "key": "layout", "label": "户型", "type": "text", "required": true },
      { "key": "availableDate", "label": "可入住日期", "type": "date", "required": true },
      { "key": "furniture", "label": "家具家电", "type": "text", "required": false },
      { "key": "extra", "label": "补充说明", "type": "textarea", "required": false }
    ]
  }
]
```

**字段边界（相对 api v1.0 / 需求 v1.6 → v1.6.2）**：

| 约定 | 说明 |
|------|------|
| 不在 `fields` 内 | `title`、`difficulty`、`taskTags`、`rewardAmount`、`deadlineAt`、清单勾选 → 走创建悬赏顶层字段 |
| 已移除的产品字段 | 需求旧「需求说明」「其他要求」**不再**作为独立 key；统一并入可选 **`extra`** |
| `extra` | 求租 / 出租均有；选填；**label 固定为「补充说明」**；勿用「令外叮嘱」 |
| 详情展示 | `extra` 为空字符串/`null` 时前端可隐藏该行 |
| 勿混淆 | 探子清单项文案中的「备注」是验核交付，不是 `warrantFields.extra` |

### 5.3 探子清单模板（按标签预勾）

- **GET** `/meta/checklist-templates?tags=帮带看,帮验房`

### 5.4 成长相关公开配置

- **GET** `/meta/growth-config`  
  日揭榜上限、每日免费体力、单次耗体、侠义兑体力汇率、等级表摘要等

### 5.5 钱庄特性开关

见 **§4.5** `GET /meta/wallet-features`（与账户字段同源配置）。

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
- **Response data**（关键字段）

```json
{
  "id": 88,
  "type": "RENT_SEEK",
  "title": "红花岗两室求租核验",
  "status": "CANCELLED",
  "difficulty": "NORMAL",
  "rewardAmount": 350,
  "deadlineAt": "2026-08-20T23:59:59+08:00",
  "taskTags": ["帮寻房", "帮带看"],
  "warrantFields": {},
  "checklistItems": [],
  "claimCount": 0,
  "claimedByMe": false,
  "publisherId": 1,
  "sourceBountyId": null,
  "canRepublish": true,
  "createdAt": "2026-08-05T10:00:00+08:00"
}
```

| 字段 | 说明 |
|------|------|
| `sourceBountyId` | 若本单由「再发一令」产生，则为来源悬赏 ID；否则 `null` |
| `canRepublish` | 当前登录用户是否可对该单发起再发；规则见 §7.8（未登录或非令主恒为 `false`） |

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
    "extra": "忌西晒；工作日晚 8 点后方便联系"
  },
  "checklistItemCodes": ["VERIFY_AUTHENTIC", "SITE_VISIT_RECORD"],
  "sourceBountyId": null
}
```

- **规则**：`rewardAmount < minReward` → `43001`；低于建议下限且未 `confirmLowReward` → `40002`；余额不足 → `42001`；字段缺 → `43002`
- **`warrantFields`**：key 必须 ⊆ 对应类型模板；必填项不可空；**`extra` 可选**（缺省或 `""` 均合法）
- **`sourceBountyId`**：普通发令省略或 `null`。若客户端走「创建接口 + 来源 ID」等价路径，须满足与 §7.8 **相同的状态/归属约束**，且仍走新建+冻结+`PENDING_REVIEW`（**禁止**复活原单）。推荐主路径仍用 **`POST /bounties/{id}/republish`**。
- **成功**：创建后状态 `PENDING_REVIEW`，赏银已冻结；响应含新 `id` 与 `sourceBountyId`

#### 7.3.1 出租令 `warrantFields` 示例

```json
{
  "district": "汇川",
  "exactAddress": "某小区附近（揭榜后可见精确门牌策略）",
  "rentPrice": 1500,
  "layout": "一室一厅",
  "availableDate": "2026-09-15",
  "furniture": "床衣柜空调",
  "extra": "转租剩租约 8 个月，可协助对接房东"
}
```

### 7.4 我发布的 / 我揭榜的

- **GET** `/bounties/mine/published?status=&page=&pageSize=`
- **GET** `/bounties/mine/claimed?status=&page=&pageSize=`
- **列表项**须含：`id`、`status`、`title`、`rewardAmount`、`deadlineAt`、`canRepublish`、`sourceBountyId`（可空）

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

### 7.8 再发一令（重新发布 · v1.8）

> 基于终态原令 **复制新建**（新 ID）；原单状态 **不变**；须重新托管并进审核。

#### 7.8.1 预填草稿（可选，便于打开发布页）

- **GET** `/bounties/{id}/republish-draft`
- **鉴权**：令主本人
- **允许的原单 `status`**：`REJECTED` | `CANCELLED` | `COMPLETED`
- **Response data**

```json
{
  "sourceBountyId": 88,
  "type": "RENT_SEEK",
  "title": "红花岗两室求租核验",
  "difficulty": "NORMAL",
  "rewardAmount": 350,
  "deadlineAt": null,
  "taskTags": ["帮寻房", "帮带看"],
  "warrantFields": {
    "district": "红花岗",
    "rentBudgetMin": 800,
    "rentBudgetMax": 1200,
    "layout": "两室一厅",
    "expectMoveInDate": "2026-09-01",
    "acceptAgency": false,
    "extra": "忌西晒；工作日晚 8 点后方便联系"
  },
  "checklistItemCodes": ["VERIFY_AUTHENTIC", "SITE_VISIT_RECORD"],
  "suggestMin": 300,
  "minReward": 200
}
```

- **规则**：预填类型/标题/令状/标签/难易度/清单/原赏银；**不**预填揭榜、会话、成果、评价；`deadlineAt` 默认 `null`（令主须重选，也可由前端给建议值）；非允许状态或非令主 → `43007`

#### 7.8.2 提交再发（主路径）

- **POST** `/bounties/{id}/republish`
- **路径 `{id}`**：来源悬赏 ID（`sourceBountyId`）
- **鉴权**：令主本人；账号须未封禁
- **Body**（均可选；缺省字段从原令复制）

```json
{
  "title": "红花岗两室求租核验（再发）",
  "difficulty": "NORMAL",
  "rewardAmount": 350,
  "confirmLowReward": false,
  "deadlineAt": "2026-09-01T23:59:59+08:00",
  "taskTags": ["帮寻房", "帮带看"],
  "warrantFields": {
    "district": "红花岗",
    "rentBudgetMin": 800,
    "rentBudgetMax": 1200,
    "layout": "两室一厅",
    "expectMoveInDate": "2026-09-01",
    "acceptAgency": false,
    "extra": "忌西晒；工作日晚 8 点后方便联系"
  },
  "checklistItemCodes": ["VERIFY_AUTHENTIC", "SITE_VISIT_RECORD"]
}
```

- **状态约束（硬性）**

| 原单 status | 可否再发 |
|-------------|----------|
| `REJECTED` | ✓ |
| `CANCELLED` | ✓ |
| `COMPLETED` | ✓ |
| `PENDING_REVIEW` / `OPEN` / `IN_COLLAB` / `PENDING_SETTLE` / `IN_DISPUTE` | ✗ → `43007` |

- **其它约束**：非 `publisherId` → `43007`；原单不存在 → `40400`；赏银/令状校验同 §7.3（`43001`/`40002`/`43002`）；余额不足 → `42001`
- **行为**：
  1. **新建**悬赏行（新 `id`），写入 `sourceBountyId={id}`
  2. 复制（或 Body 覆盖）类型、标题、令状、标签、难易度、清单快照；**不复制**揭榜/会话/成果/评价/结算/纠纷
  3. **重新冻结**赏银（新 `frozen_biz_no`）；与原单资金无关
  4. 新单状态 = `PENDING_REVIEW`（**不可**跳过审核）
  5. **原单 status 保持不变**（禁止复活/改终态）
- **Response data**（新单摘要）

```json
{
  "id": 102,
  "sourceBountyId": 88,
  "status": "PENDING_REVIEW",
  "type": "RENT_SEEK",
  "title": "红花岗两室求租核验（再发）",
  "rewardAmount": 350,
  "deadlineAt": "2026-09-01T23:59:59+08:00",
  "frozen": true,
  "canRepublish": false
}
```

#### 7.8.3 `canRepublish` 计算

对当前登录用户与目标悬赏同时满足：

1. 已登录且为该单 `publisherId`
2. 用户状态正常（未封禁）
3. `status ∈ { REJECTED, CANCELLED, COMPLETED }`

否则 `canRepublish=false`（前端无入口；强调 API 仍返回 `43007`）。

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

## 14. 站内消息（P0 / v1.7）

> 须支持导航 **未读角标**；列表未读样式；进入详情或标记已读后清除。

### 14.1 消息列表

- **GET** `/messages?page=1&pageSize=20&unreadOnly=`
- **鉴权**：侠士登录
- **Query**：`unreadOnly=true` 时仅未读
- **Response `data`**（分页）

```json
{
  "list": [
    {
      "id": 101,
      "title": "注册赠银到账",
      "content": "您已获得注册赠银 500 两，可在钱庄查看。",
      "bizType": "WALLET",
      "bizId": null,
      "read": false,
      "createdAt": "2026-08-05T16:00:00+08:00"
    }
  ],
  "total": 3,
  "page": 1,
  "pageSize": 20
}
```

| 字段 | 说明 |
|------|------|
| `read` | `false`=未读；列表须可区分样式 |
| `bizType` | 如 `WALLET` / `BOUNTY` / `DISPUTE` / `OFFICE` / `SYSTEM` 等 |

### 14.2 未读数量（角标）

- **GET** `/messages/unread-count`
- **鉴权**：侠士登录
- **Response data**

```json
{
  "count": 3
}
```

- **规则**：`count=0` 时前端隐藏角标；布局可在登录后拉取，并可短间隔轮询（建议 ≥30s，实现自定）
- **索引**：`site_message(user_id, read_flag, id)` 支撑计数

### 14.3 标记已读

- **POST** `/messages/{id}/read`  
  **Response data**：`{ "id": 101, "read": true }`；幂等（已读再调仍成功）
- **POST** `/messages/read-all`  
  **Response data**：`{ "updated": 3 }`（本次新标记条数，可为 0）

- **副作用**：单条/全部已读后，`GET /messages/unread-count` 的 `count` 相应减少

### 14.4 消息详情（可选但建议）

- **GET** `/messages/{id}`
- **规则**：返回详情时 **自动标记已读**（若原未读）；响应含完整 `content` 与 `read: true`

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

> 管理员 Token；**菜单/按钮级 RBAC**（对齐缺陷 **D-003**：禁止全体账号固定 `permissions:["*"]`）。

### 16.0 管理员 RBAC 模型（契约 SSOT）

#### 16.0.1 权限码 `permissions[]` 枚举

格式：`{module}:{action}`，全小写，冒号分隔。校验时 **精确匹配**；另见通配符规则。

| 权限码 | 含义 | 典型绑定 API |
|--------|------|----------------|
| `dashboard:view` | 工作台概览 | `GET /admin/dashboard/overview` |
| `user:read` | 侠士列表/详情/登录日志 | `GET /admin/users**` |
| `user:write` | 启停/封禁解封/备注 | `POST .../disable|enable|ban|unban`，`PUT .../remark` |
| `user:asset_adjust` | 手工调账（侠义/体力/余额） | `POST /admin/users/{id}/assets/adjust` |
| `user:real_name` | 实名查看与状态维护 | `GET/PUT /admin/users/{id}/real-name` |
| `invite:read` | 邀请查询 | `GET /admin/invites` |
| `invite:write` | 生成/失效邀请码 | `POST /admin/invites**` |
| `bounty:read` | 悬赏列表/详情/会话抽检 | `GET /admin/bounties**` |
| `bounty:write` | 强制下架/关闭 | `POST /admin/bounties/{id}/force-close` |
| `bounty:review` | 发令审核（含改判） | `POST /admin/bounty-reviews/{bountyId}` |
| `submission:review` | 成果审核（含改判） | `POST /admin/submission-reviews/{submissionId}` |
| `wallet:read` | 流水与服务费汇总 | `GET /admin/wallet/**` |
| `wallet:flag` | 异常流水标记 | `POST /admin/wallet/ledgers/{id}/flag` |
| `dispute:read` | 纠纷队列/详情 | `GET /admin/disputes**` |
| `dispute:verdict` | 纠纷终裁执行 | `POST /admin/disputes/{id}/verdict` |
| `notice:read` | 告示列表 | `GET /admin/notices**` |
| `notice:write` | 告示增改下架置顶 | `POST/PUT/DELETE /admin/notices**` |
| `office:read` | 职司定义/申请/持有人只读 | `GET /admin/offices/**` |
| `office:write` | 职司配/审批/暂停/撤销 | `PUT/POST /admin/offices/**` |
| `lord:read` | 盟主与申请只读 | `GET /admin/lord**` |
| `lord:write` | 任命/驳回/罢免 | `POST /admin/lord/**` |
| `config:read` | 等级/英雄谱/体力/赏银建议/系统参数/字典只读 | `GET /admin/configs/**`，`GET /admin/dicts` |
| `config:write` | 上述配置写 | `PUT /admin/configs/**`，`PUT /admin/dicts` |
| `product:read` | 奖品与兑换单只读 | `GET /admin/products**`，`GET /admin/redeem-orders**` |
| `product:write` | 奖品与兑换单写 | `POST/PUT/DELETE ...` |
| `checklist:read` | 探子清单模板只读 | `GET /admin/checklist-templates**` |
| `checklist:write` | 探子清单模板写 | `POST/PUT/DELETE ...` |
| `warrant_config:read` | 令状字段配置只读 | `GET /admin/warrant-field-configs**` |
| `warrant_config:write` | 令状字段配置写 | `POST/PUT/DELETE ...` |
| `audit:read` | 审计日志 | `GET /admin/audit-logs` |
| `report:read` | 举报列表 | `GET /admin/reports` |
| `report:write` | 举报处理 | `POST /admin/reports/{id}/handle` |
| `job:read` | 定时任务状态 | `GET /admin/jobs` |
| `admin:read` | 管理员账号列表/详情 | `GET /admin/admins**` |
| `admin:write` | 管理员账号增改启停/改角 | `POST/PUT /admin/admins**` |
| `role:read` | 角色与权限只读 | `GET /admin/roles**` |
| `role:write` | 角色权限配置 | `POST/PUT/DELETE /admin/roles**` |
| `menu:read` | 菜单只读（含当前用户可见菜单树） | `GET /admin/menus**` |
| `menu:write` | 菜单 CRUD | `POST/PUT/DELETE /admin/menus**` |

**通配符 `"*"` 裁定（保留，但收紧）**：

| 规则 | 说明 |
|------|------|
| 是否保留 | **保留** |
| 谁可持有 | **仅**内置角色 `SUPER_ADMIN` 的权限集允许包含 `"*"` |
| 语义 | 等价于拥有上表全部权限码；鉴权命中 `*` 即放行任意 `/admin/**`（仍须有效 Admin JWT） |
| 禁止 | `OPS_ADMIN` / `ARBITER` / `OBSERVER` **不得**配置或返回 `"*"`；登录/`me` 必须返回**展开后的显式码列表** |
| 兼容 | 旧实现「全员 `["*"]`」视为 D-003 缺陷，落地后删除 |

前端按钮显隐以 `permissions` 精确包含为准（超管用 `*` 或前端将 `*` 视为全开）。

#### 16.0.2 四类内置角色 `code` 与默认权限集

| code | 名称 | 可改权限集 | 默认 `permissions` |
|------|------|------------|-------------------|
| `SUPER_ADMIN` | 超级管理员 | 否（内置只读） | `["*"]` |
| `OPS_ADMIN` | 运营管理员 | 是（不可赋 `*`，不可含 `role:write`/`menu:write`/`admin:write` 默认关闭；超管可改配置但种子不含） | 见下表 A |
| `ARBITER` | 终裁仲裁员 | 是 | 见下表 B |
| `OBSERVER` | 观察者 | 是 | 见下表 C（全只读） |

**表 A · `OPS_ADMIN` 默认集**（用户/悬赏/配置/职司/公告等；**无**纠纷终裁、**无**账号/角色/菜单写）：

```json
[
  "dashboard:view",
  "user:read", "user:write", "user:asset_adjust", "user:real_name",
  "invite:read", "invite:write",
  "bounty:read", "bounty:write", "bounty:review",
  "submission:review",
  "wallet:read", "wallet:flag",
  "dispute:read",
  "notice:read", "notice:write",
  "office:read", "office:write",
  "lord:read", "lord:write",
  "config:read", "config:write",
  "product:read", "product:write",
  "checklist:read", "checklist:write",
  "warrant_config:read", "warrant_config:write",
  "audit:read", "report:read", "report:write", "job:read",
  "admin:read", "role:read", "menu:read"
]
```

**表 B · `ARBITER` 默认集**：

```json
[
  "dashboard:view",
  "user:read",
  "bounty:read", "bounty:review",
  "submission:review",
  "wallet:read",
  "dispute:read", "dispute:verdict",
  "audit:read",
  "menu:read"
]
```

**表 C · `OBSERVER` 默认集**（无任何 `*:write` / `*:verdict` / `*:asset_adjust` / `*:review`）：

```json
[
  "dashboard:view",
  "user:read", "invite:read", "bounty:read", "wallet:read", "dispute:read",
  "notice:read", "office:read", "lord:read", "config:read",
  "product:read", "checklist:read", "warrant_config:read",
  "audit:read", "report:read", "job:read",
  "admin:read", "role:read", "menu:read"
]
```

账号可绑定 **多个角色**；最终权限 = 各角色权限码并集（若并集含 `*` 则等价全量）。种子数据至少 1 个 `SUPER_ADMIN` 账号。

#### 16.0.3 数据模型（与 architecture §3.2.4 一致）

| 表 | 用途 |
|----|------|
| `admin_user` | 后台账号 |
| `admin_role` | 角色（含四类内置） |
| `admin_permission` | 权限码字典 |
| `admin_role_permission` | 角色↔权限 |
| `admin_user_role` | 账号↔角色 |
| `admin_menu` | 菜单/目录/按钮树；`permission_code` 控制可见 |

#### 16.0.4 无权限时的 API 拦截策略

1. **认证**：`/api/v1/admin/**`（除 `POST /admin/auth/login`）须 Admin JWT；失败 → `40100` / `40101`。账号 `status≠ACTIVE` → `40301`。  
2. **默认拒绝**：已认证但未声明所需权限码（且无 `*`）→ **`40300`**，`message` 建议：`无权限: {permissionCode}`。  
3. **声明方式**：Controller 方法（或类）标注所需权限码；路径→权限映射以 §16.0.1「典型绑定」为准，未列出的写接口默认挂对应 `*:write`。  
4. **登录/me/logout**：`login` 公开；`logout`/`me` 仅需有效 Admin Token，**不**再要求业务权限码。  
5. **菜单过滤**：`GET /admin/menus/tree`（当前用户）只返回其 `permissions` 可覆盖的节点；按钮同理。前端隐藏不等于安全边界，**服务端必须拦**。  
6. **侠士 Token**：访问 `/admin/**` → `40300`（或 `40100`，实现统一一种并保持；推荐 **40300** 表示身份类型错误）。  
7. **审计**：`admin:write` / `role:write` / `menu:write` / `user:asset_adjust` / `dispute:verdict` / `lord:write` / `office:write` 成功后写 `audit_log`。

---

### 16.1 管理员登录 / 当前用户

#### 16.1.1 登录

- **POST** `/admin/auth/login`（公开）
- **Body**

```json
{
  "username": "superadmin",
  "password": "ChangeMe123!"
}
```

- **Response `data`**

```json
{
  "token": "eyJhbGciOi...",
  "expiresIn": 7200,
  "admin": {
    "id": 1,
    "username": "superadmin",
    "displayName": "武林盟主事",
    "status": "ACTIVE",
    "roles": [
      { "code": "SUPER_ADMIN", "name": "超级管理员" }
    ],
    "permissions": ["*"],
    "menus": []
  }
}
```

非超管示例（`permissions` **禁止**再写 `"*"`）：

```json
{
  "token": "eyJhbGciOi...",
  "expiresIn": 7200,
  "admin": {
    "id": 2,
    "username": "ops01",
    "displayName": "运营甲",
    "status": "ACTIVE",
    "roles": [
      { "code": "OPS_ADMIN", "name": "运营管理员" }
    ],
    "permissions": [
      "dashboard:view",
      "user:read",
      "user:write",
      "bounty:read",
      "bounty:review",
      "menu:read"
    ],
    "menus": []
  }
}
```

> `menus` 可在登录响应省略空数组，改由 `GET /admin/menus/tree` 拉取；若返回则结构同菜单树节点。  
> **废弃**：顶层再冗余一份与 `admin.permissions` 重复的 `data.permissions`（若兼容期保留，须与 `admin.permissions` 一致）。

#### 16.1.2 登出

- **POST** `/admin/auth/logout`  
- **鉴权**：Admin Token  
- **Response `data`**：`null`

#### 16.1.3 当前管理员

- **GET** `/admin/auth/me`  
- **鉴权**：Admin Token  
- **Response `data`**

```json
{
  "id": 2,
  "username": "ops01",
  "displayName": "运营甲",
  "status": "ACTIVE",
  "roles": [
    { "code": "OPS_ADMIN", "name": "运营管理员" }
  ],
  "permissions": [
    "dashboard:view",
    "user:read",
    "user:write",
    "bounty:read",
    "bounty:review",
    "menu:read"
  ]
}
```

### 16.2 工作台

- **GET** `/admin/dashboard/overview`  
  用户数、待审发令/成果、纠纷数、今日揭榜、托管汇总等

### 16.3 用户管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/users?page=1&pageSize=20&keyword=&status=` | 列表筛选 |
| GET | `/admin/users/{id}` | 详情（等级/声望/资产/邀请） |
| POST | `/admin/users/{id}/disable` | 禁用 |
| POST | `/admin/users/{id}/enable` | 启用 |
| POST | `/admin/users/{id}/ban` | 封禁 |
| POST | `/admin/users/{id}/unban` | 解封 |
| PUT | `/admin/users/{id}/remark` | 备注 |
| POST | `/admin/users/{id}/assets/adjust` | 手工调整侠义/体力/余额（审计） |
| GET | `/admin/users/{id}/login-logs` | 登录日志 |
| GET | `/admin/users/{id}/real-name` | 实名查看/状态维护 PUT |

**`GET /admin/users` 查询参数**：

| 参数 | 说明 |
|------|------|
| `keyword` | 可选；模糊匹配 **`user.username` / `user.phone` / `user_profile.nickname`**（任一命中即返回） |
| `status` | 可选；`ACTIVE` / `DISABLED` / `BANNED` |
| `page` / `pageSize` | 分页，默认 `1` / `20` |

列表项至少含：`id`、`username`、`phone`、`nickname`、`status`、`city`、`level`、`levelTitle`、`createdAt`。

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

### 16.10 系统管理（含完整 RBAC CRUD）

> 下列接口除标注外均需 Admin Token + 对应权限码；无权限 → `40300`。

#### 16.10.1 管理员账号 `/admin/admins`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/admin/admins?page=1&pageSize=20&keyword=&status=` | `admin:read` | 分页列表 |
| GET | `/admin/admins/{id}` | `admin:read` | 详情 |
| POST | `/admin/admins` | `admin:write` | 创建 |
| PUT | `/admin/admins/{id}` | `admin:write` | 更新资料/角色/状态 |
| POST | `/admin/admins/{id}/reset-password` | `admin:write` | 重置密码 |
| POST | `/admin/admins/{id}/disable` | `admin:write` | 停用 |
| POST | `/admin/admins/{id}/enable` | `admin:write` | 启用 |

**列表项 / 详情 `data` 示例**：

```json
{
  "id": 2,
  "username": "ops01",
  "displayName": "运营甲",
  "status": "ACTIVE",
  "roleCodes": ["OPS_ADMIN"],
  "roles": [
    { "code": "OPS_ADMIN", "name": "运营管理员" }
  ],
  "createdAt": "2026-08-05T10:00:00+08:00",
  "updatedAt": "2026-08-05T12:00:00+08:00"
}
```

**创建 Body**：

```json
{
  "username": "arbiter01",
  "password": "InitPass123!",
  "displayName": "仲裁乙",
  "roleCodes": ["ARBITER"],
  "status": "ACTIVE"
}
```

**更新 Body**（可部分字段）：

```json
{
  "displayName": "仲裁乙·改",
  "roleCodes": ["ARBITER", "OBSERVER"],
  "status": "ACTIVE"
}
```

**重置密码 Body**：`{ "newPassword": "NewPass123!" }`

规则：不可删除最后一个 `SUPER_ADMIN`；不可去掉自身超管角色导致系统无超管（服务端校验）。密码不明文回传。

#### 16.10.2 角色 `/admin/roles`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/admin/roles` | `role:read` | 全部角色（含权限码） |
| GET | `/admin/roles/{code}` | `role:read` | 单角色 |
| PUT | `/admin/roles/{code}/permissions` | `role:write` | 覆盖该角色权限集 |
| GET | `/admin/roles/permission-catalog` | `role:read` | 权限码目录（§16.0.1 全量） |

MVP **不提供**自定义新建角色 code（仅四类内置）；`role:write` 只改非超管角色的权限绑定。`SUPER_ADMIN` 的权限集只读，PUT → `40002`。

**角色详情示例**：

```json
{
  "code": "OPS_ADMIN",
  "name": "运营管理员",
  "builtin": true,
  "description": "用户/悬赏/配置/职司/公告等运营权限",
  "permissions": [
    "dashboard:view",
    "user:read",
    "user:write",
    "bounty:read",
    "bounty:review",
    "menu:read"
  ],
  "updatedAt": "2026-08-05T12:00:00+08:00"
}
```

**覆盖权限 Body**：

```json
{
  "permissions": [
    "dashboard:view",
    "user:read",
    "bounty:read",
    "menu:read"
  ]
}
```

校验：列表中出现 `"*"` 且 `code≠SUPER_ADMIN` → `40001`；未知权限码 → `40001`。

**权限目录项示例**：

```json
{
  "code": "dispute:verdict",
  "name": "纠纷终裁",
  "module": "dispute",
  "type": "API"
}
```

`type` 枚举：`API | MENU | BUTTON`（目录用途；鉴权统一按 `code` 字符串）。

#### 16.10.3 菜单 `/admin/menus`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/admin/menus/tree` | `menu:read`（或仅登录） | **当前用户**可见菜单树（按权限过滤） |
| GET | `/admin/menus/all` | `menu:read` | 全量菜单树（配置用） |
| POST | `/admin/menus` | `menu:write` | 新增节点 |
| PUT | `/admin/menus/{id}` | `menu:write` | 更新 |
| DELETE | `/admin/menus/{id}` | `menu:write` | 删除（无子节点） |

> `GET /admin/menus/tree`：允许「仅 Admin 登录」即可（便于侧栏渲染）；若实现要求 `menu:read`，则四类角色默认集均已包含。

**菜单节点字段**：

```json
{
  "id": 10,
  "parentId": 0,
  "type": "MENU",
  "name": "侠士管理",
  "path": "/admin/users",
  "component": "admin/UsersView",
  "icon": "User",
  "sort": 20,
  "visible": true,
  "permissionCode": "user:read",
  "children": [
    {
      "id": 11,
      "parentId": 10,
      "type": "BUTTON",
      "name": "资产调账",
      "path": "",
      "component": "",
      "icon": "",
      "sort": 1,
      "visible": true,
      "permissionCode": "user:asset_adjust",
      "children": []
    }
  ]
}
```

`type` 枚举：`DIR`（目录）| `MENU`（页面）| `BUTTON`（按钮）。  
种子菜单须覆盖前端现有 `/admin/*` 路由（工作台、侠士、邀请、悬赏、钱庄、纠纷、告示、职司、盟主、运营参数、奖品、探子清单、令状字段、系统配置，以及 RBAC：管理员/角色/菜单）。

**新增 Body 示例**：

```json
{
  "parentId": 0,
  "type": "MENU",
  "name": "管理员账号",
  "path": "/admin/admins",
  "component": "admin/AdminsView",
  "icon": "Setting",
  "sort": 90,
  "visible": true,
  "permissionCode": "admin:read"
}
```

#### 16.10.4 其它系统接口

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET/PUT | `/admin/dicts` | `config:read` / `config:write` | 字典枚举 |
| GET/PUT | `/admin/configs/system` | `config:read` / `config:write` | 系统参数（费率等） |
| GET | `/admin/jobs` | `job:read` | 定时任务状态 |
| GET | `/admin/login-logs` | `audit:read` | 管理员登录日志 |

---

## 17. P0 需求 ↔ 接口映射

| 需求验收 / 功能 | 主要接口 |
|-----------------|----------|
| 邀请注册 + 双登录 | 2.1–2.4 |
| 邀请生成与追溯 | 2.7–2.8，16.4 |
| 资料与实名入口 | 3.1–3.2 |
| 模拟钱庄 | 4.x，16.6 |
| 结构化令状 + 赏银建议 ≥200 | 5.1–5.2，7.3 |
| 令状自由文本「补充说明」(`extra`) | 5.2，7.3；label 固定，禁止平行 key |
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
| 管理员完整 RBAC（D-003） | **16.0**，16.1，16.10 |
| 注册赠银 500 / 邀新奖励 100 | **2.3**，4.4（`REGISTER_GRANT`/`INVITE_REWARD`） |
| 充值/提现暂时隐藏（可配置再开） | **4.1～4.5**，错误码 `42004` |
| 消息未读角标 | **14.2** `GET /messages/unread-count`；14.1/14.3 |
| 再发一令（v1.8） | **7.8**；详情/列表 `canRepublish`/`sourceBountyId`；错误码 `43007` |

---

## 18. 变更记录

| 版本 | 日期 | 相对上一版差异（字段级） |
|------|------|--------------------------|
| v0.1 | — | 模板初始化 |
| v1.0 | 2026-08-05 | 按 MVP P0 全量定义接口契约 |
| v1.0.1 | 2026-08-05 | **对齐需求 v1.6.1/v1.6.2 + architecture v1.0.1**：① §0 明确 `extra`→「补充说明」、禁用「令外叮嘱」及平行备注 key；② **§5.2 补全**默认 `warrant-templates` JSON（含两套模板字段表）；③ 明确旧「需求说明/其他要求」不落独立 key；④ §7.3 补充 `extra` 可选规则与出租示例；⑤ 对齐引用需求版本号 |
| v1.0.2 | 2026-08-05 | **D-003 管理员完整 RBAC**：① 新增 **§16.0**（权限码枚举、`*` 仅超管、四角色默认集、拦截策略）；② **§16.1** login/me 字段补全 `roles`/`permissions`/`status`，废弃顶层重复 permissions；③ **§16.10** 展开 `/admin/admins`、`/roles`、`/menus` 请求响应示例与权限要求；④ 路径↔权限码绑定表 |
| v1.0.3 | 2026-08-05 | **需求 v1.7.1**：① §2.3 注册副作用：赠银/`REGISTER_GRANT`、邀新/`INVITE_REWARD`+幂等 biz_no；② §4 账户增加 `rechargeEnabled`/`withdrawEnabled`；充值提现关时 **`42004`**（接口保留）；③ 流水枚举补发放类；④ §4.5 `/meta/wallet-features`；⑤ §14 补列表字段、`GET /messages/unread-count`、已读响应；⑥ 错误码 `42004` |
| **v1.0.4** | 2026-08-05 | **需求 v1.8.0 再发一令**：① 新增 **§7.8** `GET .../republish-draft` + `POST .../republish`；② 详情/我的发布增加 `sourceBountyId`、`canRepublish`；③ 仅 `REJECTED`/`CANCELLED`/`COMPLETED` 可再发；原单不变、新单 `PENDING_REVIEW`+重新冻结；④ 错误码 **`43007`**；⑤ `POST /bounties` 可选 `sourceBountyId`（等价约束） |
| **v1.0.5** | 2026-08-05 | **§16.3** 明确 `GET /admin/users` 的 `keyword`：模糊匹配 `username` / `phone` / **`user_profile.nickname`**；补列表项字段说明 |
