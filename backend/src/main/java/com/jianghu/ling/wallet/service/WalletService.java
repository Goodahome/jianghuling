package com.jianghu.ling.wallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.wallet.domain.WalletAccount;
import com.jianghu.ling.wallet.domain.WalletLedger;
import com.jianghu.ling.wallet.mapper.WalletAccountMapper;
import com.jianghu.ling.wallet.mapper.WalletLedgerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WalletService {

    public static final String RECHARGE = "RECHARGE";
    public static final String FREEZE = "FREEZE";
    public static final String UNFREEZE_REFUND = "UNFREEZE_REFUND";
    public static final String SETTLE_PAY = "SETTLE_PAY";
    public static final String SETTLE_INCOME = "SETTLE_INCOME";
    public static final String PLATFORM_FEE = "PLATFORM_FEE";
    public static final String WITHDRAW = "WITHDRAW";
    public static final String ADJUST = "ADJUST";
    public static final String REGISTER_GRANT = "REGISTER_GRANT";
    public static final String INVITE_REWARD = "INVITE_REWARD";

    public static final String CFG_RECHARGE_ENABLED = "wallet.rechargeEnabled";
    public static final String CFG_WITHDRAW_ENABLED = "wallet.withdrawEnabled";
    public static final String CFG_REGISTER_GRANT = "wallet.registerGrantAmount";
    public static final String CFG_INVITE_REWARD = "wallet.inviteRewardAmount";

    private final WalletAccountMapper accountMapper;
    private final WalletLedgerMapper ledgerMapper;
    private final ConfigService configService;

    public WalletAccount getOrCreate(Long userId) {
        WalletAccount account = accountMapper.selectOne(new LambdaQueryWrapper<WalletAccount>()
                .eq(WalletAccount::getUserId, userId)
                .last("LIMIT 1"));
        if (account != null) {
            return account;
        }
        account = new WalletAccount();
        account.setUserId(userId);
        account.setBalance(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        account.setFrozen(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP));
        account.setVersion(0);
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        try {
            accountMapper.insert(account);
        } catch (DuplicateKeyException e) {
            return accountMapper.selectOne(new LambdaQueryWrapper<WalletAccount>()
                    .eq(WalletAccount::getUserId, userId)
                    .last("LIMIT 1"));
        }
        return account;
    }

    public boolean isRechargeEnabled() {
        return configService.getBoolean(CFG_RECHARGE_ENABLED, false);
    }

    public boolean isWithdrawEnabled() {
        return configService.getBoolean(CFG_WITHDRAW_ENABLED, false);
    }

    public BigDecimal registerGrantAmount() {
        return configService.getDecimal(CFG_REGISTER_GRANT, "500").setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal inviteRewardAmount() {
        return configService.getDecimal(CFG_INVITE_REWARD, "100").setScale(2, RoundingMode.HALF_UP);
    }

    public Map<String, Object> walletFeatures() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("rechargeEnabled", isRechargeEnabled());
        data.put("withdrawEnabled", isWithdrawEnabled());
        data.put("registerGrantAmount", registerGrantAmount());
        data.put("inviteRewardAmount", inviteRewardAmount());
        data.put("currencyLabel", "两");
        data.put("simulated", true);
        data.put("hint", "模拟银两，由平台发放与悬赏流转");
        return data;
    }

    public Map<String, Object> accountView(Long userId) {
        WalletAccount account = getOrCreate(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("balance", account.getBalance());
        map.put("frozen", account.getFrozen());
        map.put("currency", "两");
        map.put("simulated", true);
        map.put("rechargeEnabled", isRechargeEnabled());
        map.put("withdrawEnabled", isWithdrawEnabled());
        return map;
    }

    @Transactional
    public Map<String, Object> recharge(Long userId, BigDecimal amount, String bizNo) {
        if (!isRechargeEnabled()) {
            throw new BizException(ErrorCode.WALLET_FEATURE_DISABLED);
        }
        assertPositive(amount);
        WalletLedger existing = findByBizNo(bizNo);
        if (existing != null) {
            return rechargeView(userId, existing.getBizNo());
        }
        WalletAccount account = getOrCreate(userId);
        account.setBalance(account.getBalance().add(amount));
        updateAccount(account);
        writeLedger(bizNo, userId, RECHARGE, amount, account, "WALLET", null, "模拟充值");
        return rechargeView(userId, bizNo);
    }

    @Transactional
    public Map<String, Object> withdraw(Long userId, BigDecimal amount, String bizNo) {
        if (!isWithdrawEnabled()) {
            throw new BizException(ErrorCode.WALLET_FEATURE_DISABLED);
        }
        assertPositive(amount);
        WalletLedger existing = findByBizNo(bizNo);
        if (existing != null) {
            return rechargeView(userId, existing.getBizNo());
        }
        WalletAccount account = getOrCreate(userId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BizException(ErrorCode.WALLET_INSUFFICIENT);
        }
        account.setBalance(account.getBalance().subtract(amount));
        updateAccount(account);
        writeLedger(bizNo, userId, WITHDRAW, amount.negate(), account, "WALLET", null, "模拟提现");
        return rechargeView(userId, bizNo);
    }

    /** 注册赠银：biz_no=REG_GRANT:{userId} */
    @Transactional
    public void grantRegisterBonus(Long userId) {
        BigDecimal amount = registerGrantAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        String bizNo = "REG_GRANT:" + userId;
        if (findByBizNo(bizNo) != null) {
            return;
        }
        WalletAccount account = getOrCreate(userId);
        account.setBalance(account.getBalance().add(amount));
        updateAccount(account);
        writeLedger(bizNo, userId, REGISTER_GRANT, amount, account, "USER", userId, "注册赠银");
    }

    /** 邀新奖励入邀请人：biz_no=INV_REWARD:{inviteeId}，同一被邀请人仅一次 */
    @Transactional
    public void grantInviteReward(Long inviterId, Long inviteeId) {
        if (inviterId == null || inviterId <= 0 || inviteeId == null) {
            return;
        }
        BigDecimal amount = inviteRewardAmount();
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        String bizNo = "INV_REWARD:" + inviteeId;
        if (findByBizNo(bizNo) != null) {
            return;
        }
        WalletAccount account = getOrCreate(inviterId);
        account.setBalance(account.getBalance().add(amount));
        updateAccount(account);
        writeLedger(bizNo, inviterId, INVITE_REWARD, amount, account, "USER", inviteeId, "邀新奖励");
    }

    private Map<String, Object> rechargeView(Long userId, String bizNo) {
        Map<String, Object> view = accountView(userId);
        view.put("ledgerBizNo", bizNo);
        view.put("bizNo", bizNo);
        return view;
    }

    @Transactional
    public void freeze(Long userId, BigDecimal amount, String bizNo, String refType, Long refId) {
        assertPositive(amount);
        if (findByBizNo(bizNo) != null) {
            return;
        }
        WalletAccount account = getOrCreate(userId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BizException(ErrorCode.WALLET_INSUFFICIENT);
        }
        account.setBalance(account.getBalance().subtract(amount));
        account.setFrozen(account.getFrozen().add(amount));
        updateAccount(account);
        writeLedger(bizNo, userId, FREEZE, amount, account, refType, refId, "发令托管冻结");
    }

    @Transactional
    public void unfreezeRefund(Long userId, BigDecimal amount, String bizNo, String refType, Long refId, String remark) {
        assertPositive(amount);
        if (findByBizNo(bizNo) != null) {
            return;
        }
        // 同一业务已解冻退款则幂等跳过（避免重复驳回/强制关闭报错）
        if (refType != null && refId != null && findRefundByRef(refType, refId) != null) {
            return;
        }
        WalletAccount account = getOrCreate(userId);
        if (account.getFrozen() == null || account.getFrozen().compareTo(BigDecimal.ZERO) <= 0) {
            // 无冻结余额：视为已退或不需退，不抛错
            return;
        }
        // 仅退当前可退冻结额，避免多单冻结时或状态不一致导致「冻结或解冻失败」
        BigDecimal refund = amount.min(account.getFrozen());
        account.setFrozen(account.getFrozen().subtract(refund));
        account.setBalance(account.getBalance().add(refund));
        updateAccount(account);
        writeLedger(bizNo, userId, UNFREEZE_REFUND, refund, account, refType, refId, remark);
    }

    private WalletLedger findRefundByRef(String refType, Long refId) {
        return ledgerMapper.selectOne(new LambdaQueryWrapper<WalletLedger>()
                .eq(WalletLedger::getRefType, refType)
                .eq(WalletLedger::getRefId, refId)
                .eq(WalletLedger::getType, UNFREEZE_REFUND)
                .last("LIMIT 1"));
    }

    @Transactional
    public void settlePay(Long userId, BigDecimal amount, String bizNo, Long bountyId) {
        assertPositive(amount);
        if (findByBizNo(bizNo) != null) {
            return;
        }
        WalletAccount account = getOrCreate(userId);
        if (account.getFrozen().compareTo(amount) < 0) {
            throw new BizException(ErrorCode.WALLET_FREEZE_FAIL);
        }
        account.setFrozen(account.getFrozen().subtract(amount));
        updateAccount(account);
        writeLedger(bizNo, userId, SETTLE_PAY, amount.negate(), account, "BOUNTY", bountyId, "结算扣托管");
    }

    @Transactional
    public void settleIncome(Long userId, BigDecimal amount, String bizNo, Long bountyId) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "入账金额非法");
        }
        if (findByBizNo(bizNo) != null) {
            return;
        }
        WalletAccount account = getOrCreate(userId);
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            account.setBalance(account.getBalance().add(amount));
            updateAccount(account);
        }
        writeLedger(bizNo, userId, SETTLE_INCOME, amount, account, "BOUNTY", bountyId, "揭榜结算入账");
    }

    @Transactional
    public void platformFee(Long platformUserId, BigDecimal amount, String bizNo, Long bountyId) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (findByBizNo(bizNo) != null) {
            return;
        }
        // 平台费记流水即可；平台账户可复用特殊用户或仅记账
        WalletAccount account = getOrCreate(platformUserId);
        account.setBalance(account.getBalance().add(amount));
        updateAccount(account);
        writeLedger(bizNo, platformUserId, PLATFORM_FEE, amount, account, "BOUNTY", bountyId, "平台服务费");
    }

    @Transactional
    public void adjustBalance(Long userId, BigDecimal delta, String bizNo, String remark) {
        if (findByBizNo(bizNo) != null) {
            return;
        }
        WalletAccount account = getOrCreate(userId);
        BigDecimal next = account.getBalance().add(delta);
        if (next.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException(ErrorCode.WALLET_INSUFFICIENT);
        }
        account.setBalance(next);
        updateAccount(account);
        writeLedger(bizNo, userId, ADJUST, delta, account, "ADMIN", null, remark);
    }

    public PageResult<WalletLedger> pageLedgers(Long userId, String type, long page, long pageSize) {
        Page<WalletLedger> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<WalletLedger> q = new LambdaQueryWrapper<WalletLedger>()
                .eq(WalletLedger::getUserId, userId)
                .eq(type != null && !type.isBlank(), WalletLedger::getType, type)
                .orderByDesc(WalletLedger::getId);
        Page<WalletLedger> result = ledgerMapper.selectPage(p, q);
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    public PageResult<WalletLedger> pageAllLedgers(String type, long page, long pageSize) {
        Page<WalletLedger> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<WalletLedger> q = new LambdaQueryWrapper<WalletLedger>()
                .eq(type != null && !type.isBlank(), WalletLedger::getType, type)
                .orderByDesc(WalletLedger::getId);
        Page<WalletLedger> result = ledgerMapper.selectPage(p, q);
        return PageResult.of(result.getRecords(), result.getTotal(), page, pageSize);
    }

    private void updateAccount(WalletAccount account) {
        account.setUpdatedAt(LocalDateTime.now());
        int rows = accountMapper.updateById(account);
        if (rows == 0) {
            throw new BizException(ErrorCode.WALLET_FREEZE_FAIL, "账户更新冲突，请重试");
        }
    }

    private void writeLedger(String bizNo, Long userId, String type, BigDecimal amount,
                             WalletAccount account, String refType, Long refId, String remark) {
        WalletLedger ledger = new WalletLedger();
        ledger.setBizNo(bizNo);
        ledger.setUserId(userId);
        ledger.setType(type);
        ledger.setAmount(amount);
        ledger.setBalanceAfter(account.getBalance());
        ledger.setFrozenAfter(account.getFrozen());
        ledger.setRefType(refType);
        ledger.setRefId(refId);
        ledger.setRemark(remark);
        ledger.setCreatedAt(LocalDateTime.now());
        try {
            ledgerMapper.insert(ledger);
        } catch (DuplicateKeyException e) {
            // idempotent
        }
    }

    private WalletLedger findByBizNo(String bizNo) {
        return ledgerMapper.selectOne(new LambdaQueryWrapper<WalletLedger>()
                .eq(WalletLedger::getBizNo, bizNo)
                .last("LIMIT 1"));
    }

    private void assertPositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(ErrorCode.PARAM_INVALID, "金额必须大于0");
        }
    }
}
