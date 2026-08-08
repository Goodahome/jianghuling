-- v1.8.16：用户反馈表 + 频控配置 + RBAC/菜单种子
-- 用法：在目标库执行（可重复执行）

USE jianghu_ling;

CREATE TABLE IF NOT EXISTS user_feedback (
  id                         BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id                    BIGINT        NOT NULL,
  type                       VARCHAR(20)   NOT NULL COMMENT 'BUG|SUGGEST|COMPLAINT|OTHER',
  title                      VARCHAR(100)  NOT NULL,
  content                    VARCHAR(2000) NOT NULL,
  contact                    VARCHAR(64)   NULL,
  related_ref                VARCHAR(128)  NULL,
  attachment_urls_json       TEXT          NULL,
  status                     VARCHAR(20)   NOT NULL DEFAULT 'NEW' COMMENT 'NEW|PROCESSING|RESOLVED|CLOSED',
  handle_remark              VARCHAR(1000) NULL,
  status_changed_at          DATETIME      NULL,
  status_changed_by_admin_id BIGINT        NULL,
  status_history_json        TEXT          NULL,
  created_at                 DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at                 DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_feedback_user (user_id, id),
  KEY idx_feedback_status (status, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO sys_config (config_key, config_value, remark)
SELECT 'feedback.cooldownSeconds', '60', '用户反馈短时冷却秒'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'feedback.cooldownSeconds');

INSERT INTO sys_config (config_key, config_value, remark)
SELECT 'feedback.dailyLimit', '10', '用户反馈自然日上限'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'feedback.dailyLimit');

INSERT INTO admin_permission (code, name, module, type)
SELECT v.code, v.name, v.module, v.type FROM (
  SELECT 'feedback:read' AS code, '用户反馈只读' AS name, 'feedback' AS module, 'API' AS type
  UNION ALL SELECT 'feedback:write', '用户反馈写', 'feedback', 'API'
) v
WHERE NOT EXISTS (SELECT 1 FROM admin_permission p WHERE p.code = v.code);

-- OPS_ADMIN：读写
INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r
JOIN admin_permission p ON p.code IN ('feedback:read', 'feedback:write')
WHERE r.code = 'OPS_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission x WHERE x.role_id = r.id AND x.permission_id = p.id
  );

-- OBSERVER：只读
INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r
JOIN admin_permission p ON p.code = 'feedback:read'
WHERE r.code = 'OBSERVER'
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission x WHERE x.role_id = r.id AND x.permission_id = p.id
  );

-- 一级菜单「用户反馈」
INSERT INTO admin_menu (parent_id, type, name, path, component, icon, sort, visible, permission_code, status)
SELECT 0, 'MENU', '用户反馈', '/admin/feedbacks', 'admin/FeedbacksAdminView', 'ChatDotRound', 75, 1, 'feedback:read', 'ACTIVE'
WHERE NOT EXISTS (
  SELECT 1 FROM admin_menu WHERE path = '/admin/feedbacks' AND type = 'MENU'
);

-- 按钮「改状态」（挂在用户反馈菜单下）
INSERT INTO admin_menu (parent_id, type, name, path, component, icon, sort, visible, permission_code, status)
SELECT m.id, 'BUTTON', '改状态', '', '', '', 1, 1, 'feedback:write', 'ACTIVE'
FROM admin_menu m
WHERE m.path = '/admin/feedbacks' AND m.type = 'MENU'
  AND NOT EXISTS (
    SELECT 1 FROM admin_menu b
    WHERE b.parent_id = m.id AND b.type = 'BUTTON' AND b.permission_code = 'feedback:write'
  );
