package com.jianghu.ling.wallet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

    private final WalletAccountMapper accountMapper;
    private final WalletLedgerMapper ledgerMapper;

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

    public Map<String, Object> accountView(Long userId) {
        WalletAccount account = getOrCreate(userId);
        Map<String, Object> map = new HashMap<>();
        map.put("balance", account.getBalance());
        map.put("frozen", account.getFrozen());
        map.put("currency", "两");
        map.put("simulated", true);
        return map;
    }

    @Transactional
    public Map<String, Object> recharge(Long userId, BigDecimal amount, String bizNo) {
        assertPositive(amount);
        WalletLedger existing = findByBizNo(bizNo);
        if (existing != null) {
            Map<String, Object> view = accountView(userId);
            view.put("bizNo", existing.getBizNo());
            return view;
        }
        WalletAccount account = getOrCreate(userId);
        account.setBalance(account.getBalance().add(amount));
        updateAccount(account);
        writeLedger(bizNo, userId, RECHARGE, amount, account, "WALLET", null, "模拟充值");
        Map<String, Object> view = accountView(userId);
        view.put("bizNo", bizNo);
        return view;
    }

    @Transactional
    public Map<String, Object> withdraw(Long userId, BigDecimal amount, String bizNo) {
        assertPositive(amount);
        WalletLedger existing = findByBizNo(bizNo);
        if (existing != null) {
            Map<String, Object> view = accountView(userId);
            view.put("bizNo", existing.getBizNo());
            return view;
        }
        WalletAccount account = getOrCreate(userId);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BizException(ErrorCode.WALLET_INSUFFICIENT);
        }
        account.setBalance(account.getBalance().subtract(amount));
        updateAccount(account);
        writeLedger(bizNo, userId, WITHDRAW, amount.negate(), account, "WALLET", null, "模拟提现");
        Map<String, Object> view = accountView(userId);
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
        WalletAccount account = getOrCreate(userId);
        if (account.getFrozen().compareTo(amount) < 0) {
            throw new BizException(ErrorCode.WALLET_FREEZE_FAIL);
        }
        account.setFrozen(account.getFrozen().subtract(amount));
        account.setBalance(account.getBalance().add(amount));
        updateAccount(account);
        writeLedger(bizNo, userId, UNFREEZE_REFUND, amount, account, refType, refId, remark);
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
