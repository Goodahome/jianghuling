USE jianghu_ling;

-- v1.8.9 / api v1.0.6：第三套转租令状 + 武侠展示名对齐（租房/出租/转租）

UPDATE warrant_field_config
SET template_name = '租房令状'
WHERE template_code = 'RENT_SEEK' AND template_name <> '租房令状';

INSERT INTO warrant_field_config (template_code, template_name, field_key, label, field_type, required, mask_until_claimed, sort_no, status)
SELECT * FROM (
  SELECT 'RENT_TRANSFER' AS template_code, '转租令状' AS template_name, 'district' AS field_key, '区域' AS label, 'text' AS field_type, 1 AS required, 0 AS mask_until_claimed, 1 AS sort_no, 'ACTIVE' AS status
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'exactAddress', '精确位置', 'text', 1, 1, 2, 'ACTIVE'
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'rentPrice', '租金(元/月)', 'number', 1, 0, 3, 'ACTIVE'
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'layout', '户型', 'text', 1, 0, 4, 'ACTIVE'
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'availableDate', '可入住日期', 'date', 1, 0, 5, 'ACTIVE'
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'complianceNote', '转租合规说明', 'textarea', 0, 0, 6, 'ACTIVE'
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'furniture', '家具家电', 'text', 0, 0, 7, 'ACTIVE'
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'extra', '补充说明', 'textarea', 0, 0, 8, 'ACTIVE'
) t
WHERE NOT EXISTS (
  SELECT 1 FROM warrant_field_config w
  WHERE w.template_code = t.template_code AND w.field_key = t.field_key
);
