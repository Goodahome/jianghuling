CREATE DATABASE IF NOT EXISTS jianghu_ling DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jianghu_ling;

SET NAMES utf8mb4;

DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS admin_menu;
DROP TABLE IF EXISTS admin_user_role;
DROP TABLE IF EXISTS admin_role_permission;
DROP TABLE IF EXISTS admin_permission;
DROP TABLE IF EXISTS admin_role;
DROP TABLE IF EXISTS dispute;
DROP TABLE IF EXISTS platform_lord;
DROP TABLE IF EXISTS lord_application;
DROP TABLE IF EXISTS office_application;
DROP TABLE IF EXISTS redeem_order;
DROP TABLE IF EXISTS reward_product;
DROP TABLE IF EXISTS warrant_field_config;
DROP TABLE IF EXISTS user_level_config;
DROP TABLE IF EXISTS site_message;
DROP TABLE IF EXISTS evaluation;
DROP TABLE IF EXISTS settlement_item;
DROP TABLE IF EXISTS settlement;
DROP TABLE IF EXISTS review_record;
DROP TABLE IF EXISTS submission_item;
DROP TABLE IF EXISTS submission;
DROP TABLE IF EXISTS bounty_message;
DROP TABLE IF EXISTS bounty_claim;
DROP TABLE IF EXISTS bounty_checklist;
DROP TABLE IF EXISTS bounty_warrant;
DROP TABLE IF EXISTS bounty;
DROP TABLE IF EXISTS notice;
DROP TABLE IF EXISTS checklist_template;
DROP TABLE IF EXISTS reward_suggest_config;
DROP TABLE IF EXISTS sys_config;
DROP TABLE IF EXISTS user_office;
DROP TABLE IF EXISTS office_def;
DROP TABLE IF EXISTS wallet_ledger;
DROP TABLE IF EXISTS wallet_account;
DROP TABLE IF EXISTS user_asset;
DROP TABLE IF EXISTS invite_relation;
DROP TABLE IF EXISTS invite_code;
DROP TABLE IF EXISTS login_log;
DROP TABLE IF EXISTS user_profile;
DROP TABLE IF EXISTS `user`;
DROP TABLE IF EXISTS admin_user;

