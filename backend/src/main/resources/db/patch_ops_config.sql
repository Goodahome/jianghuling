USE jianghu_ling;

CREATE TABLE IF NOT EXISTS user_level_config (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  level         INT          NOT NULL,
  title         VARCHAR(64)  NOT NULL,
  min_chivalry  INT          NOT NULL DEFAULT 0,
  privileges_json VARCHAR(1000) NULL,
  sort_no       INT          NOT NULL DEFAULT 0,
  UNIQUE KEY uk_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS warrant_field_config (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  template_code      VARCHAR(32)  NOT NULL,
  template_name      VARCHAR(64)  NOT NULL,
  field_key          VARCHAR(64)  NOT NULL,
  label              VARCHAR(64)  NOT NULL,
  field_type         VARCHAR(32)  NOT NULL DEFAULT 'text',
  required           TINYINT(1)   NOT NULL DEFAULT 0,
  mask_until_claimed TINYINT(1)   NOT NULL DEFAULT 0,
  sort_no            INT          NOT NULL DEFAULT 0,
  status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uk_warrant_field (template_code, field_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reward_product (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  name          VARCHAR(128) NOT NULL,
  description   VARCHAR(512) NULL,
  cost_chivalry INT          NOT NULL DEFAULT 0,
  stock         INT          NOT NULL DEFAULT 0,
  cover_url     VARCHAR(512) NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS redeem_order (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id       BIGINT       NOT NULL,
  product_id    BIGINT       NOT NULL,
  quantity      INT          NOT NULL DEFAULT 1,
  chivalry_cost INT          NOT NULL DEFAULT 0,
  status        VARCHAR(20)  NOT NULL DEFAULT 'DONE' COMMENT 'DONE|PENDING|SHIPPED|CANCELLED',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_redeem_user (user_id, id),
  KEY idx_redeem_status (status, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO user_level_config (level, title, min_chivalry, privileges_json, sort_no)
SELECT * FROM (
  SELECT 1 AS level, '初入江湖' AS title, 0 AS min_chivalry, '[]' AS privileges_json, 1 AS sort_no
  UNION ALL SELECT 2, '初显身手', 50, '[]', 2
  UNION ALL SELECT 3, '小有名气', 200, '[]', 3
  UNION ALL SELECT 4, '名扬江湖', 500, '[]', 4
) t
WHERE NOT EXISTS (SELECT 1 FROM user_level_config LIMIT 1);

INSERT INTO warrant_field_config (template_code, template_name, field_key, label, field_type, required, mask_until_claimed, sort_no)
SELECT * FROM (
  SELECT 'RENT_SEEK' AS template_code, '租房令状' AS template_name, 'district' AS field_key, '区域' AS label, 'text' AS field_type, 1 AS required, 0 AS mask_until_claimed, 1 AS sort_no
  UNION ALL SELECT 'RENT_SEEK', '租房令状', 'rentBudgetMin', '预算下限(元/月)', 'number', 1, 0, 2
  UNION ALL SELECT 'RENT_SEEK', '租房令状', 'rentBudgetMax', '预算上限(元/月)', 'number', 1, 0, 3
  UNION ALL SELECT 'RENT_SEEK', '租房令状', 'layout', '户型', 'text', 1, 0, 4
  UNION ALL SELECT 'RENT_SEEK', '租房令状', 'expectMoveInDate', '期望入住', 'date', 1, 0, 5
  UNION ALL SELECT 'RENT_SEEK', '租房令状', 'acceptAgency', '是否接受中介', 'boolean', 1, 0, 6
  UNION ALL SELECT 'RENT_SEEK', '租房令状', 'extra', '补充说明', 'textarea', 0, 0, 7
  UNION ALL SELECT 'RENT_OUT', '出租令状', 'district', '区域', 'text', 1, 0, 1
  UNION ALL SELECT 'RENT_OUT', '出租令状', 'exactAddress', '精确位置', 'text', 1, 1, 2
  UNION ALL SELECT 'RENT_OUT', '出租令状', 'rentPrice', '租金(元/月)', 'number', 1, 0, 3
  UNION ALL SELECT 'RENT_OUT', '出租令状', 'layout', '户型', 'text', 1, 0, 4
  UNION ALL SELECT 'RENT_OUT', '出租令状', 'availableDate', '可入住日期', 'date', 1, 0, 5
  UNION ALL SELECT 'RENT_OUT', '出租令状', 'furniture', '家具家电', 'text', 0, 0, 6
  UNION ALL SELECT 'RENT_OUT', '出租令状', 'extra', '补充说明', 'textarea', 0, 0, 7
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'district', '区域', 'text', 1, 0, 1
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'exactAddress', '精确位置', 'text', 1, 1, 2
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'rentPrice', '租金(元/月)', 'number', 1, 0, 3
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'layout', '户型', 'text', 1, 0, 4
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'availableDate', '可入住日期', 'date', 1, 0, 5
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'complianceNote', '转租合规说明', 'textarea', 0, 0, 6
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'furniture', '家具家电', 'text', 0, 0, 7
  UNION ALL SELECT 'RENT_TRANSFER', '转租令状', 'extra', '补充说明', 'textarea', 0, 0, 8
) t
WHERE NOT EXISTS (SELECT 1 FROM warrant_field_config LIMIT 1);

INSERT INTO reward_product (name, description, cost_chivalry, stock, cover_url, status)
SELECT * FROM (
  SELECT '遵义茶礼' AS name, '本地特产礼盒（模拟）' AS description, 30 AS cost_chivalry, 99 AS stock, '' AS cover_url, 'ACTIVE' AS status
  UNION ALL SELECT '江湖腰牌', '个性展示徽章（模拟）', 50, 50, '', 'ACTIVE'
  UNION ALL SELECT '体力补给包', '额外体力展示道具（模拟）', 20, 200, '', 'ACTIVE'
) t
WHERE NOT EXISTS (SELECT 1 FROM reward_product LIMIT 1);

INSERT INTO sys_config (config_key, config_value, remark)
SELECT 'ranks_config',
       '{"refreshMinutes":10,"excludeBanned":true,"lordTopDisplay":true,"eligibleForLordTopN":1,"rejectCooldownDays":7}',
       '英雄谱规则'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'ranks_config');
