-- v1.8.13：修正武林盟侧栏运营配置菜单 path，与前端 router 对齐
-- 用法：在目标库执行本脚本（可重复执行）

UPDATE admin_menu
SET path = '/admin/ops?tab=levels',
    component = 'admin/OpsConfigView'
WHERE name = '等级配置'
  AND path LIKE '/admin/configs/levels%';

UPDATE admin_menu
SET path = '/admin/ops?tab=growth',
    component = 'admin/OpsConfigView'
WHERE name = '成长参数'
  AND path LIKE '/admin/configs/growth%';

UPDATE admin_menu
SET path = '/admin/ops?tab=ranks',
    component = 'admin/OpsConfigView'
WHERE name = '英雄谱规则'
  AND path LIKE '/admin/configs/ranks%';

UPDATE admin_menu
SET path = '/admin/ops?tab=reward',
    component = 'admin/OpsConfigView'
WHERE name = '赏银建议'
  AND (path LIKE '/admin/configs/reward%' OR path = '/admin/configs/reward-suggest');

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
