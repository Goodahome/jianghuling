USE jianghu_ling;

CREATE TABLE IF NOT EXISTS office_application (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT       NOT NULL,
  office_code VARCHAR(32)  NOT NULL,
  statement   VARCHAR(1000) NULL,
  status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|APPROVED|REJECTED',
  reason      VARCHAR(255) NULL,
  reviewer_id BIGINT       NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_office_app_status (status, id),
  KEY idx_office_app_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS lord_application (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id     BIGINT       NOT NULL,
  statement   VARCHAR(1000) NULL,
  status      VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|APPROVED|REJECTED',
  reason      VARCHAR(255) NULL,
  reviewer_id BIGINT       NULL,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_lord_app_status (status, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS platform_lord (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id    BIGINT   NOT NULL,
  start_at   DATETIME NOT NULL,
  end_at     DATETIME NULL,
  status     VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_lord_active_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dispute (
  id            BIGINT PRIMARY KEY AUTO_INCREMENT,
  settlement_id BIGINT       NULL,
  bounty_id     BIGINT       NOT NULL,
  initiator_id  BIGINT       NOT NULL,
  status        VARCHAR(20)  NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN|CLOSED',
  reason        VARCHAR(512) NULL,
  evidence_json JSON         NULL,
  verdict_json  JSON         NULL,
  deadline_at   DATETIME     NULL,
  created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY idx_dispute_status (status, id),
  KEY idx_dispute_bounty (bounty_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS audit_log (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator   VARCHAR(64)  NOT NULL,
  action     VARCHAR(64)  NOT NULL,
  detail     VARCHAR(1000) NULL,
  created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_audit_time (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
