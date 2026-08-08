# 接口文档（API）

> 由 **架构师 AI（@architect）** 定义；**后端 / 前端** 严格对齐本文件。  
> 对齐：`docs/requirements.md`（**v1.8.17**）P0、`docs/architecture.md`（**v1.0.9**）。

**版本**：v1.0.13  
**Base URL**：`/api/v1`  
**鉴权**：`Authorization: Bearer <accessToken>`（标注「公开」的除外）  
**最后更新**：2026-08-08

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
| 43008 | 当前状态不可发送协作消息（终态/纠纷中/无协作参与/已退出等） |
| 43009 | 当前状态不可提交成果（终态/纠纷中/已退出/非可交状态等） |
| 43010 | 本令已有成果提交，禁止全额退回取消（须走分配分支 `cancelOutcome=ALLOCATE`） |
| 43011 | 有成果取消待分配未完成（不可重复取消 / 不可按「完结」口径结案等） |

**邀请专项**

| code | 含义 |
|------|------|
| 44001 | 邀请码无效/已用尽/过期 |

**用户反馈专项（v1.8.16）**

| code | 含义 |
|------|------|
| 45001 | 反馈提交过于频繁（短时冷却未到） |
| 45002 | 今日反馈条数已达上限 |

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
- **Response data**：数组；默认 **三套**模板（v1.8.9）。**以下为默认契约示例，须与实现种子/Meta 默认一致**。

```json
[
  {
    "type": "RENT_SEEK",
    "code": "RENT_SEEK",
    "name": "租房令状",
    "displayName": "租房悬赏",
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
    "displayName": "出租悬赏",
    "fields": [
      { "key": "district", "label": "区域", "type": "text", "required": true },
      { "key": "exactAddress", "label": "精确位置", "type": "text", "required": true, "maskUntilClaimed": true },
      { "key": "rentPrice", "label": "租金(元/月)", "type": "number", "required": true },
      { "key": "layout", "label": "户型", "type": "text", "required": true },
      { "key": "availableDate", "label": "可入住日期", "type": "date", "required": true },
      { "key": "furniture", "label": "家具家电", "type": "text", "required": false },
      { "key": "extra", "label": "补充说明", "type": "textarea", "required": false }
    ]
  },
  {
    "type": "RENT_TRANSFER",
    "code": "RENT_TRANSFER",
    "name": "转租令状",
    "displayName": "转租悬赏",
    "fields": [
      { "key": "district", "label": "区域", "type": "text", "required": true },
      { "key": "exactAddress", "label": "精确位置", "type": "text", "required": true, "maskUntilClaimed": true },
      { "key": "rentPrice", "label": "租金(元/月)", "type": "number", "required": true },
      { "key": "layout", "label": "户型", "type": "text", "required": true },
      { "key": "availableDate", "label": "可入住日期", "type": "date", "required": true },
      { "key": "complianceNote", "label": "转租合规说明", "type": "textarea", "required": false },
      { "key": "furniture", "label": "家具家电", "type": "text", "required": false },
      { "key": "extra", "label": "补充说明", "type": "textarea", "required": false }
    ]
  }
]
```

**令种枚举与武侠展示名（v1.8.9 裁定：三值）**：

| `type` / `code` | 模板 `name` | C 端/执事堂 `displayName`（界面必用） |
|-----------------|-------------|--------------------------------------|
| `RENT_SEEK` | 租房令状 | **租房悬赏** |
| `RENT_OUT` | 出租令状 | **出租悬赏** |
| `RENT_TRANSFER` | 转租令状 | **转租悬赏** |

- 广场筛选、发令、列表/详情标签、审核列表：**禁止**再写「求租」「出租/转租」合并档；一律用上表 `displayName`（或模板返回的同名字段）。
- `RENT_OUT` 历史数据保持；新发「转租」必须用 `RENT_TRANSFER`，不得再写入 `RENT_OUT` 冒充转租。
- `extra`：三套模板均有；选填；label 固定「补充说明」。

**字段边界**：

| 约定 | 说明 |
|------|------|
| 不在 `fields` 内 | `title`、`difficulty`、`taskTags`、`rewardAmount`、`deadlineAt`、清单勾选 → 走创建悬赏顶层字段 |
| 已移除的产品字段 | 需求旧「需求说明」「其他要求」**不再**作为独立 key；统一并入可选 **`extra`** |
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
- **Query `type`**：`RENT_SEEK` | `RENT_OUT` | `RENT_TRANSFER`（可空=全部）；筛选项展示名见 §5.2

### 7.2 详情

- **GET** `/bounties/{id}`
- **Response data**（关键字段）

```json
{
  "id": 88,
  "type": "RENT_SEEK",
  "typeDisplayName": "租房悬赏",
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
  "hasSubmissions": true,
  "cancelAllocationPending": false,
  "capabilities": {
    "canCancel": false,
    "canSendMessage": false,
    "canReadMessages": true,
    "canViewSubmissions": true,
    "canSubmit": false,
    "canSettle": false,
    "canQuitClaim": false,
    "canRepublish": true,
    "canDispute": false
  },
  "createdAt": "2026-08-05T10:00:00+08:00"
}
```

| 字段 | 说明 |
|------|------|
| `type` | `RENT_SEEK` \| `RENT_OUT` \| `RENT_TRANSFER` |
| `typeDisplayName` | 武侠展示名（§5.2）；前端也可本地映射，但不得与契约冲突 |
| `sourceBountyId` | 若本单由「再发一令」产生，则为来源悬赏 ID；否则 `null` |
| `canRepublish` | 同 `capabilities.canRepublish`（兼容旧字段） |
| `hasSubmissions` | **v1.8.17** 本令是否存在 **任意一条** 成果提交记录（含 `PENDING`/`APPROVED`/`REJECTED`）；前端取消二次确认文案分支依赖此字段 |
| `cancelAllocationPending` | **v1.8.17** 令主有成果取消后进入「待分配」分支且尚未分完；为 `true` 时会话/提交按终态收口，令主仅可走取消分支结算（见 §9.3） |
| `capabilities` | **v1.8.10** 当前用户相对本单的能力开关，算法见 **§7.9**；前端按钮区以此为准并在状态变更后重拉详情 |

（出租/转租令「精确位置」等字段按配置对未揭榜人脱敏）

### 7.3 创建悬赏（发令）

- **POST** `/bounties`
- **Body**

