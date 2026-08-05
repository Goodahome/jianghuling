USE jianghu_ling;

SET @col_exists := (
  SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'bounty' AND COLUMN_NAME = 'source_bounty_id'
);
SET @sql := IF(@col_exists = 0,
  'ALTER TABLE bounty ADD COLUMN source_bounty_id BIGINT NULL COMMENT ''再发来源悬赏ID'' AFTER cancel_reason, ADD KEY idx_bounty_source (source_bounty_id)',
  'SELECT 1');
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
