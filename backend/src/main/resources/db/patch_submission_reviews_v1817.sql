-- v1.8.17：有成果取消待分配 + settlement.kind + Admin 成果审核菜单/权限
-- 用法：在目标库执行（可重复执行）

USE jianghu_ling;

-- bounty.cancel_allocation_pending
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'bounty'
    AND COLUMN_NAME = 'cancel_allocation_pending'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE bounty ADD COLUMN cancel_allocation_pending TINYINT(1) NOT NULL DEFAULT 0 COMMENT ''有成果取消待分配'' AFTER cancel_reason',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- settlement.kind
SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'settlement'
    AND COLUMN_NAME = 'kind'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE settlement ADD COLUMN kind VARCHAR(32) NOT NULL DEFAULT ''COMPLETE'' COMMENT ''COMPLETE|CANCEL_ALLOCATE'' AFTER status',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 权限：submission:read（既有 submission:review 保留）
INSERT INTO admin_permission (code, name, module, type)
SELECT 'submission:read', '成果审核只读', 'submission', 'API'
WHERE NOT EXISTS (SELECT 1 FROM admin_permission p WHERE p.code = 'submission:read');

-- OPS_ADMIN：读写
INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r
JOIN admin_permission p ON p.code IN ('submission:read', 'submission:review')
WHERE r.code = 'OPS_ADMIN'
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission x WHERE x.role_id = r.id AND x.permission_id = p.id
  );

-- ARBITER：读写（改判）
INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r
JOIN admin_permission p ON p.code IN ('submission:read', 'submission:review')
WHERE r.code = 'ARBITER'
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission x WHERE x.role_id = r.id AND x.permission_id = p.id
  );

-- OBSERVER：只读
INSERT INTO admin_role_permission (role_id, permission_id)
SELECT r.id, p.id FROM admin_role r
JOIN admin_permission p ON p.code = 'submission:read'
WHERE r.code = 'OBSERVER'
  AND NOT EXISTS (
    SELECT 1 FROM admin_role_permission x WHERE x.role_id = r.id AND x.permission_id = p.id
  );

-- 一级菜单「成果审核」
INSERT INTO admin_menu (parent_id, type, name, path, component, icon, sort, visible, permission_code, status)
SELECT 0, 'MENU', '成果审核', '/admin/submission-reviews', 'admin/SubmissionReviewsAdminView', 'DocumentChecked', 45, 1, 'submission:read', 'ACTIVE'
WHERE NOT EXISTS (
  SELECT 1 FROM admin_menu WHERE path = '/admin/submission-reviews' AND type = 'MENU'
);

-- 按钮「通过驳回」
INSERT INTO admin_menu (parent_id, type, name, path, component, icon, sort, visible, permission_code, status)
SELECT m.id, 'BUTTON', '通过驳回', '', '', '', 1, 1, 'submission:review', 'ACTIVE'
FROM admin_menu m
WHERE m.path = '/admin/submission-reviews' AND m.type = 'MENU'
  AND NOT EXISTS (
    SELECT 1 FROM admin_menu b
    WHERE b.parent_id = m.id AND b.type = 'BUTTON' AND b.permission_code = 'submission:review'
  );
