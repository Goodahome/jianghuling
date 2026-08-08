-- v1.8.15：运营配置下四参侧栏合并为一条「运营参数」→ /admin/ops
-- 用法：在目标库执行（可重复执行）

-- 1) 将「等级配置」收成「运营参数」
UPDATE admin_menu
SET name = '运营参数',
    path = '/admin/ops',
    component = 'admin/OpsConfigView',
    sort = 101,
    visible = 1,
    status = 'ACTIVE'
WHERE name = '等级配置';

-- 2) 若已是带 tab 的运营参数分项名，但尚无「运营参数」行：把成长参数改成运营参数
UPDATE admin_menu
SET name = '运营参数',
    path = '/admin/ops',
    component = 'admin/OpsConfigView',
    sort = 101,
    visible = 1,
    status = 'ACTIVE'
WHERE name = '成长参数'
  AND NOT EXISTS (SELECT 1 FROM (SELECT id FROM admin_menu WHERE name = '运营参数' LIMIT 1) t);

-- 3) 隐藏其余分项侧栏（成长 / 英雄谱 / 赏银；以及残留的等级配置）
UPDATE admin_menu
SET visible = 0,
    status = 'INACTIVE'
WHERE name IN ('等级配置', '成长参数', '英雄谱规则', '赏银建议');

-- 4) 统一已有「运营参数」path
UPDATE admin_menu
SET path = '/admin/ops',
    component = 'admin/OpsConfigView',
    visible = 1,
    status = 'ACTIVE'
WHERE name = '运营参数';

-- 5) 探子清单 / 令状字段 path 纠偏（沿用 v1.8.13）
UPDATE admin_menu
SET path = '/admin/checklist',
    component = 'admin/ChecklistAdminView'
WHERE name = '探子清单'
  AND path IN ('/admin/checklist-templates', '/admin/checklist');

UPDATE admin_menu
SET path = '/admin/warrant-config',
    component = 'admin/WarrantConfigAdminView'
WHERE name = '令状字段'
  AND path IN ('/admin/warrant-field-configs', '/admin/warrant-config');
