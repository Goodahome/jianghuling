-- v1.8.18：系统配置菜单改为审计日志（参数编辑与运营参数页重复）
USE jianghu_ling;

UPDATE admin_menu
SET name = '审计日志',
    path = '/admin/audit-logs',
    component = 'admin/AuditLogsView',
    permission_code = 'audit:read'
WHERE path IN ('/admin/system', '/admin/audit-logs')
   OR (name = '系统配置' AND type = 'MENU');