CREATE TABLE `user` (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  phone         VARCHAR(20)  NOT NULL,
  username      VARCHAR(64)  NOT NULL,
  password_hash VARCHAR(100) NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|DISABLED|BANNED',
  city          VARCHAR(64)  NOT NULL DEFAULT '遵义',
  remark        VARCHAR(255) NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_user_phone (phone),
  UNIQUE KEY uk_user_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_profile (
  id               BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id          BIGINT       NOT NULL,
  nickname         VARCHAR(64)  NOT NULL,
  avatar_url       VARCHAR(512) NULL,
  bio              VARCHAR(512) NULL,
  real_name        VARCHAR(64)  NULL,
  id_number        VARCHAR(32)  NULL,
  real_name_status VARCHAR(20)  NOT NULL DEFAULT 'NONE' COMMENT 'NONE|PENDING|VERIFIED|REJECTED',
  created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_profile_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE invite_code (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  code           VARCHAR(32)  NOT NULL,
  owner_user_id  BIGINT       NULL,
  quota          INT          NOT NULL DEFAULT 1,
  used_count     INT          NOT NULL DEFAULT 0,
  status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|INVALID',
  expire_at      DATETIME     NULL,
  created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_invite_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE invite_relation (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  inviter_id     BIGINT   NOT NULL,
  invitee_id     BIGINT   NOT NULL,
  invite_code_id BIGINT   NOT NULL,
  created_at     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_invitee (invitee_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wallet_account (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id    BIGINT         NOT NULL,
  balance    DECIMAL(12,2)  NOT NULL DEFAULT 0,
  frozen     DECIMAL(12,2)  NOT NULL DEFAULT 0,
  version    INT            NOT NULL DEFAULT 0,
  created_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_wallet_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE wallet_ledger (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  biz_no        VARCHAR(64)   NOT NULL,
  user_id       BIGINT        NOT NULL,
  type          VARCHAR(32)   NOT NULL,
  amount        DECIMAL(12,2) NOT NULL,
  balance_after DECIMAL(12,2) NOT NULL,
  frozen_after  DECIMAL(12,2) NOT NULL,
  ref_type      VARCHAR(32)   NULL,
  ref_id        BIGINT        NULL,
  remark        VARCHAR(255)  NULL,
  created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_ledger_biz_no (biz_no),
  KEY idx_ledger_user_time (user_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_asset (
  id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id            BIGINT         NOT NULL,
  chivalry           INT            NOT NULL DEFAULT 0,
  stamina            INT            NOT NULL DEFAULT 5,
  stamina_date       DATE           NULL,
  completed_orders   INT            NOT NULL DEFAULT 0,
  good_rate          DECIMAL(5,2)   NOT NULL DEFAULT 100.00,
  reputation_score   DECIMAL(12,2)  NOT NULL DEFAULT 0,
  created_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at         DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_asset_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE login_log (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT       NULL,
  admin_id    BIGINT       NULL,
  ip          VARCHAR(64)  NULL,
  user_agent  VARCHAR(255) NULL,
  result      VARCHAR(20)  NOT NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_login_user (user_id),
  KEY idx_login_admin (admin_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE office_def (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  code          VARCHAR(32)  NOT NULL,
  name          VARCHAR(64)  NOT NULL,
  min_level     INT          NOT NULL DEFAULT 1,
  quota         INT          NOT NULL DEFAULT 10,
  term_days     INT          NOT NULL DEFAULT 90,
  status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uk_office_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_office (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT      NOT NULL,
  office_code VARCHAR(32) NOT NULL,
  status      VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|SUSPENDED|EXPIRED',
  start_at    DATETIME    NOT NULL,
  end_at      DATETIME    NULL,
  created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_user_office (user_id, office_code, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_config (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key   VARCHAR(64)  NOT NULL,
  config_value VARCHAR(512) NOT NULL,
  remark       VARCHAR(255) NULL,
  UNIQUE KEY uk_config_key (config_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reward_suggest_config (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  code        VARCHAR(32)  NOT NULL,
  name        VARCHAR(32)  NOT NULL,
  suggest_min DECIMAL(12,2) NOT NULL,
  suggest_max DECIMAL(12,2) NOT NULL,
  sort_no     INT          NOT NULL DEFAULT 0,
  UNIQUE KEY uk_reward_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE checklist_template (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  item_code   VARCHAR(64)  NOT NULL,
  item_name   VARCHAR(128) NOT NULL,
  required    TINYINT(1)   NOT NULL DEFAULT 0,
  tags_json   VARCHAR(512) NULL,
  sort_no     INT          NOT NULL DEFAULT 0,
  status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  UNIQUE KEY uk_checklist_code (item_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notice (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  category   VARCHAR(32)  NOT NULL,
  title      VARCHAR(128) NOT NULL,
  content    TEXT         NOT NULL,
  pinned     TINYINT(1)   NOT NULL DEFAULT 0,
  status     VARCHAR(20)  NOT NULL DEFAULT 'PUBLISHED',
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_notice_cat (category, status, pinned)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bounty (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  publisher_id    BIGINT         NOT NULL,
  type            VARCHAR(32)    NOT NULL COMMENT 'RENT_SEEK|RENT_OUT',
  title           VARCHAR(128)   NOT NULL,
  status          VARCHAR(32)    NOT NULL,
  city            VARCHAR(64)    NOT NULL DEFAULT '遵义',
  district        VARCHAR(64)    NULL,
  difficulty      VARCHAR(32)    NOT NULL,
  reward_amount   DECIMAL(12,2)  NOT NULL,
  deadline_at     DATETIME       NOT NULL,
  task_tags_json  VARCHAR(512)   NULL,
  frozen_biz_no   VARCHAR(64)    NULL,
  cancel_reason   VARCHAR(255)   NULL,
  source_bounty_id BIGINT        NULL COMMENT '再发来源悬赏ID',
  remind_24h_sent TINYINT(1)     NOT NULL DEFAULT 0,
  remind_2h_sent  TINYINT(1)     NOT NULL DEFAULT 0,
  created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_bounty_status_city (status, city, deadline_at),
  KEY idx_bounty_publisher (publisher_id),
  KEY idx_bounty_source (source_bounty_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bounty_warrant (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  bounty_id     BIGINT       NOT NULL,
  template_code VARCHAR(32)  NOT NULL,
  fields_json   JSON         NOT NULL,
  UNIQUE KEY uk_warrant_bounty (bounty_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bounty_checklist (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  bounty_id  BIGINT       NOT NULL,
  item_code  VARCHAR(64)  NOT NULL,
  item_name  VARCHAR(128) NOT NULL,
  required   TINYINT(1)   NOT NULL DEFAULT 0,
  sort_no    INT          NOT NULL DEFAULT 0,
  KEY idx_checklist_bounty (bounty_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bounty_claim (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  bounty_id    BIGINT      NOT NULL,
  user_id      BIGINT      NOT NULL,
  stamina_cost INT         NOT NULL DEFAULT 1,
  status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_claim (bounty_id, user_id),
  KEY idx_claim_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE bounty_message (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  bounty_id  BIGINT       NOT NULL,
  sender_id  BIGINT       NOT NULL,
  content    VARCHAR(1000) NOT NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_msg_bounty (bounty_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE submission (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  bounty_id       BIGINT       NOT NULL,
  claim_id        BIGINT       NOT NULL,
  user_id         BIGINT       NOT NULL,
  version_no      INT          NOT NULL,
  status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|APPROVED|REJECTED',
  content_summary VARCHAR(512) NULL,
  reject_reason   VARCHAR(255) NULL,
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_sub_claim (claim_id),
  KEY idx_sub_bounty_status (bounty_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE submission_item (
  id                   BIGINT PRIMARY KEY AUTO_INCREMENT,
  submission_id        BIGINT       NOT NULL,
  checklist_item_code  VARCHAR(64)  NOT NULL,
  done                 TINYINT(1)   NOT NULL DEFAULT 0,
  text                 VARCHAR(1000) NULL,
  media_urls_json      JSON         NULL,
  KEY idx_sub_item (submission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE review_record (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  target_type   VARCHAR(32)  NOT NULL COMMENT 'BOUNTY|SUBMISSION',
  target_id     BIGINT       NOT NULL,
  result        VARCHAR(20)  NOT NULL,
  reason        VARCHAR(255) NULL,
  reviewer_id   BIGINT       NOT NULL,
  reviewer_role VARCHAR(32)  NOT NULL COMMENT 'HALL|ADMIN',
  override_by   BIGINT       NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_review_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE settlement (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  bounty_id     BIGINT         NOT NULL,
  reward_b      DECIMAL(12,2)  NOT NULL,
  fee_rate      DECIMAL(6,4)   NOT NULL,
  fee           DECIMAL(12,2)  NOT NULL,
  distributable DECIMAL(12,2)  NOT NULL,
  status        VARCHAR(20)    NOT NULL DEFAULT 'DONE',
  created_at    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_settle_bounty (bounty_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE settlement_item (
  id             BIGINT PRIMARY KEY AUTO_INCREMENT,
  settlement_id  BIGINT         NOT NULL,
  user_id        BIGINT         NOT NULL,
  amount         DECIMAL(12,2)  NOT NULL,
  chivalry_bonus INT            NOT NULL DEFAULT 0,
  KEY idx_settle_item (settlement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE evaluation (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  bounty_id    BIGINT       NOT NULL,
  from_user_id BIGINT       NOT NULL,
  to_user_id   BIGINT       NOT NULL,
  score        INT          NOT NULL,
  content      VARCHAR(512) NULL,
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_eval (bounty_id, from_user_id, to_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE site_message (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id    BIGINT       NOT NULL,
  title      VARCHAR(128) NOT NULL,
  content    VARCHAR(1000) NOT NULL,
  biz_type   VARCHAR(32)  NULL,
  biz_id     BIGINT       NULL,
  read_flag  TINYINT(1)   NOT NULL DEFAULT 0,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_msg_user (user_id, read_flag, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin_user (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  username      VARCHAR(64)  NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  display_name  VARCHAR(64)  NOT NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin_role (
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

CREATE TABLE admin_permission (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  code       VARCHAR(64)  NOT NULL,
  name       VARCHAR(128) NOT NULL,
  module     VARCHAR(64)  NOT NULL,
  type       VARCHAR(20)  NOT NULL DEFAULT 'API',
  UNIQUE KEY uk_admin_perm_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin_role_permission (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id       BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin_user_role (
  id       BIGINT PRIMARY KEY AUTO_INCREMENT,
  admin_id BIGINT NOT NULL,
  role_id  BIGINT NOT NULL,
  UNIQUE KEY uk_admin_role (admin_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE admin_menu (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  parent_id       BIGINT       NOT NULL DEFAULT 0,
  type            VARCHAR(20)  NOT NULL,
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

CREATE TABLE office_application (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT       NOT NULL,
  office_code VARCHAR(32)  NOT NULL,
  statement   VARCHAR(1000) NULL,
  status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  reason      VARCHAR(255) NULL,
  reviewer_id BIGINT       NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_office_app_status (status, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE lord_application (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT       NOT NULL,
  statement   VARCHAR(1000) NULL,
  status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
  reason      VARCHAR(255) NULL,
  reviewer_id BIGINT       NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_lord_app_status (status, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE platform_lord (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id    BIGINT   NOT NULL,
  start_at   DATETIME NOT NULL,
  end_at     DATETIME NULL,
  status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_lord_active_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE dispute (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  settlement_id BIGINT       NULL,
  bounty_id     BIGINT       NOT NULL,
  initiator_id  BIGINT       NOT NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT 'OPEN',
  reason        VARCHAR(512) NULL,
  evidence_json JSON         NULL,
  verdict_json  JSON         NULL,
  deadline_at   DATETIME     NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_dispute_status (status, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE audit_log (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator   VARCHAR(64)  NOT NULL,
  action     VARCHAR(64)  NOT NULL,
  detail     VARCHAR(1000) NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE user_level_config (
  id              BIGINT PRIMARY KEY AUTO_INCREMENT,
  level           INT          NOT NULL,
  title           VARCHAR(64)  NOT NULL,
  min_chivalry    INT          NOT NULL DEFAULT 0,
  privileges_json VARCHAR(1000) NULL,
  sort_no         INT          NOT NULL DEFAULT 0,
  UNIQUE KEY uk_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE warrant_field_config (
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

CREATE TABLE reward_product (
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

CREATE TABLE redeem_order (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id       BIGINT       NOT NULL,
  product_id    BIGINT       NOT NULL,
  quantity      INT          NOT NULL DEFAULT 1,
  chivalry_cost INT          NOT NULL DEFAULT 0,
  status        VARCHAR(20)  NOT NULL DEFAULT 'DONE',
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_redeem_user (user_id, id),
  KEY idx_redeem_status (status, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
