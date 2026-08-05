USE jianghu_ling;

-- 系统参数
INSERT INTO sys_config (config_key, config_value, remark) VALUES
('min_reward', '200', '最低赏银'),
('fee_rate', '0.10', '平台服务费比例'),
('claim_day_limit', '10', '每日揭榜上限'),
('daily_free_stamina', '5', '每日免费体力'),
('claim_stamina_cost', '1', '单次揭榜耗体力'),
('submit_cooldown_seconds', '600', '成果提交冷却秒'),
('submit_day_limit', '20', '每日成果提交上限'),
('invite_daily_quota', '3', '每日邀请码额度'),
('chivalry_per_complete', '10', '完结基础侠义值');

INSERT INTO reward_suggest_config (code, name, suggest_min, suggest_max, sort_no) VALUES
('EASY', '简易', 200, 300, 1),
('NORMAL', '普通', 300, 500, 2),
('HARD', '艰辛', 500, 800, 3),
('EXTREME', '超难', 800, 1500, 4);

INSERT INTO checklist_template (item_code, item_name, required, tags_json, sort_no) VALUES
('VERIFY_AUTHENTIC', '核验房源真实性', 1, '["帮寻房","帮带看","帮验房"]', 1),
('SITE_VISIT_RECORD', '现场带看记录', 1, '["帮带看","帮验房"]', 2),
('PHOTO_EVIDENCE', '现场照片/视频', 1, '["帮带看","帮验房"]', 3),
('NEIGHBORHOOD_NOTE', '周边配套备注', 0, '["帮寻房","帮带看"]', 4),
('CONTRACT_HINT', '合同/中介风险提示', 0, '["帮验房"]', 5),
('LANDLORD_CONTACT', '房东沟通记录', 0, '["帮寻租客"]', 6);

INSERT INTO notice (category, title, content, pinned, status) VALUES
('RULES', '江湖规矩摘要', '发令须托管赏银；揭榜耗体力；完结按九一分配；严禁虚假房源。', 1, 'PUBLISHED'),
('ANTI_FRAUD', '防骗须知', '线下看房注意人身与财产安全；切勿提前转账给陌生人；平台银两均为模拟。', 1, 'PUBLISHED'),
('ZUNYI_RENT', '遵义租房须知', '试点城市遵义。求租/出租请填写结构化令状，赏银建议不低于难度区间。', 1, 'PUBLISHED'),
('ANNOUNCE', '开山告示', '江湖互助平台遵义试点开启，邀请制注册。', 0, 'PUBLISHED');

INSERT INTO office_def (code, name, min_level, quota, term_days, status) VALUES
('DECREE_REVIEWER', '令审使', 1, 20, 90, 'ACTIVE'),
('FEAT_REVIEWER', '验功使', 1, 20, 90, 'ACTIVE');

-- 平台种子邀请码（无归属用户，quota 充足便于联调）
INSERT INTO invite_code (code, owner_user_id, quota, used_count, status, expire_at) VALUES
('JHOPEN1', NULL, 999, 0, 'ACTIVE', DATE_ADD(NOW(), INTERVAL 1 YEAR)),
('JHOPEN2', NULL, 999, 0, 'ACTIVE', DATE_ADD(NOW(), INTERVAL 1 YEAR));

-- 管理员由 dev 环境 DevAdminInitializer 创建/重置：admin / admin123
