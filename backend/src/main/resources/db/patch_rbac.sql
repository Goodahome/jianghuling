USE jianghu_ling;

-- admin_user 补 updated_at
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'admin_user' AND COLUMN_NAME = 'updated_at'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE admin_user ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS admin_role (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  code        VARCHAR(32)  NOT NULL,
  name        VARCHAR(64)  NOT NULL,
  builtin     TINYINT(1)   NOT NULL DEFAULT 1,
  description VARCHAR(255) NULL,
  status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_admin_role_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admin_permission (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  code       VARCHAR(64)  NOT NULL,
  name       VARCHAR(128) NOT NULL,
  module     VARCHAR(64)  NOT NULL,
  type       VARCHAR(20)  NOT NULL DEFAULT 'API' COMMENT 'API|MENU|BUTTON',
  UNIQUE KEY uk_admin_perm_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admin_role_permission (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id       BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_perm (role_id, permission_id),
  KEY idx_arp_perm (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admin_user_role (
  id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  admin_id BIGINT NOT NULL,
  role_id  BIGINT NOT NULL,
  UNIQUE KEY uk_admin_role (admin_id, role_id),
  KEY idx_aur_role (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS admin_menu (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id       BIGINT       NOT NULL DEFAULT 0,
  type            VARCHAR(20)  NOT NULL COMMENT 'DIR|MENU|BUTTON',
  name            VARCHAR(64)  NOT NULL,
  path            VARCHAR(128) NULL,
  component       VARCHAR(128) NULL,
  icon            VARCHAR(64)  NULL,
  sort            INT          NOT NULL DEFAULT 0,
  visible         TINYINT(1)   NOT NULL DEFAULT 1,
  permission_code VARCHAR(64)  NULL,
  status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  KEY idx_menu_parent (parent_id, sort)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 权限字典
INSERT INTO admin_permission (code, name, module, type)
SELECT v.code, v.name, v.module, v.type FROM (
  SELECT '*' AS code, '全部权限' AS name, 'system' AS module, 'API' AS type
  UNION ALL SELECT 'dashboard:view', '工作台', 'dashboard', 'API'
  UNION ALL SELECT 'user:read', '侠士只读', 'user', 'API'
  UNION ALL SELECT 'user:write', '侠士写', 'user', 'API'
  UNION ALL SELECT 'user:asset_adjust', '资产调账', 'user', 'API'
  UNION ALL SELECT 'user:real_name', '实名维护', 'user', 'API'
  UNION ALL SELECT 'invite:read', '邀请只读', 'invite', 'API'
  UNION ALL SELECT 'invite:write', '邀请写', 'invite', 'API'
  UNION ALL SELECT 'bounty:read', '悬赏只读', 'bounty', 'API'
  UNION ALL SELECT 'bounty:write', '悬赏强制关闭', 'bounty', 'API'
  UNION ALL SELECT 'bounty:review', '发令审核', 'bounty', 'API'
  UNION ALL SELECT 'submission:review', '成果审核', 'submission', 'API'
  UNION ALL SELECT 'wallet:read', '钱庄只读', 'wallet', 'API'
  UNION ALL SELECT 'wallet:flag', '流水标记', 'wallet', 'API'
  UNION ALL SELECT 'dispute:read', '纠纷只读', 'dispute', 'API'
  UNION ALL SELECT 'dispute:verdict', '纠纷终裁', 'dispute', 'API'
  UNION ALL SELECT 'notice:read', '告示只读', 'notice', 'API'
  UNION ALL SELECT 'notice:write', '告示写', 'notice', 'API'
  UNION ALL SELECT 'office:read', '职司只读', 'office', 'API'
  UNION ALL SELECT 'office:write', '职司写', 'office', 'API'
  UNION ALL SELECT 'lord:read', '盟主只读', 'lord', 'API'
  UNION ALL SELECT 'lord:write', '盟主写', 'lord', 'API'
  UNION ALL SELECT 'config:read', '配置只读', 'config', 'API'
  UNION ALL SELECT 'config:write', '配置写', 'config', 'API'
  UNION ALL SELECT 'product:read', '奖品只读', 'product', 'API'
  UNION ALL SELECT 'product:write', '奖品写', 'product', 'API'
  UNION ALL SELECT 'checklist:read', '清单只读', 'checklist', 'API'
  UNION ALL SELECT 'checklist:write', '清单写', 'checklist', 'API'
  UNION ALL SELECT 'warrant_config:read', '令状配置只读', 'warrant_config', 'API'
  UNION ALL SELECT 'warrant_config:write', '令状配置写', 'warrant_config', 'API'
  UNION ALL SELECT 'audit:read', '审计只读', 'audit', 'API'
  UNION ALL SELECT 'report:read', '举报只读', 'report', 'API'
  UNION ALL SELECT 'report:write', '举报处理', 'report', 'API'
  UNION ALL SELECT 'job:read', '任务只读', 'job', 'API'
  UNION ALL SELECT 'admin:read', '管理员只读', 'admin', 'API'
  UNION ALL SELECT 'admin:write', '管理员写', 'admin', 'API'
  UNION ALL SELECT 'role:read', '角色只读', 'role', 'API'
  UNION ALL SELECT 'role:write', '角色写', 'role', 'API'
  UNION ALL SELECT 'menu:read', '菜单只读', 'menu', 'API'
  UNION ALL SELECT 'menu:write', '菜单写', 'menu', 'API'
) v
WHERE NOT EXISTS (SELECT 1 FROM admin_permission p WHERE p.code = v.code);

-- 四角色
INSERT INTO admin_role (code, name, builtin, description, status)
SELECT v.code, v.name, 1, v.description, 'ACTIVE' FROM (
  SELECT 'SUPER_ADMIN' AS code, '超级管理员' AS name, '全部 L0；含权限模型与管理员账号' AS description
  UNION ALL SELECT 'OPS_ADMIN', '运营管理员', '用户/悬赏/配置/职司/公告等运营权限'
  UNION ALL SELECT 'ARBITER', '终裁仲裁员', '纠纷终裁、审核改判'
  UNION ALL SELECT 'OBSERVER', '观察者', '只读日志与报表'
) v
WHERE NOT EXISTS (SELECT 1 FROM admin_role r WHERE r.code = v.code);

-- SUPER_ADMIN → *
INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r, admin_permission p
WHERE r.code = 'SUPER_ADMIN' AND p.code = '*'
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission x WHERE x.role_id = r.id AND x.permission_id = p.id
  );

-- OPS_ADMIN 默认集
INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r
JOIN admin_permission p ON p.code IN (
  'dashboard:view',
  'user:read','user:write','user:asset_adjust','user:real_name',
  'invite:read','invite:write',
  'bounty:read','bounty:write','bounty:review','submission:review',
  'wallet:read','wallet:flag',
  'dispute:read',
  'notice:read','notice:write',
  'office:read','office:write',
  'lord:read','lord:write',
  'config:read','config:write',
  'product:read','product:write',
  'checklist:read','checklist:write',
  'warrant_config:read','warrant_config:write',
  'audit:read','report:read','report:write','job:read',
  'admin:read','role:read','menu:read'
)
WHERE r.code = 'OPS_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission x WHERE x.role_id = r.id AND x.permission_id = p.id
  );

-- ARBITER
INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r
JOIN admin_permission p ON p.code IN (
  'dashboard:view','user:read',
  'bounty:read','bounty:review','submission:review',
  'wallet:read','dispute:read','dispute:verdict','audit:read','menu:read'
)
WHERE r.code = 'ARBITER'
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission x WHERE x.role_id = r.id AND x.permission_id = p.id
  );

-- OBSERVER
INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r
JOIN admin_permission p ON p.code IN (
  'dashboard:view','user:read','invite:read','bounty:read','wallet:read','dispute:read',
  'notice:read','office:read','lord:read','config:read',
  'product:read','checklist:read','warrant_config:read',
  'audit:read','report:read','job:read','admin:read','role:read','menu:read'
)
WHERE r.code = 'OBSERVER'
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission x WHERE x.role_id = r.id AND x.permission_id = p.id
  );

-- 菜单树（仅空表时种子）
INSERT INTO admin_menu (id, parent_id, type, name, path, component, icon, sort, visible, permission_code, status)
SELECT * FROM (
  SELECT 1 AS id, 0 AS parent_id, 'MENU' AS type, '工作台' AS name, '/admin' AS path, 'admin/DashboardView' AS component, 'Odometer' AS icon, 10 AS sort, 1 AS visible, 'dashboard:view' AS permission_code, 'ACTIVE' AS status
  UNION ALL SELECT 2, 0, 'MENU', '侠士管理', '/admin/users', 'admin/UsersView', 'User', 20, 1, 'user:read', 'ACTIVE'
  UNION ALL SELECT 3, 0, 'MENU', '邀请管理', '/admin/invites', 'admin/InvitesView', 'Ticket', 30, 1, 'invite:read', 'ACTIVE'
  UNION ALL SELECT 4, 0, 'MENU', '悬赏管理', '/admin/bounties', 'admin/BountiesView', 'Document', 40, 1, 'bounty:read', 'ACTIVE'
  UNION ALL SELECT 5, 0, 'MENU', '钱庄流水', '/admin/wallet', 'admin/WalletLedgersView', 'Wallet', 50, 1, 'wallet:read', 'ACTIVE'
  UNION ALL SELECT 6, 0, 'MENU', '纠纷仲裁', '/admin/disputes', 'admin/DisputesView', 'Warning', 60, 1, 'dispute:read', 'ACTIVE'
  UNION ALL SELECT 7, 0, 'MENU', '告示管理', '/admin/notices', 'admin/NoticesAdminView', 'Bell', 70, 1, 'notice:read', 'ACTIVE'
  UNION ALL SELECT 8, 0, 'MENU', '职司管理', '/admin/offices', 'admin/OfficesAdminView', 'Stamp', 80, 1, 'office:read', 'ACTIVE'
  UNION ALL SELECT 9, 0, 'MENU', '盟主管理', '/admin/lord', 'admin/LordAdminView', 'Trophy', 90, 1, 'lord:read', 'ACTIVE'
  UNION ALL SELECT 10, 0, 'DIR', '运营配置', '', '', 'Setting', 100, 1, 'config:read', 'ACTIVE'
  UNION ALL SELECT 11, 10, 'MENU', '等级配置', '/admin/configs/levels', 'admin/ConfigLevelsView', '', 101, 1, 'config:read', 'ACTIVE'
  UNION ALL SELECT 12, 10, 'MENU', '成长参数', '/admin/configs/growth', 'admin/ConfigGrowthView', '', 102, 1, 'config:read', 'ACTIVE'
  UNION ALL SELECT 13, 10, 'MENU', '英雄谱规则', '/admin/configs/ranks', 'admin/ConfigRanksView', '', 103, 1, 'config:read', 'ACTIVE'
  UNION ALL SELECT 14, 10, 'MENU', '赏银建议', '/admin/configs/reward-suggest', 'admin/ConfigRewardView', '', 104, 1, 'config:read', 'ACTIVE'
  UNION ALL SELECT 15, 10, 'MENU', '奖品管理', '/admin/products', 'admin/ProductsView', '', 105, 1, 'product:read', 'ACTIVE'
  UNION ALL SELECT 16, 10, 'MENU', '探子清单', '/admin/checklist-templates', 'admin/ChecklistView', '', 106, 1, 'checklist:read', 'ACTIVE'
  UNION ALL SELECT 17, 10, 'MENU', '令状字段', '/admin/warrant-field-configs', 'admin/WarrantConfigView', '', 107, 1, 'warrant_config:read', 'ACTIVE'
  UNION ALL SELECT 18, 0, 'MENU', '系统配置', '/admin/system', 'admin/SystemView', 'Tools', 110, 1, 'config:read', 'ACTIVE'
  UNION ALL SELECT 19, 0, 'DIR', '权限管理', '', '', 'Lock', 120, 1, 'admin:read', 'ACTIVE'
  UNION ALL SELECT 20, 19, 'MENU', '管理员账号', '/admin/admins', 'admin/AdminsView', '', 121, 1, 'admin:read', 'ACTIVE'
  UNION ALL SELECT 21, 19, 'MENU', '角色权限', '/admin/roles', 'admin/RolesView', '', 122, 1, 'role:read', 'ACTIVE'
  UNION ALL SELECT 22, 19, 'MENU', '菜单管理', '/admin/menus', 'admin/MenusView', '', 123, 1, 'menu:read', 'ACTIVE'
  UNION ALL SELECT 23, 2, 'BUTTON', '资产调账', '', '', '', 1, 1, 'user:asset_adjust', 'ACTIVE'
  UNION ALL SELECT 24, 6, 'BUTTON', '终裁执行', '', '', '', 1, 1, 'dispute:verdict', 'ACTIVE'
) t
WHERE NOT EXISTS (SELECT 1 FROM admin_menu LIMIT 1);

-- 将已有 admin 账号绑 SUPER_ADMIN（若不存在绑定）
INSERT INTO admin_user_role (admin_id, role_id)
SELECT u.id, r.id
FROM admin_user u
CROSS JOIN admin_role r
WHERE u.username = 'admin' AND r.code = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM admin_user_role x WHERE x.admin_id = u.id AND x.role_id = r.id
  );
