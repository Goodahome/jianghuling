/*
 Navicat Premium Data Transfer

 Source Server         : 本地数据库
 Source Server Type    : MySQL
 Source Server Version : 80012
 Source Host           : localhost:3306
 Source Schema         : jianghu_ling

 Target Server Type    : MySQL
 Target Server Version : 80012
 File Encoding         : 65001

 Date: 07/08/2026 18:24:25
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_menu
-- ----------------------------
DROP TABLE IF EXISTS `admin_menu`;
CREATE TABLE `admin_menu`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `parent_id` bigint(20) NOT NULL DEFAULT 0,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'DIR|MENU|BUTTON',
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `path` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `component` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `icon` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort` int(11) NOT NULL DEFAULT 0,
  `visible` tinyint(1) NOT NULL DEFAULT 1,
  `permission_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_menu_parent`(`parent_id`, `sort`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 25 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_menu
-- ----------------------------
INSERT INTO `admin_menu` VALUES (1, 0, 'MENU', '工作台', '/admin', 'admin/DashboardView', 'Odometer', 10, 1, 'dashboard:view', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (2, 0, 'MENU', '侠士管理', '/admin/users', 'admin/UsersView', 'User', 20, 1, 'user:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (3, 0, 'MENU', '邀请管理', '/admin/invites', 'admin/InvitesView', 'Ticket', 30, 1, 'invite:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (4, 0, 'MENU', '悬赏管理', '/admin/bounties', 'admin/BountiesView', 'Document', 40, 1, 'bounty:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (5, 0, 'MENU', '钱庄流水', '/admin/wallet', 'admin/WalletLedgersView', 'Wallet', 50, 1, 'wallet:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (6, 0, 'MENU', '纠纷仲裁', '/admin/disputes', 'admin/DisputesView', 'Warning', 60, 1, 'dispute:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (7, 0, 'MENU', '告示管理', '/admin/notices', 'admin/NoticesAdminView', 'Bell', 70, 1, 'notice:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (8, 0, 'MENU', '职司管理', '/admin/offices', 'admin/OfficesAdminView', 'Stamp', 80, 1, 'office:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (9, 0, 'MENU', '盟主管理', '/admin/lord', 'admin/LordAdminView', 'Trophy', 90, 1, 'lord:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (10, 0, 'DIR', '运营配置', '', '', 'Setting', 100, 1, 'config:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (11, 10, 'MENU', '运营参数', '/admin/ops', 'admin/OpsConfigView', '', 101, 1, 'config:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (12, 10, 'MENU', '成长参数', '/admin/ops?tab=growth', 'admin/OpsConfigView', '', 102, 0, 'config:read', 'INACTIVE');
INSERT INTO `admin_menu` VALUES (13, 10, 'MENU', '英雄谱规则', '/admin/ops?tab=ranks', 'admin/OpsConfigView', '', 103, 0, 'config:read', 'INACTIVE');
INSERT INTO `admin_menu` VALUES (14, 10, 'MENU', '赏银建议', '/admin/ops?tab=reward', 'admin/OpsConfigView', '', 104, 0, 'config:read', 'INACTIVE');
INSERT INTO `admin_menu` VALUES (15, 10, 'MENU', '奖品管理', '/admin/products', 'admin/ProductsView', '', 105, 1, 'product:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (16, 10, 'MENU', '探子清单', '/admin/checklist', 'admin/ChecklistAdminView', '', 106, 1, 'checklist:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (17, 10, 'MENU', '令状字段', '/admin/warrant-config', 'admin/WarrantConfigAdminView', '', 107, 1, 'warrant_config:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (18, 0, 'MENU', '审计日志', '/admin/audit-logs', 'admin/AuditLogsView', 'Tools', 110, 1, 'audit:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (19, 0, 'DIR', '权限管理', '', '', 'Lock', 120, 1, 'admin:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (20, 19, 'MENU', '管理员账号', '/admin/admins', 'admin/AdminsView', '', 121, 1, 'admin:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (21, 19, 'MENU', '角色权限', '/admin/roles', 'admin/RolesView', '', 122, 1, 'role:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (22, 19, 'MENU', '菜单管理', '/admin/menus', 'admin/MenusView', '', 123, 1, 'menu:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (23, 2, 'BUTTON', '资产调账', '', '', '', 1, 1, 'user:asset_adjust', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (24, 6, 'BUTTON', '终裁执行', '', '', '', 1, 1, 'dispute:verdict', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (25, 0, 'MENU', '成果审核', '/admin/submission-reviews', 'admin/SubmissionReviewsAdminView', 'DocumentChecked', 45, 1, 'submission:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (26, 25, 'BUTTON', '通过驳回', '', '', '', 1, 1, 'submission:review', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (27, 0, 'MENU', '用户反馈', '/admin/feedbacks', 'admin/FeedbacksAdminView', 'ChatDotRound', 75, 1, 'feedback:read', 'ACTIVE');
INSERT INTO `admin_menu` VALUES (28, 27, 'BUTTON', '改状态', '', '', '', 1, 1, 'feedback:write', 'ACTIVE');

-- ----------------------------
-- Table structure for admin_permission
-- ----------------------------
DROP TABLE IF EXISTS `admin_permission`;
CREATE TABLE `admin_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `module` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'API' COMMENT 'API|MENU|BUTTON',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_perm_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_permission
-- ----------------------------
INSERT INTO `admin_permission` VALUES (1, '*', '全部权限', 'system', 'API');
INSERT INTO `admin_permission` VALUES (2, 'dashboard:view', '工作台', 'dashboard', 'API');
INSERT INTO `admin_permission` VALUES (3, 'user:read', '侠士只读', 'user', 'API');
INSERT INTO `admin_permission` VALUES (4, 'user:write', '侠士写', 'user', 'API');
INSERT INTO `admin_permission` VALUES (5, 'user:asset_adjust', '资产调账', 'user', 'API');
INSERT INTO `admin_permission` VALUES (6, 'user:real_name', '实名维护', 'user', 'API');
INSERT INTO `admin_permission` VALUES (7, 'invite:read', '邀请只读', 'invite', 'API');
INSERT INTO `admin_permission` VALUES (8, 'invite:write', '邀请写', 'invite', 'API');
INSERT INTO `admin_permission` VALUES (9, 'bounty:read', '悬赏只读', 'bounty', 'API');
INSERT INTO `admin_permission` VALUES (10, 'bounty:write', '悬赏强制关闭', 'bounty', 'API');
INSERT INTO `admin_permission` VALUES (11, 'bounty:review', '发令审核', 'bounty', 'API');
INSERT INTO `admin_permission` VALUES (12, 'submission:review', '成果审核', 'submission', 'API');
INSERT INTO `admin_permission` VALUES (13, 'wallet:read', '钱庄只读', 'wallet', 'API');
INSERT INTO `admin_permission` VALUES (14, 'wallet:flag', '流水标记', 'wallet', 'API');
INSERT INTO `admin_permission` VALUES (15, 'dispute:read', '纠纷只读', 'dispute', 'API');
INSERT INTO `admin_permission` VALUES (16, 'dispute:verdict', '纠纷终裁', 'dispute', 'API');
INSERT INTO `admin_permission` VALUES (17, 'notice:read', '告示只读', 'notice', 'API');
INSERT INTO `admin_permission` VALUES (18, 'notice:write', '告示写', 'notice', 'API');
INSERT INTO `admin_permission` VALUES (19, 'office:read', '职司只读', 'office', 'API');
INSERT INTO `admin_permission` VALUES (20, 'office:write', '职司写', 'office', 'API');
INSERT INTO `admin_permission` VALUES (21, 'lord:read', '盟主只读', 'lord', 'API');
INSERT INTO `admin_permission` VALUES (22, 'lord:write', '盟主写', 'lord', 'API');
INSERT INTO `admin_permission` VALUES (23, 'config:read', '配置只读', 'config', 'API');
INSERT INTO `admin_permission` VALUES (24, 'config:write', '配置写', 'config', 'API');
INSERT INTO `admin_permission` VALUES (25, 'product:read', '奖品只读', 'product', 'API');
INSERT INTO `admin_permission` VALUES (26, 'product:write', '奖品写', 'product', 'API');
INSERT INTO `admin_permission` VALUES (27, 'checklist:read', '清单只读', 'checklist', 'API');
INSERT INTO `admin_permission` VALUES (28, 'checklist:write', '清单写', 'checklist', 'API');
INSERT INTO `admin_permission` VALUES (29, 'warrant_config:read', '令状配置只读', 'warrant_config', 'API');
INSERT INTO `admin_permission` VALUES (30, 'warrant_config:write', '令状配置写', 'warrant_config', 'API');
INSERT INTO `admin_permission` VALUES (31, 'audit:read', '审计只读', 'audit', 'API');
INSERT INTO `admin_permission` VALUES (32, 'report:read', '举报只读', 'report', 'API');
INSERT INTO `admin_permission` VALUES (33, 'report:write', '举报处理', 'report', 'API');
INSERT INTO `admin_permission` VALUES (34, 'job:read', '任务只读', 'job', 'API');
INSERT INTO `admin_permission` VALUES (35, 'admin:read', '管理员只读', 'admin', 'API');
INSERT INTO `admin_permission` VALUES (36, 'admin:write', '管理员写', 'admin', 'API');
INSERT INTO `admin_permission` VALUES (37, 'role:read', '角色只读', 'role', 'API');
INSERT INTO `admin_permission` VALUES (38, 'role:write', '角色写', 'role', 'API');
INSERT INTO `admin_permission` VALUES (39, 'menu:read', '菜单只读', 'menu', 'API');
INSERT INTO `admin_permission` VALUES (40, 'menu:write', '菜单写', 'menu', 'API');
INSERT INTO `admin_permission` VALUES (64, 'submission:read', '成果审核只读', 'submission', 'API');
INSERT INTO `admin_permission` VALUES (65, 'feedback:read', '用户反馈只读', 'feedback', 'API');
INSERT INTO `admin_permission` VALUES (66, 'feedback:write', '用户反馈写', 'feedback', 'API');

-- ----------------------------
-- Table structure for admin_role
-- ----------------------------
DROP TABLE IF EXISTS `admin_role`;
CREATE TABLE `admin_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `builtin` tinyint(1) NOT NULL DEFAULT 1,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_role_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_role
-- ----------------------------
INSERT INTO `admin_role` VALUES (1, 'SUPER_ADMIN', '超级管理员', 1, '全部 L0；含权限模型与管理员账号', 'ACTIVE', '2026-08-05 15:52:18', '2026-08-05 15:52:18');
INSERT INTO `admin_role` VALUES (2, 'OPS_ADMIN', '运营管理员', 1, '用户/悬赏/配置/职司/公告等运营权限', 'ACTIVE', '2026-08-05 15:52:18', '2026-08-05 15:52:18');
INSERT INTO `admin_role` VALUES (3, 'ARBITER', '终裁仲裁员', 1, '纠纷终裁、审核改判', 'ACTIVE', '2026-08-05 15:52:18', '2026-08-05 15:52:18');
INSERT INTO `admin_role` VALUES (4, 'OBSERVER', '观察者', 1, '只读日志与报表', 'ACTIVE', '2026-08-05 15:52:18', '2026-08-05 15:52:18');

-- ----------------------------
-- Table structure for admin_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `admin_role_permission`;
CREATE TABLE `admin_role_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `role_id` bigint(20) NOT NULL,
  `permission_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_perm`(`role_id`, `permission_id`) USING BTREE,
  INDEX `idx_arp_perm`(`permission_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 111 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_role_permission
-- ----------------------------
INSERT INTO `admin_role_permission` VALUES (1, 1, 1);
INSERT INTO `admin_role_permission` VALUES (11, 2, 2);
INSERT INTO `admin_role_permission` VALUES (30, 2, 3);
INSERT INTO `admin_role_permission` VALUES (32, 2, 4);
INSERT INTO `admin_role_permission` VALUES (29, 2, 5);
INSERT INTO `admin_role_permission` VALUES (31, 2, 6);
INSERT INTO `admin_role_permission` VALUES (13, 2, 7);
INSERT INTO `admin_role_permission` VALUES (14, 2, 8);
INSERT INTO `admin_role_permission` VALUES (4, 2, 9);
INSERT INTO `admin_role_permission` VALUES (6, 2, 10);
INSERT INTO `admin_role_permission` VALUES (5, 2, 11);
INSERT INTO `admin_role_permission` VALUES (28, 2, 12);
INSERT INTO `admin_role_permission` VALUES (34, 2, 13);
INSERT INTO `admin_role_permission` VALUES (33, 2, 14);
INSERT INTO `admin_role_permission` VALUES (12, 2, 15);
INSERT INTO `admin_role_permission` VALUES (19, 2, 17);
INSERT INTO `admin_role_permission` VALUES (20, 2, 18);
INSERT INTO `admin_role_permission` VALUES (21, 2, 19);
INSERT INTO `admin_role_permission` VALUES (22, 2, 20);
INSERT INTO `admin_role_permission` VALUES (16, 2, 21);
INSERT INTO `admin_role_permission` VALUES (17, 2, 22);
INSERT INTO `admin_role_permission` VALUES (9, 2, 23);
INSERT INTO `admin_role_permission` VALUES (10, 2, 24);
INSERT INTO `admin_role_permission` VALUES (23, 2, 25);
INSERT INTO `admin_role_permission` VALUES (24, 2, 26);
INSERT INTO `admin_role_permission` VALUES (7, 2, 27);
INSERT INTO `admin_role_permission` VALUES (8, 2, 28);
INSERT INTO `admin_role_permission` VALUES (35, 2, 29);
INSERT INTO `admin_role_permission` VALUES (36, 2, 30);
INSERT INTO `admin_role_permission` VALUES (3, 2, 31);
INSERT INTO `admin_role_permission` VALUES (25, 2, 32);
INSERT INTO `admin_role_permission` VALUES (26, 2, 33);
INSERT INTO `admin_role_permission` VALUES (15, 2, 34);
INSERT INTO `admin_role_permission` VALUES (2, 2, 35);
INSERT INTO `admin_role_permission` VALUES (27, 2, 37);
INSERT INTO `admin_role_permission` VALUES (18, 2, 39);
INSERT INTO `admin_role_permission` VALUES (111, 2, 64);
INSERT INTO `admin_role_permission` VALUES (114, 2, 65);
INSERT INTO `admin_role_permission` VALUES (115, 2, 66);
INSERT INTO `admin_role_permission` VALUES (68, 3, 2);
INSERT INTO `admin_role_permission` VALUES (73, 3, 3);
INSERT INTO `admin_role_permission` VALUES (66, 3, 9);
INSERT INTO `admin_role_permission` VALUES (67, 3, 11);
INSERT INTO `admin_role_permission` VALUES (72, 3, 12);
INSERT INTO `admin_role_permission` VALUES (74, 3, 13);
INSERT INTO `admin_role_permission` VALUES (69, 3, 15);
INSERT INTO `admin_role_permission` VALUES (70, 3, 16);
INSERT INTO `admin_role_permission` VALUES (65, 3, 31);
INSERT INTO `admin_role_permission` VALUES (71, 3, 39);
INSERT INTO `admin_role_permission` VALUES (112, 3, 64);
INSERT INTO `admin_role_permission` VALUES (85, 4, 2);
INSERT INTO `admin_role_permission` VALUES (96, 4, 3);
INSERT INTO `admin_role_permission` VALUES (87, 4, 7);
INSERT INTO `admin_role_permission` VALUES (82, 4, 9);
INSERT INTO `admin_role_permission` VALUES (97, 4, 13);
INSERT INTO `admin_role_permission` VALUES (86, 4, 15);
INSERT INTO `admin_role_permission` VALUES (91, 4, 17);
INSERT INTO `admin_role_permission` VALUES (92, 4, 19);
INSERT INTO `admin_role_permission` VALUES (89, 4, 21);
INSERT INTO `admin_role_permission` VALUES (84, 4, 23);
INSERT INTO `admin_role_permission` VALUES (93, 4, 25);
INSERT INTO `admin_role_permission` VALUES (83, 4, 27);
INSERT INTO `admin_role_permission` VALUES (98, 4, 29);
INSERT INTO `admin_role_permission` VALUES (81, 4, 31);
INSERT INTO `admin_role_permission` VALUES (94, 4, 32);
INSERT INTO `admin_role_permission` VALUES (88, 4, 34);
INSERT INTO `admin_role_permission` VALUES (80, 4, 35);
INSERT INTO `admin_role_permission` VALUES (95, 4, 37);
INSERT INTO `admin_role_permission` VALUES (90, 4, 39);
INSERT INTO `admin_role_permission` VALUES (113, 4, 64);
INSERT INTO `admin_role_permission` VALUES (117, 4, 65);

-- ----------------------------
-- Table structure for admin_user
-- ----------------------------
DROP TABLE IF EXISTS `admin_user`;
CREATE TABLE `admin_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `display_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_user
-- ----------------------------
INSERT INTO `admin_user` VALUES (1, 'admin', '$2a$10$umn.cNn08Kx/FxGeCI.jjuAHa6M55LKdT208gIGF3eNKGN3VL1aEO', '武林盟主事', 'ACTIVE', '2026-08-05 13:43:49', '2026-08-05 15:52:18');

-- ----------------------------
-- Table structure for admin_user_role
-- ----------------------------
DROP TABLE IF EXISTS `admin_user_role`;
CREATE TABLE `admin_user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `admin_id` bigint(20) NOT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_role`(`admin_id`, `role_id`) USING BTREE,
  INDEX `idx_aur_role`(`role_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_user_role
-- ----------------------------
INSERT INTO `admin_user_role` VALUES (1, 1, 1);

-- ----------------------------
-- Table structure for audit_log
-- ----------------------------
DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE `audit_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `operator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `action` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `detail` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_audit_time`(`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of audit_log
-- ----------------------------
INSERT INTO `audit_log` VALUES (1, 'admin:1', 'DISPUTE_VERDICT', 'id=2, action=KEEP, qa keep', '2026-08-05 16:01:12');
INSERT INTO `audit_log` VALUES (2, 'admin:1', 'USER_ASSET_ADJUST', 'userId=9, type=BALANCE, delta=50, reason=qa v17 grant', '2026-08-05 16:34:41');
INSERT INTO `audit_log` VALUES (3, 'admin:1', 'OFFICE_APPROVE', 'appId=1, userId=1, code=DECREE_REVIEWER', '2026-08-05 17:08:43');
INSERT INTO `audit_log` VALUES (4, 'admin:1', 'NOTICE_UPDATE', 'id=4', '2026-08-07 13:35:29');
INSERT INTO `audit_log` VALUES (5, 'admin:1', 'CONFIG_GROWTH_UPDATE', '[claimDayLimit, dailyFreeStamina, claimStaminaCost, chivalryPerStamina, submitCooldownSeconds, submitDayLimit, chivalryPerComplete, inviteDailyQuota, minReward, feeRate]', '2026-08-07 16:55:50');

-- ----------------------------
-- Table structure for bounty
-- ----------------------------
DROP TABLE IF EXISTS `bounty`;
CREATE TABLE `bounty`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `publisher_id` bigint(20) NOT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'RENT_SEEK|RENT_OUT',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '遵义',
  `district` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `difficulty` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `reward_amount` decimal(12, 2) NOT NULL,
  `deadline_at` datetime(0) NULL,
  `task_tags_json` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `frozen_biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cancel_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cancel_allocation_pending` tinyint(1) NOT NULL DEFAULT 0 COMMENT '有成果取消待分配',
  `source_bounty_id` bigint(20) NULL DEFAULT NULL COMMENT '再发来源悬赏ID',
  `remind_24h_sent` tinyint(1) NOT NULL DEFAULT 0,
  `remind_2h_sent` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_bounty_status_city`(`status`, `city`, `deadline_at`) USING BTREE,
  INDEX `idx_bounty_publisher`(`publisher_id`) USING BTREE,
  INDEX `idx_bounty_source`(`source_bounty_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bounty
-- ----------------------------
INSERT INTO `bounty` VALUES (1, 1, 'RENT_SEEK', '寻找医学院附近的房子', 'CANCELLED', '遵义', '医学院附近', 'EASY', 250.00, '2026-08-31 00:00:00', '[\"帮寻房\",\"帮带看\",\"帮验房\",\"帮谈价\",\"帮核验真伪\"]', 'FZ-6aae9cb8ac404aa5a8fdb6276d38d5ff', '测试', 0, NULL, 0, 0, '2026-08-05 14:31:23', '2026-08-05 16:49:19');
INSERT INTO `bounty` VALUES (2, 3, 'RENT_SEEK', 'QA-E2E-65568', 'CANCELLED', '遵义', 'MedSchool', 'EASY', 200.00, '2026-08-12 15:40:50', '[\"tag\"]', 'FZ-b8daede945a04cffb7b6ff085692bb2b', '管理员强制关闭', 0, NULL, 0, 0, '2026-08-05 15:40:51', '2026-08-05 16:49:53');
INSERT INTO `bounty` VALUES (3, 5, 'RENT_SEEK', 'QA-S4-41416', 'IN_COLLAB', '遵义', 'Hong', 'EASY', 200.00, '2026-08-12 15:58:28', '[\"tag\"]', 'FZ-0859166828a54d29b07e190263946f2f', NULL, 0, NULL, 0, 0, '2026-08-05 15:58:29', '2026-08-06 13:20:35');
INSERT INTO `bounty` VALUES (4, 6, 'RENT_SEEK', 'S4-73452', 'COMPLETED', '遵义', 'H', 'EASY', 200.00, '2026-08-12 16:01:11', '[\"t\"]', 'FZ-87c7e8d65b01413987b95d0f3ae2864c', NULL, 0, NULL, 0, 0, '2026-08-05 16:01:12', '2026-08-05 16:01:12');
INSERT INTO `bounty` VALUES (5, 1, 'RENT_SEEK', '寻找医学院附近的房子', 'CANCELLED', '遵义', '医学院附近', 'EASY', 250.00, '2026-08-06 00:00:00', '[\"帮寻房\",\"帮带看\",\"帮验房\",\"帮谈价\",\"帮核验真伪\"]', 'FZ-66e405eb3b1646938211e365d5773ce6', '超时自动取消', 0, 1, 1, 1, '2026-08-05 17:04:26', '2026-08-06 00:00:14');
INSERT INTO `bounty` VALUES (6, 12, 'RENT_SEEK', 'V18-SRC-61562', 'REJECTED', '遵义', 'H', 'EASY', 200.00, '2026-08-12 20:07:05', '[\"t\"]', 'FZ-5b127eb20ea5446cb4c0ca9b80d3365f', NULL, 0, NULL, 0, 0, '2026-08-05 20:07:06', '2026-08-05 20:07:06');
INSERT INTO `bounty` VALUES (7, 14, 'RENT_SEEK', 'V18-SRC-19063', 'REJECTED', '遵义', 'H', 'EASY', 200.00, '2026-08-12 20:07:35', '[\"t\"]', 'FZ-7350969cf9c444e8b9eb1c435a9bfbaf', NULL, 0, NULL, 0, 0, '2026-08-05 20:07:35', '2026-08-05 20:07:35');
INSERT INTO `bounty` VALUES (8, 14, 'RENT_SEEK', 'V18-SRC-19063', 'COMPLETED', '遵义', 'H', 'EASY', 200.00, '2026-08-15 20:07:35', '[\"t\"]', 'FZ-4ba2e399f4b74da4abb6d6188dcb7c03', NULL, 0, 7, 0, 0, '2026-08-05 20:07:36', '2026-08-05 20:07:36');
INSERT INTO `bounty` VALUES (9, 16, 'RENT_SEEK', 'QA-E2E-27629', 'IN_DISPUTE', '遵义', 'MedSchool', 'EASY', 200.00, '2026-08-12 23:05:07', '[\"tag\"]', 'FZ-2b205a1540904be38d8d7f95f757bafc', NULL, 0, NULL, 0, 0, '2026-08-05 23:05:07', '2026-08-05 23:05:08');
INSERT INTO `bounty` VALUES (10, 1, 'RENT_SEEK', '寻找医学院附近的房子', 'CANCELLED', '遵义', '医学院附近', 'EASY', 250.00, '2026-08-07 00:00:00', '[\"帮寻房\",\"帮带看\",\"帮验房\",\"帮谈价\",\"帮核验真伪\"]', 'FZ-e41e6652bfd74e02ae5a8cd57c57b0b8', '超时自动取消', 0, 5, 1, 1, '2026-08-06 13:23:50', '2026-08-07 00:00:24');
INSERT INTO `bounty` VALUES (11, 21, 'RENT_SEEK', 'V189-CHAT-75996', 'IN_COLLAB', '遵义', '汇川', 'NORMAL', 200.00, '2026-08-14 11:29:44', '[\"帮寻房\"]', 'FZ-989a942702f145ef920573e54b1b55c4', NULL, 0, NULL, 0, 0, '2026-08-07 11:29:45', '2026-08-07 14:12:56');
INSERT INTO `bounty` VALUES (12, 1, 'RENT_SEEK', '寻找医学院附近的房子', 'CANCELLED', '遵义', '医学院附近', 'EASY', 250.00, '2026-08-08 00:00:00', '[\"帮寻房\",\"帮带看\",\"帮验房\",\"帮谈价\",\"帮核验真伪\"]', 'FZ-ffb8abbb7fa34968a67a621fa8200a0c', 'ces', 0, 10, 1, 0, '2026-08-07 12:43:44', '2026-08-07 15:05:57');
INSERT INTO `bounty` VALUES (13, 23, 'RENT_SEEK', 'LC-78085', 'PENDING_REVIEW', '遵义', '汇川', 'NORMAL', 200.00, '2026-08-17 13:03:31', '[\"帮寻房\"]', 'FZ-55817880ba1f437a81799df5be971ba2', NULL, 0, NULL, 0, 0, '2026-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `bounty` VALUES (14, 25, 'RENT_SEEK', 'LC2-53919', 'CANCELLED', '遵义', '汇川', 'NORMAL', 200.00, '2026-08-17 13:03:31', '[\"帮寻房\"]', 'FZ-de74e97abf5649d6a244ec21a55a442b', 'no-sub-cancel', 0, NULL, 0, 0, '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `bounty` VALUES (15, 25, 'RENT_SEEK', 'LC3-53919', 'PENDING_REVIEW', '遵义', '汇川', 'NORMAL', 200.00, '2026-08-17 13:03:31', '[\"帮寻房\"]', 'FZ-280591a02f9842a292ebfec460bbafaa', NULL, 0, NULL, 0, 0, '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `bounty` VALUES (16, 25, 'RENT_SEEK', 'LC4-53919', 'PENDING_REVIEW', '遵义', '汇川', 'NORMAL', 200.00, '2026-08-17 13:03:31', '[\"帮寻房\"]', 'FZ-7cbabdc3fcb445f18b0e6b01c6d2bc44', NULL, 0, NULL, 0, 0, '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `bounty` VALUES (19, 30, 'RENT_SEEK', 'LC-13676', 'IN_COLLAB', '遵义', '汇川', 'NORMAL', 200.00, '2026-08-17 13:04:21', '[\"帮寻房\"]', 'FZ-7e5b776c0bf0406e84fc18824df90033', NULL, 0, NULL, 0, 0, '2026-08-07 13:04:21', '2026-08-07 13:04:22');
INSERT INTO `bounty` VALUES (20, 32, 'RENT_SEEK', 'LC2-78902', 'CANCELLED', '遵义', '汇川', 'NORMAL', 200.00, '2026-08-17 13:04:21', '[\"帮寻房\"]', 'FZ-7c416aaac15e45b0a91c5a65b3919834', 'no-sub-cancel', 0, NULL, 0, 0, '2026-08-07 13:04:22', '2026-08-07 13:04:22');
INSERT INTO `bounty` VALUES (21, 32, 'RENT_SEEK', 'LC3-78902', 'REJECTED', '遵义', '汇川', 'NORMAL', 200.00, '2026-08-17 13:04:21', '[\"帮寻房\"]', 'FZ-adf1be4bbc4a4230a8674d1263f68d1c', NULL, 0, NULL, 0, 0, '2026-08-07 13:04:22', '2026-08-07 13:04:22');
INSERT INTO `bounty` VALUES (22, 32, 'RENT_SEEK', 'LC4-78902', 'IN_COLLAB', '遵义', '汇川', 'NORMAL', 200.00, '2026-08-17 13:04:21', '[\"帮寻房\"]', 'FZ-4322ed274c1f465a8ab148db8c131030', NULL, 0, NULL, 0, 0, '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `bounty` VALUES (23, 32, 'RENT_SEEK', 'LC5-78902', 'CANCELLED', '遵义', '汇川', 'NORMAL', 200.00, '2026-08-17 13:04:21', '[\"帮寻房\"]', 'FZ-72a4144922dc4b0d93fcabd80f58a5c7', 'has-sub-cancel', 0, NULL, 0, 0, '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `bounty` VALUES (24, 32, 'RENT_SEEK', 'LC6-78902', 'CANCELLED', '遵义', '汇川', 'NORMAL', 200.00, '2026-08-17 13:04:21', '[\"帮寻房\"]', 'FZ-61c1b1d56b0e431da6cbc3a58b1ba233', 'ps-cancel', 0, NULL, 0, 0, '2026-08-07 13:04:24', '2026-08-07 13:04:24');
INSERT INTO `bounty` VALUES (25, 1, 'RENT_SEEK', '测试的悬赏', 'IN_COLLAB', '遵义', '德宝花园小区', 'EXTREME', 550.00, '2026-08-10 00:00:00', '[\"帮带看\",\"帮寻房\",\"帮谈价\",\"帮验房\"]', 'FZ-372237dcecb841e391a42b926649636d', NULL, 0, NULL, 0, 0, '2026-08-07 15:40:42', '2026-08-07 16:02:34');

-- ----------------------------
-- Table structure for bounty_checklist
-- ----------------------------
DROP TABLE IF EXISTS `bounty_checklist`;
CREATE TABLE `bounty_checklist`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bounty_id` bigint(20) NOT NULL,
  `item_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `item_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `required` tinyint(1) NOT NULL DEFAULT 0,
  `sort_no` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_checklist_bounty`(`bounty_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bounty_checklist
-- ----------------------------
INSERT INTO `bounty_checklist` VALUES (1, 1, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (2, 1, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (3, 1, 'PHOTO_EVIDENCE', '现场照片/视频', 1, 3);
INSERT INTO `bounty_checklist` VALUES (4, 1, 'NEIGHBORHOOD_NOTE', '周边配套备注', 0, 4);
INSERT INTO `bounty_checklist` VALUES (5, 1, 'CONTRACT_HINT', '合同/中介风险提示', 0, 5);
INSERT INTO `bounty_checklist` VALUES (6, 1, 'LANDLORD_CONTACT', '房东沟通记录', 0, 6);
INSERT INTO `bounty_checklist` VALUES (7, 2, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (8, 2, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (9, 3, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (10, 3, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (11, 4, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (12, 4, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (13, 5, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (14, 5, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (15, 5, 'PHOTO_EVIDENCE', '现场照片/视频', 1, 3);
INSERT INTO `bounty_checklist` VALUES (16, 5, 'NEIGHBORHOOD_NOTE', '周边配套备注', 0, 4);
INSERT INTO `bounty_checklist` VALUES (17, 5, 'CONTRACT_HINT', '合同/中介风险提示', 0, 5);
INSERT INTO `bounty_checklist` VALUES (18, 6, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (19, 6, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (20, 7, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (21, 7, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (22, 8, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (23, 8, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (24, 9, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (25, 9, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (26, 10, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (27, 10, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (28, 10, 'PHOTO_EVIDENCE', '现场照片/视频', 1, 3);
INSERT INTO `bounty_checklist` VALUES (29, 10, 'NEIGHBORHOOD_NOTE', '周边配套备注', 0, 4);
INSERT INTO `bounty_checklist` VALUES (30, 10, 'CONTRACT_HINT', '合同/中介风险提示', 0, 5);
INSERT INTO `bounty_checklist` VALUES (31, 12, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (32, 12, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (33, 12, 'PHOTO_EVIDENCE', '现场照片/视频', 1, 3);
INSERT INTO `bounty_checklist` VALUES (34, 12, 'NEIGHBORHOOD_NOTE', '周边配套备注', 0, 4);
INSERT INTO `bounty_checklist` VALUES (35, 12, 'CONTRACT_HINT', '合同/中介风险提示', 0, 5);
INSERT INTO `bounty_checklist` VALUES (36, 25, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, 1);
INSERT INTO `bounty_checklist` VALUES (37, 25, 'SITE_VISIT_RECORD', '现场带看记录', 1, 2);
INSERT INTO `bounty_checklist` VALUES (38, 25, 'PHOTO_EVIDENCE', '现场照片/视频', 1, 3);

-- ----------------------------
-- Table structure for bounty_claim
-- ----------------------------
DROP TABLE IF EXISTS `bounty_claim`;
CREATE TABLE `bounty_claim`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bounty_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `stamina_cost` int(11) NOT NULL DEFAULT 1,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_claim`(`bounty_id`, `user_id`) USING BTREE,
  INDEX `idx_claim_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bounty_claim
-- ----------------------------
INSERT INTO `bounty_claim` VALUES (1, 2, 4, 1, 'ACTIVE', '2026-08-05 15:40:51');
INSERT INTO `bounty_claim` VALUES (2, 4, 8, 1, 'ACTIVE', '2026-08-05 16:01:12');
INSERT INTO `bounty_claim` VALUES (3, 8, 15, 1, 'ACTIVE', '2026-08-05 20:07:36');
INSERT INTO `bounty_claim` VALUES (4, 9, 17, 1, 'ACTIVE', '2026-08-05 23:05:08');
INSERT INTO `bounty_claim` VALUES (5, 3, 20, 1, 'ACTIVE', '2026-08-06 13:20:35');
INSERT INTO `bounty_claim` VALUES (6, 10, 20, 1, 'ACTIVE', '2026-08-06 13:25:54');
INSERT INTO `bounty_claim` VALUES (7, 12, 20, 1, 'ACTIVE', '2026-08-07 12:46:11');
INSERT INTO `bounty_claim` VALUES (8, 19, 31, 1, 'ACTIVE', '2026-08-07 13:04:22');
INSERT INTO `bounty_claim` VALUES (9, 20, 33, 1, 'ACTIVE', '2026-08-07 13:04:22');
INSERT INTO `bounty_claim` VALUES (10, 22, 34, 1, 'ACTIVE', '2026-08-07 13:04:23');
INSERT INTO `bounty_claim` VALUES (11, 23, 35, 1, 'ACTIVE', '2026-08-07 13:04:23');
INSERT INTO `bounty_claim` VALUES (12, 24, 36, 1, 'ACTIVE', '2026-08-07 13:04:24');
INSERT INTO `bounty_claim` VALUES (13, 11, 20, 1, 'ACTIVE', '2026-08-07 14:12:56');
INSERT INTO `bounty_claim` VALUES (14, 25, 20, 1, 'ACTIVE', '2026-08-07 16:02:34');
INSERT INTO `bounty_claim` VALUES (15, 25, 38, 1, 'ACTIVE', '2026-08-07 16:27:00');

-- ----------------------------
-- Table structure for bounty_message
-- ----------------------------
DROP TABLE IF EXISTS `bounty_message`;
CREATE TABLE `bounty_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bounty_id` bigint(20) NOT NULL,
  `sender_id` bigint(20) NOT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_msg_bounty`(`bounty_id`, `id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bounty_message
-- ----------------------------
INSERT INTO `bounty_message` VALUES (1, 2, 4, 'hello', '2026-08-05 15:40:51');
INSERT INTO `bounty_message` VALUES (2, 9, 17, 'hello', '2026-08-05 23:05:08');
INSERT INTO `bounty_message` VALUES (3, 3, 20, '你好', '2026-08-06 13:20:49');
INSERT INTO `bounty_message` VALUES (4, 10, 20, '你好啊', '2026-08-06 13:26:07');
INSERT INTO `bounty_message` VALUES (5, 11, 21, 'hello-from-publisher', '2026-08-07 11:29:45');
INSERT INTO `bounty_message` VALUES (6, 10, 20, 'dsfads', '2026-08-07 11:31:01');
INSERT INTO `bounty_message` VALUES (7, 12, 20, '你好', '2026-08-07 12:46:21');
INSERT INTO `bounty_message` VALUES (8, 13, 23, 'msg-from-publisher', '2026-08-07 13:03:32');
INSERT INTO `bounty_message` VALUES (9, 14, 25, 'after-cancel', '2026-08-07 13:03:33');
INSERT INTO `bounty_message` VALUES (10, 15, 25, 'rej', '2026-08-07 13:03:33');
INSERT INTO `bounty_message` VALUES (11, 16, 25, 'after-complete', '2026-08-07 13:03:33');
INSERT INTO `bounty_message` VALUES (12, 19, 30, 'msg-from-publisher', '2026-08-07 13:04:22');
INSERT INTO `bounty_message` VALUES (13, 19, 31, 'msg-from-claimer', '2026-08-07 13:04:22');
INSERT INTO `bounty_message` VALUES (14, 19, 31, 'should-fail-after-quit', '2026-08-07 13:04:22');
INSERT INTO `bounty_message` VALUES (15, 20, 32, 'after-cancel', '2026-08-07 13:04:22');
INSERT INTO `bounty_message` VALUES (16, 21, 32, 'rej', '2026-08-07 13:04:23');
INSERT INTO `bounty_message` VALUES (17, 22, 32, 'after-complete', '2026-08-07 13:04:23');
INSERT INTO `bounty_message` VALUES (18, 23, 32, 'x', '2026-08-07 13:04:23');
INSERT INTO `bounty_message` VALUES (19, 24, 32, 'x', '2026-08-07 13:04:24');
INSERT INTO `bounty_message` VALUES (20, 12, 20, '巴萨夺冠发生的', '2026-08-07 13:13:35');
INSERT INTO `bounty_message` VALUES (21, 12, 1, '你是谁', '2026-08-07 13:14:07');
INSERT INTO `bounty_message` VALUES (22, 25, 20, '你好，我可以帮你找', '2026-08-07 16:02:55');
INSERT INTO `bounty_message` VALUES (23, 25, 20, '在吗', '2026-08-07 16:08:04');
INSERT INTO `bounty_message` VALUES (24, 25, 1, '好啊', '2026-08-07 16:26:37');
INSERT INTO `bounty_message` VALUES (25, 25, 38, '我也来帮你', '2026-08-07 16:27:24');

-- ----------------------------
-- Table structure for bounty_warrant
-- ----------------------------
DROP TABLE IF EXISTS `bounty_warrant`;
CREATE TABLE `bounty_warrant`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bounty_id` bigint(20) NOT NULL,
  `template_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `fields_json` json NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warrant_bounty`(`bounty_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of bounty_warrant
-- ----------------------------
INSERT INTO `bounty_warrant` VALUES (1, 1, 'RENT_SEEK', '{\"extra\": \"\", \"layout\": \"两室一厅\", \"district\": \"医学院附近\", \"acceptAgency\": false, \"rentBudgetMax\": \"1000\", \"rentBudgetMin\": \"800\", \"expectMoveInDate\": \"30日内\"}');
INSERT INTO `bounty_warrant` VALUES (2, 2, 'RENT_SEEK', '{\"extra\": \"\", \"layout\": \"2BR\", \"district\": \"MedSchool\", \"acceptAgency\": false, \"rentBudgetMax\": 1000, \"rentBudgetMin\": 800, \"expectMoveInDate\": \"30d\"}');
INSERT INTO `bounty_warrant` VALUES (3, 3, 'RENT_SEEK', '{\"extra\": \"\", \"layout\": \"2BR\", \"district\": \"Hong\", \"acceptAgency\": false, \"rentBudgetMax\": 1000, \"rentBudgetMin\": 800, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (4, 4, 'RENT_SEEK', '{\"extra\": \"\", \"layout\": \"2\", \"district\": \"H\", \"acceptAgency\": false, \"rentBudgetMax\": 1000, \"rentBudgetMin\": 800, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (5, 5, 'RENT_SEEK', '{\"extra\": \"\", \"layout\": \"两室一厅\", \"district\": \"医学院附近\", \"acceptAgency\": false, \"rentBudgetMax\": \"1000\", \"rentBudgetMin\": \"800\", \"expectMoveInDate\": \"30日内\"}');
INSERT INTO `bounty_warrant` VALUES (6, 6, 'RENT_SEEK', '{\"extra\": \"\", \"layout\": \"2\", \"district\": \"H\", \"acceptAgency\": false, \"rentBudgetMax\": 1000, \"rentBudgetMin\": 800, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (7, 7, 'RENT_SEEK', '{\"extra\": \"\", \"layout\": \"2\", \"district\": \"H\", \"acceptAgency\": false, \"rentBudgetMax\": 1000, \"rentBudgetMin\": 800, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (8, 8, 'RENT_SEEK', '{\"extra\": \"\", \"layout\": \"2\", \"district\": \"H\", \"acceptAgency\": false, \"rentBudgetMax\": 1000, \"rentBudgetMin\": 800, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (9, 9, 'RENT_SEEK', '{\"extra\": \"\", \"layout\": \"2BR\", \"district\": \"MedSchool\", \"acceptAgency\": false, \"rentBudgetMax\": 1000, \"rentBudgetMin\": 800, \"expectMoveInDate\": \"30d\"}');
INSERT INTO `bounty_warrant` VALUES (10, 10, 'RENT_SEEK', '{\"extra\": \"\", \"layout\": \"两室一厅\", \"district\": \"医学院附近\", \"acceptAgency\": false, \"rentBudgetMax\": \"1000\", \"rentBudgetMin\": \"800\", \"expectMoveInDate\": \"30日内\"}');
INSERT INTO `bounty_warrant` VALUES (11, 11, 'RENT_SEEK', '{\"extra\": \"qa\", \"layout\": \"两室\", \"district\": \"汇川\", \"acceptAgency\": false, \"rentBudgetMax\": 2000, \"rentBudgetMin\": 1000, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (12, 12, 'RENT_SEEK', '{\"extra\": \"\", \"layout\": \"两室一厅\", \"district\": \"医学院附近\", \"acceptAgency\": false, \"rentBudgetMax\": \"1000\", \"rentBudgetMin\": \"800\", \"expectMoveInDate\": \"30日内\"}');
INSERT INTO `bounty_warrant` VALUES (13, 13, 'RENT_SEEK', '{\"extra\": \"qa\", \"layout\": \"两室\", \"district\": \"汇川\", \"acceptAgency\": false, \"rentBudgetMax\": 2000, \"rentBudgetMin\": 1000, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (14, 14, 'RENT_SEEK', '{\"extra\": \"qa\", \"layout\": \"两室\", \"district\": \"汇川\", \"acceptAgency\": false, \"rentBudgetMax\": 2000, \"rentBudgetMin\": 1000, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (15, 15, 'RENT_SEEK', '{\"extra\": \"qa\", \"layout\": \"两室\", \"district\": \"汇川\", \"acceptAgency\": false, \"rentBudgetMax\": 2000, \"rentBudgetMin\": 1000, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (16, 16, 'RENT_SEEK', '{\"extra\": \"qa\", \"layout\": \"两室\", \"district\": \"汇川\", \"acceptAgency\": false, \"rentBudgetMax\": 2000, \"rentBudgetMin\": 1000, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (17, 19, 'RENT_SEEK', '{\"extra\": \"qa\", \"layout\": \"两室\", \"district\": \"汇川\", \"acceptAgency\": false, \"rentBudgetMax\": 2000, \"rentBudgetMin\": 1000, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (18, 20, 'RENT_SEEK', '{\"extra\": \"qa\", \"layout\": \"两室\", \"district\": \"汇川\", \"acceptAgency\": false, \"rentBudgetMax\": 2000, \"rentBudgetMin\": 1000, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (19, 21, 'RENT_SEEK', '{\"extra\": \"qa\", \"layout\": \"两室\", \"district\": \"汇川\", \"acceptAgency\": false, \"rentBudgetMax\": 2000, \"rentBudgetMin\": 1000, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (20, 22, 'RENT_SEEK', '{\"extra\": \"qa\", \"layout\": \"两室\", \"district\": \"汇川\", \"acceptAgency\": false, \"rentBudgetMax\": 2000, \"rentBudgetMin\": 1000, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (21, 23, 'RENT_SEEK', '{\"extra\": \"qa\", \"layout\": \"两室\", \"district\": \"汇川\", \"acceptAgency\": false, \"rentBudgetMax\": 2000, \"rentBudgetMin\": 1000, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (22, 24, 'RENT_SEEK', '{\"extra\": \"qa\", \"layout\": \"两室\", \"district\": \"汇川\", \"acceptAgency\": false, \"rentBudgetMax\": 2000, \"rentBudgetMin\": 1000, \"expectMoveInDate\": \"2026-09-01\"}');
INSERT INTO `bounty_warrant` VALUES (23, 25, 'RENT_SEEK', '{\"extra\": \"这是测试的补充说明\", \"layout\": \"三室一厅\", \"district\": \"德宝花园小区\", \"acceptAgency\": false, \"rentBudgetMax\": \"1000\", \"rentBudgetMin\": \"800\", \"expectMoveInDate\": \"半个月内\"}');

-- ----------------------------
-- Table structure for checklist_template
-- ----------------------------
DROP TABLE IF EXISTS `checklist_template`;
CREATE TABLE `checklist_template`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `item_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `item_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `required` tinyint(1) NOT NULL DEFAULT 0,
  `tags_json` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort_no` int(11) NOT NULL DEFAULT 0,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_checklist_code`(`item_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of checklist_template
-- ----------------------------
INSERT INTO `checklist_template` VALUES (1, 'VERIFY_AUTHENTIC', '核验房源真实性', 1, '[\"帮寻房\",\"帮带看\",\"帮验房\"]', 1, 'ACTIVE');
INSERT INTO `checklist_template` VALUES (2, 'SITE_VISIT_RECORD', '现场带看记录', 1, '[\"帮带看\",\"帮验房\"]', 2, 'ACTIVE');
INSERT INTO `checklist_template` VALUES (3, 'PHOTO_EVIDENCE', '现场照片/视频', 1, '[\"帮带看\",\"帮验房\"]', 3, 'ACTIVE');
INSERT INTO `checklist_template` VALUES (4, 'NEIGHBORHOOD_NOTE', '周边配套备注', 0, '[\"帮寻房\",\"帮带看\"]', 4, 'ACTIVE');
INSERT INTO `checklist_template` VALUES (5, 'CONTRACT_HINT', '合同/中介风险提示', 0, '[\"帮验房\"]', 5, 'ACTIVE');
INSERT INTO `checklist_template` VALUES (6, 'LANDLORD_CONTACT', '房东沟通记录', 0, '[\"帮寻租客\"]', 6, 'ACTIVE');

-- ----------------------------
-- Table structure for dispute
-- ----------------------------
DROP TABLE IF EXISTS `dispute`;
CREATE TABLE `dispute`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `settlement_id` bigint(20) NULL DEFAULT NULL,
  `bounty_id` bigint(20) NOT NULL,
  `initiator_id` bigint(20) NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'OPEN' COMMENT 'OPEN|CLOSED',
  `reason` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `evidence_json` json NULL,
  `verdict_json` json NULL,
  `deadline_at` datetime(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_dispute_status`(`status`, `id`) USING BTREE,
  INDEX `idx_dispute_bounty`(`bounty_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dispute
-- ----------------------------
INSERT INTO `dispute` VALUES (1, 1, 2, 3, 'OPEN', 'qa dispute', '{\"text\": \"qa\", \"urls\": []}', NULL, '2026-08-12 15:40:52', '2026-08-05 15:40:52', '2026-08-05 15:40:52');
INSERT INTO `dispute` VALUES (2, 2, 4, 6, 'CLOSED', 'qa', '{\"text\": \"x\", \"urls\": []}', '{\"at\": \"2026-08-05T16:01:12.258259900\", \"action\": \"KEEP\", \"comment\": \"qa keep\"}', '2026-08-12 16:01:12', '2026-08-05 16:01:12', '2026-08-05 16:01:12');
INSERT INTO `dispute` VALUES (3, 4, 9, 16, 'OPEN', 'qa dispute', '{\"text\": \"qa\", \"urls\": []}', NULL, '2026-08-12 23:05:08', '2026-08-05 23:05:08', '2026-08-05 23:05:08');

-- ----------------------------
-- Table structure for evaluation
-- ----------------------------
DROP TABLE IF EXISTS `evaluation`;
CREATE TABLE `evaluation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bounty_id` bigint(20) NOT NULL,
  `from_user_id` bigint(20) NOT NULL,
  `to_user_id` bigint(20) NOT NULL,
  `score` int(11) NOT NULL,
  `content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_eval`(`bounty_id`, `from_user_id`, `to_user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of evaluation
-- ----------------------------
INSERT INTO `evaluation` VALUES (1, 2, 3, 4, 5, 'good', '2026-08-05 15:40:52');
INSERT INTO `evaluation` VALUES (2, 2, 4, 3, 5, 'good', '2026-08-05 15:40:52');
INSERT INTO `evaluation` VALUES (3, 9, 16, 17, 5, 'good', '2026-08-05 23:05:08');
INSERT INTO `evaluation` VALUES (4, 9, 17, 16, 5, 'good', '2026-08-05 23:05:08');

-- ----------------------------
-- Table structure for invite_code
-- ----------------------------
DROP TABLE IF EXISTS `invite_code`;
CREATE TABLE `invite_code`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `owner_user_id` bigint(20) NULL DEFAULT NULL,
  `quota` int(11) NOT NULL DEFAULT 1,
  `used_count` int(11) NOT NULL DEFAULT 0,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|INVALID',
  `expire_at` datetime(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_invite_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of invite_code
-- ----------------------------
INSERT INTO `invite_code` VALUES (1, 'JHOPEN1', NULL, 999, 2, 'ACTIVE', '2027-08-05 13:40:37', '2026-08-05 13:40:37');
INSERT INTO `invite_code` VALUES (2, 'JHOPEN2', NULL, 999, 4, 'ACTIVE', '2027-08-05 13:40:37', '2026-08-05 13:40:37');
INSERT INTO `invite_code` VALUES (3, '4ECZL87C', NULL, 5, 0, 'ACTIVE', '2027-08-05 15:39:54', '2026-08-05 15:39:54');
INSERT INTO `invite_code` VALUES (4, 'SHLCEYSG', NULL, 10, 1, 'ACTIVE', '2027-08-05 15:58:28', '2026-08-05 15:58:28');
INSERT INTO `invite_code` VALUES (5, 'GUZL5XGH', NULL, 5, 0, 'ACTIVE', '2027-08-05 15:58:29', '2026-08-05 15:58:29');
INSERT INTO `invite_code` VALUES (6, 'LBY3ZE25', NULL, 3, 0, 'ACTIVE', '2027-08-05 15:58:29', '2026-08-05 15:58:29');
INSERT INTO `invite_code` VALUES (7, 'JNQFJS8T', NULL, 5, 0, 'ACTIVE', '2027-08-05 15:58:54', '2026-08-05 15:58:54');
INSERT INTO `invite_code` VALUES (8, '2Q3Q469A', NULL, 5, 1, 'ACTIVE', '2027-08-05 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `invite_code` VALUES (9, 'GN469TJ2', NULL, 5, 1, 'ACTIVE', '2027-08-05 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `invite_code` VALUES (10, 'RXDVF5U9', NULL, 5, 1, 'ACTIVE', '2027-08-05 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `invite_code` VALUES (11, 'RCZE72', 1, 1, 1, 'ACTIVE', '2026-09-04 16:05:48', '2026-08-05 16:05:48');
INSERT INTO `invite_code` VALUES (12, 'HXD4RZUQ', NULL, 5, 1, 'ACTIVE', '2027-08-05 16:34:40', '2026-08-05 16:34:40');
INSERT INTO `invite_code` VALUES (13, 'XUSRTB', 9, 1, 1, 'ACTIVE', '2026-09-04 16:34:41', '2026-08-05 16:34:41');
INSERT INTO `invite_code` VALUES (14, 'WWBFMN', 9, 1, 1, 'ACTIVE', '2026-09-04 16:34:41', '2026-08-05 16:34:41');
INSERT INTO `invite_code` VALUES (15, 'CMJRKFK9', NULL, 5, 1, 'ACTIVE', '2027-08-05 20:07:06', '2026-08-05 20:07:06');
INSERT INTO `invite_code` VALUES (16, 'ZVV8CHQB', NULL, 5, 1, 'ACTIVE', '2027-08-05 20:07:06', '2026-08-05 20:07:06');
INSERT INTO `invite_code` VALUES (17, 'DFMPPW67', NULL, 5, 1, 'ACTIVE', '2027-08-05 20:07:35', '2026-08-05 20:07:35');
INSERT INTO `invite_code` VALUES (18, '52VHVEYM', NULL, 5, 1, 'ACTIVE', '2027-08-05 20:07:35', '2026-08-05 20:07:35');
INSERT INTO `invite_code` VALUES (19, 'RMV3QZNE', NULL, 1, 0, 'ACTIVE', '2027-08-05 23:34:21', '2026-08-05 23:34:21');
INSERT INTO `invite_code` VALUES (20, 'D94XB6FN', NULL, 1, 1, 'ACTIVE', '2027-08-05 23:35:19', '2026-08-05 23:35:19');
INSERT INTO `invite_code` VALUES (21, '5SJEJQ8L', NULL, 1, 1, 'ACTIVE', '2027-08-05 23:59:34', '2026-08-05 23:59:34');
INSERT INTO `invite_code` VALUES (22, 'W7JCBKLE', NULL, 1, 1, 'ACTIVE', '2027-08-07 11:29:44', '2026-08-07 11:29:44');
INSERT INTO `invite_code` VALUES (23, 'QBUUQ8ZG', NULL, 1, 1, 'ACTIVE', '2027-08-07 11:29:44', '2026-08-07 11:29:44');
INSERT INTO `invite_code` VALUES (24, 'PEB97NHT', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `invite_code` VALUES (25, 'EYK5WN8W', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `invite_code` VALUES (26, '9TE3BR8V', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `invite_code` VALUES (27, '3CVWTSR6', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `invite_code` VALUES (28, 'WTMXFLLF', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `invite_code` VALUES (29, '2HRNXYBK', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `invite_code` VALUES (30, 'C7WG3BUN', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:03:34', '2026-08-07 13:03:34');
INSERT INTO `invite_code` VALUES (31, 'PMPK8PPN', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:04:21', '2026-08-07 13:04:21');
INSERT INTO `invite_code` VALUES (32, '2YF7TP2S', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:04:21', '2026-08-07 13:04:21');
INSERT INTO `invite_code` VALUES (33, 'PR34VL5D', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:04:22', '2026-08-07 13:04:22');
INSERT INTO `invite_code` VALUES (34, 'Z6DRA2F9', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:04:22', '2026-08-07 13:04:22');
INSERT INTO `invite_code` VALUES (35, 'H76YUGAS', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `invite_code` VALUES (36, 'CGCN4TEN', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `invite_code` VALUES (37, 'UKXBQJPZ', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `invite_code` VALUES (38, '5GHGUH5M', NULL, 1, 1, 'ACTIVE', '2027-08-07 13:04:39', '2026-08-07 13:04:39');
INSERT INTO `invite_code` VALUES (39, 'J88LQ4', 1, 1, 1, 'ACTIVE', '2026-09-06 15:11:56', '2026-08-07 15:11:56');
INSERT INTO `invite_code` VALUES (40, 'L77M7X', 1, 1, 0, 'ACTIVE', '2026-09-06 15:13:33', '2026-08-07 15:13:33');

-- ----------------------------
-- Table structure for invite_relation
-- ----------------------------
DROP TABLE IF EXISTS `invite_relation`;
CREATE TABLE `invite_relation`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `inviter_id` bigint(20) NOT NULL,
  `invitee_id` bigint(20) NOT NULL,
  `invite_code_id` bigint(20) NOT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_invitee`(`invitee_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of invite_relation
-- ----------------------------
INSERT INTO `invite_relation` VALUES (1, 0, 1, 2, '2026-08-05 14:24:22');
INSERT INTO `invite_relation` VALUES (2, 0, 2, 2, '2026-08-05 14:40:28');
INSERT INTO `invite_relation` VALUES (3, 0, 3, 1, '2026-08-05 15:40:51');
INSERT INTO `invite_relation` VALUES (4, 0, 4, 2, '2026-08-05 15:40:51');
INSERT INTO `invite_relation` VALUES (5, 0, 5, 4, '2026-08-05 15:58:29');
INSERT INTO `invite_relation` VALUES (6, 0, 6, 8, '2026-08-05 16:01:11');
INSERT INTO `invite_relation` VALUES (7, 0, 7, 9, '2026-08-05 16:01:11');
INSERT INTO `invite_relation` VALUES (8, 0, 8, 10, '2026-08-05 16:01:11');
INSERT INTO `invite_relation` VALUES (9, 0, 9, 12, '2026-08-05 16:34:41');
INSERT INTO `invite_relation` VALUES (10, 9, 10, 13, '2026-08-05 16:34:41');
INSERT INTO `invite_relation` VALUES (11, 9, 11, 14, '2026-08-05 16:34:41');
INSERT INTO `invite_relation` VALUES (12, 0, 12, 15, '2026-08-05 20:07:06');
INSERT INTO `invite_relation` VALUES (13, 0, 13, 16, '2026-08-05 20:07:06');
INSERT INTO `invite_relation` VALUES (14, 0, 14, 17, '2026-08-05 20:07:35');
INSERT INTO `invite_relation` VALUES (15, 0, 15, 18, '2026-08-05 20:07:35');
INSERT INTO `invite_relation` VALUES (16, 0, 16, 1, '2026-08-05 23:05:07');
INSERT INTO `invite_relation` VALUES (17, 0, 17, 2, '2026-08-05 23:05:07');
INSERT INTO `invite_relation` VALUES (18, 0, 18, 20, '2026-08-05 23:35:20');
INSERT INTO `invite_relation` VALUES (19, 0, 19, 21, '2026-08-05 23:59:34');
INSERT INTO `invite_relation` VALUES (20, 1, 20, 11, '2026-08-06 13:18:01');
INSERT INTO `invite_relation` VALUES (21, 0, 21, 22, '2026-08-07 11:29:44');
INSERT INTO `invite_relation` VALUES (22, 0, 22, 23, '2026-08-07 11:29:45');
INSERT INTO `invite_relation` VALUES (23, 0, 23, 24, '2026-08-07 13:03:32');
INSERT INTO `invite_relation` VALUES (24, 0, 24, 25, '2026-08-07 13:03:32');
INSERT INTO `invite_relation` VALUES (25, 0, 25, 26, '2026-08-07 13:03:33');
INSERT INTO `invite_relation` VALUES (26, 0, 26, 27, '2026-08-07 13:03:33');
INSERT INTO `invite_relation` VALUES (27, 0, 27, 28, '2026-08-07 13:03:33');
INSERT INTO `invite_relation` VALUES (28, 0, 28, 29, '2026-08-07 13:03:34');
INSERT INTO `invite_relation` VALUES (29, 0, 29, 30, '2026-08-07 13:03:34');
INSERT INTO `invite_relation` VALUES (30, 0, 30, 31, '2026-08-07 13:04:21');
INSERT INTO `invite_relation` VALUES (31, 0, 31, 32, '2026-08-07 13:04:21');
INSERT INTO `invite_relation` VALUES (32, 0, 32, 33, '2026-08-07 13:04:22');
INSERT INTO `invite_relation` VALUES (33, 0, 33, 34, '2026-08-07 13:04:22');
INSERT INTO `invite_relation` VALUES (34, 0, 34, 35, '2026-08-07 13:04:23');
INSERT INTO `invite_relation` VALUES (35, 0, 35, 36, '2026-08-07 13:04:23');
INSERT INTO `invite_relation` VALUES (36, 0, 36, 37, '2026-08-07 13:04:23');
INSERT INTO `invite_relation` VALUES (37, 0, 37, 38, '2026-08-07 13:04:39');
INSERT INTO `invite_relation` VALUES (38, 1, 38, 39, '2026-08-07 15:13:16');

-- ----------------------------
-- Table structure for login_log
-- ----------------------------
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NULL DEFAULT NULL,
  `admin_id` bigint(20) NULL DEFAULT NULL,
  `ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `user_agent` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_login_user`(`user_id`) USING BTREE,
  INDEX `idx_login_admin`(`admin_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 75 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of login_log
-- ----------------------------
INSERT INTO `login_log` VALUES (4, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-05 14:00:08');
INSERT INTO `login_log` VALUES (5, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-05 14:00:55');
INSERT INTO `login_log` VALUES (6, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-05 14:04:20');
INSERT INTO `login_log` VALUES (7, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-05 14:24:22');
INSERT INTO `login_log` VALUES (8, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 14:39:54');
INSERT INTO `login_log` VALUES (9, 2, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 14:40:28');
INSERT INTO `login_log` VALUES (10, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 14:40:28');
INSERT INTO `login_log` VALUES (11, 2, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 14:40:42');
INSERT INTO `login_log` VALUES (12, 2, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 14:40:51');
INSERT INTO `login_log` VALUES (13, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 14:40:51');
INSERT INTO `login_log` VALUES (14, 2, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 14:41:02');
INSERT INTO `login_log` VALUES (15, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-05 15:31:48');
INSERT INTO `login_log` VALUES (16, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 15:39:53');
INSERT INTO `login_log` VALUES (18, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 15:39:54');
INSERT INTO `login_log` VALUES (19, 3, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 15:40:51');
INSERT INTO `login_log` VALUES (20, 4, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 15:40:51');
INSERT INTO `login_log` VALUES (21, 3, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 15:40:51');
INSERT INTO `login_log` VALUES (22, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 15:40:51');
INSERT INTO `login_log` VALUES (23, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 15:58:28');
INSERT INTO `login_log` VALUES (24, 5, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 15:58:29');
INSERT INTO `login_log` VALUES (25, 5, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 15:58:29');
INSERT INTO `login_log` VALUES (28, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 15:58:53');
INSERT INTO `login_log` VALUES (29, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 16:01:10');
INSERT INTO `login_log` VALUES (30, 6, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 16:01:11');
INSERT INTO `login_log` VALUES (31, 7, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 16:01:11');
INSERT INTO `login_log` VALUES (32, 8, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 16:01:11');
INSERT INTO `login_log` VALUES (33, 6, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 16:01:12');
INSERT INTO `login_log` VALUES (34, 7, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 16:01:12');
INSERT INTO `login_log` VALUES (35, 8, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 16:01:12');
INSERT INTO `login_log` VALUES (36, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-05 16:03:29');
INSERT INTO `login_log` VALUES (37, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 16:34:40');
INSERT INTO `login_log` VALUES (38, 9, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 16:34:41');
INSERT INTO `login_log` VALUES (39, 10, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 16:34:41');
INSERT INTO `login_log` VALUES (40, 11, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 16:34:41');
INSERT INTO `login_log` VALUES (41, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 20:07:05');
INSERT INTO `login_log` VALUES (42, 12, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 20:07:06');
INSERT INTO `login_log` VALUES (43, 13, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 20:07:06');
INSERT INTO `login_log` VALUES (44, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 20:07:35');
INSERT INTO `login_log` VALUES (45, 14, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 20:07:35');
INSERT INTO `login_log` VALUES (46, 15, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 20:07:35');
INSERT INTO `login_log` VALUES (47, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-05 20:28:06');
INSERT INTO `login_log` VALUES (48, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-05 21:20:53');
INSERT INTO `login_log` VALUES (49, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-05 22:37:53');
INSERT INTO `login_log` VALUES (50, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-05 22:40:53');
INSERT INTO `login_log` VALUES (51, 16, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 23:05:07');
INSERT INTO `login_log` VALUES (52, 17, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 23:05:07');
INSERT INTO `login_log` VALUES (53, 16, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 23:05:07');
INSERT INTO `login_log` VALUES (54, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 23:05:08');
INSERT INTO `login_log` VALUES (55, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Linux; Android 15; Pixel 9) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Mobile Safari/537.36', 'SUCCESS', '2026-08-05 23:21:19');
INSERT INTO `login_log` VALUES (56, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 23:34:21');
INSERT INTO `login_log` VALUES (57, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 23:35:19');
INSERT INTO `login_log` VALUES (58, 18, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 23:35:20');
INSERT INTO `login_log` VALUES (59, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-05 23:49:57');
INSERT INTO `login_log` VALUES (60, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 23:59:34');
INSERT INTO `login_log` VALUES (61, 19, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-05 23:59:34');
INSERT INTO `login_log` VALUES (65, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-06 00:05:31');
INSERT INTO `login_log` VALUES (66, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-06 13:04:27');
INSERT INTO `login_log` VALUES (67, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-06 13:16:27');
INSERT INTO `login_log` VALUES (68, 20, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-06 13:18:01');
INSERT INTO `login_log` VALUES (69, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-06 13:23:32');
INSERT INTO `login_log` VALUES (70, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-06 13:24:30');
INSERT INTO `login_log` VALUES (71, 20, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-06 17:08:25');
INSERT INTO `login_log` VALUES (72, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-06 17:17:12');
INSERT INTO `login_log` VALUES (73, 20, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-06 19:14:07');
INSERT INTO `login_log` VALUES (74, 20, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-07 08:35:19');
INSERT INTO `login_log` VALUES (75, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-07 10:13:10');
INSERT INTO `login_log` VALUES (76, 20, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-07 10:39:59');
INSERT INTO `login_log` VALUES (77, 20, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-07 11:03:14');
INSERT INTO `login_log` VALUES (78, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 11:29:44');
INSERT INTO `login_log` VALUES (79, 21, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 11:29:44');
INSERT INTO `login_log` VALUES (80, 22, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 11:29:45');
INSERT INTO `login_log` VALUES (81, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-07 12:30:00');
INSERT INTO `login_log` VALUES (82, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-07 12:36:53');
INSERT INTO `login_log` VALUES (83, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:03:31');
INSERT INTO `login_log` VALUES (84, 23, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:03:32');
INSERT INTO `login_log` VALUES (85, 24, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:03:32');
INSERT INTO `login_log` VALUES (86, 25, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:03:33');
INSERT INTO `login_log` VALUES (87, 26, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:03:33');
INSERT INTO `login_log` VALUES (88, 27, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:03:33');
INSERT INTO `login_log` VALUES (89, 28, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:03:34');
INSERT INTO `login_log` VALUES (90, 29, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:03:34');
INSERT INTO `login_log` VALUES (91, 20, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-07 13:04:01');
INSERT INTO `login_log` VALUES (92, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:04:21');
INSERT INTO `login_log` VALUES (93, 30, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:04:21');
INSERT INTO `login_log` VALUES (94, 31, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:04:21');
INSERT INTO `login_log` VALUES (95, 32, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:04:22');
INSERT INTO `login_log` VALUES (96, 33, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:04:22');
INSERT INTO `login_log` VALUES (97, 34, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:04:23');
INSERT INTO `login_log` VALUES (98, 35, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:04:23');
INSERT INTO `login_log` VALUES (99, 36, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:04:23');
INSERT INTO `login_log` VALUES (100, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:04:39');
INSERT INTO `login_log` VALUES (101, 37, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:04:39');
INSERT INTO `login_log` VALUES (102, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT; Windows NT 10.0; zh-CN) WindowsPowerShell/5.1.19041.7548', 'SUCCESS', '2026-08-07 13:04:55');
INSERT INTO `login_log` VALUES (103, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-07 14:35:44');
INSERT INTO `login_log` VALUES (104, 1, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-07 14:58:41');
INSERT INTO `login_log` VALUES (105, 20, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-07 15:04:07');
INSERT INTO `login_log` VALUES (106, 38, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36 Edg/151.0.0.0', 'SUCCESS', '2026-08-07 15:13:16');
INSERT INTO `login_log` VALUES (107, 38, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/151.0.0.0 Safari/537.36 Edg/151.0.0.0', 'SUCCESS', '2026-08-07 15:13:49');
INSERT INTO `login_log` VALUES (109, 38, NULL, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (iPhone; CPU iPhone OS 18_7 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/26.1 Mobile/15E148 Safari/604.1', 'SUCCESS', '2026-08-07 16:41:48');
INSERT INTO `login_log` VALUES (110, NULL, 1, '0:0:0:0:0:0:0:1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/150.0.0.0 Safari/537.36', 'SUCCESS', '2026-08-07 16:52:45');

-- ----------------------------
-- Table structure for lord_application
-- ----------------------------
DROP TABLE IF EXISTS `lord_application`;
CREATE TABLE `lord_application`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `statement` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|APPROVED|REJECTED',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reviewer_id` bigint(20) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_lord_app_status`(`status`, `id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for notice
-- ----------------------------
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `pinned` tinyint(1) NOT NULL DEFAULT 0,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PUBLISHED',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notice_cat`(`category`, `status`, `pinned`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notice
-- ----------------------------
INSERT INTO `notice` VALUES (1, 'RULES', '江湖规矩摘要', '一、张贴悬赏须按令状填写，赏银以「两」计，发令即托管；未满建议档须二次确认，硬性最低二百两。\n二、揭榜即入协作，同令仅可揭一次；每日揭榜有上限，且消耗体力。\n三、完结须将可分配赏银（扣除一成堂口服务费后）全部分完；允许某侠零两，可另赏侠义值。\n四、严禁虚假房源、假带看、刷侠义、诱导私下大额转账。违者可被驳回、封禁或强制关令。\n五、本平台为悬赏协作工具，非房屋中介，不成交担保。线下看房风险自负，且须遵守防骗箴言。\n六、银两为模拟记账，非法定货币；内测阶段充值提现暂不开放。', 1, 'PUBLISHED', '2026-08-05 13:40:37', '2026-08-07 14:34:39');
INSERT INTO `notice` VALUES (2, 'ANTI_FRAUD', '防骗箴言', '一、线下看房选白天与公共场所，告知亲友行程；勿独自前往偏僻处所。\n二、切勿向陌生人预付定金、房租或「跑腿费」至私人账户以绕开平台托管。\n三、平台内「两」均为模拟银两，任何人声称可兑成人民币或要求站外充值，皆为欺诈。\n四、勿轻信「内部房源」「内部折扣」；验房、核验按探子清单留证，勿只看口头承诺。\n五、证件、合同原件勿交不相识之人；拍照留证时注意脱敏，勿泄露他人隐私。\n六、遇可疑情形立即终止会面，并向武林盟举报或发起纠纷。', 1, 'PUBLISHED', '2026-08-05 13:40:37', '2026-08-07 14:34:39');
INSERT INTO `notice` VALUES (3, 'ZUNYI_RENT', '遵义租房须知', '一、本平台首发范围为遵义单城试点；令状请如实填写片区、户型、租金预算或挂牌租金、入住时间等。\n二、遵义民间常见「押一付三」等习惯仅供参考，具体以双方约定与合同为准，本告示不构成法律意见。\n三、求租发「租房悬赏」，房东发「出租悬赏」，转租发「转租悬赏」；赏银用于酬谢带看、核验等劳动，与房租（元/月）分开计算。\n四、建议赏银覆盖同城交通与合理时间成本，勿亏待行侠同道。\n五、转租须确认原租约是否允许转租，平台不审核产权合法性，令主自行担责。', 1, 'PUBLISHED', '2026-08-05 13:40:37', '2026-08-07 14:34:39');
INSERT INTO `notice` VALUES (4, 'ANNOUNCE', '开山告示', '江湖令遵义试点开启。持有效邀请方可入江湖。张贴悬赏、揭榜行侠、钱庄托管、声望成长，皆在告示板内。内测期间功能与规则或有调整，以告示与站内通知为准。天下有悬赏，江湖有侠士。江湖不让善意吃亏。', 0, 'PUBLISHED', '2026-08-05 13:40:37', '2026-08-07 14:34:39');
INSERT INTO `notice` VALUES (5, 'RULES', '赏银托管与分配说明', '发令成功后赏银冻结于模拟钱庄。审核驳回或超时未成，按规则解冻退回。验功通过后令主完结分配：一成服务费归堂口，九成可分配池须在揭榜侠士间分完。协作中取消且已有成果提交的，进入分配页处理；无成果则全额退回。细则见用户协议与站内规则。', 0, 'PUBLISHED', '2026-08-07 14:34:39', '2026-08-07 14:34:39');
INSERT INTO `notice` VALUES (6, 'ANNOUNCE', '邀请制与执事堂简说', '注册须持邀请码或邀请链接。侠士可在额度内邀同道入江湖。达到等级可申请令审使、验功使等职司，由武林盟授予后进入执事堂履职。武林盟主为声望荣誉顶点，不自动获得后台超管之权。', 0, 'PUBLISHED', '2026-08-07 14:34:39', '2026-08-07 14:34:39');

-- ----------------------------
-- Table structure for office_application
-- ----------------------------
DROP TABLE IF EXISTS `office_application`;
CREATE TABLE `office_application`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `office_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `statement` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|APPROVED|REJECTED',
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reviewer_id` bigint(20) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_office_app_status`(`status`, `id`) USING BTREE,
  INDEX `idx_office_app_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of office_application
-- ----------------------------
INSERT INTO `office_application` VALUES (1, 1, 'DECREE_REVIEWER', '愿尽职司，守护悬赏秩序。', 'APPROVED', NULL, 1, '2026-08-05 17:08:31', '2026-08-05 17:08:43');

-- ----------------------------
-- Table structure for office_def
-- ----------------------------
DROP TABLE IF EXISTS `office_def`;
CREATE TABLE `office_def`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `min_level` int(11) NOT NULL DEFAULT 1,
  `quota` int(11) NOT NULL DEFAULT 10,
  `term_days` int(11) NOT NULL DEFAULT 90,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_office_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of office_def
-- ----------------------------
INSERT INTO `office_def` VALUES (1, 'DECREE_REVIEWER', '令审使', 1, 20, 90, 'ACTIVE');
INSERT INTO `office_def` VALUES (2, 'FEAT_REVIEWER', '验功使', 1, 20, 90, 'ACTIVE');

-- ----------------------------
-- Table structure for platform_lord
-- ----------------------------
DROP TABLE IF EXISTS `platform_lord`;
CREATE TABLE `platform_lord`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `start_at` datetime(0) NULL,
  `end_at` datetime(0) NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_lord_active_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for redeem_order
-- ----------------------------
DROP TABLE IF EXISTS `redeem_order`;
CREATE TABLE `redeem_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT 1,
  `chivalry_cost` int(11) NOT NULL DEFAULT 0,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DONE' COMMENT 'DONE|PENDING|SHIPPED|CANCELLED',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_redeem_user`(`user_id`, `id`) USING BTREE,
  INDEX `idx_redeem_status`(`status`, `id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for review_record
-- ----------------------------
DROP TABLE IF EXISTS `review_record`;
CREATE TABLE `review_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `target_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'BOUNTY|SUBMISSION',
  `target_id` bigint(20) NOT NULL,
  `result` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reviewer_id` bigint(20) NOT NULL,
  `reviewer_role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'HALL|ADMIN',
  `override_by` bigint(20) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review_target`(`target_type`, `target_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 19 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review_record
-- ----------------------------
INSERT INTO `review_record` VALUES (1, 'BOUNTY', 1, 'APPROVE', '', 1, 'ADMIN', 1, '2026-08-05 14:40:53');
INSERT INTO `review_record` VALUES (2, 'BOUNTY', 1, 'APPROVE', '', 1, 'ADMIN', 1, '2026-08-05 14:41:02');
INSERT INTO `review_record` VALUES (3, 'BOUNTY', 2, 'APPROVE', 'ok', 1, 'ADMIN', 1, '2026-08-05 15:40:51');
INSERT INTO `review_record` VALUES (4, 'SUBMISSION', 1, 'APPROVE', 'ok', 1, 'ADMIN', 1, '2026-08-05 15:40:52');
INSERT INTO `review_record` VALUES (5, 'BOUNTY', 4, 'APPROVE', 'ok', 7, 'HALL', NULL, '2026-08-05 16:01:12');
INSERT INTO `review_record` VALUES (6, 'SUBMISSION', 2, 'APPROVE', 'ok', 7, 'HALL', NULL, '2026-08-05 16:01:12');
INSERT INTO `review_record` VALUES (7, 'BOUNTY', 2, 'APPROVE', '', 1, 'ADMIN', 1, '2026-08-05 16:13:43');
INSERT INTO `review_record` VALUES (8, 'BOUNTY', 1, 'REJECT', '测试', 1, 'ADMIN', 1, '2026-08-05 16:14:57');
INSERT INTO `review_record` VALUES (9, 'BOUNTY', 1, 'APPROVE', '', 1, 'ADMIN', 1, '2026-08-05 16:15:03');
INSERT INTO `review_record` VALUES (10, 'BOUNTY', 5, 'APPROVE', '', 1, 'ADMIN', 1, '2026-08-05 17:07:08');
INSERT INTO `review_record` VALUES (11, 'BOUNTY', 3, 'APPROVE', '', 1, 'HALL', NULL, '2026-08-05 17:31:42');
INSERT INTO `review_record` VALUES (12, 'BOUNTY', 6, 'REJECT', 'qa reject for republish', 1, 'ADMIN', 1, '2026-08-05 20:07:06');
INSERT INTO `review_record` VALUES (13, 'BOUNTY', 7, 'REJECT', 'qa reject for republish', 1, 'ADMIN', 1, '2026-08-05 20:07:35');
INSERT INTO `review_record` VALUES (14, 'BOUNTY', 8, 'APPROVE', 'ok', 1, 'ADMIN', 1, '2026-08-05 20:07:36');
INSERT INTO `review_record` VALUES (15, 'SUBMISSION', 3, 'APPROVE', 'ok', 1, 'ADMIN', 1, '2026-08-05 20:07:36');
INSERT INTO `review_record` VALUES (16, 'BOUNTY', 9, 'APPROVE', 'ok', 1, 'ADMIN', 1, '2026-08-05 23:05:08');
INSERT INTO `review_record` VALUES (17, 'SUBMISSION', 4, 'APPROVE', 'ok', 1, 'ADMIN', 1, '2026-08-05 23:05:08');
INSERT INTO `review_record` VALUES (18, 'BOUNTY', 10, 'APPROVE', '', 1, 'ADMIN', 1, '2026-08-06 13:25:28');
INSERT INTO `review_record` VALUES (19, 'BOUNTY', 11, 'APPROVE', '', 1, 'HALL', NULL, '2026-08-07 12:44:27');
INSERT INTO `review_record` VALUES (20, 'BOUNTY', 12, 'APPROVE', '', 1, 'ADMIN', 1, '2026-08-07 12:45:59');
INSERT INTO `review_record` VALUES (21, 'BOUNTY', 19, 'APPROVE', 'ok', 1, 'ADMIN', 1, '2026-08-07 13:04:22');
INSERT INTO `review_record` VALUES (22, 'BOUNTY', 20, 'APPROVE', 'ok', 1, 'ADMIN', 1, '2026-08-07 13:04:22');
INSERT INTO `review_record` VALUES (23, 'BOUNTY', 21, 'REJECT', 'reject-qa', 1, 'ADMIN', 1, '2026-08-07 13:04:22');
INSERT INTO `review_record` VALUES (24, 'BOUNTY', 22, 'APPROVE', 'ok', 1, 'ADMIN', 1, '2026-08-07 13:04:23');
INSERT INTO `review_record` VALUES (25, 'BOUNTY', 23, 'APPROVE', 'ok', 1, 'ADMIN', 1, '2026-08-07 13:04:23');
INSERT INTO `review_record` VALUES (26, 'BOUNTY', 24, 'APPROVE', 'ok', 1, 'ADMIN', 1, '2026-08-07 13:04:24');
INSERT INTO `review_record` VALUES (27, 'SUBMISSION', 5, 'APPROVE', '', 1, 'ADMIN', 1, '2026-08-07 14:58:12');
INSERT INTO `review_record` VALUES (28, 'BOUNTY', 25, 'APPROVE', '', 1, 'ADMIN', 1, '2026-08-07 16:02:24');

-- ----------------------------
-- Table structure for reward_product
-- ----------------------------
DROP TABLE IF EXISTS `reward_product`;
CREATE TABLE `reward_product`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `cost_chivalry` int(11) NOT NULL DEFAULT 0,
  `stock` int(11) NOT NULL DEFAULT 0,
  `cover_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reward_product
-- ----------------------------
INSERT INTO `reward_product` VALUES (1, '遵义茶礼', '本地特产礼盒（模拟）', 30, 99, '', 'ACTIVE', '2026-08-05 15:22:13', '2026-08-05 15:22:13');
INSERT INTO `reward_product` VALUES (2, '江湖腰牌', '个性展示徽章（模拟）', 50, 50, '', 'ACTIVE', '2026-08-05 15:22:13', '2026-08-05 15:22:13');
INSERT INTO `reward_product` VALUES (3, '体力补给包', '额外体力展示道具（模拟）', 20, 200, '', 'ACTIVE', '2026-08-05 15:22:13', '2026-08-05 15:22:13');

-- ----------------------------
-- Table structure for reward_suggest_config
-- ----------------------------
DROP TABLE IF EXISTS `reward_suggest_config`;
CREATE TABLE `reward_suggest_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `suggest_min` decimal(12, 2) NOT NULL,
  `suggest_max` decimal(12, 2) NOT NULL,
  `sort_no` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_reward_code`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of reward_suggest_config
-- ----------------------------
INSERT INTO `reward_suggest_config` VALUES (1, 'EASY', '简易', 200.00, 300.00, 1);
INSERT INTO `reward_suggest_config` VALUES (2, 'NORMAL', '普通', 300.00, 500.00, 2);
INSERT INTO `reward_suggest_config` VALUES (3, 'HARD', '艰辛', 500.00, 800.00, 3);
INSERT INTO `reward_suggest_config` VALUES (4, 'EXTREME', '超难', 800.00, 1500.00, 4);

-- ----------------------------
-- Table structure for settlement
-- ----------------------------
DROP TABLE IF EXISTS `settlement`;
CREATE TABLE `settlement`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bounty_id` bigint(20) NOT NULL,
  `reward_b` decimal(12, 2) NOT NULL,
  `fee_rate` decimal(6, 4) NOT NULL,
  `fee` decimal(12, 2) NOT NULL,
  `distributable` decimal(12, 2) NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'DONE',
  `kind` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'COMPLETE' COMMENT 'COMPLETE|CANCEL_ALLOCATE',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_settle_bounty`(`bounty_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of settlement
-- ----------------------------
INSERT INTO `settlement` VALUES (1, 2, 200.00, 0.1000, 20.00, 180.00, 'DONE', 'COMPLETE', '2026-08-05 15:40:52');
INSERT INTO `settlement` VALUES (2, 4, 200.00, 0.1000, 20.00, 180.00, 'DONE', 'COMPLETE', '2026-08-05 16:01:12');
INSERT INTO `settlement` VALUES (3, 8, 200.00, 0.1000, 20.00, 180.00, 'DONE', 'COMPLETE', '2026-08-05 20:07:36');
INSERT INTO `settlement` VALUES (4, 9, 200.00, 0.1000, 20.00, 180.00, 'DONE', 'COMPLETE', '2026-08-05 23:05:08');

-- ----------------------------
-- Table structure for settlement_item
-- ----------------------------
DROP TABLE IF EXISTS `settlement_item`;
CREATE TABLE `settlement_item`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `settlement_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `amount` decimal(12, 2) NOT NULL,
  `chivalry_bonus` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_settle_item`(`settlement_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of settlement_item
-- ----------------------------
INSERT INTO `settlement_item` VALUES (1, 1, 4, 180.00, 0);
INSERT INTO `settlement_item` VALUES (2, 2, 8, 180.00, 0);
INSERT INTO `settlement_item` VALUES (3, 3, 15, 180.00, 0);
INSERT INTO `settlement_item` VALUES (4, 4, 17, 180.00, 0);

-- ----------------------------
-- Table structure for site_message
-- ----------------------------
DROP TABLE IF EXISTS `site_message`;
CREATE TABLE `site_message`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `biz_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `biz_id` bigint(20) NULL DEFAULT NULL,
  `read_flag` tinyint(1) NOT NULL DEFAULT 0,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_msg_user`(`user_id`, `read_flag`, `id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 53 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of site_message
-- ----------------------------
INSERT INTO `site_message` VALUES (1, 1, '发令审核通过', '悬赏「寻找医学院附近的房子」已张贴', 'BOUNTY', 1, 1, '2026-08-05 14:40:53');
INSERT INTO `site_message` VALUES (2, 1, '发令审核通过', '悬赏「寻找医学院附近的房子」已张贴', 'BOUNTY', 1, 1, '2026-08-05 14:41:02');
INSERT INTO `site_message` VALUES (3, 3, '发令审核通过', '悬赏「QA-E2E-65568」已张贴', 'BOUNTY', 2, 0, '2026-08-05 15:40:51');
INSERT INTO `site_message` VALUES (4, 4, '成果审核通过', '你在悬赏#2的成果已通过', 'SUBMISSION', 1, 0, '2026-08-05 15:40:52');
INSERT INTO `site_message` VALUES (5, 4, '悬赏结算到账', '悬赏#2结算入账 180.00 两', 'SETTLEMENT', 1, 0, '2026-08-05 15:40:52');
INSERT INTO `site_message` VALUES (6, 3, '悬赏已完结', '悬赏「QA-E2E-65568」结算完成', 'SETTLEMENT', 1, 0, '2026-08-05 15:40:52');
INSERT INTO `site_message` VALUES (7, 3, '纠纷已发起', '悬赏#2进入纠纷：qa dispute', 'DISPUTE', 1, 0, '2026-08-05 15:40:52');
INSERT INTO `site_message` VALUES (8, 6, '发令审核通过', '悬赏「S4-73452」已张贴', 'BOUNTY', 4, 0, '2026-08-05 16:01:12');
INSERT INTO `site_message` VALUES (9, 8, '成果审核通过', '你在悬赏#4的成果已通过', 'SUBMISSION', 2, 0, '2026-08-05 16:01:12');
INSERT INTO `site_message` VALUES (10, 8, '悬赏结算到账', '悬赏#4结算入账 180.00 两', 'SETTLEMENT', 2, 0, '2026-08-05 16:01:12');
INSERT INTO `site_message` VALUES (11, 6, '悬赏已完结', '悬赏「S4-73452」结算完成', 'SETTLEMENT', 2, 0, '2026-08-05 16:01:12');
INSERT INTO `site_message` VALUES (12, 6, '纠纷已发起', '悬赏#4进入纠纷：qa', 'DISPUTE', 2, 0, '2026-08-05 16:01:12');
INSERT INTO `site_message` VALUES (13, 3, '发令审核通过', '悬赏「QA-E2E-65568」已张贴', 'BOUNTY', 2, 0, '2026-08-05 16:13:43');
INSERT INTO `site_message` VALUES (14, 1, '发令审核驳回', '悬赏「寻找医学院附近的房子」被驳回：测试', 'BOUNTY', 1, 1, '2026-08-05 16:14:57');
INSERT INTO `site_message` VALUES (15, 1, '发令审核通过', '悬赏「寻找医学院附近的房子」已张贴', 'BOUNTY', 1, 1, '2026-08-05 16:15:03');
INSERT INTO `site_message` VALUES (16, 9, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 9, 0, '2026-08-05 16:34:41');
INSERT INTO `site_message` VALUES (17, 10, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 10, 1, '2026-08-05 16:34:41');
INSERT INTO `site_message` VALUES (18, 9, '邀新奖励到账', '好友成功注册，您获得邀新奖励 100 两。', 'WALLET', 10, 0, '2026-08-05 16:34:41');
INSERT INTO `site_message` VALUES (19, 11, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 11, 0, '2026-08-05 16:34:41');
INSERT INTO `site_message` VALUES (20, 9, '邀新奖励到账', '好友成功注册，您获得邀新奖励 100 两。', 'WALLET', 11, 0, '2026-08-05 16:34:41');
INSERT INTO `site_message` VALUES (21, 1, '悬赏被强制关闭', '悬赏「寻找医学院附近的房子」已被管理员关闭', 'BOUNTY', 1, 1, '2026-08-05 16:49:19');
INSERT INTO `site_message` VALUES (22, 3, '悬赏被强制关闭', '悬赏「QA-E2E-65568」已被管理员关闭', 'BOUNTY', 2, 0, '2026-08-05 16:49:53');
INSERT INTO `site_message` VALUES (23, 1, '发令审核通过', '悬赏「寻找医学院附近的房子」已张贴', 'BOUNTY', 5, 1, '2026-08-05 17:07:08');
INSERT INTO `site_message` VALUES (24, 1, '截止提醒 T-24h', '悬赏「寻找医学院附近的房子」将在24小时内截止', 'BOUNTY', 5, 1, '2026-08-05 17:08:54');
INSERT INTO `site_message` VALUES (25, 5, '发令审核通过', '悬赏「QA-S4-41416」已张贴', 'BOUNTY', 3, 0, '2026-08-05 17:31:42');
INSERT INTO `site_message` VALUES (26, 12, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 12, 0, '2026-08-05 20:07:06');
INSERT INTO `site_message` VALUES (27, 13, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 13, 0, '2026-08-05 20:07:06');
INSERT INTO `site_message` VALUES (28, 12, '发令审核驳回', '悬赏「V18-SRC-61562」被驳回：qa reject for republish', 'BOUNTY', 6, 0, '2026-08-05 20:07:06');
INSERT INTO `site_message` VALUES (29, 14, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 14, 0, '2026-08-05 20:07:35');
INSERT INTO `site_message` VALUES (30, 15, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 15, 0, '2026-08-05 20:07:35');
INSERT INTO `site_message` VALUES (31, 14, '发令审核驳回', '悬赏「V18-SRC-19063」被驳回：qa reject for republish', 'BOUNTY', 7, 0, '2026-08-05 20:07:35');
INSERT INTO `site_message` VALUES (32, 14, '发令审核通过', '悬赏「V18-SRC-19063」已张贴', 'BOUNTY', 8, 0, '2026-08-05 20:07:36');
INSERT INTO `site_message` VALUES (33, 15, '成果审核通过', '你在悬赏#8的成果已通过', 'SUBMISSION', 3, 0, '2026-08-05 20:07:36');
INSERT INTO `site_message` VALUES (34, 15, '悬赏结算到账', '悬赏#8结算入账 180.00 两', 'SETTLEMENT', 3, 0, '2026-08-05 20:07:36');
INSERT INTO `site_message` VALUES (35, 14, '悬赏已完结', '悬赏「V18-SRC-19063」结算完成', 'SETTLEMENT', 3, 0, '2026-08-05 20:07:36');
INSERT INTO `site_message` VALUES (36, 1, '截止提醒 T-2h', '悬赏「寻找医学院附近的房子」将在2小时内截止', 'BOUNTY', 5, 1, '2026-08-05 22:03:55');
INSERT INTO `site_message` VALUES (37, 16, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 16, 0, '2026-08-05 23:05:07');
INSERT INTO `site_message` VALUES (38, 17, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 17, 0, '2026-08-05 23:05:07');
INSERT INTO `site_message` VALUES (39, 16, '发令审核通过', '悬赏「QA-E2E-27629」已张贴', 'BOUNTY', 9, 0, '2026-08-05 23:05:08');
INSERT INTO `site_message` VALUES (40, 17, '成果审核通过', '你在悬赏#9的成果已通过', 'SUBMISSION', 4, 0, '2026-08-05 23:05:08');
INSERT INTO `site_message` VALUES (41, 17, '悬赏结算到账', '悬赏#9结算入账 180.00 两', 'SETTLEMENT', 4, 0, '2026-08-05 23:05:08');
INSERT INTO `site_message` VALUES (42, 16, '悬赏已完结', '悬赏「QA-E2E-27629」结算完成', 'SETTLEMENT', 4, 0, '2026-08-05 23:05:08');
INSERT INTO `site_message` VALUES (43, 16, '纠纷已发起', '悬赏#9进入纠纷：qa dispute', 'DISPUTE', 3, 0, '2026-08-05 23:05:08');
INSERT INTO `site_message` VALUES (44, 18, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 18, 0, '2026-08-05 23:35:20');
INSERT INTO `site_message` VALUES (45, 19, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 19, 0, '2026-08-05 23:59:34');
INSERT INTO `site_message` VALUES (46, 1, '悬赏超时取消', '悬赏「寻找医学院附近的房子」已超时取消，赏银已退回', 'BOUNTY', 5, 1, '2026-08-06 00:00:14');
INSERT INTO `site_message` VALUES (47, 20, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 20, 1, '2026-08-06 13:18:01');
INSERT INTO `site_message` VALUES (48, 1, '邀新奖励到账', '好友成功注册，您获得邀新奖励 100 两。', 'WALLET', 20, 1, '2026-08-06 13:18:01');
INSERT INTO `site_message` VALUES (49, 1, '发令审核通过', '悬赏「寻找医学院附近的房子」已张贴', 'BOUNTY', 10, 1, '2026-08-06 13:25:28');
INSERT INTO `site_message` VALUES (50, 1, '截止提醒 T-24h', '悬赏「寻找医学院附近的房子」将在24小时内截止', 'BOUNTY', 10, 1, '2026-08-06 13:29:38');
INSERT INTO `site_message` VALUES (51, 1, '截止提醒 T-2h', '悬赏「寻找医学院附近的房子」将在2小时内截止', 'BOUNTY', 10, 1, '2026-08-06 22:01:21');
INSERT INTO `site_message` VALUES (52, 1, '悬赏超时取消', '悬赏「寻找医学院附近的房子」已超时取消，赏银已退回', 'BOUNTY', 10, 1, '2026-08-07 00:00:24');
INSERT INTO `site_message` VALUES (53, 21, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 21, 0, '2026-08-07 11:29:44');
INSERT INTO `site_message` VALUES (54, 22, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 22, 0, '2026-08-07 11:29:45');
INSERT INTO `site_message` VALUES (55, 21, '发令审核通过', '悬赏「V189-CHAT-75996」已张贴', 'BOUNTY', 11, 0, '2026-08-07 12:44:27');
INSERT INTO `site_message` VALUES (56, 1, '发令审核通过', '悬赏「寻找医学院附近的房子」已张贴', 'BOUNTY', 12, 1, '2026-08-07 12:45:59');
INSERT INTO `site_message` VALUES (57, 1, '截止提醒 T-24h', '悬赏「寻找医学院附近的房子」将在24小时内截止', 'BOUNTY', 12, 1, '2026-08-07 12:49:04');
INSERT INTO `site_message` VALUES (58, 23, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 23, 0, '2026-08-07 13:03:32');
INSERT INTO `site_message` VALUES (59, 24, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 24, 0, '2026-08-07 13:03:32');
INSERT INTO `site_message` VALUES (60, 25, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 25, 0, '2026-08-07 13:03:33');
INSERT INTO `site_message` VALUES (61, 26, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 26, 0, '2026-08-07 13:03:33');
INSERT INTO `site_message` VALUES (62, 27, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 27, 0, '2026-08-07 13:03:33');
INSERT INTO `site_message` VALUES (63, 28, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 28, 0, '2026-08-07 13:03:34');
INSERT INTO `site_message` VALUES (64, 29, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 29, 0, '2026-08-07 13:03:34');
INSERT INTO `site_message` VALUES (65, 30, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 30, 0, '2026-08-07 13:04:21');
INSERT INTO `site_message` VALUES (66, 31, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 31, 0, '2026-08-07 13:04:21');
INSERT INTO `site_message` VALUES (67, 30, '发令审核通过', '悬赏「LC-13676」已张贴', 'BOUNTY', 19, 0, '2026-08-07 13:04:22');
INSERT INTO `site_message` VALUES (68, 32, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 32, 0, '2026-08-07 13:04:22');
INSERT INTO `site_message` VALUES (69, 33, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 33, 0, '2026-08-07 13:04:22');
INSERT INTO `site_message` VALUES (70, 32, '发令审核通过', '悬赏「LC2-78902」已张贴', 'BOUNTY', 20, 0, '2026-08-07 13:04:22');
INSERT INTO `site_message` VALUES (71, 32, '发令审核驳回', '悬赏「LC3-78902」被驳回：reject-qa', 'BOUNTY', 21, 0, '2026-08-07 13:04:22');
INSERT INTO `site_message` VALUES (72, 34, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 34, 0, '2026-08-07 13:04:23');
INSERT INTO `site_message` VALUES (73, 32, '发令审核通过', '悬赏「LC4-78902」已张贴', 'BOUNTY', 22, 0, '2026-08-07 13:04:23');
INSERT INTO `site_message` VALUES (74, 35, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 35, 0, '2026-08-07 13:04:23');
INSERT INTO `site_message` VALUES (75, 32, '发令审核通过', '悬赏「LC5-78902」已张贴', 'BOUNTY', 23, 0, '2026-08-07 13:04:23');
INSERT INTO `site_message` VALUES (76, 36, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 36, 0, '2026-08-07 13:04:23');
INSERT INTO `site_message` VALUES (77, 32, '发令审核通过', '悬赏「LC6-78902」已张贴', 'BOUNTY', 24, 0, '2026-08-07 13:04:24');
INSERT INTO `site_message` VALUES (78, 37, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 37, 0, '2026-08-07 13:04:39');
INSERT INTO `site_message` VALUES (79, 20, '成果审核通过', '你在悬赏#12的成果已通过', 'SUBMISSION', 5, 1, '2026-08-07 14:58:12');
INSERT INTO `site_message` VALUES (80, 38, '注册赠银到账', '您已获得注册赠银 500 两，可在钱庄查看。', 'WALLET', 38, 1, '2026-08-07 15:13:16');
INSERT INTO `site_message` VALUES (81, 1, '邀新奖励到账', '好友成功注册，您获得邀新奖励 100 两。', 'WALLET', 38, 1, '2026-08-07 15:13:16');
INSERT INTO `site_message` VALUES (82, 1, '发令审核通过', '悬赏「测试的悬赏」已张贴', 'BOUNTY', 25, 1, '2026-08-07 16:02:24');

-- ----------------------------
-- Table structure for submission
-- ----------------------------
DROP TABLE IF EXISTS `submission`;
CREATE TABLE `submission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `bounty_id` bigint(20) NOT NULL,
  `claim_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `version_no` int(11) NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING|APPROVED|REJECTED',
  `content_summary` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `reject_reason` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sub_claim`(`claim_id`) USING BTREE,
  INDEX `idx_sub_bounty_status`(`bounty_id`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of submission
-- ----------------------------
INSERT INTO `submission` VALUES (1, 2, 1, 4, 1, 'APPROVED', 'done', NULL, '2026-08-05 15:40:51', '2026-08-05 15:40:52');
INSERT INTO `submission` VALUES (2, 4, 2, 8, 1, 'APPROVED', 'd', NULL, '2026-08-05 16:01:12', '2026-08-05 16:01:12');
INSERT INTO `submission` VALUES (3, 8, 3, 15, 1, 'APPROVED', 'd', NULL, '2026-08-05 20:07:36', '2026-08-05 20:07:36');
INSERT INTO `submission` VALUES (4, 9, 4, 17, 1, 'APPROVED', 'done', NULL, '2026-08-05 23:05:08', '2026-08-05 23:05:08');
INSERT INTO `submission` VALUES (5, 12, 7, 20, 1, 'APPROVED', 'ces', NULL, '2026-08-07 14:56:42', '2026-08-07 14:58:12');
INSERT INTO `submission` VALUES (6, 25, 14, 20, 1, 'PENDING', '手动发大厦', NULL, '2026-08-07 16:44:53', '2026-08-07 16:44:53');

-- ----------------------------
-- Table structure for submission_item
-- ----------------------------
DROP TABLE IF EXISTS `submission_item`;
CREATE TABLE `submission_item`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `submission_id` bigint(20) NOT NULL,
  `checklist_item_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `done` tinyint(1) NOT NULL DEFAULT 0,
  `text` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `media_urls_json` json NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sub_item`(`submission_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of submission_item
-- ----------------------------
INSERT INTO `submission_item` VALUES (1, 1, 'VERIFY_AUTHENTIC', 1, 'done', '[]');
INSERT INTO `submission_item` VALUES (2, 1, 'SITE_VISIT_RECORD', 1, 'done', '[]');
INSERT INTO `submission_item` VALUES (3, 2, 'VERIFY_AUTHENTIC', 1, 'd', '[]');
INSERT INTO `submission_item` VALUES (4, 2, 'SITE_VISIT_RECORD', 1, 'd', '[]');
INSERT INTO `submission_item` VALUES (5, 3, 'VERIFY_AUTHENTIC', 1, 'd', '[]');
INSERT INTO `submission_item` VALUES (6, 3, 'SITE_VISIT_RECORD', 1, 'd', '[]');
INSERT INTO `submission_item` VALUES (7, 4, 'VERIFY_AUTHENTIC', 1, 'done', '[]');
INSERT INTO `submission_item` VALUES (8, 4, 'SITE_VISIT_RECORD', 1, 'done', '[]');
INSERT INTO `submission_item` VALUES (9, 5, 'VERIFY_AUTHENTIC', 1, 'csdfasd', '[\"/files/1db0e224fde04290948452d8308d66ac.JPG\"]');
INSERT INTO `submission_item` VALUES (10, 5, 'SITE_VISIT_RECORD', 1, 'sdfas', '[\"/files/2ae2af9f895a471296f0d2046cb402ee.JPG\"]');
INSERT INTO `submission_item` VALUES (11, 5, 'PHOTO_EVIDENCE', 1, 'sdfasd', '[\"/files/67ff7f507c9849bd8b913a11f14b81de.JPG\"]');
INSERT INTO `submission_item` VALUES (12, 5, 'NEIGHBORHOOD_NOTE', 0, '', '[]');
INSERT INTO `submission_item` VALUES (13, 5, 'CONTRACT_HINT', 0, '', '[]');
INSERT INTO `submission_item` VALUES (14, 6, 'VERIFY_AUTHENTIC', 1, '撒大噶', '[]');
INSERT INTO `submission_item` VALUES (15, 6, 'SITE_VISIT_RECORD', 1, '打发士大夫', '[]');
INSERT INTO `submission_item` VALUES (16, 6, 'PHOTO_EVIDENCE', 1, '', '[]');

-- ----------------------------
-- Table structure for sys_config
-- ----------------------------
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `config_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `config_value` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 15 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_config
-- ----------------------------
INSERT INTO `sys_config` VALUES (1, 'min_reward', '200', '最低赏银');
INSERT INTO `sys_config` VALUES (2, 'fee_rate', '0.1', '平台服务费比例');
INSERT INTO `sys_config` VALUES (3, 'claim_day_limit', '10', '每日揭榜上限');
INSERT INTO `sys_config` VALUES (4, 'daily_free_stamina', '5', '每日免费体力');
INSERT INTO `sys_config` VALUES (5, 'claim_stamina_cost', '1', '单次揭榜耗体力');
INSERT INTO `sys_config` VALUES (6, 'submit_cooldown_seconds', '600', '成果提交冷却秒');
INSERT INTO `sys_config` VALUES (7, 'submit_day_limit', '20', '每日成果提交上限');
INSERT INTO `sys_config` VALUES (8, 'invite_daily_quota', '3', '每日邀请码额度');
INSERT INTO `sys_config` VALUES (9, 'chivalry_per_complete', '10', '完结基础侠义值');
INSERT INTO `sys_config` VALUES (10, 'ranks_config', '{\"refreshMinutes\":10,\"excludeBanned\":true,\"lordTopDisplay\":true,\"eligibleForLordTopN\":1,\"rejectCooldownDays\":7}', '英雄谱规则');
INSERT INTO `sys_config` VALUES (11, 'wallet.rechargeEnabled', 'false', '用户充值开关（默认关）');
INSERT INTO `sys_config` VALUES (12, 'wallet.withdrawEnabled', 'false', '用户提现开关（默认关）');
INSERT INTO `sys_config` VALUES (13, 'wallet.registerGrantAmount', '500', '注册赠银（两）');
INSERT INTO `sys_config` VALUES (14, 'wallet.inviteRewardAmount', '100', '邀新奖励（两，入邀请人）');
INSERT INTO `sys_config` VALUES (15, 'feedback.cooldownSeconds', '60', '用户反馈短时冷却秒');
INSERT INTO `sys_config` VALUES (16, 'feedback.dailyLimit', '10', '用户反馈自然日上限');
INSERT INTO `sys_config` VALUES (17, 'chivalry_per_stamina', '10', 'growth');

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `password_hash` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|DISABLED|BANNED',
  `city` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT '遵义',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_phone`(`phone`) USING BTREE,
  UNIQUE INDEX `uk_user_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, '18275458776', 'admin', '$2a$10$WwQhQEF7qAQwY.GjzTHFwu8amUJ0nxidwR8mn8uFLnwp8Hy1z/v2m', 'ACTIVE', '遵义', NULL, '2026-08-05 14:24:22', '2026-08-05 14:24:22');
INSERT INTO `user` VALUES (2, '13928458145', 'qa79119', '$2a$10$C3OfSmTBGun2naYIIjMRE.hhK4EEYV6XrvHlgDmjI3CxSFmzxU1vC', 'ACTIVE', '遵义', NULL, '2026-08-05 14:40:28', '2026-08-05 14:40:28');
INSERT INTO `user` VALUES (3, '13830722544', 'qaa65568', '$2a$10$1pHafYqXriOTpFKnQc./HO6jpgkRq0UCE.7pTQy4PwlSrRAWdwVja', 'ACTIVE', '遵义', NULL, '2026-08-05 15:40:51', '2026-08-05 15:40:51');
INSERT INTO `user` VALUES (4, '13964463848', 'qab65568', '$2a$10$fX6Ub4alzZrAfB2Ro9V4D.xA3nyQkYfm5OGVJqOL7Svywnz/WNo1a', 'ACTIVE', '遵义', NULL, '2026-08-05 15:40:51', '2026-08-05 15:40:51');
INSERT INTO `user` VALUES (5, '13733145093', 'qap41416', '$2a$10$p.OUnDw1krzNjsvXs/F1JOJiBClu5PbcSDTFBlqlRKDtra6T5gCQO', 'ACTIVE', '遵义', NULL, '2026-08-05 15:58:29', '2026-08-05 15:58:29');
INSERT INTO `user` VALUES (6, '13740634581', 'qsp73452', '$2a$10$qwfG6OjZE0fEyttzWNnxPen96VLVphPQXgoaeJqh3Re9QS2dFoOKu', 'ACTIVE', '遵义', NULL, '2026-08-05 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `user` VALUES (7, '13694446482', 'qsr73452', '$2a$10$uZKzItIKHzpXomBkj5tQ4.BZXFYj4dfUJrXPgeMVglstuebIXuE.y', 'ACTIVE', '遵义', NULL, '2026-08-05 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `user` VALUES (8, '13583636891', 'qsc73452', '$2a$10$o95ILip9QoqY.zhVeSSYeerOyXbyTIBmuY7tG0ObNdL/FkprIVan2', 'ACTIVE', '遵义', NULL, '2026-08-05 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `user` VALUES (9, '13778699377', 'v17a12706', '$2a$10$zxKbTeDT2gMXpxGU4PvZD.M/189Qeitt1YWdxExjxsZzf0YTYDpq2', 'ACTIVE', '遵义', NULL, '2026-08-05 16:34:41', '2026-08-05 16:34:41');
INSERT INTO `user` VALUES (10, '13861250913', 'v17b12706', '$2a$10$Irjq3tCOuXV6gmbb6R18f.oBt1pjs3Fk/TxykZbWBFCdzliKbBDTe', 'ACTIVE', '遵义', NULL, '2026-08-05 16:34:41', '2026-08-05 16:34:41');
INSERT INTO `user` VALUES (11, '13971324721', 'v17c12706', '$2a$10$uqxegTotnIqiXe32jaqo5.ic4wH.UiI5kMT2QLf7a8Tw6gyzX0A2S', 'ACTIVE', '遵义', NULL, '2026-08-05 16:34:41', '2026-08-05 16:34:41');
INSERT INTO `user` VALUES (12, '13788344869', 'v18p61562', '$2a$10$34GjTNY9DLlvg0NJNvL0BuK5SgsefkNiyjD7IaYUWuyVEBHIIjNGC', 'ACTIVE', '遵义', NULL, '2026-08-05 20:07:06', '2026-08-05 20:07:06');
INSERT INTO `user` VALUES (13, '13826343354', 'v18c61562', '$2a$10$YX64nItf9RJauwwl/f/P6Ob.wqLk60wXVrjQdS5B97FpYb.e.D.Ze', 'ACTIVE', '遵义', NULL, '2026-08-05 20:07:06', '2026-08-05 20:07:06');
INSERT INTO `user` VALUES (14, '13790831991', 'v18p19063', '$2a$10$V03UP3blviLiUA/e/0ar0.GSg6Gz9AZDfX5ypfu4VDrZBziJab/6K', 'ACTIVE', '遵义', NULL, '2026-08-05 20:07:35', '2026-08-05 20:07:35');
INSERT INTO `user` VALUES (15, '13863855258', 'v18c19063', '$2a$10$PTK/O6nydKlTZ.EAQc5nz.2h4EJOyJ50XZqmNEB6u5qXOS6ApDj9K', 'ACTIVE', '遵义', NULL, '2026-08-05 20:07:35', '2026-08-05 20:07:35');
INSERT INTO `user` VALUES (16, '13873582504', 'qaa27629', '$2a$10$aLxI4jPKZNNMfenOJcEfs.VrgQOY/9XCbPtACdMfMTT0dMyKKtMci', 'ACTIVE', '遵义', NULL, '2026-08-05 23:05:07', '2026-08-05 23:05:07');
INSERT INTO `user` VALUES (17, '13974698521', 'qab27629', '$2a$10$uib0bK6rUSQvEHkNDOa3meb/7by.oAA9HAECQ0CDWrzlXl0ie1y0y', 'ACTIVE', '遵义', NULL, '2026-08-05 23:05:07', '2026-08-05 23:05:07');
INSERT INTO `user` VALUES (18, '13967277080', 'flt70421', '$2a$10$pH0I1m211yMvSZd7mFIQQe72S8pi1n/0HtVJ41Cd4H404shTmyHIq', 'ACTIVE', '遵义', NULL, '2026-08-05 23:35:20', '2026-08-05 23:35:20');
INSERT INTO `user` VALUES (19, '13703913271', 'df10365', '$2a$10$lvYS90uT7EJ7dmVabVcpPeARB5w0YPBlgGtVe.pDruaxKv0bekXfm', 'ACTIVE', '遵义', NULL, '2026-08-05 23:59:34', '2026-08-05 23:59:34');
INSERT INTO `user` VALUES (20, '18275458775', 'zzx', '$2a$10$fKivGIausnlxXmm8RwLfeORXz8dn4Qwec3hKexGt50t8d9CL/Z1cm', 'ACTIVE', '遵义', NULL, '2026-08-06 13:18:01', '2026-08-06 13:18:01');
INSERT INTO `user` VALUES (21, '13697409521', 'pub75996', '$2a$10$YdNGo9B6SYBx9Y6WaEsgKO/Y3SWUdYC0nE12.vJi92lCXrWXL7FDu', 'ACTIVE', '遵义', NULL, '2026-08-07 11:29:44', '2026-08-07 11:29:44');
INSERT INTO `user` VALUES (22, '13583290086', 'clm75996', '$2a$10$RdgHnpVBH/XvT/4iiGq5xO5TmdO1FiOh9D95Tut1bK4FcdssyFFg.', 'ACTIVE', '遵义', NULL, '2026-08-07 11:29:45', '2026-08-07 11:29:45');
INSERT INTO `user` VALUES (23, '13693387215', 'publc78085', '$2a$10$LAp//m7deDhOYUBtXsIC6OB4.HBx8Nm83UdMbOCssxC/B9jFtFtuK', 'ACTIVE', '遵义', NULL, '2026-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `user` VALUES (24, '13557934328', 'clmlc78085', '$2a$10$D5R.ujGDbCgIb5I2yxJAYOMVqmkvF2eVWs/yG.Exkhe3Q6IOIFIYW', 'ACTIVE', '遵义', NULL, '2026-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `user` VALUES (25, '13414433055', 'pub253919', '$2a$10$4qZz8VCNmmrgcEozjaMyb.ALDEu4rnRMG/8lZZT8H0BhbwdsUErNm', 'ACTIVE', '遵义', NULL, '2026-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `user` VALUES (26, '13319698349', 'clm253919', '$2a$10$2wplSKVIiehpIRRPWbD/me9vSbO3nwOOYs2jBBSXVgE7FEojOXD36', 'ACTIVE', '遵义', NULL, '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `user` VALUES (27, '13248420077', 'clm353919', '$2a$10$pW/uJNi5IGOYvWfhvArU7ukJ96pA8fl7pDeUrh/L32vhZMmNz3JjO', 'ACTIVE', '遵义', NULL, '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `user` VALUES (28, '13169658715', 'clm453919', '$2a$10$A.0P5q7G3r9Kuc/3PI0iyeg08UJAlLSzLhSCTJr6/drGJ85aKKV3y', 'ACTIVE', '遵义', NULL, '2026-08-07 13:03:34', '2026-08-07 13:03:34');
INSERT INTO `user` VALUES (29, '13090085462', 'clm553919', '$2a$10$XI5Sy9Pe31qirn6L9k0IneYHLN4WXM46hpILzPDDO6mpORAL1jtVO', 'ACTIVE', '遵义', NULL, '2026-08-07 13:03:34', '2026-08-07 13:03:34');
INSERT INTO `user` VALUES (30, '13688923830', 'publc13676', '$2a$10$CtAqAgEjUubYj8gRhE2zJusOAoLr5ObufYJCGmz1MiPN6TOp3vPEG', 'ACTIVE', '遵义', NULL, '2026-08-07 13:04:21', '2026-08-07 13:04:21');
INSERT INTO `user` VALUES (31, '13537945498', 'clmlc13676', '$2a$10$2WiZ1LgKNK1NQPbmL8fpde.LBut5B.xLOKAOdNOivlvKEw2aAhMbq', 'ACTIVE', '遵义', NULL, '2026-08-07 13:04:21', '2026-08-07 13:04:21');
INSERT INTO `user` VALUES (32, '13439522834', 'pub278902', '$2a$10$v6DtWu8wi66uenOkyJNneuUeqi8CUYpb5BuYQSfQ8YSDCb04Bc4U.', 'ACTIVE', '遵义', NULL, '2026-08-07 13:04:22', '2026-08-07 13:04:22');
INSERT INTO `user` VALUES (33, '13361514729', 'clm278902', '$2a$10$LJtmytnofIMrgQDy3Wj92uq34kZ.SkQSx/7C0bl0LcW5KN7d9MDWG', 'ACTIVE', '遵义', NULL, '2026-08-07 13:04:22', '2026-08-07 13:04:22');
INSERT INTO `user` VALUES (34, '13260798025', 'clm378902', '$2a$10$V9TYX2DxL8NafhBWhhRH0.Gu1p/UEQM35ROkcU5YOmVJ1vD1y..5e', 'ACTIVE', '遵义', NULL, '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `user` VALUES (35, '13161346065', 'clm478902', '$2a$10$t0wAU51aXAE.iPF5MPsTquykL/kjbye4INhmm0xJsOVmHGSgojn8a', 'ACTIVE', '遵义', NULL, '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `user` VALUES (36, '13071333154', 'clm578902', '$2a$10$VECWlWGmgyr8FPQXVfP31eHmxoG//5xXu6tXGRSqtV9MIYRnwu0qi', 'ACTIVE', '遵义', NULL, '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `user` VALUES (37, '13749034772', 'capp43127', '$2a$10$nTnqUHrapAdSapoCmsuXnO5Pdyd.ZTIdnnZFOXnTFyUTlak9eoLLO', 'ACTIVE', '遵义', NULL, '2026-08-07 13:04:39', '2026-08-07 13:04:39');
INSERT INTO `user` VALUES (38, '18275458898', 'gtx', '$2a$10$FVWhtpUH8wr7HHej7JP4Ku.hLXLRfuiH6QP06n.WqhOJh9w.0qc9i', 'DISABLED', '遵义', NULL, '2026-08-07 15:13:16', '2026-08-07 17:02:41');

-- ----------------------------
-- Table structure for user_asset
-- ----------------------------
DROP TABLE IF EXISTS `user_asset`;
CREATE TABLE `user_asset`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `chivalry` int(11) NOT NULL DEFAULT 0,
  `stamina` int(11) NOT NULL DEFAULT 5,
  `stamina_date` date NULL DEFAULT NULL,
  `completed_orders` int(11) NOT NULL DEFAULT 0,
  `good_rate` decimal(5, 2) NOT NULL DEFAULT 100.00,
  `reputation_score` decimal(12, 2) NOT NULL DEFAULT 0.00,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_asset_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_asset
-- ----------------------------
INSERT INTO `user_asset` VALUES (1, 1, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-05 14:24:22', '2026-08-07 10:13:10');
INSERT INTO `user_asset` VALUES (2, 2, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-05 14:40:28', '2026-08-07 12:45:12');
INSERT INTO `user_asset` VALUES (3, 3, 10, 5, '2026-08-07', 1, 100.00, 10010.00, '2026-08-05 15:40:51', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (4, 4, 10, 5, '2026-08-07', 1, 100.00, 10010.00, '2026-08-05 15:40:51', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (5, 5, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-05 15:58:29', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (6, 6, 10, 5, '2026-08-07', 1, 100.00, 10010.00, '2026-08-05 16:01:11', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (7, 7, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-05 16:01:11', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (8, 8, 10, 5, '2026-08-07', 1, 100.00, 10010.00, '2026-08-05 16:01:11', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (9, 9, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-05 16:34:41', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (10, 10, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-05 16:34:41', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (11, 11, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-05 16:34:41', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (12, 12, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-05 20:07:06', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (13, 13, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-05 20:07:06', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (14, 14, 10, 5, '2026-08-07', 1, 100.00, 10010.00, '2026-08-05 20:07:35', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (15, 15, 10, 5, '2026-08-07', 1, 100.00, 10010.00, '2026-08-05 20:07:35', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (16, 16, 10, 5, '2026-08-07', 1, 100.00, 10010.00, '2026-08-05 23:05:07', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (17, 17, 10, 5, '2026-08-07', 1, 100.00, 10010.00, '2026-08-05 23:05:07', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (18, 18, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-05 23:35:20', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (19, 19, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-05 23:59:34', '2026-08-07 12:31:05');
INSERT INTO `user_asset` VALUES (20, 20, 0, 2, '2026-08-07', 0, 100.00, 0.00, '2026-08-06 13:18:01', '2026-08-07 16:02:34');
INSERT INTO `user_asset` VALUES (21, 21, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 11:29:44', '2026-08-07 11:29:44');
INSERT INTO `user_asset` VALUES (22, 22, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 11:29:45', '2026-08-07 11:29:45');
INSERT INTO `user_asset` VALUES (23, 23, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `user_asset` VALUES (24, 24, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `user_asset` VALUES (25, 25, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `user_asset` VALUES (26, 26, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `user_asset` VALUES (27, 27, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `user_asset` VALUES (28, 28, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:03:34', '2026-08-07 13:03:34');
INSERT INTO `user_asset` VALUES (29, 29, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:03:34', '2026-08-07 13:03:34');
INSERT INTO `user_asset` VALUES (30, 30, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:04:21', '2026-08-07 13:04:21');
INSERT INTO `user_asset` VALUES (31, 31, 0, 4, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:04:21', '2026-08-07 13:04:22');
INSERT INTO `user_asset` VALUES (32, 32, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:04:22', '2026-08-07 13:04:22');
INSERT INTO `user_asset` VALUES (33, 33, 0, 4, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:04:22', '2026-08-07 13:04:22');
INSERT INTO `user_asset` VALUES (34, 34, 0, 4, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `user_asset` VALUES (35, 35, 0, 4, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `user_asset` VALUES (36, 36, 0, 4, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:04:23', '2026-08-07 13:04:24');
INSERT INTO `user_asset` VALUES (37, 37, 0, 5, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 13:04:39', '2026-08-07 13:04:39');
INSERT INTO `user_asset` VALUES (38, 38, 0, 4, '2026-08-07', 0, 100.00, 0.00, '2026-08-07 15:13:16', '2026-08-07 16:27:00');

-- ----------------------------
-- Table structure for user_feedback
-- ----------------------------
DROP TABLE IF EXISTS `user_feedback`;
CREATE TABLE `user_feedback`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'BUG|SUGGEST|COMPLAINT|OTHER',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `content` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `contact` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `related_ref` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `attachment_urls_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NEW' COMMENT 'NEW|PROCESSING|RESOLVED|CLOSED',
  `handle_remark` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `status_changed_at` datetime(0) NULL DEFAULT NULL,
  `status_changed_by_admin_id` bigint(20) NULL DEFAULT NULL,
  `status_history_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_feedback_user`(`user_id`, `id`) USING BTREE,
  INDEX `idx_feedback_status`(`status`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_level_config
-- ----------------------------
DROP TABLE IF EXISTS `user_level_config`;
CREATE TABLE `user_level_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `level` int(11) NOT NULL,
  `title` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `min_chivalry` int(11) NOT NULL DEFAULT 0,
  `privileges_json` varchar(1000) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort_no` int(11) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_level`(`level`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_level_config
-- ----------------------------
INSERT INTO `user_level_config` VALUES (1, 1, '初入江湖', 0, '[]', 1);
INSERT INTO `user_level_config` VALUES (2, 2, '初显身手', 50, '[]', 2);
INSERT INTO `user_level_config` VALUES (3, 3, '小有名气', 200, '[]', 3);
INSERT INTO `user_level_config` VALUES (4, 4, '名扬江湖', 500, '[]', 4);

-- ----------------------------
-- Table structure for user_office
-- ----------------------------
DROP TABLE IF EXISTS `user_office`;
CREATE TABLE `user_office`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `office_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE|SUSPENDED|EXPIRED',
  `start_at` datetime(0) NULL,
  `end_at` datetime(0) NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_office`(`user_id`, `office_code`, `status`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_office
-- ----------------------------
INSERT INTO `user_office` VALUES (1, 6, 'DECREE_REVIEWER', 'ACTIVE', '2026-08-05 16:01:11', '2026-11-03 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `user_office` VALUES (2, 7, 'DECREE_REVIEWER', 'ACTIVE', '2026-08-05 16:01:11', '2026-11-03 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `user_office` VALUES (3, 7, 'FEAT_REVIEWER', 'ACTIVE', '2026-08-05 16:01:11', '2026-11-03 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `user_office` VALUES (4, 8, 'FEAT_REVIEWER', 'ACTIVE', '2026-08-05 16:01:11', '2026-11-03 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `user_office` VALUES (5, 1, 'DECREE_REVIEWER', 'ACTIVE', '2026-08-05 17:08:43', '2026-11-03 17:08:43', '2026-08-05 17:08:43');

-- ----------------------------
-- Table structure for user_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_profile`;
CREATE TABLE `user_profile`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `nickname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `avatar_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `bio` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `id_number` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `real_name_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'NONE' COMMENT 'NONE|PENDING|VERIFIED|REJECTED',
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_profile_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_profile
-- ----------------------------
INSERT INTO `user_profile` VALUES (1, 1, '遵义无名侠', '/files/f1bec516c9fa4b8eac03d07d7b2c1714.jpg', '', NULL, NULL, 'NONE', '2026-08-05 14:24:22', '2026-08-05 23:49:53');
INSERT INTO `user_profile` VALUES (2, 2, 'QAHero', '', '', NULL, NULL, 'NONE', '2026-08-05 14:40:28', '2026-08-05 14:40:28');
INSERT INTO `user_profile` VALUES (3, 3, 'QAA65568', '', '', NULL, NULL, 'NONE', '2026-08-05 15:40:51', '2026-08-05 15:40:51');
INSERT INTO `user_profile` VALUES (4, 4, 'QAB65568', '', '', NULL, NULL, 'NONE', '2026-08-05 15:40:51', '2026-08-05 15:40:51');
INSERT INTO `user_profile` VALUES (5, 5, 'Pub41416', '', '', NULL, NULL, 'NONE', '2026-08-05 15:58:29', '2026-08-05 15:58:29');
INSERT INTO `user_profile` VALUES (6, 6, 'Pub73452', '', '', NULL, NULL, 'NONE', '2026-08-05 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `user_profile` VALUES (7, 7, 'Rev73452', '', '', NULL, NULL, 'NONE', '2026-08-05 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `user_profile` VALUES (8, 8, 'Clm73452', '', '', NULL, NULL, 'NONE', '2026-08-05 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `user_profile` VALUES (9, 9, 'Inv12706', '', '', NULL, NULL, 'NONE', '2026-08-05 16:34:41', '2026-08-05 16:34:41');
INSERT INTO `user_profile` VALUES (10, 10, 'Bee12706', '', '', NULL, NULL, 'NONE', '2026-08-05 16:34:41', '2026-08-05 16:34:41');
INSERT INTO `user_profile` VALUES (11, 11, 'Cee12706', '', '', NULL, NULL, 'NONE', '2026-08-05 16:34:41', '2026-08-05 16:34:41');
INSERT INTO `user_profile` VALUES (12, 12, 'P61562', '', '', NULL, NULL, 'NONE', '2026-08-05 20:07:06', '2026-08-05 20:07:06');
INSERT INTO `user_profile` VALUES (13, 13, 'C61562', '', '', NULL, NULL, 'NONE', '2026-08-05 20:07:06', '2026-08-05 20:07:06');
INSERT INTO `user_profile` VALUES (14, 14, 'P19063', '', '', NULL, NULL, 'NONE', '2026-08-05 20:07:35', '2026-08-05 20:07:35');
INSERT INTO `user_profile` VALUES (15, 15, 'C19063', '', '', NULL, NULL, 'NONE', '2026-08-05 20:07:35', '2026-08-05 20:07:35');
INSERT INTO `user_profile` VALUES (16, 16, 'QAA27629', '', '', NULL, NULL, 'NONE', '2026-08-05 23:05:07', '2026-08-05 23:05:07');
INSERT INTO `user_profile` VALUES (17, 17, 'QAB27629', '', '', NULL, NULL, 'NONE', '2026-08-05 23:05:07', '2026-08-05 23:05:07');
INSERT INTO `user_profile` VALUES (18, 18, 'NickFlt70421', '', '', NULL, NULL, 'NONE', '2026-08-05 23:35:20', '2026-08-05 23:35:20');
INSERT INTO `user_profile` VALUES (19, 19, 'NickDf10365', '', '', NULL, NULL, 'NONE', '2026-08-05 23:59:34', '2026-08-05 23:59:34');
INSERT INTO `user_profile` VALUES (20, 20, '蜘蛛侠', '', '的是国宝地方就爱上大驾光临卡号发给拉萨', NULL, NULL, 'NONE', '2026-08-06 13:18:01', '2026-08-07 16:54:50');
INSERT INTO `user_profile` VALUES (21, 21, 'Pub75996', '', '', NULL, NULL, 'NONE', '2026-08-07 11:29:44', '2026-08-07 11:29:44');
INSERT INTO `user_profile` VALUES (22, 22, 'Clm75996', '', '', NULL, NULL, 'NONE', '2026-08-07 11:29:45', '2026-08-07 11:29:45');
INSERT INTO `user_profile` VALUES (23, 23, 'PubLC78085', '', '', NULL, NULL, 'NONE', '2026-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `user_profile` VALUES (24, 24, 'ClmLC78085', '', '', NULL, NULL, 'NONE', '2026-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `user_profile` VALUES (25, 25, 'Pub253919', '', '', NULL, NULL, 'NONE', '2026-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `user_profile` VALUES (26, 26, 'Clm253919', '', '', NULL, NULL, 'NONE', '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `user_profile` VALUES (27, 27, 'Clm353919', '', '', NULL, NULL, 'NONE', '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `user_profile` VALUES (28, 28, 'Clm453919', '', '', NULL, NULL, 'NONE', '2026-08-07 13:03:34', '2026-08-07 13:03:34');
INSERT INTO `user_profile` VALUES (29, 29, 'Clm553919', '', '', NULL, NULL, 'NONE', '2026-08-07 13:03:34', '2026-08-07 13:03:34');
INSERT INTO `user_profile` VALUES (30, 30, 'PubLC13676', '', '', NULL, NULL, 'NONE', '2026-08-07 13:04:21', '2026-08-07 13:04:21');
INSERT INTO `user_profile` VALUES (31, 31, 'ClmLC13676', '', '', NULL, NULL, 'NONE', '2026-08-07 13:04:21', '2026-08-07 13:04:21');
INSERT INTO `user_profile` VALUES (32, 32, 'Pub278902', '', '', NULL, NULL, 'NONE', '2026-08-07 13:04:22', '2026-08-07 13:04:22');
INSERT INTO `user_profile` VALUES (33, 33, 'Clm278902', '', '', NULL, NULL, 'NONE', '2026-08-07 13:04:22', '2026-08-07 13:04:22');
INSERT INTO `user_profile` VALUES (34, 34, 'Clm378902', '', '', NULL, NULL, 'NONE', '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `user_profile` VALUES (35, 35, 'Clm478902', '', '', NULL, NULL, 'NONE', '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `user_profile` VALUES (36, 36, 'Clm578902', '', '', NULL, NULL, 'NONE', '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `user_profile` VALUES (37, 37, 'CapProbe', '', '', NULL, NULL, 'NONE', '2026-08-07 13:04:39', '2026-08-07 13:04:39');
INSERT INTO `user_profile` VALUES (38, 38, '钢铁侠', '', '', NULL, NULL, 'NONE', '2026-08-07 15:13:16', '2026-08-07 15:13:16');

-- ----------------------------
-- Table structure for wallet_account
-- ----------------------------
DROP TABLE IF EXISTS `wallet_account`;
CREATE TABLE `wallet_account`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `balance` decimal(12, 2) NOT NULL DEFAULT 0.00,
  `frozen` decimal(12, 2) NOT NULL DEFAULT 0.00,
  `version` int(11) NOT NULL DEFAULT 0,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP(0),
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_wallet_user`(`user_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 22 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wallet_account
-- ----------------------------
INSERT INTO `wallet_account` VALUES (1, 1, 200.00, 550.00, 14, '2026-08-05 14:24:22', '2026-08-07 15:40:42');
INSERT INTO `wallet_account` VALUES (2, 2, 1000.00, 0.00, 1, '2026-08-05 14:40:28', '2026-08-05 14:40:51');
INSERT INTO `wallet_account` VALUES (3, 3, 800.00, 0.00, 3, '2026-08-05 15:40:51', '2026-08-05 15:40:52');
INSERT INTO `wallet_account` VALUES (4, 4, 180.00, 0.00, 1, '2026-08-05 15:40:51', '2026-08-05 15:40:52');
INSERT INTO `wallet_account` VALUES (5, 0, 80.00, 0.00, 4, '2026-08-05 15:40:52', '2026-08-05 23:05:08');
INSERT INTO `wallet_account` VALUES (6, 5, 300.00, 200.00, 2, '2026-08-05 15:58:29', '2026-08-05 15:58:29');
INSERT INTO `wallet_account` VALUES (7, 6, 300.00, 0.00, 3, '2026-08-05 16:01:11', '2026-08-05 16:01:12');
INSERT INTO `wallet_account` VALUES (8, 7, 0.00, 0.00, 0, '2026-08-05 16:01:11', '2026-08-05 16:01:11');
INSERT INTO `wallet_account` VALUES (9, 8, 180.00, 0.00, 1, '2026-08-05 16:01:11', '2026-08-05 16:01:12');
INSERT INTO `wallet_account` VALUES (10, 9, 750.00, 0.00, 4, '2026-08-05 16:34:41', '2026-08-05 16:34:41');
INSERT INTO `wallet_account` VALUES (11, 10, 500.00, 0.00, 1, '2026-08-05 16:34:41', '2026-08-05 16:34:41');
INSERT INTO `wallet_account` VALUES (12, 11, 500.00, 0.00, 1, '2026-08-05 16:34:41', '2026-08-05 16:34:41');
INSERT INTO `wallet_account` VALUES (13, 12, 500.00, 0.00, 3, '2026-08-05 20:07:06', '2026-08-05 20:07:06');
INSERT INTO `wallet_account` VALUES (14, 13, 500.00, 0.00, 1, '2026-08-05 20:07:06', '2026-08-05 20:07:06');
INSERT INTO `wallet_account` VALUES (15, 14, 300.00, 0.00, 5, '2026-08-05 20:07:35', '2026-08-05 20:07:36');
INSERT INTO `wallet_account` VALUES (16, 15, 680.00, 0.00, 2, '2026-08-05 20:07:35', '2026-08-05 20:07:36');
INSERT INTO `wallet_account` VALUES (17, 16, 300.00, 0.00, 3, '2026-08-05 23:05:07', '2026-08-05 23:05:08');
INSERT INTO `wallet_account` VALUES (18, 17, 680.00, 0.00, 2, '2026-08-05 23:05:07', '2026-08-05 23:05:08');
INSERT INTO `wallet_account` VALUES (19, 18, 500.00, 0.00, 1, '2026-08-05 23:35:20', '2026-08-05 23:35:20');
INSERT INTO `wallet_account` VALUES (20, 19, 500.00, 0.00, 1, '2026-08-05 23:59:34', '2026-08-05 23:59:34');
INSERT INTO `wallet_account` VALUES (21, 20, 500.00, 0.00, 1, '2026-08-06 13:18:01', '2026-08-06 13:18:01');
INSERT INTO `wallet_account` VALUES (22, 21, 300.00, 200.00, 2, '2026-08-07 11:29:44', '2026-08-07 11:29:45');
INSERT INTO `wallet_account` VALUES (23, 22, 500.00, 0.00, 1, '2026-08-07 11:29:45', '2026-08-07 11:29:45');
INSERT INTO `wallet_account` VALUES (24, 23, 300.00, 200.00, 2, '2026-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `wallet_account` VALUES (25, 24, 500.00, 0.00, 1, '2026-08-07 13:03:32', '2026-08-07 13:03:32');
INSERT INTO `wallet_account` VALUES (26, 25, 100.00, 400.00, 5, '2026-08-07 13:03:32', '2026-08-07 13:03:33');
INSERT INTO `wallet_account` VALUES (27, 26, 500.00, 0.00, 1, '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `wallet_account` VALUES (28, 27, 500.00, 0.00, 1, '2026-08-07 13:03:33', '2026-08-07 13:03:33');
INSERT INTO `wallet_account` VALUES (29, 28, 500.00, 0.00, 1, '2026-08-07 13:03:34', '2026-08-07 13:03:34');
INSERT INTO `wallet_account` VALUES (30, 29, 500.00, 0.00, 1, '2026-08-07 13:03:34', '2026-08-07 13:03:34');
INSERT INTO `wallet_account` VALUES (31, 30, 300.00, 200.00, 2, '2026-08-07 13:04:21', '2026-08-07 13:04:21');
INSERT INTO `wallet_account` VALUES (32, 31, 500.00, 0.00, 1, '2026-08-07 13:04:21', '2026-08-07 13:04:21');
INSERT INTO `wallet_account` VALUES (33, 32, 300.00, 200.00, 10, '2026-08-07 13:04:22', '2026-08-07 13:04:24');
INSERT INTO `wallet_account` VALUES (34, 33, 500.00, 0.00, 1, '2026-08-07 13:04:22', '2026-08-07 13:04:22');
INSERT INTO `wallet_account` VALUES (35, 34, 500.00, 0.00, 1, '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `wallet_account` VALUES (36, 35, 500.00, 0.00, 1, '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `wallet_account` VALUES (37, 36, 500.00, 0.00, 1, '2026-08-07 13:04:23', '2026-08-07 13:04:23');
INSERT INTO `wallet_account` VALUES (38, 37, 500.00, 0.00, 1, '2026-08-07 13:04:39', '2026-08-07 13:04:39');
INSERT INTO `wallet_account` VALUES (39, 38, 500.00, 0.00, 1, '2026-08-07 15:13:16', '2026-08-07 15:13:16');

-- ----------------------------
-- Table structure for wallet_ledger
-- ----------------------------
DROP TABLE IF EXISTS `wallet_ledger`;
CREATE TABLE `wallet_ledger`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `biz_no` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `amount` decimal(12, 2) NOT NULL,
  `balance_after` decimal(12, 2) NOT NULL,
  `frozen_after` decimal(12, 2) NOT NULL,
  `ref_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `ref_id` bigint(20) NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `created_at` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_ledger_biz_no`(`biz_no`) USING BTREE,
  INDEX `idx_ledger_user_time`(`user_id`, `created_at`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 51 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wallet_ledger
-- ----------------------------
INSERT INTO `wallet_ledger` VALUES (1, 'RC-recharge-1785911101922-oaj3s4', 1, 'RECHARGE', 100.00, 100.00, 0.00, 'WALLET', NULL, '模拟充值', '2026-08-05 14:25:02');
INSERT INTO `wallet_ledger` VALUES (2, 'RC-recharge-1785911107532-1nu16e', 1, 'RECHARGE', 150.00, 250.00, 0.00, 'WALLET', NULL, '模拟充值', '2026-08-05 14:25:08');
INSERT INTO `wallet_ledger` VALUES (3, 'RC-recharge-1785911110330-4l72y9', 1, 'RECHARGE', 300.00, 550.00, 0.00, 'WALLET', NULL, '模拟充值', '2026-08-05 14:25:10');
INSERT INTO `wallet_ledger` VALUES (4, 'FZ-6aae9cb8ac404aa5a8fdb6276d38d5ff', 1, 'FREEZE', 250.00, 300.00, 250.00, 'BOUNTY', 1, '发令托管冻结', '2026-08-05 14:31:23');
INSERT INTO `wallet_ledger` VALUES (5, 'RC-qa-idem-003', 2, 'RECHARGE', 1000.00, 1000.00, 0.00, 'WALLET', NULL, '模拟充值', '2026-08-05 14:40:51');
INSERT INTO `wallet_ledger` VALUES (6, 'RC-qa-e2e-65568', 3, 'RECHARGE', 1000.00, 1000.00, 0.00, 'WALLET', NULL, '模拟充值', '2026-08-05 15:40:51');
INSERT INTO `wallet_ledger` VALUES (7, 'FZ-b8daede945a04cffb7b6ff085692bb2b', 3, 'FREEZE', 200.00, 800.00, 200.00, 'BOUNTY', 2, '发令托管冻结', '2026-08-05 15:40:51');
INSERT INTO `wallet_ledger` VALUES (8, 'SP-44544492ed7644af8049b9d09d79f3c1', 3, 'SETTLE_PAY', -200.00, 800.00, 0.00, 'BOUNTY', 2, '结算扣托管', '2026-08-05 15:40:52');
INSERT INTO `wallet_ledger` VALUES (9, 'PF-bbba76fd1f004cd28d69c2d49be682e4', 0, 'PLATFORM_FEE', 20.00, 20.00, 0.00, 'BOUNTY', 2, '平台服务费', '2026-08-05 15:40:52');
INSERT INTO `wallet_ledger` VALUES (10, 'SI-e58db9b3485a461f85c9c31ea783bb22', 4, 'SETTLE_INCOME', 180.00, 180.00, 0.00, 'BOUNTY', 2, '揭榜结算入账', '2026-08-05 15:40:52');
INSERT INTO `wallet_ledger` VALUES (11, 'RC-qa-s4-41416', 5, 'RECHARGE', 500.00, 500.00, 0.00, 'WALLET', NULL, '模拟充值', '2026-08-05 15:58:29');
INSERT INTO `wallet_ledger` VALUES (12, 'FZ-0859166828a54d29b07e190263946f2f', 5, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 3, '发令托管冻结', '2026-08-05 15:58:29');
INSERT INTO `wallet_ledger` VALUES (13, 'RC-s4-73452', 6, 'RECHARGE', 500.00, 500.00, 0.00, 'WALLET', NULL, '模拟充值', '2026-08-05 16:01:12');
INSERT INTO `wallet_ledger` VALUES (14, 'FZ-87c7e8d65b01413987b95d0f3ae2864c', 6, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 4, '发令托管冻结', '2026-08-05 16:01:12');
INSERT INTO `wallet_ledger` VALUES (15, 'SP-91b78b48d175492e9d6605135b051692', 6, 'SETTLE_PAY', -200.00, 300.00, 0.00, 'BOUNTY', 4, '结算扣托管', '2026-08-05 16:01:12');
INSERT INTO `wallet_ledger` VALUES (16, 'PF-8ba927a061a244278a6e7f0ce1054549', 0, 'PLATFORM_FEE', 20.00, 40.00, 0.00, 'BOUNTY', 4, '平台服务费', '2026-08-05 16:01:12');
INSERT INTO `wallet_ledger` VALUES (17, 'SI-2bde568863b24f32af709b5d53fb66d3', 8, 'SETTLE_INCOME', 180.00, 180.00, 0.00, 'BOUNTY', 4, '揭榜结算入账', '2026-08-05 16:01:12');
INSERT INTO `wallet_ledger` VALUES (18, 'UR-6130684879434212a8d0e68cf81e01bd', 1, 'UNFREEZE_REFUND', 250.00, 550.00, 0.00, 'BOUNTY', 1, '发令审核驳回退款', '2026-08-05 16:14:57');
INSERT INTO `wallet_ledger` VALUES (19, 'REG_GRANT:9', 9, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 9, '注册赠银', '2026-08-05 16:34:41');
INSERT INTO `wallet_ledger` VALUES (20, 'REG_GRANT:10', 10, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 10, '注册赠银', '2026-08-05 16:34:41');
INSERT INTO `wallet_ledger` VALUES (21, 'INV_REWARD:10', 9, 'INVITE_REWARD', 100.00, 600.00, 0.00, 'USER', 10, '邀新奖励', '2026-08-05 16:34:41');
INSERT INTO `wallet_ledger` VALUES (22, 'REG_GRANT:11', 11, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 11, '注册赠银', '2026-08-05 16:34:41');
INSERT INTO `wallet_ledger` VALUES (23, 'INV_REWARD:11', 9, 'INVITE_REWARD', 100.00, 700.00, 0.00, 'USER', 11, '邀新奖励', '2026-08-05 16:34:41');
INSERT INTO `wallet_ledger` VALUES (24, 'ADJ-fa8557e9a71548cc8c89dc4d563453da', 9, 'ADJUST', 50.00, 750.00, 0.00, 'ADMIN', NULL, 'qa v17 grant', '2026-08-05 16:34:41');
INSERT INTO `wallet_ledger` VALUES (25, 'FZ-66e405eb3b1646938211e365d5773ce6', 1, 'FREEZE', 250.00, 300.00, 250.00, 'BOUNTY', 5, '发令托管冻结', '2026-08-05 17:04:26');
INSERT INTO `wallet_ledger` VALUES (26, 'REG_GRANT:12', 12, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 12, '注册赠银', '2026-08-05 20:07:06');
INSERT INTO `wallet_ledger` VALUES (27, 'REG_GRANT:13', 13, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 13, '注册赠银', '2026-08-05 20:07:06');
INSERT INTO `wallet_ledger` VALUES (28, 'FZ-5b127eb20ea5446cb4c0ca9b80d3365f', 12, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 6, '发令托管冻结', '2026-08-05 20:07:06');
INSERT INTO `wallet_ledger` VALUES (29, 'UR-31392334a73e4ea88964efbf6a874346', 12, 'UNFREEZE_REFUND', 200.00, 500.00, 0.00, 'BOUNTY', 6, '发令审核驳回退款', '2026-08-05 20:07:06');
INSERT INTO `wallet_ledger` VALUES (30, 'REG_GRANT:14', 14, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 14, '注册赠银', '2026-08-05 20:07:35');
INSERT INTO `wallet_ledger` VALUES (31, 'REG_GRANT:15', 15, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 15, '注册赠银', '2026-08-05 20:07:35');
INSERT INTO `wallet_ledger` VALUES (32, 'FZ-7350969cf9c444e8b9eb1c435a9bfbaf', 14, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 7, '发令托管冻结', '2026-08-05 20:07:35');
INSERT INTO `wallet_ledger` VALUES (33, 'UR-91944cb9e5874c21971e15d780a35270', 14, 'UNFREEZE_REFUND', 200.00, 500.00, 0.00, 'BOUNTY', 7, '发令审核驳回退款', '2026-08-05 20:07:35');
INSERT INTO `wallet_ledger` VALUES (34, 'FZ-4ba2e399f4b74da4abb6d6188dcb7c03', 14, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 8, '发令托管冻结', '2026-08-05 20:07:36');
INSERT INTO `wallet_ledger` VALUES (35, 'SP-48366ac9fa5a49d085eda7820b5e425e', 14, 'SETTLE_PAY', -200.00, 300.00, 0.00, 'BOUNTY', 8, '结算扣托管', '2026-08-05 20:07:36');
INSERT INTO `wallet_ledger` VALUES (36, 'PF-42ab3918c59b4724872330a0193d3b12', 0, 'PLATFORM_FEE', 20.00, 60.00, 0.00, 'BOUNTY', 8, '平台服务费', '2026-08-05 20:07:36');
INSERT INTO `wallet_ledger` VALUES (37, 'SI-74bbff6eab334d2da7f99b6bb5a3ddae', 15, 'SETTLE_INCOME', 180.00, 680.00, 0.00, 'BOUNTY', 8, '揭榜结算入账', '2026-08-05 20:07:36');
INSERT INTO `wallet_ledger` VALUES (38, 'REG_GRANT:16', 16, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 16, '注册赠银', '2026-08-05 23:05:07');
INSERT INTO `wallet_ledger` VALUES (39, 'REG_GRANT:17', 17, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 17, '注册赠银', '2026-08-05 23:05:07');
INSERT INTO `wallet_ledger` VALUES (40, 'FZ-2b205a1540904be38d8d7f95f757bafc', 16, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 9, '发令托管冻结', '2026-08-05 23:05:07');
INSERT INTO `wallet_ledger` VALUES (41, 'SP-2da6966c135c482e9ee96c4ef349dd8c', 16, 'SETTLE_PAY', -200.00, 300.00, 0.00, 'BOUNTY', 9, '结算扣托管', '2026-08-05 23:05:08');
INSERT INTO `wallet_ledger` VALUES (42, 'PF-1fcc72f1d21540989dafd56fe99e61a0', 0, 'PLATFORM_FEE', 20.00, 80.00, 0.00, 'BOUNTY', 9, '平台服务费', '2026-08-05 23:05:08');
INSERT INTO `wallet_ledger` VALUES (43, 'SI-7ddd2c88734940348cc89240a6943c98', 17, 'SETTLE_INCOME', 180.00, 680.00, 0.00, 'BOUNTY', 9, '揭榜结算入账', '2026-08-05 23:05:08');
INSERT INTO `wallet_ledger` VALUES (44, 'REG_GRANT:18', 18, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 18, '注册赠银', '2026-08-05 23:35:20');
INSERT INTO `wallet_ledger` VALUES (45, 'REG_GRANT:19', 19, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 19, '注册赠银', '2026-08-05 23:59:34');
INSERT INTO `wallet_ledger` VALUES (46, 'UR-8a8ffa6337a645b0b8093e2ee3b5cd7c', 1, 'UNFREEZE_REFUND', 250.00, 550.00, 0.00, 'BOUNTY', 5, '超时自动退款', '2026-08-06 00:00:14');
INSERT INTO `wallet_ledger` VALUES (47, 'REG_GRANT:20', 20, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 20, '注册赠银', '2026-08-06 13:18:01');
INSERT INTO `wallet_ledger` VALUES (48, 'INV_REWARD:20', 1, 'INVITE_REWARD', 100.00, 650.00, 0.00, 'USER', 20, '邀新奖励', '2026-08-06 13:18:01');
INSERT INTO `wallet_ledger` VALUES (49, 'FZ-e41e6652bfd74e02ae5a8cd57c57b0b8', 1, 'FREEZE', 250.00, 400.00, 250.00, 'BOUNTY', 10, '发令托管冻结', '2026-08-06 13:23:50');
INSERT INTO `wallet_ledger` VALUES (50, 'UR-b6b80e845985432db48c92343c461ca0', 1, 'UNFREEZE_REFUND', 250.00, 650.00, 0.00, 'BOUNTY', 10, '超时自动退款', '2026-08-07 00:00:24');
INSERT INTO `wallet_ledger` VALUES (51, 'REG_GRANT:21', 21, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 21, '注册赠银', '2026-08-07 11:29:44');
INSERT INTO `wallet_ledger` VALUES (52, 'REG_GRANT:22', 22, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 22, '注册赠银', '2026-08-07 11:29:45');
INSERT INTO `wallet_ledger` VALUES (53, 'FZ-989a942702f145ef920573e54b1b55c4', 21, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 11, '发令托管冻结', '2026-08-07 11:29:45');
INSERT INTO `wallet_ledger` VALUES (54, 'FZ-ffb8abbb7fa34968a67a621fa8200a0c', 1, 'FREEZE', 250.00, 400.00, 250.00, 'BOUNTY', 12, '发令托管冻结', '2026-08-07 12:43:44');
INSERT INTO `wallet_ledger` VALUES (55, 'REG_GRANT:23', 23, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 23, '注册赠银', '2026-08-07 13:03:32');
INSERT INTO `wallet_ledger` VALUES (56, 'REG_GRANT:24', 24, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 24, '注册赠银', '2026-08-07 13:03:32');
INSERT INTO `wallet_ledger` VALUES (57, 'FZ-55817880ba1f437a81799df5be971ba2', 23, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 13, '发令托管冻结', '2026-08-07 13:03:32');
INSERT INTO `wallet_ledger` VALUES (58, 'REG_GRANT:25', 25, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 25, '注册赠银', '2026-08-07 13:03:33');
INSERT INTO `wallet_ledger` VALUES (59, 'REG_GRANT:26', 26, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 26, '注册赠银', '2026-08-07 13:03:33');
INSERT INTO `wallet_ledger` VALUES (60, 'FZ-de74e97abf5649d6a244ec21a55a442b', 25, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 14, '发令托管冻结', '2026-08-07 13:03:33');
INSERT INTO `wallet_ledger` VALUES (61, 'UR-24249bedba7e4fc0b75ae04a77c027d2', 25, 'UNFREEZE_REFUND', 200.00, 500.00, 0.00, 'BOUNTY', 14, '悬赏取消退款', '2026-08-07 13:03:33');
INSERT INTO `wallet_ledger` VALUES (62, 'FZ-280591a02f9842a292ebfec460bbafaa', 25, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 15, '发令托管冻结', '2026-08-07 13:03:33');
INSERT INTO `wallet_ledger` VALUES (63, 'REG_GRANT:27', 27, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 27, '注册赠银', '2026-08-07 13:03:33');
INSERT INTO `wallet_ledger` VALUES (64, 'FZ-7cbabdc3fcb445f18b0e6b01c6d2bc44', 25, 'FREEZE', 200.00, 100.00, 400.00, 'BOUNTY', 16, '发令托管冻结', '2026-08-07 13:03:33');
INSERT INTO `wallet_ledger` VALUES (65, 'REG_GRANT:28', 28, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 28, '注册赠银', '2026-08-07 13:03:34');
INSERT INTO `wallet_ledger` VALUES (66, 'REG_GRANT:29', 29, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 29, '注册赠银', '2026-08-07 13:03:34');
INSERT INTO `wallet_ledger` VALUES (67, 'REG_GRANT:30', 30, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 30, '注册赠银', '2026-08-07 13:04:21');
INSERT INTO `wallet_ledger` VALUES (68, 'REG_GRANT:31', 31, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 31, '注册赠银', '2026-08-07 13:04:21');
INSERT INTO `wallet_ledger` VALUES (69, 'FZ-7e5b776c0bf0406e84fc18824df90033', 30, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 19, '发令托管冻结', '2026-08-07 13:04:21');
INSERT INTO `wallet_ledger` VALUES (70, 'REG_GRANT:32', 32, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 32, '注册赠银', '2026-08-07 13:04:22');
INSERT INTO `wallet_ledger` VALUES (71, 'REG_GRANT:33', 33, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 33, '注册赠银', '2026-08-07 13:04:22');
INSERT INTO `wallet_ledger` VALUES (72, 'FZ-7c416aaac15e45b0a91c5a65b3919834', 32, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 20, '发令托管冻结', '2026-08-07 13:04:22');
INSERT INTO `wallet_ledger` VALUES (73, 'UR-71b2e3e12cfb42979c82f4038ad104d7', 32, 'UNFREEZE_REFUND', 200.00, 500.00, 0.00, 'BOUNTY', 20, '悬赏取消退款', '2026-08-07 13:04:22');
INSERT INTO `wallet_ledger` VALUES (74, 'FZ-adf1be4bbc4a4230a8674d1263f68d1c', 32, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 21, '发令托管冻结', '2026-08-07 13:04:22');
INSERT INTO `wallet_ledger` VALUES (75, 'UR-f381490382d74f22beae40e0496d6644', 32, 'UNFREEZE_REFUND', 200.00, 500.00, 0.00, 'BOUNTY', 21, '发令审核驳回退款', '2026-08-07 13:04:22');
INSERT INTO `wallet_ledger` VALUES (76, 'REG_GRANT:34', 34, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 34, '注册赠银', '2026-08-07 13:04:23');
INSERT INTO `wallet_ledger` VALUES (77, 'FZ-4322ed274c1f465a8ab148db8c131030', 32, 'FREEZE', 200.00, 300.00, 200.00, 'BOUNTY', 22, '发令托管冻结', '2026-08-07 13:04:23');
INSERT INTO `wallet_ledger` VALUES (78, 'REG_GRANT:35', 35, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 35, '注册赠银', '2026-08-07 13:04:23');
INSERT INTO `wallet_ledger` VALUES (79, 'FZ-72a4144922dc4b0d93fcabd80f58a5c7', 32, 'FREEZE', 200.00, 100.00, 400.00, 'BOUNTY', 23, '发令托管冻结', '2026-08-07 13:04:23');
INSERT INTO `wallet_ledger` VALUES (80, 'UR-b69b4ed405b441818d1774db59e2e8ee', 32, 'UNFREEZE_REFUND', 200.00, 300.00, 200.00, 'BOUNTY', 23, '悬赏取消退款', '2026-08-07 13:04:23');
INSERT INTO `wallet_ledger` VALUES (81, 'REG_GRANT:36', 36, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 36, '注册赠银', '2026-08-07 13:04:23');
INSERT INTO `wallet_ledger` VALUES (82, 'FZ-61c1b1d56b0e431da6cbc3a58b1ba233', 32, 'FREEZE', 200.00, 100.00, 400.00, 'BOUNTY', 24, '发令托管冻结', '2026-08-07 13:04:24');
INSERT INTO `wallet_ledger` VALUES (83, 'UR-45ae5ae040bd45a1a0c3f2e34c8a0479', 32, 'UNFREEZE_REFUND', 200.00, 300.00, 200.00, 'BOUNTY', 24, '悬赏取消退款', '2026-08-07 13:04:24');
INSERT INTO `wallet_ledger` VALUES (84, 'REG_GRANT:37', 37, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 37, '注册赠银', '2026-08-07 13:04:39');
INSERT INTO `wallet_ledger` VALUES (85, 'UR-3b7454c41eba4f8cbb34b3fda04f2baa', 1, 'UNFREEZE_REFUND', 250.00, 650.00, 0.00, 'BOUNTY', 12, '悬赏取消退款', '2026-08-07 15:05:57');
INSERT INTO `wallet_ledger` VALUES (86, 'REG_GRANT:38', 38, 'REGISTER_GRANT', 500.00, 500.00, 0.00, 'USER', 38, '注册赠银', '2026-08-07 15:13:16');
INSERT INTO `wallet_ledger` VALUES (87, 'INV_REWARD:38', 1, 'INVITE_REWARD', 100.00, 750.00, 0.00, 'USER', 38, '邀新奖励', '2026-08-07 15:13:16');
INSERT INTO `wallet_ledger` VALUES (88, 'FZ-372237dcecb841e391a42b926649636d', 1, 'FREEZE', 550.00, 200.00, 550.00, 'BOUNTY', 25, '发令托管冻结', '2026-08-07 15:40:42');

-- ----------------------------
-- Table structure for warrant_field_config
-- ----------------------------
DROP TABLE IF EXISTS `warrant_field_config`;
CREATE TABLE `warrant_field_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `template_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `template_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `field_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'text',
  `required` tinyint(1) NOT NULL DEFAULT 0,
  `mask_until_claimed` tinyint(1) NOT NULL DEFAULT 0,
  `sort_no` int(11) NOT NULL DEFAULT 0,
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'ACTIVE',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_warrant_field`(`template_code`, `field_key`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of warrant_field_config
-- ----------------------------
INSERT INTO `warrant_field_config` VALUES (1, 'RENT_SEEK', '租房令状', 'district', '区域', 'text', 1, 0, 1, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (2, 'RENT_SEEK', '租房令状', 'rentBudgetMin', '预算下限(元/月)', 'number', 1, 0, 2, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (3, 'RENT_SEEK', '租房令状', 'rentBudgetMax', '预算上限(元/月)', 'number', 1, 0, 3, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (4, 'RENT_SEEK', '租房令状', 'layout', '户型', 'text', 1, 0, 4, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (5, 'RENT_SEEK', '租房令状', 'expectMoveInDate', '期望入住', 'date', 1, 0, 5, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (6, 'RENT_SEEK', '租房令状', 'acceptAgency', '是否接受中介', 'boolean', 1, 0, 6, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (7, 'RENT_SEEK', '租房令状', 'extra', '补充说明', 'textarea', 0, 0, 7, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (8, 'RENT_OUT', '出租令状', 'district', '区域', 'text', 1, 0, 1, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (9, 'RENT_OUT', '出租令状', 'exactAddress', '精确位置', 'text', 1, 1, 2, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (10, 'RENT_OUT', '出租令状', 'rentPrice', '租金(元/月)', 'number', 1, 0, 3, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (11, 'RENT_OUT', '出租令状', 'layout', '户型', 'text', 1, 0, 4, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (12, 'RENT_OUT', '出租令状', 'availableDate', '可入住日期', 'date', 1, 0, 5, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (13, 'RENT_OUT', '出租令状', 'furniture', '家具家电', 'text', 0, 0, 6, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (14, 'RENT_OUT', '出租令状', 'extra', '补充说明', 'textarea', 0, 0, 7, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (16, 'RENT_TRANSFER', '转租令状', 'district', '区域', 'text', 1, 0, 1, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (17, 'RENT_TRANSFER', '转租令状', 'exactAddress', '精确位置', 'text', 1, 1, 2, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (18, 'RENT_TRANSFER', '转租令状', 'rentPrice', '租金(元/月)', 'number', 1, 0, 3, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (19, 'RENT_TRANSFER', '转租令状', 'layout', '户型', 'text', 1, 0, 4, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (20, 'RENT_TRANSFER', '转租令状', 'availableDate', '可入住日期', 'date', 1, 0, 5, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (21, 'RENT_TRANSFER', '转租令状', 'complianceNote', '转租合规说明', 'textarea', 0, 0, 6, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (22, 'RENT_TRANSFER', '转租令状', 'furniture', '家具家电', 'text', 0, 0, 7, 'ACTIVE');
INSERT INTO `warrant_field_config` VALUES (23, 'RENT_TRANSFER', '转租令状', 'extra', '补充说明', 'textarea', 0, 0, 8, 'ACTIVE');
