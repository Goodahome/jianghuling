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
- 管理端：登录、工作台、用户启停封禁/调账、发令与成果审核、强制关闭

后续批次（未实现）：成长兑换、英雄谱/盟主、职司申请、纠纷、站内消息列表、完整后台 RBAC/配置 CRUD。

## 环境要求

- JDK 17+（推荐 21）
- Maven 3.9+
- MySQL 8
- Redis 6+

## 初始化数据库

```bash
mysql -uroot -p < src/main/resources/db/schema.sql
mysql -uroot -p < src/main/resources/db/data.sql
```

如需执事堂联调，给某侠士授职（替换用户 ID）：

```sql
INSERT INTO user_office (user_id, office_code, status, start_at, end_at) VALUES
(2, 'DECREE_REVIEWER', 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY)),
(2, 'FEAT_REVIEWER', 'ACTIVE', NOW(), DATE_ADD(NOW(), INTERVAL 90 DAY));
```

## 配置

默认 `dev` profile，见 `application-dev.yml`。可用环境变量覆盖：

| 变量 | 默认 |
|------|------|
| `MYSQL_HOST` | `127.0.0.1` |
| `MYSQL_PORT` | `3306` |
| `MYSQL_DB` | `jianghu_ling` |
| `MYSQL_USER` | `root` |
| `MYSQL_PASSWORD` | `root` |
| `REDIS_HOST` | `127.0.0.1` |
| `REDIS_PORT` | `6379` |
| `JWT_SECRET` | 开发默认值（生产必改） |
| `UPLOAD_DIR` | `./uploads` |
| `MOCK_SMS_CODE` | `123456` |

## 启动

Windows PowerShell（确保 JAVA_HOME 指向 JDK 17+）：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21.0.10'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
mvn -DskipTests spring-boot:run
```

健康检查：`GET http://localhost:8080/actuator/health`

## 联调账号

| 角色 | 说明 |
|------|------|
| 侠士 | 邀请码 `JHOPEN1` / `JHOPEN2` 注册；短信验证码固定 `123456` |
| 管理员 | `admin` / `admin123`（dev 启动时自动创建/重置） |
| 执事 | 注册后按上文 SQL 授 `DECREE_REVIEWER` / `FEAT_REVIEWER` |

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

包含：`WalletServiceTest`（充值/幂等/冻结）、`BountyClaimRulesTest`（自揭/重复揭榜）。
