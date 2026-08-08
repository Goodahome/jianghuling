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
('chivalry_per_complete', '10', '完结基础侠义值'),
('chivalry_per_stamina', '10', '侠义兑1点体力所需侠义值'),
('wallet.rechargeEnabled', 'false', '用户充值开关（默认关）'),
('wallet.withdrawEnabled', 'false', '用户提现开关（默认关）'),
('wallet.registerGrantAmount', '500', '注册赠银（两）'),
('wallet.inviteRewardAmount', '100', '邀新奖励（两，入邀请人）'),
('feedback.cooldownSeconds', '60', '用户反馈短时冷却秒'),
('feedback.dailyLimit', '10', '用户反馈自然日上限');

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

-- 标准告示正文对齐 docs/notices/standard-notices.md（N1–N6）
INSERT INTO notice (category, title, content, pinned, status) VALUES
('RULES', '江湖规矩摘要',
'一、张贴悬赏须按令状填写，赏银以「两」计，发令即托管；未满建议档须二次确认，硬性最低二百两。
二、揭榜即入协作，同令仅可揭一次；每日揭榜有上限，且消耗体力。
三、完结须将可分配赏银（扣除一成堂口服务费后）全部分完；允许某侠零两，可另赏侠义值。
四、严禁虚假房源、假带看、刷侠义、诱导私下大额转账。违者可被驳回、封禁或强制关令。
五、本平台为悬赏协作工具，非房屋中介，不成交担保。线下看房风险自负，且须遵守防骗箴言。
六、银两为模拟记账，非法定货币；内测阶段充值提现暂不开放。', 1, 'PUBLISHED'),
('ANTI_FRAUD', '防骗箴言',
'一、线下看房选白天与公共场所，告知亲友行程；勿独自前往偏僻处所。
二、切勿向陌生人预付定金、房租或「跑腿费」至私人账户以绕开平台托管。
三、平台内「两」均为模拟银两，任何人声称可兑成人民币或要求站外充值，皆为欺诈。
四、勿轻信「内部房源」「内部折扣」；验房、核验按探子清单留证，勿只看口头承诺。
五、证件、合同原件勿交不相识之人；拍照留证时注意脱敏，勿泄露他人隐私。
六、遇可疑情形立即终止会面，并向武林盟举报或发起纠纷。', 1, 'PUBLISHED'),
('ZUNYI_RENT', '遵义租房须知',
'一、本平台首发范围为遵义单城试点；令状请如实填写片区、户型、租金预算或挂牌租金、入住时间等。
二、遵义民间常见「押一付三」等习惯仅供参考，具体以双方约定与合同为准，本告示不构成法律意见。
三、求租发「租房悬赏」，房东发「出租悬赏」，转租发「转租悬赏」；赏银用于酬谢带看、核验等劳动，与房租（元/月）分开计算。
四、建议赏银覆盖同城交通与合理时间成本，勿亏待行侠同道。
五、转租须确认原租约是否允许转租，平台不审核产权合法性，令主自行担责。', 1, 'PUBLISHED'),
('ANNOUNCE', '开山告示',
'江湖令遵义试点开启。持有效邀请方可入江湖。张贴悬赏、揭榜行侠、钱庄托管、声望成长，皆在告示板内。内测期间功能与规则或有调整，以告示与站内通知为准。天下有悬赏，江湖有侠士。江湖不让善意吃亏。', 0, 'PUBLISHED'),
('RULES', '赏银托管与分配说明',
'发令成功后赏银冻结于模拟钱庄。审核驳回或超时未成，按规则解冻退回。验功通过后令主完结分配：一成服务费归堂口，九成可分配池须在揭榜侠士间分完。协作中取消且已有成果提交的，进入分配页处理；无成果则全额退回。细则见用户协议与站内规则。', 0, 'PUBLISHED'),
('ANNOUNCE', '邀请制与执事堂简说',
'注册须持邀请码或邀请链接。侠士可在额度内邀同道入江湖。达到等级可申请令审使、验功使等职司，由武林盟授予后进入执事堂履职。武林盟主为声望荣誉顶点，不自动获得后台超管之权。', 0, 'PUBLISHED');

INSERT INTO office_def (code, name, min_level, quota, term_days, status) VALUES
('DECREE_REVIEWER', '令审使', 1, 20, 90, 'ACTIVE'),
('FEAT_REVIEWER', '验功使', 1, 20, 90, 'ACTIVE');

INSERT INTO user_level_config (level, title, min_chivalry, privileges_json, sort_no) VALUES
(1, '初入江湖', 0, '[]', 1),
(2, '初显身手', 50, '[]', 2),
(3, '小有名气', 200, '[]', 3),
(4, '名扬江湖', 500, '[]', 4);

INSERT INTO warrant_field_config (template_code, template_name, field_key, label, field_type, required, mask_until_claimed, sort_no) VALUES
('RENT_SEEK', '租房令状', 'district', '区域', 'text', 1, 0, 1),
('RENT_SEEK', '租房令状', 'rentBudgetMin', '预算下限(元/月)', 'number', 1, 0, 2),
('RENT_SEEK', '租房令状', 'rentBudgetMax', '预算上限(元/月)', 'number', 1, 0, 3),
('RENT_SEEK', '租房令状', 'layout', '户型', 'text', 1, 0, 4),
('RENT_SEEK', '租房令状', 'expectMoveInDate', '期望入住', 'date', 1, 0, 5),
('RENT_SEEK', '租房令状', 'acceptAgency', '是否接受中介', 'boolean', 1, 0, 6),
('RENT_SEEK', '租房令状', 'extra', '补充说明', 'textarea', 0, 0, 7),
('RENT_OUT', '出租令状', 'district', '区域', 'text', 1, 0, 1),
('RENT_OUT', '出租令状', 'exactAddress', '精确位置', 'text', 1, 1, 2),
('RENT_OUT', '出租令状', 'rentPrice', '租金(元/月)', 'number', 1, 0, 3),
('RENT_OUT', '出租令状', 'layout', '户型', 'text', 1, 0, 4),
('RENT_OUT', '出租令状', 'availableDate', '可入住日期', 'date', 1, 0, 5),
('RENT_OUT', '出租令状', 'furniture', '家具家电', 'text', 0, 0, 6),
('RENT_OUT', '出租令状', 'extra', '补充说明', 'textarea', 0, 0, 7),
('RENT_TRANSFER', '转租令状', 'district', '区域', 'text', 1, 0, 1),
('RENT_TRANSFER', '转租令状', 'exactAddress', '精确位置', 'text', 1, 1, 2),
('RENT_TRANSFER', '转租令状', 'rentPrice', '租金(元/月)', 'number', 1, 0, 3),
('RENT_TRANSFER', '转租令状', 'layout', '户型', 'text', 1, 0, 4),
('RENT_TRANSFER', '转租令状', 'availableDate', '可入住日期', 'date', 1, 0, 5),
('RENT_TRANSFER', '转租令状', 'complianceNote', '转租合规说明', 'textarea', 0, 0, 6),
('RENT_TRANSFER', '转租令状', 'furniture', '家具家电', 'text', 0, 0, 7),
('RENT_TRANSFER', '转租令状', 'extra', '补充说明', 'textarea', 0, 0, 8);

INSERT INTO reward_product (name, description, cost_chivalry, stock, cover_url, status) VALUES
('遵义茶礼', '本地特产礼盒（模拟）', 30, 99, '', 'ACTIVE'),
('江湖腰牌', '个性展示徽章（模拟）', 50, 50, '', 'ACTIVE'),
('体力补给包', '额外体力展示道具（模拟）', 20, 200, '', 'ACTIVE');

INSERT INTO sys_config (config_key, config_value, remark) VALUES
('ranks_config', '{"refreshMinutes":10,"excludeBanned":true,"lordTopDisplay":true,"eligibleForLordTopN":1,"rejectCooldownDays":7}', '英雄谱规则');

-- 平台种子邀请码（无归属用户，quota 充足便于联调）
INSERT INTO invite_code (code, owner_user_id, quota, used_count, status, expire_at) VALUES
('JHOPEN1', NULL, 999, 0, 'ACTIVE', DATE_ADD(NOW(), INTERVAL 1 YEAR)),
('JHOPEN2', NULL, 999, 0, 'ACTIVE', DATE_ADD(NOW(), INTERVAL 1 YEAR));

-- 管理员由 dev 环境 DevAdminInitializer 创建/重置：admin / admin123