```json
{
  "type": "RENT_SEEK | RENT_OUT | RENT_TRANSFER",
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

- **规则**：`type` 必须为三枚举之一；`rewardAmount < minReward` → `43001`；低于建议下限且未 `confirmLowReward` → `40002`；余额不足 → `42001`；字段缺 → `43002`
- **`warrantFields`**：key 必须 ⊆ 对应类型模板；必填项不可空；**`extra` 可选**（缺省或 `""` 均合法）
- **`RENT_OUT` / `RENT_TRANSFER` 必填 key**：`district`、`exactAddress`、`rentPrice`、`layout`、`availableDate`（`complianceNote`/`furniture`/`extra` 可选）
- **`sourceBountyId`**：普通发令省略或 `null`。若客户端走「创建接口 + 来源 ID」等价路径，须满足与 §7.8 **相同的状态/归属约束**，且仍走新建+冻结+`PENDING_REVIEW`（**禁止**复活原单）。推荐主路径仍用 **`POST /bounties/{id}/republish`**。
- **成功**：创建后状态 `PENDING_REVIEW`，赏银已冻结；响应含新 `id` 与 `sourceBountyId`；建议同时返回 `type` 与 `typeDisplayName`（取自 §5.2 映射）

#### 7.3.1 出租令 `warrantFields` 示例

```json
{
  "district": "汇川",
  "exactAddress": "某小区附近（揭榜后可见精确门牌策略）",
  "rentPrice": 1500,
  "layout": "一室一厅",
  "availableDate": "2026-09-15",
  "furniture": "床衣柜空调",
  "extra": "可协助对接看房"
}
```

#### 7.3.2 转租令 `warrantFields` 示例（`type=RENT_TRANSFER`）

```json
{
  "district": "播州",
  "exactAddress": "某小区（揭榜后可见）",
  "rentPrice": 1200,
  "layout": "两室一厅",
  "availableDate": "2026-09-10",
  "complianceNote": "转租已征得房东口头同意，可协助对接",
  "furniture": "床衣柜",
  "extra": "剩租约 8 个月"
}
```

### 7.4 我发布的 / 我揭榜的

- **GET** `/bounties/mine/published?status=&page=&pageSize=`
- **GET** `/bounties/mine/claimed?status=&page=&pageSize=`
- **列表项**须含：`id`、`type`、`typeDisplayName`（可选但前端可用映射表）、`status`、`title`、`rewardAmount`、`deadlineAt`、`canRepublish`、`sourceBountyId`（可空）
- **我的悬赏附加（v1.8.17+ 体验）**：
  - `submissionCount`：本令已提交成果条数（任意状态）
  - `unreadCollabCount`：协作会话未读数（他人发送且晚于本人最近一次拉取消息水位）；打开 `GET /bounties/{id}/messages` 即清零对本令的未读
  - 前端「我的悬赏」默认将 `IN_COLLAB`（进行中）排在前；展示名：我的页 `进行中`、悬赏榜 `悬赏中`

### 7.5 一键揭榜

- **POST** `/bounties/{id}/claims`
- **Body**：`{}`
- **错误**：`43003` 体力不足；`43004` 日上限；`43005` 不可揭榜；`40900` 已揭过

### 7.5.1 退出揭榜（v1.8.8 / 能力矩阵）

- **POST** `/bounties/{id}/claims/quit`
- **鉴权**：揭榜侠，`claim.status=ACTIVE`，悬赏状态 **`IN_COLLAB`**（`PENDING_SETTLE` 默认禁止）
- **Body**：`{}` 或 `{ "reason": "临时有事" }`（reason 可选）
- **行为**：claim → `QUIT`；**返还**本次揭榜体力；**不**回退当日揭榜次数；同令不可再揭；通知令主
- **Response data**：`{ "claimId", "status": "QUIT", "staminaRefunded": 1 }`
- **错误**：非 ACTIVE / 状态不允许 → `40002`；非揭榜人 → `40300`
- **副作用**：此后 `canSendMessage`/`canSubmit`/`canQuitClaim`=false；历史会话/成果只读

### 7.6 协作会话消息列表（v1.8.9 / v1.8.10）

- **GET** `/bounties/{id}/messages?page=1&pageSize=50`
- **鉴权**：侠士登录
- **参与人（可读）**：
  - 该悬赏 **令主**（`publisherId`），或
  - 存在揭榜关系的侠士（含 `ACTIVE` / `QUIT`）→ **可拉历史**
  - 且 `capabilities.canReadMessages=true`（见 §7.9；终态仍可只读）
- **可见性（硬性）**：返回该悬赏下的 **共享消息流**（按 `id` 升序）；**禁止**按当前用户过滤为「仅本人发送」；任一参与人发送的消息，其他参与人刷新/轮询后必须可见。
- **Response `data`**（分页）

```json
{
  "list": [
    {
      "id": 1,
      "bountyId": 88,
      "senderId": 10,
      "senderNickname": "令主甲",
      "content": "今晚 7 点可带看",
      "createdAt": "2026-08-07T19:00:00+08:00"
    },
    {
      "id": 2,
      "bountyId": 88,
      "senderId": 20,
      "senderNickname": "揭榜乙",
      "content": "收到，我准时到",
      "createdAt": "2026-08-07T19:01:00+08:00"
    }
  ],
  "total": 2,
  "page": 1,
  "pageSize": 50
}
```

| 字段 | 说明 |
|------|------|
| `senderId` | 发送人用户 ID |
| `senderNickname` | **必填**（联表资料）；前端气泡区分己方/对方 |
| `content` | 正文 |

- **错误**：非参与人 → `40300`；悬赏不存在 → `40400`
- **验收**：账号 A（令主）与 B（揭榜侠）互发各 ≥1 条后，双方 `GET` 得到同一套消息集合（顺序一致）

### 7.7 发送协作会话消息（v1.8.9 / v1.8.10 状态拦截）

- **POST** `/bounties/{id}/messages`
- **鉴权**：侠士登录
- **可写前提（同时满足）**：
  1. 身份：令主，或揭榜关系 **`status=ACTIVE`**
  2. 悬赏状态 ∈ `{ IN_COLLAB, PENDING_SETTLE }`  
     （**禁止**在 `CANCELLED` / `REJECTED` / `COMPLETED` / `IN_DISPUTE` / `PENDING_REVIEW` / `OPEN`（尚无协作）发送）
  3. 取消已生效进入「待分配」分支时：按终态收口，**禁发**（`43008`）
- **Body**

```json
{
  "content": "今晚可带看"
}
```

- **规则**：`content` trim 后非空，长度 ≤1000；成功写入 `bounty_message`
- **Response data**：单条消息对象（字段同列表项，含 `senderNickname`）
- **错误**：
  - 内容非法 → `40001`
  - 非参与人 / 已退出 → `40300` 或 `43008`
  - **状态不允许发送** → **`43008`**
- **前端**：仅当 `capabilities.canSendMessage=true` 展示发送框；终态进入只读会话；轮询 `GET` 建议 ≤8s

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

### 7.9 全生命周期能力矩阵（v1.8.10 · 对齐 requirements §6.28；v1.8.17 取消分支补强）

> 详情 `capabilities` 与写接口拦截 **同一套规则**。前端藏按钮 ≠ 安全。

#### 7.9.1 `capabilities` 字段语义

| 字段 | 含义 |
|------|------|
| `canCancel` | 可 `POST /bounties/{id}/cancel`（**有成果时仍为 true**——取消=进分配，**不是**禁按钮；见 §9.3） |
| `canSendMessage` | 可 `POST .../messages` |
| `canReadMessages` | 可进会话页并 `GET .../messages`（只读或可发） |
| `canViewSubmissions` | 可查看本令成果列表/详情（令主看全部；揭榜侠看自己；见 §8） |
| `canSubmit` | 可 `POST .../submissions` |
| `canSettle` | 可进入分配页并调用 preview/settlement：**正常完结**，或 **`cancelAllocationPending=true` 的取消分支分配** |
| `canQuitClaim` | 可退出揭榜 |
| `canRepublish` | 可再发一令 |
| `canDispute` | 可发起纠纷（窗口内） |

未登录：全部 `false`（公开详情仍可看基础字段）。

#### 7.9.2 令主（`publisherId=当前用户`）

| 能力 | PENDING_REVIEW | OPEN | IN_COLLAB | PENDING_SETTLE | COMPLETED | CANCELLED | REJECTED | IN_DISPUTE |
|------|----------------|-----|-----------|----------------|-----------|-----------|----------|------------|
| canCancel | ✅ | ✅ | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| canSendMessage | ❌ | ❌* | ✅ | ✅ | ❌ | ❌ | ❌ | ❌ |
| canReadMessages | ❌ | ❌* | ✅ | ✅ | 👁→✅ | 👁→✅ | ❌ | 👁→✅ |
| canViewSubmissions | ❌ | ❌* | ✅ | ✅ | 👁→✅ | 👁→✅ | ❌ | 👁→✅ |
| canSubmit | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ | ❌ |
| canSettle | ❌ | ❌ | ✅** | ✅ | ❌ | ❌ | ❌ | ❌ |
| canRepublish | ❌ | ❌ | ❌ | ❌ | ✅ | ✅ | ✅ | ❌ |
| canDispute | ❌ | ❌ | ❌ | ❌ | ✅† | ❌ | ❌ | — |

\* `OPEN` 且尚无揭榜人：会话/成果均为 false；有人揭榜后状态应为 `IN_COLLAB`。  
\*\* 须满足结算前置（≥1 揭榜且存在审核通过成果）。  
\*\*\* **`cancelAllocationPending=true`（有成果取消待分配）**：无论主状态暂为 `IN_COLLAB` 或 `PENDING_SETTLE`，一律：`canCancel=false`，`canSendMessage=false`，`canSubmit=false`，`canViewSubmissions=true`，**`canSettle=true`**（仅取消分支分配；完成后 `status→CANCELLED`）。  
† 结算后 7 日窗口。  
👁→✅：布尔值仍为 `true`（可进只读），写操作另由 `canSendMessage`/`canSubmit` 关闭。

#### 7.9.3 揭榜侠（`claimedByMe` 且 claim=`ACTIVE`）

| 能力 | IN_COLLAB | PENDING_SETTLE | COMPLETED | CANCELLED | claim=QUIT 后 |
|------|-----------|----------------|-----------|-----------|---------------|
| canSendMessage | ✅ | ✅ | ❌ | ❌ | ❌ |
| canReadMessages | ✅ | ✅ | ✅ | ✅ | ✅（历史） |
| canSubmit | ✅ | ✅‡ | ❌ | ❌ | ❌ |
| canViewSubmissions | ✅ | ✅ | ✅ | ✅ | ✅ |
| canQuitClaim | ✅ | ❌§ | ❌ | ❌ | — |
| canSettle / canCancel / canRepublish | ❌ | ❌ | ❌ | ❌ | ❌ |
| canDispute | — | — | ✅† | 取消争议窗口 | 按参与资格 |

‡ MVP 待验收仍允许补交（频控仍生效）。  
§ 待验收默认不可退出。  
`cancelAllocationPending=true` 时揭榜侠：`canSendMessage=false`，`canSubmit=false`，`canViewSubmissions=true`（只读）。

#### 7.9.4 写接口状态硬拦截（后端必做）

| 接口 | 允许状态（另加身份） | 拒绝码 |
|------|----------------------|--------|
| `POST /bounties/{id}/messages` | `IN_COLLAB`, `PENDING_SETTLE`（且非 `cancelAllocationPending`） | **`43008`** |
| `POST /bounties/{id}/submissions` | `IN_COLLAB`, `PENDING_SETTLE`（且非 `cancelAllocationPending`） | **`43009`** |
| `POST /bounties/{id}/cancel` | `PENDING_REVIEW`, `OPEN`, `IN_COLLAB`, `PENDING_SETTLE`（且非 `cancelAllocationPending`） | `40002`；有成果却走全额退 → **`43010`** |
| `POST /bounties/{id}/settlement` | 正常完结：`IN_COLLAB`（条件满足）/`PENDING_SETTLE`；**取消分支**：`cancelAllocationPending=true` | `40002` / **`43011`** |

终态集合（禁会话发送、禁交成果）：`CANCELLED` | `REJECTED` | `COMPLETED` | `IN_DISPUTE`。  
取消已生效进入分配页期间（`cancelAllocationPending=true`）：会话/提交按终态收口 → `43008` / `43009`。  
有成果取消 ≠ 隐藏取消按钮；`canCancel` 在取消生效前于协作中/待验收仍为 true。

---

## 8. 成果提交与查看（P0 / v1.8.10 · **v1.8.17 详情可达**）

> 对齐 requirements §6.4 / §6.28 / **§6.34.1**。  
> 「成果查看」= **列表**（§8.2）+ **可下钻完整详情**（§8.4）；禁止仅摘要一行、点不开正文。  
> **共享详情 VO**（§8.0）同时被 C 端、执事堂、Admin 详情复用；禁止各端另造平行字段。

### 8.0 成果详情 VO（`SubmissionDetail` · SSOT）

以下字段为详情响应 **必返回**（无则填 `null` / `[]`，不得省略键名以免前端猜字段）：

```json
{
  "submissionId": 501,
  "bountyId": 88,
  "bountyTitle": "红花岗两室求租核验",
  "claimId": 33,
  "claimerUserId": 20,
  "claimerNickname": "揭榜乙",
  "versionNo": 2,
  "status": "APPROVED",
  "summary": "已完成两套房带看",
  "items": [
    {
      "itemCode": "SITE_VISIT_RECORD",
      "itemName": "实地探访记录",
      "done": true,
      "text": "2026-08-06 15:00 某某小区，接待方中介王某",
      "mediaUrls": ["https://cdn.example/1.jpg", "https://cdn.example/2.jpg"]
    }
  ],
  "reviewReason": null,
  "reviewedAt": "2026-08-07T16:00:00+08:00",
  "createdAt": "2026-08-07T15:00:00+08:00",
  "updatedAt": "2026-08-07T16:00:00+08:00"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `submissionId` | long | 成果 ID（**主键对外名**；勿再用裸 `id` 作为契约主字段） |
| `bountyId` | long | 关联悬赏 |
| `bountyTitle` | string | 悬赏标题（详情/审核上下文必见） |
| `claimId` | long | 揭榜关系 ID |
| `claimerUserId` | long | 提交人用户 ID |
| `claimerNickname` | string | 提交人昵称 |
| `versionNo` | int | 同 claim 下版本号 |
| `status` | string | `PENDING` \| `APPROVED` \| `REJECTED` |
| `summary` | string | 成果摘要 / 总说明 |
| `items[]` | array | 探子清单各项填写内容（**正文必见**） |
| `items[].itemCode` | string | 清单项 code（与发令快照一致） |
| `items[].itemName` | string | 清单项展示名（来自本单清单快照；可空则前端用 code） |
| `items[].done` | boolean | 是否完成该项；**服务端以该项是否有 `text` 或 `mediaUrls` 为准落库**（客户端可省略或传任意值，不作为唯一依据） |
| `items[].text` | string\|null | 文字说明 |
| `items[].mediaUrls` | string[] | 图片附件 URL 列表（可预览） |
| `reviewReason` | string\|null | 驳回/改判原因；通过且无说明时为 `null` |
| `reviewedAt` | string\|null | 最近一次审核时间；未审为 `null` |
| `createdAt` | string | 提交时间 |
| `updatedAt` | string | 最后更新时间 |

**兼容说明**：旧实现若仍返回 `id` / `userId` / `rejectReason`，须 **同时**返回上表契约字段；前端与联调以本表为准，禁止只认旧键。

### 8.1 提交成果（可多次）

- **POST** `/bounties/{id}/submissions`
- **鉴权**：揭榜侠，`claim.status=ACTIVE`
- **允许状态**：`IN_COLLAB` | `PENDING_SETTLE`（且非 `cancelAllocationPending`）；其余 → **`43009`**
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

- **成功 Response `data`**：同 §8.0 `SubmissionDetail`（刚创建版本，`status=PENDING`）
- **错误**：`43006` 冷却/日限/空内容/必验项无说明且无凭证；**`43009`** 状态/身份不允许；`40300` 未揭榜或已退出

> **必验判定（v1.8.18）**：必验清单项须至少填写 `text` 或上传 `mediaUrls` 之一，方可提交；不再依赖「已完成」勾选开关。响应中的 `done` 由服务端按是否有内容写入。

### 8.2 本令成果列表（「成果查看」主数据源 · v1.8.10 / **v1.8.17**）

- **GET** `/bounties/{id}/submissions`
- **鉴权**：登录侠士
- **权限**（对齐 §6.34.1）：
  - **令主**：返回该悬赏下 **全部** 揭榜人的全部提交版本（含待审/通过/驳回），只读
  - **揭榜侠**（本令有揭榜关系，含已 `QUIT`）：仅返回本人 `claimId` 下的版本
  - 路人 / 非参与者 → `40300`
  - 协作中 / 待验收 / 终态只读阶段 **均可**拉取（由 `capabilities.canViewSubmissions` 控制入口；API 仍按上身份校验）
- **Query**：`page=1&pageSize=20`（可选）；`claimId=` 可选过滤（仅令主有效）
- **Response `data`**

```json
{
  "list": [
    {
      "submissionId": 501,
      "bountyId": 88,
      "claimId": 33,
      "claimerUserId": 20,
      "claimerNickname": "揭榜乙",
      "versionNo": 2,
      "status": "APPROVED",
      "summary": "已完成两套房带看",
      "createdAt": "2026-08-07T15:00:00+08:00",
      "reviewedAt": "2026-08-07T16:00:00+08:00",
      "reviewReason": null
    }
  ],
  "total": 1,
  "page": 1,
  "pageSize": 20
}
```

- **列表业务必见**：提交人、提交时间、审核状态、摘要（可截断）；**列表项必须可下钻** → `GET /submissions/{submissionId}`（§8.4）
- **`status` 枚举**：`PENDING` | `APPROVED` | `REJECTED`
- **说明**：列表可不返回 `items`（减负）；正文以详情为准。禁止前端用列表摘要冒充「已看详情」。

### 8.3 某揭榜关系的成果版本列表

- **GET** `/bounties/{id}/claims/{claimId}/submissions`
- **权限**：令主，或该 `claimId` 所属揭榜侠
- **说明**：保留；令主也可用 §8.2 总览。建议列表项字段与 §8.2 对齐（至少含 `submissionId`/`status`/`summary`/`createdAt`）

### 8.4 成果详情（C 端下钻 · **v1.8.17 必补正文**）

- **GET** `/submissions/{submissionId}`
- **鉴权**：登录侠士
- **权限**：
  - 本令 **令主**；或
  - **提交人**（`claimerUserId`）；或
  - 执事堂/Admin 请走 §15.3.1 / §16.12.2（本路径为侠士 Token）
  - 其它 → `40300`；不存在 → `40400`
- **Response `data`**：完整 **§8.0 `SubmissionDetail`**（须含 `items[].text` / `mediaUrls` 等正文）
- **只读**：本接口无写副作用；终态/待分配阶段均可读（权限满足时）

---

## 9. 结算与评价（P0）

### 9.1 预览可分配池

- **GET** `/bounties/{id}/settlement/preview`
- **权限**：令主；且 `capabilities.canSettle=true`（含 **取消分支** `cancelAllocationPending=true`）
- **Response data**

```json
{
  "bountyId": 88,
  "settlementKind": "CANCEL_ALLOCATE",
  "rewardB": 350,
  "feeRate": 0.1,
  "fee": 35,
  "distributable": 315,
  "cancelAllocationPending": true,
  "claimants": [
    {
      "userId": 20,
      "nickname": "揭榜乙",
      "submissionCount": 2,
      "approvedSubmissionCount": 1
    }
  ]
}
```

| 字段 | 说明 |
|------|------|
| `settlementKind` | `COMPLETE`=正常完结分配；`CANCEL_ALLOCATE`=有成果取消后的分配 |
| `claimants[].submissionCount` | 该侠任意状态提交条数（含待审/驳回） |
| `claimants[].approvedSubmissionCount` | 审核通过条数 |

- **候选人规则**：
  - `COMPLETE`：建议仅列出有 **审核通过** 成果的揭榜侠（与既有完结口径一致）
  - `CANCEL_ALLOCATE`：**凡 `submissionCount≥1` 的揭榜侠均可分**（**不以审核通过为门槛**，对齐 §6.34.3）

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
- **前置**：
  - `COMPLETE`：至少 1 名揭榜人且 ≥1 条审核通过成果
  - `CANCEL_ALLOCATE`：`cancelAllocationPending=true`；仅可分给有任意提交记录的侠士；分完后悬赏 **`status=CANCELLED`**（**不是** `COMPLETED`），并清除 `cancelAllocationPending`
- **成功 Response `data`（示例 · 取消分支）**

```json
{
  "settlementId": 90,
  "bountyId": 88,
  "settlementKind": "CANCEL_ALLOCATE",
  "status": "CANCELLED",
  "fee": 35,
  "distributable": 315
}
```

- **错误**：`42003`；状态不允许 → `40002`；取消待分配被误当作其它操作 → **`43011`**

### 9.3 令主主动取消（v1.8.17 · 有成果禁止直接全额退）

- **POST** `/bounties/{id}/cancel`
- **Body**

```json
{
  "reason": "已租到"
}
```

- **允许状态**：`PENDING_REVIEW` | `OPEN` | `IN_COLLAB` | `PENDING_SETTLE`  
  - 已 `cancelAllocationPending` / 终态 → `40002`
- **原因**：`IN_COLLAB` / `PENDING_SETTLE` **必填**非空 `reason`；其余可空（服务端可默认「令主取消」）
- **「有成果」判定**：本令存在 **任意一条** `submission` 记录（`PENDING`/`APPROVED`/`REJECTED` 均算）；**不以审核通过为门槛**

#### 9.3.1 分支与响应（前端必须按 `cancelOutcome` 分流）

| 分支 | 条件 | 资金 | 状态副作用 | `cancelOutcome` |
|------|------|------|------------|-----------------|
| 全额退 | 无任何成果提交 | 全额解冻退回令主 | `status=CANCELLED`；`cancelAllocationPending=false` | **`REFUND`** |
| 须分配 | ≥1 条成果提交，且状态为 `IN_COLLAB`/`PENDING_SETTLE` | **禁止**全额解冻；资金保持冻结 | 置 `cancelAllocationPending=true`；建议将主状态规范为 `PENDING_SETTLE`（若原已是则可保持）；**此时不得**直接 `CANCELLED` | **`ALLOCATE`** |

**有成果时成功 Response `data`**：

```json
{
  "bountyId": 88,
  "status": "PENDING_SETTLE",
  "cancelOutcome": "ALLOCATE",
  "hasSubmissions": true,
  "cancelAllocationPending": true,
  "settlementRequired": true
}
```

**无成果时成功 Response `data`**：

```json
{
  "bountyId": 88,
  "status": "CANCELLED",
  "cancelOutcome": "REFUND",
  "hasSubmissions": false,
  "cancelAllocationPending": false,
  "settlementRequired": false
}
```

| 字段 | 说明 |
|------|------|
| `cancelOutcome` | **`ALLOCATE`** \| **`REFUND`**（必返；前端路由/文案以此为准） |
| `hasSubmissions` | 是否按「有成果」判定命中 |
| `cancelAllocationPending` | 是否进入取消分支待分配 |
| `settlementRequired` | 等价于 `cancelOutcome=ALLOCATE`；便于布尔判断 |
| `status` | 取消接口执行后的悬赏状态 |

- **前端**：二次确认文案——有成果须提示「已有成果，须分配」；**禁止**在有成果时表现为「一键取消即退款」。有成果取消后跳转分配页（复用 §9.1/§9.2）。
- **后端硬拦**：有成果却执行全额退 → **`43010`**；待分配未完成又企图重复取消/错分支 → **`40002`** / **`43011`**
- **其它**：`PENDING_REVIEW`/`OPEN` 无成果场景走 `REFUND`；超时自动取消 / 武林盟强制关闭资金口径仍服从 requirements §6.23（本接口针对 **令主主动取消**）
- **错误**：终态或不允许 → `40002`；协作中/待验收缺原因 → `40001`；**`43010`**

### 9.4 提交互评

- **POST** `/bounties/{id}/evaluations`
- **Body**：`{ "toUserId": 2, "score": 5, "content": "靠谱" }`  
- **说明**：结算完成后开放；更新好评率与声望。**取消分支分配完成（`CANCELLED`）不开放互评**（与 §6.23.3 一致）

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
| `bizId` | 与 `bizType` 对应的业务主键；`BOUNTY` 时为 **悬赏 id** |

**文案与跳转约定（悬赏相关）**

- 成果审核通过/驳回、发令审核等：`bizType=BOUNTY`，`bizId=<bountyId>`；`content` 须含悬赏标题，格式 **`悬赏「{title}」…`**（禁止仅写 `悬赏#id`）。
- 前端站内消息：书名号内标题可点，跳转 **`/bounties/{bizId}?from=mine`**，详情面包屑为「我的悬赏 / 悬赏详情」。

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

## 14.5 用户反馈（侠士端 · v1.8.16 / P0）

> 对齐需求 §6.32。登录侠士可提交；执事堂用户共用本套接口，不另做表单。  
> **本期不做**：C 端公开回复墙、工单派职司、邮件/短信自动回访。  
> 附件图复用 **§3.3** `POST /files/upload`，将返回的 `url` 填入 `attachmentUrls`（最多 3 个）。

### 14.5.0 枚举

**`type`（反馈类型）**

| 值 | 展示名（建议） |
|----|----------------|
| `BUG` | 缺陷反馈 |
| `SUGGEST` | 功能建议 |
| `COMPLAINT` | 投诉举报 |
| `OTHER` | 其他 |

**`status`（处理状态）**

| 值 | 含义 |
|----|------|
| `NEW` | 待处理 |
| `PROCESSING` | 处理中 |
| `RESOLVED` | 已完结 |
| `CLOSED` | 已关闭（无效/重复等） |

MVP 流转：`NEW` → `PROCESSING` → `RESOLVED`；任意非终态可直接改为 `CLOSED`；`RESOLVED`/`CLOSED` 为终态（再改 → `40002`，除非超管运维特例——本期不开放回退）。

### 14.5.1 提交反馈

- **POST** `/feedbacks`
- **鉴权**：侠士登录（未登录 → `40100`）
- **错误码**：`40001`（校验失败）、`40100`/`40101`、`45001`（短时限流）、`45002`（日上限）、`50000`

**Body**

```json
{
  "type": "BUG",
  "title": "揭榜按钮偶发无响应",
  "content": "在悬赏详情页连续点击揭榜，偶发无任何提示。已清缓存复现。",
  "contact": "微信：jianghu_hero",
  "relatedRef": "/bounties/88",
  "attachmentUrls": [
    "/files/fb-1.png",
    "/files/fb-2.png"
  ]
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `type` | 是 | `BUG` \| `SUGGEST` \| `COMPLAINT` \| `OTHER` |
| `title` | 是 | 短文本；建议 1～100 字 |
| `content` | 是 | 正文；建议 1～2000 字 |
| `contact` | 否 | 手机/微信号等，便于回访；建议 ≤64 字 |
| `relatedRef` | 否 | 相关页面路径或悬赏 ID 等自由文本；建议 ≤128 字 |
| `attachmentUrls` | 否 | 图片 URL 数组；**最多 3**；须为已上传资源（§3.3）；超限或非法 URL → `40001` |

**Response `data`**

```json
{
  "id": 9001,
  "type": "BUG",
  "title": "揭榜按钮偶发无响应",
  "status": "NEW",
  "createdAt": "2026-08-07T14:20:00+08:00"
}
```

**频控（服务端强制）**

| 规则 | 配置键（`sys_config`，建议） | 默认 | 超限错误码 |
|------|------------------------------|------|------------|
| 同一账号短时冷却 | `feedback.cooldownSeconds` | `60`（1 分钟 1 条） | `45001` |
| 同一账号自然日上限 | `feedback.dailyLimit` | `10` | `45002` |

实现：Redis（或等价）按 `userId` 记冷却与日计数；日界按 `Asia/Shanghai`。

**成功提示文案（产品）**：前端展示「已送达武林盟」；可引导至「我的反馈」。

### 14.5.2 我的反馈列表

- **GET** `/feedbacks?page=1&pageSize=20&status=`
- **鉴权**：侠士登录
- **Query**：`status` 可选，过滤本人记录；仅能查 **自己** 提交的反馈
- **错误码**：`40100`/`40101`、`40001`

**Response `data`**（统一分页）

```json
{
  "list": [
    {
      "id": 9001,
      "type": "BUG",
      "title": "揭榜按钮偶发无响应",
      "status": "PROCESSING",
      "createdAt": "2026-08-07T14:20:00+08:00",
      "updatedAt": "2026-08-07T15:00:00+08:00"
    }
  ],
  "total": 1,
  "page": 1,
  "pageSize": 20
}
```

> C 端列表 **不** 返回 `handleRemark`、管理员信息、联系方式全文（隐私/内部字段）；详情见下。

### 14.5.3 我的反馈详情

- **GET** `/feedbacks/{id}`
- **鉴权**：侠士登录；非本人 → `40400`（防枚举）或 `40300`（实现二选一，推荐 **40400**）
- **错误码**：`40100`/`40101`、`40400`

**Response `data`**

```json
{
  "id": 9001,
  "type": "BUG",
  "title": "揭榜按钮偶发无响应",
  "content": "在悬赏详情页连续点击揭榜，偶发无任何提示。已清缓存复现。",
  "contact": "微信：jianghu_hero",
  "relatedRef": "/bounties/88",
  "attachmentUrls": ["/files/fb-1.png", "/files/fb-2.png"],
  "status": "PROCESSING",
  "createdAt": "2026-08-07T14:20:00+08:00",
  "updatedAt": "2026-08-07T15:00:00+08:00"
}
```

规则：C 端详情 **不** 暴露 `handleRemark`、处理人等内部字段（本期无公开回复）。

---

## 15. 执事堂（L1，P0）

> 需有效职司；回避规则服务端强制。

### 15.1 待审发令队列（令审使）

- **GET** `/hall/bounty-reviews?status=PENDING&page=&pageSize=`
- **回避与 `total`**：排除本人发布、本人揭榜的令；**`total` = 当前职司可见条数**（与 `list` 同源过滤后再分页），不得把回避项计入统计。

### 15.2 审核发令

- **POST** `/hall/bounty-reviews/{bountyId}`
- **Body**：`{ "result": "APPROVE | REJECT", "reason": "赏银与难度不符" }`

### 15.3 待审成果队列（验功使）

- **GET** `/hall/submission-reviews?status=PENDING&page=&pageSize=`
- **Query `status`**：`PENDING`（默认）| `APPROVED` | `REJECTED` | `REVIEWED`（`APPROVED`∪`REJECTED`）
- **回避与 `total`**：排除本人提交、本人发布悬赏、本人揭榜悬赏下的成果；**`total` = 当前职司可见条数**（与 `list` 同源过滤后再分页）。首页「待审成果」、导航红点须使用该 `total`，不得与队列可见数不一致。
- **列表项字段（必返）**

```json
{
  "list": [
    {
      "submissionId": 501,
      "bountyId": 88,
      "bountyTitle": "红花岗两室求租核验",
      "claimId": 33,
      "claimerUserId": 20,
      "claimerNickname": "揭榜乙",
      "versionNo": 2,
      "status": "PENDING",
      "summary": "已完成两套房带看",
      "createdAt": "2026-08-07T15:00:00+08:00"
    }
  ],
  "total": 1,
  "page": 1,
  "pageSize": 20
}
```

> 旧键 `id`/`userId` 若仍返回，须同时返回 `submissionId`/`claimerUserId`。

#### 15.3.1 成果审核详情（正文必见 · v1.8.17）

- **GET** `/hall/submission-reviews/{submissionId}`
- **鉴权**：有效验功使职司；回避规则同审核
- **Response `data`**：完整 **§8.0 `SubmissionDetail`**
- **说明**：对齐 requirements §6.19 / §6.34.2——点进详情须能看清单填写与图片附件后再审；禁止仅摘要

### 15.4 审核成果

- **POST** `/hall/submission-reviews/{submissionId}`
- **Body**：`{ "result": "APPROVE | REJECT", "reason": "...", "itemComments": [] }`
- **规则**：`REJECT` 须非空 `reason`；职司不可改判已审（管理员改判见 §16.12.3）
- **成功**：可返回精简 `{ "submissionId", "status" }`，或以更新后的 §8.0 详情为准（二选一须在实现中统一；推荐带回 `status`）

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
| `submission:read` | 成果审核列表/详情（只读） | `GET /admin/submission-reviews**` |
| `submission:review` | 成果审核通过/驳回（含改判） | `POST /admin/submission-reviews/{submissionId}` |
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
| `feedback:read` | 用户反馈列表/详情 | `GET /admin/feedbacks**` |
| `feedback:write` | 用户反馈改状态/处理备注 | `PUT /admin/feedbacks/{id}/status` |
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
  "submission:read", "submission:review",
  "wallet:read", "wallet:flag",
  "dispute:read",
  "notice:read", "notice:write",
  "office:read", "office:write",
  "lord:read", "lord:write",
  "config:read", "config:write",
  "product:read", "product:write",
  "checklist:read", "checklist:write",
  "warrant_config:read", "warrant_config:write",
  "audit:read", "report:read", "report:write", "feedback:read", "feedback:write", "job:read",
  "admin:read", "role:read", "menu:read"
]
```

**表 B · `ARBITER` 默认集**：

```json
[
  "dashboard:view",
  "user:read",
  "bounty:read", "bounty:review",
  "submission:read", "submission:review",
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
  "user:read", "invite:read", "bounty:read", "submission:read", "wallet:read", "dispute:read",
  "notice:read", "office:read", "lord:read", "config:read",
  "product:read", "checklist:read", "warrant_config:read",
  "audit:read", "report:read", "feedback:read", "job:read",
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
7. **审计**：`admin:write` / `role:write` / `menu:write` / `user:asset_adjust` / `dispute:verdict` / `lord:write` / `office:write` / `feedback:write` / `submission:review` 成功后写 `audit_log`。

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

> 对齐需求 v1.8.11 §6.29.1；字段以 `AdminUserController` / `AdminUserService` 为准。

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/admin/users?page=1&pageSize=20&keyword=&status=` | `user:read` | 列表筛选 |
| GET | `/admin/users/{id}` | `user:read` | 详情（资料/等级/资产/钱庄；见裁定） |
| POST | `/admin/users/{id}/disable` | `user:write` | 禁用 → `DISABLED` |
| POST | `/admin/users/{id}/enable` | `user:write` | 启用 → `ACTIVE` |
| POST | `/admin/users/{id}/ban` | `user:write` | 封禁 → `BANNED` |
| POST | `/admin/users/{id}/unban` | `user:write` | 解封 → `ACTIVE` |
| PUT | `/admin/users/{id}/remark` | `user:write` | 运营备注 |
| POST | `/admin/users/{id}/assets/adjust` | `user:asset_adjust` | 手工调账（审计） |
| GET | `/admin/users/{id}/login-logs?page=1&pageSize=20` | `user:read` | 登录日志分页 |
| GET | `/admin/users/{id}/real-name` | `user:real_name` | 实名查看 |
| PUT | `/admin/users/{id}/real-name` | `user:real_name` | 实名状态维护 |

**`GET /admin/users` 查询参数**：

| 参数 | 说明 |
|------|------|
| `keyword` | 可选；模糊匹配 **`user.username` / `user.phone` / `user_profile.nickname`**（任一命中即返回） |
| `status` | 可选；`ACTIVE` / `DISABLED` / `BANNED` |
| `page` / `pageSize` | 分页，默认 `1` / `20` |

列表项至少含：`id`、`username`、`phone`、`nickname`、`status`、`city`、`level`、`levelTitle`、`createdAt`。

#### 16.3.1 详情 `GET /admin/users/{id}`

**Response `data` 示例**（camelCase；嵌套对象按后端实体序列化）：

```json
{
  "id": 1001,
  "username": "xiaxia01",
  "phone": "13800138000",
  "status": "ACTIVE",
  "remark": "客服备注：已核验",
  "level": 3,
  "levelTitle": "三流侠士",
  "profile": {
    "id": 2001,
    "userId": 1001,
    "nickname": "青衫客",
    "avatarUrl": "https://cdn.example.com/a.png",
    "bio": "江湖行走",
    "realName": "张三",
    "idNumber": "110101199001011234",
    "realNameStatus": "VERIFIED",
    "createdAt": "2026-08-01T10:00:00",
    "updatedAt": "2026-08-05T12:00:00"
  },
  "asset": {
    "id": 3001,
    "userId": 1001,
    "chivalry": 120,
    "stamina": 8,
    "staminaDate": "2026-08-07",
    "completedOrders": 5,
    "goodRate": 0.96,
    "reputationScore": 86.5,
    "createdAt": "2026-08-01T10:00:00",
    "updatedAt": "2026-08-07T09:00:00"
  },
  "wallet": {
    "balance": 450.00,
    "frozen": 50.00,
    "currency": "两",
    "simulated": true,
    "rechargeEnabled": false,
    "withdrawEnabled": false
  }
}
```

| 顶层字段 | 类型 | 说明 |
|----------|------|------|
| `id` / `username` / `phone` / `status` / `remark` | — | 账号基本信息；`status`=`ACTIVE`\|`DISABLED`\|`BANNED` |
| `level` / `levelTitle` | number / string | 由侠义值推导 |
| `profile` | object | `user_profile` 全量；含昵称/头像/简介及实名原始字段 |
| `asset` | object | 侠义/体力/**完成单**/**好评率**/**声望分**（`reputationScore`） |
| `wallet` | object | 钱庄视图：`balance`/`frozen`/`currency`/`simulated`/`rechargeEnabled`/`withdrawEnabled` |

**裁定（v1.8.11 / 对齐 `AdminUserService.detail`）**：

| 区块 | 是否进详情 | 说明 |
|------|------------|------|
| 声望 / 完成单 / 好评率 | **是** | 经嵌套 `asset.reputationScore` / `asset.completedOrders` / `asset.goodRate` |
| 职司（持有/申请） | **否** | 本期详情**不**嵌入 `offices`/`officeHolders`；前端可链到职司管理（§16.8） |
| 邀请关系 | **否** | 本期详情**不**嵌入邀请树；走 §16.4 `/admin/invites` |

#### 16.3.2 备注 `PUT /admin/users/{id}/remark`

**Body**：

```json
{
  "remark": "客服备注：已核验"
}
```

**Response `data`**：`null`（`code=0`）。无 `remark` 键时按空/null 写入（以实现为准）。

#### 16.3.3 资产调账 `POST /admin/users/{id}/assets/adjust`

**Body**：

```json
{
  "assetType": "BALANCE",
  "delta": 10,
  "reason": "客服补偿"
}
```

| 字段 | 说明 |
|------|------|
| `assetType` | `BALANCE` \| `CHIVALRY` \| `STAMINA`（大小写不敏感，契约示例用大写） |
| `delta` | number；余额为金额，侠义/体力为整数增量（可负） |
| `reason` | 必填；审计留痕 |

#### 16.3.4 登录日志 `GET /admin/users/{id}/login-logs`

**Query**：`page`（默认 1）、`pageSize`（默认 20）

**Response `data`**（分页）：

```json
{
  "list": [
    {
      "id": 9001,
      "userId": 1001,
      "adminId": null,
      "ip": "203.0.113.10",
      "userAgent": "Mozilla/5.0 …",
      "result": "SUCCESS",
      "createdAt": "2026-08-07T08:30:00"
    }
  ],
  "total": 1,
  "page": 1,
  "pageSize": 20
}
```

| 列表项字段 | 说明 |
|------------|------|
| `id` | 日志主键 |
| `userId` | 侠士用户 ID |
| `adminId` | 管理员登录时可为非空；侠士端登录多为 `null` |
| `ip` / `userAgent` | 客户端信息 |
| `result` | 登录结果字符串（如 `SUCCESS` / `FAIL`，以实现落库值为准） |
| `createdAt` | 时间 |

#### 16.3.5 实名 `GET/PUT /admin/users/{id}/real-name`

**GET Response `data`**：

```json
{
  "realName": "张三",
  "idNumber": "110101199001011234",
  "status": "VERIFIED"
}
```

> 注意：此处响应键为 **`status`**（取值来自 `user_profile.real_name_status`），与详情里 `profile.realNameStatus` 同源；前端展示实名分区优先用本接口（需 `user:real_name`）。

**PUT Body**：

```json
{
  "status": "VERIFIED"
}
```

**PUT Response `data`**：

```json
{
  "status": "VERIFIED"
}
```

`status` 枚举（与 §3.2 实名一致，含注册初始值）：`NONE` | `PENDING` | `VERIFIED` | `REJECTED`。

### 16.4 邀请管理

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/invites` | 邀请码/关系查询 |
| POST | `/admin/invites` | 批量生成 |
| POST | `/admin/invites/{id}/invalidate` | 失效 |

### 16.5 悬赏与双审核（索引）

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/admin/bounties` | `bounty:read` | 列表 |
| GET | `/admin/bounties/{id}` | `bounty:read` | 详情含揭榜/提交摘要；内嵌成果须可下钻 §16.12.2 |
| POST | `/admin/bounties/{id}/force-close` | `bounty:write` | 强制关闭/下架 |
| POST | `/admin/bounty-reviews/{bountyId}` | `bounty:review` | 发令审核（可改判） |
| GET | `/admin/submission-reviews` | `submission:read` | **独立成果审核列表**（§16.12.1） |
| GET | `/admin/submission-reviews/{submissionId}` | `submission:read` | **成果详情正文**（§16.12.2） |
| POST | `/admin/submission-reviews/{submissionId}` | `submission:review` | 通过/驳回（可改判）（§16.12.3） |
| GET | `/admin/bounties/{id}/messages` | `bounty:read` | 会话抽检 |

> **裁定（v1.8.17）**：Admin 成果审核独立入口 path = **`/admin/submission-reviews`**（与执事堂 `/hall/submission-reviews` 语义对齐）。**不采用** `/admin/submissions`。详情见 **§16.12**。

### 16.6 钱庄与流水

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/admin/wallet/ledgers` | 托管/结算/退款/提现流水 |
| GET | `/admin/wallet/fee-summary` | 服务费汇总 |
| POST | `/admin/wallet/ledgers/{id}/flag` | 异常标记 |

### 16.7 纠纷仲裁（终裁）

> 对齐需求 v1.8.11 §6.29.3；字段以 `AdminDisputeController` / `AdminDisputeService` 为准。  
> 前端须**结构化渲染**本节约定字段，禁止整包 JSON dump。

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/admin/disputes?page=1&pageSize=20` | `dispute:read` | 队列 |
| GET | `/admin/disputes/{id}` | `dispute:read` | 详情举证 |
| POST | `/admin/disputes/{id}/verdict` | `dispute:verdict` | 裁决执行 |

#### 16.7.1 队列项 / 列表

列表项字段（`brief`）：

```json
{
  "id": 501,
  "bountyId": 88,
  "status": "OPEN",
  "reason": "成果与约定不符"
}
```

`status` 枚举：`OPEN` | `CLOSED`。

#### 16.7.2 详情 `GET /admin/disputes/{id}`

**Response `data` 示例**：

```json
{
  "id": 501,
  "bountyId": 88,
  "status": "OPEN",
  "reason": "成果与约定不符",
  "evidenceJson": "{\"text\":\"截图见附件\",\"urls\":[\"https://cdn.example.com/e1.png\"]}",
  "verdictJson": null,
  "settlementId": 77,
  "initiatorId": 1001,
  "deadlineAt": "2026-08-14T12:00:00",
  "createdAt": "2026-08-07T12:00:00"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | number | 纠纷 ID |
| `bountyId` | number | 关联悬赏 ID（**不**内嵌 `bounty` 对象；前端可链 `/admin/bounties/{bountyId}` 或再拉 §16.5） |
| `status` | string | `OPEN` \| `CLOSED` |
| `reason` | string | 发起原因 |
| `evidenceJson` | string \| null | 举证 **JSON 字符串**（见下表内层 schema） |
| `verdictJson` | string \| null | 已结案时为裁决 JSON 字符串；未结案多为 `null` |
| `settlementId` | number | 关联结算单 |
| `initiatorId` | number | 发起人用户 ID |
| `deadlineAt` / `createdAt` | string | 截止 / 创建时间 |

**`evidenceJson` 内层 schema**（与 C 端 `DisputeService.create` 写入一致；前端解析后展示，勿整包 dump）：

```json
{
  "text": "截图见附件",
  "urls": ["https://cdn.example.com/e1.png"]
}
```

| 内层键 | 对应发起 Body（§13.1） | 说明 |
|--------|------------------------|------|
| `text` | `evidenceText` | 举证说明 |
| `urls` | `evidenceUrls` | 图片/文件 URL 列表 |

**`verdictJson` 内层 schema**（结案后由终裁写入，MVP）：

```json
{
  "action": "KEEP",
  "comment": "维持原结算",
  "at": "2026-08-07T15:00:00"
}
```

#### 16.7.3 终裁 `POST /admin/disputes/{id}/verdict`

**Body 示例**：

```json
{
  "action": "KEEP",
  "comment": "维持原结算",
  "reallocations": [],
  "punishments": []
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `action` | 是 | `KEEP` \| `REALLOCATE` \| `REFUND` \| `PUNISH`（服务端 trim + 大写） |
| `comment` | 建议必填 | 裁决说明；缺省按 `""`；产品口径终裁须留痕 |
| `reallocations` | `REALLOCATE` 时使用 | 数组，项形见下；其它 action 可传 `[]` |
| `punishments` | `PUNISH` 时使用 | 数组，项形见下；其它 action 可传 `[]` |

**`reallocations[]` 项形**：

```json
{
  "userId": 1002,
  "amount": 80.00
}
```

| 项字段 | 类型 | 说明 |
|--------|------|------|
| `userId` | number | 再分配目标用户 |
| `amount` | number | 金额（两） |

**`punishments[]` 项形**：

```json
{
  "userId": 1002,
  "type": "REPUTATION_DEDUCT",
  "value": 10
}
```

| 项字段 | 类型 | 说明 |
|--------|------|------|
| `userId` | number | 处罚对象 |
| `type` | string | `REPUTATION_DEDUCT`（扣声望）\| `BAN`（封禁） |
| `value` | number | 扣减幅度等参数；`BAN` 可为 `0` |

**action 语义与 MVP 执行深度（`AdminDisputeService`）**：

| action | 含义 | 当前实现 |
|--------|------|----------|
| `KEEP` | 维持原结算 | 写 `verdictJson`，纠纷 `CLOSED`，悬赏若 `IN_DISPUTE` → `COMPLETED` |
| `REALLOCATE` | 调整分配 | **同结案路径**；`reallocations` **契约保留项形**，MVP **尚未按项执行资金**（前端仍应按表单收集并提交） |
| `REFUND` | 退回 | 简化：尝试对令主调账退 `settlement.rewardB` + 结案；深度资金回滚后续批次 |
| `PUNISH` | 处罚 | **同结案路径**；`punishments` **契约保留项形**，MVP **尚未按项执行处罚** |

仅 `status=OPEN` 可裁决；否则 `40002`（业务规则：纠纷已结案）。`action` 缺失/非法 → `40001`。

**Response `data`**：`null`（`code=0`）。

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
| GET | `/admin/audit-logs` | 审计日志（分页；可选 `operator`/`action`/`keyword` 筛选） |
| GET | `/admin/office-metrics` | 职司驳回率/改判率等 |
| GET | `/admin/reports` | 举报列表（若有）处理 POST |
| 见 §16.11 | `/admin/feedbacks` | 用户反馈列表/详情/改状态（v1.8.16） |

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

**种子菜单覆盖核对**（`patch_rbac.sql` / 空表种子，对齐 v1.8.17）：

| 侧栏/路由 | 种子 `path` | 结论 |
|-----------|-------------|------|
| 工作台 / 侠士 / 邀请 / 悬赏 / 钱庄 / 纠纷 / 告示 / 职司 / 盟主 / 运营配置子页 / 系统配置 | 已有对应 `MENU` | 已覆盖 |
| RBAC：`/admin/admins`、`/admin/roles`、`/admin/menus` | id 20–22（父 DIR「权限管理」） | **已覆盖** |
| **用户反馈** `/admin/feedbacks` | **须新增 MENU**（见下） | **v1.8.16 必补** |
| **成果审核** `/admin/submission-reviews` | **须新增 MENU**（见下） | **v1.8.17 必补** |
| 二级详情 `/admin/users/:id`、`/admin/disputes/:id`、`/admin/feedbacks/:id`、`/admin/submission-reviews/:id` | **无独立 MENU 节点** | **不要求种子侧栏项**；由前端挂在对应列表下钻 |
| 按钮：资产调账 / 终裁 / 反馈改状态 / 成果审核操作 | `user:asset_adjust` / `dispute:verdict` / `feedback:write` / `submission:review` | 反馈与成果按钮 **须补** |

种子菜单须覆盖前端现有 `/admin/*` **一级侧栏**路由（含 RBAC 三页、用户反馈、**成果审核**）；二级详情路由不进侧栏种子。

**用户反馈菜单种子要点（v1.8.16）**

| 项 | 约定 |
|----|------|
| 推荐形态 | 一级侧栏 `MENU`「用户反馈」（与「告示管理」同级）；亦可挂在新建 DIR「内容与风控」下，与告示并列 |
| `path` | `/admin/feedbacks` |
| `component` | `admin/FeedbacksAdminView`（实现名可等价，须可路由到列表页） |
| `permissionCode`（菜单可见） | `feedback:read` |
| 按钮节点（可选） | `type=BUTTON`，`name=改状态`，`permissionCode=feedback:write` |
| 详情路由 | `/admin/feedbacks/:id` **不进**侧栏种子 |

**权限字典种子**：须插入 `feedback:read` / `feedback:write`；`OPS_ADMIN` 默认含读写；`OBSERVER` 默认仅 `feedback:read`；`ARBITER` 默认不含（非仲裁域）。

**成果审核菜单种子要点（v1.8.17）**

| 项 | 约定 |
|----|------|
| 推荐形态 | 挂在「悬赏管理」同级，或父 DIR「悬赏与协作」下的独立 `MENU`「成果审核」 |
| `path` | **`/admin/submission-reviews`**（**已裁定**；禁止用 `/admin/submissions`） |
| `component` | `admin/SubmissionReviewsAdminView`（实现名可等价） |
| `permissionCode`（菜单可见） | **`submission:read`** |
| 按钮节点（可选） | `type=BUTTON`，`name=通过驳回`，`permissionCode=submission:review` |
| 详情路由 | `/admin/submission-reviews/:submissionId` **不进**侧栏种子 |

**权限字典种子（增量）**：须插入 **`submission:read`**（已有 `submission:review` 保留）；`OPS_ADMIN` / `ARBITER` 默认含 `submission:read`+`submission:review`；`OBSERVER` 默认仅 `submission:read`。

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

### 16.11 用户反馈管理（武林盟 · v1.8.16 / P0）

> 对齐需求 §6.32.2。反馈可能含隐私，仅持 `feedback:read` / `feedback:write`（或 `*`）的管理员可见；职司默认无权限。  
> 枚举同 §14.5.0。状态变更须写 `audit_log`（谁、何时、从何状态到何状态）。

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/admin/feedbacks` | `feedback:read` | 分页列表 + 筛选 |
| GET | `/admin/feedbacks/{id}` | `feedback:read` | 详情（含处理备注） |
| PUT | `/admin/feedbacks/{id}/status` | `feedback:write` | 改状态 / 写处理备注 |

#### 16.11.1 列表 `GET /admin/feedbacks`

- **Query**：`page`、`pageSize`、`status`（可选）、`type`（可选）、`keyword`（可选，模糊匹配标题 / 提交人昵称 / 提交人 ID 字符串）
- **错误码**：`40100`/`40101`、`40300`、`40001`

**Response `data`**（统一分页）

```json
{
  "list": [
    {
      "id": 9001,
      "type": "BUG",
      "title": "揭榜按钮偶发无响应",
      "status": "NEW",
      "submitterId": 1001,
      "submitterNickname": "青衫客",
      "createdAt": "2026-08-07T14:20:00+08:00",
      "updatedAt": "2026-08-07T14:20:00+08:00"
    }
  ],
  "total": 1,
  "page": 1,
  "pageSize": 20
}
```

| 字段 | 说明 |
|------|------|
| `submitterId` | 提交人用户 ID |
| `submitterNickname` | 提交人昵称（无则空串或用户名，实现统一一种） |
| `type` / `status` | 见 §14.5.0 |

列表可不返回 `content` / `attachmentUrls` / `handleRemark`（详情再给）。

#### 16.11.2 详情 `GET /admin/feedbacks/{id}`

- **错误码**：`40100`/`40101`、`40300`、`40400`

**Response `data`**

```json
{
  "id": 9001,
  "type": "BUG",
  "title": "揭榜按钮偶发无响应",
  "content": "在悬赏详情页连续点击揭榜，偶发无任何提示。已清缓存复现。",
  "contact": "微信：jianghu_hero",
  "relatedRef": "/bounties/88",
  "attachmentUrls": ["/files/fb-1.png", "/files/fb-2.png"],
  "status": "PROCESSING",
  "handleRemark": "已复现，排期修复",
  "submitterId": 1001,
  "submitterNickname": "青衫客",
  "statusChangedAt": "2026-08-07T15:00:00+08:00",
  "statusChangedByAdminId": 2,
  "statusChangedByAdminName": "运营甲",
  "createdAt": "2026-08-07T14:20:00+08:00",
  "updatedAt": "2026-08-07T15:00:00+08:00",
  "statusHistory": [
    {
      "fromStatus": null,
      "toStatus": "NEW",
      "adminId": null,
      "adminName": null,
      "remark": null,
      "at": "2026-08-07T14:20:00+08:00"
    },
    {
      "fromStatus": "NEW",
      "toStatus": "PROCESSING",
      "adminId": 2,
      "adminName": "运营甲",
      "remark": "已复现，排期修复",
      "at": "2026-08-07T15:00:00+08:00"
    }
  ]
}
```

| 字段 | 说明 |
|------|------|
| `handleRemark` | 处理备注，**仅 Admin 可见**；C 端不返回 |
| `statusChangedAt` / `statusChangedByAdminId` / `statusChangedByAdminName` | 最近一次状态变更审计摘要；新建时可为 `null` |
| `statusHistory` | 状态变更轨迹（建议实现；至少保证 `audit_log` 可追溯）。`fromStatus=null` 表示创建 |

#### 16.11.3 改状态 `PUT /admin/feedbacks/{id}/status`

- **权限**：`feedback:write`
- **错误码**：`40001`、`40002`（非法流转/已终态）、`40100`/`40101`、`40300`、`40400`

**Body**

```json
{
  "status": "PROCESSING",
  "handleRemark": "已复现，排期修复"
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `status` | 是 | 目标状态：`NEW` \| `PROCESSING` \| `RESOLVED` \| `CLOSED` |
| `handleRemark` | 否 | 处理备注；传入则覆盖更新；不传则保留原备注 |

**合法流转（MVP）**

| 当前 | 允许目标 |
|------|----------|
| `NEW` | `PROCESSING`、`RESOLVED`、`CLOSED` |
| `PROCESSING` | `RESOLVED`、`CLOSED`（允许回到说明：不开放回 `NEW`） |
| `RESOLVED` / `CLOSED` | 无（再改 → `40002`） |

**Response `data`**：同详情结构（更新后的完整对象，可省略冗长 `statusHistory` 但建议带回最新一条）。

**副作用**：

1. 更新 `status`、`handleRemark`、`statusChanged*`、`updatedAt`。  
2. 追加 `statusHistory`（若落库）并写 **`audit_log`**：`action=FEEDBACK_STATUS_CHANGE`，载荷含 `feedbackId`、`fromStatus`、`toStatus`、`adminId`、`at`。  
3. 站内消息通知提交人「已处理」为 **可选增强**，非本期阻断项。

---

### 16.12 成果审核（武林盟独立入口 · v1.8.17 / P0）

> 对齐 requirements §6.34.2 / §9.19。  
> **Path 裁定**：`/admin/submission-reviews`（与 `/hall/submission-reviews` 对齐）。  
> 在既有 `POST /admin/submission-reviews/{submissionId}` 上扩展 **列表 + 详情**；悬赏详情内嵌可保留为快捷入口，但须下钻到 **同一详情契约**（§16.12.2 = §8.0）。

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/admin/submission-reviews` | `submission:read` | 分页列表 + 待审/已审筛选 |
| GET | `/admin/submission-reviews/{submissionId}` | `submission:read` | 完整成果详情 |
| POST | `/admin/submission-reviews/{submissionId}` | `submission:review` | 通过/驳回（管理员可改判） |

#### 16.12.1 列表 `GET /admin/submission-reviews`

- **Query**：
  - `page`、`pageSize`
  - `status`：`PENDING` \| `APPROVED` \| `REJECTED` \| `REVIEWED`（`REVIEWED`=已审=`APPROVED`∪`REJECTED`）；缺省建议 `PENDING`
  - `bountyId`（可选）
  - `keyword`（可选：悬赏标题 / 提交人昵称 / 提交人 ID）
- **错误码**：`40100`/`40101`、`40300`、`40001`

**Response `data`**

```json
{
  "list": [
    {
      "submissionId": 501,
      "bountyId": 88,
      "bountyTitle": "红花岗两室求租核验",
      "claimId": 33,
      "claimerUserId": 20,
      "claimerNickname": "揭榜乙",
      "versionNo": 2,
      "status": "PENDING",
      "summary": "已完成两套房带看",
      "createdAt": "2026-08-07T15:00:00+08:00",
      "reviewedAt": null,
      "reviewReason": null
    }
  ],
  "total": 1,
  "page": 1,
  "pageSize": 20
}
```

列表业务必见：悬赏关联（`bountyId`/`bountyTitle`）、提交人、时间、审核状态。

#### 16.12.2 详情 `GET /admin/submission-reviews/{submissionId}`

- **权限**：`submission:read`
- **Response `data`**：完整 **§8.0 `SubmissionDetail`**（摘要、清单项、图片附件、审核状态/原因、提交时间、提交人、悬赏标识）
- **不存在** → `40400`

悬赏详情 `GET /admin/bounties/{id}` 内嵌成果列表项至少含 `submissionId`，前端跳转本详情；**不得**仅嵌列表且看不到正文即算交付。

#### 16.12.3 审核 `POST /admin/submission-reviews/{submissionId}`

- **权限**：`submission:review`
- **Body**

```json
{
  "result": "APPROVE",
  "reason": null,
  "itemComments": []
}
```

| 字段 | 必填 | 说明 |
|------|------|------|
| `result` | 是 | `APPROVE` \| `REJECT` |
| `reason` | `REJECT` 时必填 | 驳回/改判说明 |
| `itemComments` | 否 | 单项意见数组（MVP 可空 `[]`；项形实现自洽即可，建议 `{ "itemCode", "comment" }`） |

- **规则**：管理员允许对已审成果 **改判**（与执事堂「仅 PENDING」不同）；改判须写 `audit_log`
- **成功 Response `data`**（推荐）

```json
{
  "submissionId": 501,
  "status": "APPROVED",
  "reviewReason": null,
  "reviewedAt": "2026-08-07T16:30:00+08:00"
}
```

- **错误**：`40001`（驳回无原因等）、`40002`（非法）、`40300`、`40400`

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
| 协作会话（双向共享流） | **7.6–7.7**（v1.8.9） |
| 全生命周期能力矩阵（v1.8.10） | **7.9**；详情 `capabilities`；`43008`/`43009` |
| 令主成果总览 / 详情可达（v1.8.17） | **8.0–8.4**；列表 §8.2 + 详情 §8.4 |
| 令种三枚举 + 武侠展示名 | **5.2**，7.1–7.3（`RENT_TRANSFER`） |
| 再发一令（v1.8） | **7.8**；详情/列表 `canRepublish`/`sourceBountyId`；错误码 `43007` |
| 成果多次提交防刷 | 8.x |
| 双审核（职司/管理） | 15.x，**16.12** |
| **Admin 独立成果审核（v1.8.17）** | **16.12** `/admin/submission-reviews`；权限 `submission:read`/`submission:review`；菜单种子同 path |
| **有成果取消硬规则（v1.8.17）** | **9.3** `cancelOutcome`；**9.1/9.2** `settlementKind=CANCEL_ALLOCATE`；错误码 **`43010`/`43011`**；详情 `hasSubmissions`/`cancelAllocationPending` |
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
| 武林盟运营缺口（v1.8.11） | **16.3** 侠士详情/备注/实名/登录日志；**16.7** 纠纷结构化详情+裁决项形；**16.10** RBAC 三页+种子 |
| **用户反馈（v1.8.16）** | **14.5** C 端提交/我的列表详情；**16.11** Admin 列表/详情/改状态；权限 `feedback:read`/`feedback:write`；错误码 `45001`/`45002`；菜单种子 `/admin/feedbacks` |
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
| v1.0.4 | 2026-08-05 | **需求 v1.8.0 再发一令**：① 新增 **§7.8** `GET .../republish-draft` + `POST .../republish`；② 详情/我的发布增加 `sourceBountyId`、`canRepublish`；③ 仅 `REJECTED`/`CANCELLED`/`COMPLETED` 可再发；原单不变、新单 `PENDING_REVIEW`+重新冻结；④ 错误码 **`43007`**；⑤ `POST /bounties` 可选 `sourceBountyId`（等价约束） |
| v1.0.5 | 2026-08-05 | **§16.3** 明确 `GET /admin/users` 的 `keyword`：模糊匹配 `username` / `phone` / **`user_profile.nickname`**；补列表项字段说明 |
| **v1.0.6** | 2026-08-07 | **需求 v1.8.9**：① **§7.6/7.7** 裁定协作会话为悬赏级共享流，禁止按发送方过滤；可读=令主/有揭榜关系，可写=令主/`ACTIVE` 揭榜；响应必含 `senderNickname`；② **新增令种 `RENT_TRANSFER`**，三枚举 + 武侠展示名（租房/出租/转租悬赏）；③ §5.2 三套 warrant-templates（含 `displayName`/`complianceNote`）；④ 详情增加 `typeDisplayName` |
| **v1.0.7** | 2026-08-07 | **需求 v1.8.10 §6.28**：① 新增 **§7.9** 角色×状态 `capabilities` 矩阵；② 详情响应增加 `capabilities`；③ 消息发送/成果提交状态硬拦 → **`43008`/`43009`**；④ 补齐 **§8.2** `GET /bounties/{id}/submissions`（令主看全部成果）；⑤ §9.3 取消允许 `PENDING_SETTLE`；⑥ **§7.5.1** `POST .../claims/quit` 退出揭榜 |
| **v1.0.8** | 2026-08-07 | **需求 v1.8.11 §6.29 / §9.15**：① **§16.3** 补齐 `GET /users/{id}` 详情（`profile`/`asset`/`wallet`/`level*`/`remark`）、`PUT remark`、`login-logs` 列表项、`real-name` GET/PUT；裁定 **声望/完成单/好评率进详情（经 asset）**，**职司/邀请不进详情**；② **§16.7** 补齐详情结构化字段（含 `evidenceJson`/`verdictJson` 内层 schema）、`verdict` 的 `reallocations[{userId,amount}]` / `punishments[{userId,type,value}]` 项形与 MVP 执行深度说明；③ **§16.10** 种子菜单核对：已覆盖 `/admin/admins\|roles\|menus`；`users/:id`、`disputes/:id` 为二级下钻不进侧栏种子 |
| **v1.0.9** | 2026-08-07 | **需求 v1.8.16 §6.32 用户反馈**：① 新增 **§14.5** `POST/GET /feedbacks`、`GET /feedbacks/{id}`（类型 `BUG\|SUGGEST\|COMPLAINT\|OTHER`，状态 `NEW\|PROCESSING\|RESOLVED\|CLOSED`，附件复用 §3.3）；② 新增 **§16.11** `/admin/feedbacks` 列表/详情/`PUT .../status`；③ 错误码 **`45001`/`45002`**；④ RBAC 增 `feedback:read`/`feedback:write` 与 OPS/观察者默认集、审计；⑤ §16.10.3 菜单种子 path=`/admin/feedbacks`；⑥ **面包屑 §6.33 不进本契约**（纯前端） |
| **v1.0.11** | 2026-08-07 | **提交成果必验判定**：§8.0/§8.1 `items[].done` 由服务端按该项是否有 `text`/`mediaUrls` 落库；必验项须有内容，不再依赖「已完成」勾选；错误文案同步。**后台**：`GET /admin/audit-logs` 增可选 `operator`/`action`/`keyword`；侧栏「系统配置」改为「审计日志」页（参数编辑归运营参数） |
| **v1.0.12** | 2026-08-07 | **执事堂 §15.1 / §15.3**：`total` 与队列 `list` 同源应用回避规则后再分页；回避项不计入统计（修红点/首页待审数与队列不一致） |
| **v1.0.13** | 2026-08-08 | **§14.1**：悬赏相关站内信 `bizType=BOUNTY`+标题书名号文案；前端跳转 `/bounties/{id}?from=mine`（成果审核通过/驳回不再用 `SUBMISSION`+`悬赏#id`） |
| **v1.0.10** | 2026-08-07 | **需求 v1.8.17 §6.34**：① **§8.0** 共享 `SubmissionDetail` VO；扩展 **§8.2/§8.4** 列表+详情正文与权限；② **§16.12** Admin 独立成果审核 `GET/POST /admin/submission-reviews`（裁定 path，非 `/admin/submissions`）；增 `submission:read`；菜单种子；③ 执事堂 **§15.3.1** 详情正文；④ **§9.3** 取消响应 `cancelOutcome=ALLOCATE\|REFUND`、`hasSubmissions`/`cancelAllocationPending`；§9.1/9.2 `settlementKind`；详情增 `hasSubmissions`；⑤ 错误码 **`43010`/`43011`**；⑥ §7.9：有成果取消≠禁按钮，`cancelAllocationPending` 时 `canSettle=true` |
