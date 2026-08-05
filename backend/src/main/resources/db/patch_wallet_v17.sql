USE jianghu_ling;

INSERT INTO sys_config (config_key, config_value, remark)
SELECT 'wallet.rechargeEnabled', 'false', '用户充值开关（默认关）'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wallet.rechargeEnabled');

INSERT INTO sys_config (config_key, config_value, remark)
SELECT 'wallet.withdrawEnabled', 'false', '用户提现开关（默认关）'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wallet.withdrawEnabled');

INSERT INTO sys_config (config_key, config_value, remark)
SELECT 'wallet.registerGrantAmount', '500', '注册赠银（两）'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wallet.registerGrantAmount');

INSERT INTO sys_config (config_key, config_value, remark)
SELECT 'wallet.inviteRewardAmount', '100', '邀新奖励（两，入邀请人）'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'wallet.inviteRewardAmount');
