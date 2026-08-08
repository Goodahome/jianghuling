# 江湖互助平台 · Backend（第一批可运行闭环）

Spring Boot 3 + MyBatis-Plus + MySQL + Redis。

对齐文档：`docs/requirements.md`、`docs/architecture.md`、`docs/api.md`。

## 本批范围

已实现：

- 鉴权/邀请注册/登录/资料/上传
- 模拟钱庄（充值/提现/冻结结算账本）
- Meta（赏银建议/令状模板/清单模板）与告示只读
- 悬赏发令/广场/揭榜/会话/成果
- 执事堂双审、结算/取消/互评
- 超时取消退款、截止提醒 Job
- 成长等级/兑体力/奖品兑换（奖品内存种子）
- 站内消息列表与已读
- 侠士端纠纷发起/详情/我的
- 职司 defs / 申请 / 我的职司与申请
- 英雄谱（声望/侠义/完令）与盟主申请
- 管理端：登录、工作台、用户/邀请、悬赏审核、钱庄、纠纷终裁、告示、职司/盟主、系统配置、审计
- 运营配置 CRUD：等级 / 英雄谱规则 / 成长参数 / 赏银建议、清单模板、令状字段、奖品与兑换单、职司 defs
- 管理员完整 RBAC（D-003）：四角色权限、`@RequireAdminPerm` 拦截、`/admin/admins|roles|menus`
- v1.7：注册赠银 500 / 邀新奖 100；充值提现默认关（`42004`）；`GET /messages/unread-count`
- v1.8：再发一令（`republish-draft` / `republish`，`source_bounty_id`，终态复制新建进待审）
- v1.8.9：协作会话共享流 + `senderNickname`；令种 `RENT_TRANSFER` + 三套 warrant/`displayName`；标准告示 N1–N6 全文入库
- v1.8.17：成果详情 §8.0；有成果取消 `cancelOutcome`/`cancel_allocation_pending`；Admin `/admin/submission-reviews`；错误码 `43010`/`43011`

后续简化项：真实短信/支付、纠纷资金完整回滚、英雄谱物化表。

## 环境要求

- JDK 17+（推荐 21）
- Maven 3.9+
- MySQL 8
- Redis 6+



## 初始化数据库

统一用已登录的 `mysql>` 执行 `SOURCE`（路径用正斜杠；后续增量补丁也按此方式）。

先登录：

```powershell
& "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -uroot -p --default-character-set=utf8mb4
```

全新安装：

```sql
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/schema.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/data.sql;
```

已有库增量补丁（按需、按顺序）：

```sql
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_admin_phase2.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_ops_config.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_rbac.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_wallet_v17.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_bounty_republish.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_rent_transfer_v189.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_standard_notices_v189.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_admin_menu_paths_v1813.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_admin_menu_ops_merge_v1815.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_feedback_v1816.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_submission_reviews_v1817.sql;
SOURCE F:/Jinanghu_Ling/backend/src/main/resources/db/patch_admin_menu_audit_logs_v1818.sql;
```

全新安装在 `schema.sql` + `data.sql` 之后，仍需执行 `patch_rbac.sql`（或后续并入 data）以写入四角色/权限/菜单种子；`data.sql` 已含 v1.7 钱庄开关种子时可不重复执行 `patch_wallet_v17.sql`。已有库侧栏运营菜单异常时：先按需执行 `patch_admin_menu_paths_v1813.sql`，再执行 `patch_admin_menu_ops_merge_v1815.sql`（四参合并为一条「运营参数」）。v1.8.17 成果审核与取消待分配：执行 `patch_submission_reviews_v1817.sql`（列 `cancel_allocation_pending` / `settlement.kind`、权限 `submission:read`、菜单 `/admin/submission-reviews`）。v1.8.18「系统配置」改审计日志：执行 `patch_admin_menu_audit_logs_v1818.sql`。

如需执事堂联调，给某侠士授职（替换用户 ID）：

```sql
INSERT INTO user_office (user_id, office_code, status, start_at, end_at) VALUES
(2, 'DECREE_REVIEWER', 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
(2, 'FEAT_REVIEWER', 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY));
```



## 配置

默认 `dev` profile，见 `application-dev.yml`。可用环境变量覆盖：


| 变量               | 默认             |
| ---------------- | -------------- |
| `MYSQL_HOST`     | `127.0.0.1`    |
| `MYSQL_PORT`     | `3306`         |
| `MYSQL_DB`       | `jianghu_ling` |
| `MYSQL_USER`     | `root`         |
| `MYSQL_PASSWORD` | `root`         |
| `REDIS_HOST`     | `127.0.0.1`    |
| `REDIS_PORT`     | `6379`         |
| `JWT_SECRET`     | 开发默认值（生产必改）    |
| `UPLOAD_DIR`     | `./uploads`    |
| `MOCK_SMS_CODE`  | `123456`       |




## 启动

Windows PowerShell（确保 JAVA_HOME 指向 JDK 17+）：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.10'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
mvn -DskipTests spring-boot:run

# 编译
cd F:\Jinanghu_Ling\backend

# 如需指定 JDK
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.10'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"

# 编译并打包（跳过测试，常用）
mvn -DskipTests clean package

# 或跑完测试再打包
mvn clean package
```

健康检查：`GET http://localhost:8080/actuator/health`

## 联调账号


| 角色  | 说明                                                                                     |
| --- | -------------------------------------------------------------------------------------- |
| 侠士  | 邀请码 `JHOPEN1` / `JHOPEN2` 注册；短信验证码固定 `123456`                                          |
| 管理员 | 接口 `POST /api/v1/admin/auth/login`，账号 `admin` / `admin123`（勿用侠士端 `/api/v1/auth/login`） |
| 执事  | 注册后按上文 SQL 授 `DECREE_REVIEWER` / `FEAT_REVIEWER`                                       |




## 主闭环手测路径

1. `POST /api/v1/auth/register`（邀请码 + 短信/密码）
2. `POST /api/v1/wallet/recharge`
3. `POST /api/v1/bounties` 发令 → 状态 `PENDING_REVIEW`，赏银已冻结
4. 执事堂或管理员 `APPROVE` → `OPEN`
5. 另一侠士揭榜 → 会话/提交成果
6. 验功使/管理员通过成果
7. 令主 `POST .../settlement` 分完 90% 池 → `COMPLETED`



## 测试

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.10'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
mvn -q test
```

包含：`WalletServiceTest`（充值/幂等/冻结）、`BountyClaimRulesTest`（自揭/重复揭榜）、`BountyCancelRulesTest`（有成果取消 ALLOCATE / 无成果 REFUND）。