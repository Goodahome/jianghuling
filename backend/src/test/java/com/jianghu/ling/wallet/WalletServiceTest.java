package com.jianghu.ling.wallet;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.wallet.domain.WalletAccount;
import com.jianghu.ling.wallet.domain.WalletLedger;
import com.jianghu.ling.wallet.mapper.WalletAccountMapper;
import com.jianghu.ling.wallet.mapper.WalletLedgerMapper;
import com.jianghu.ling.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletAccountMapper accountMapper;
    @Mock
    private WalletLedgerMapper ledgerMapper;
    @InjectMocks
    private WalletService walletService;

    private WalletAccount account;

    @BeforeEach
    void setUp() {
        account = new WalletAccount();
        account.setId(1L);
        account.setUserId(10L);
        account.setBalance(new BigDecimal("1000.00"));
        account.setFrozen(new BigDecimal("0.00"));
        account.setVersion(0);
    }

    @Test
    void recharge_increasesBalance() {
        when(ledgerMapper.selectOne(any())).thenReturn(null);
        when(accountMapper.selectOne(any())).thenReturn(account);
        when(accountMapper.updateById(any(WalletAccount.class))).thenReturn(1);

        var result = walletService.recharge(10L, new BigDecimal("100"), "RC-1");

        assertEquals(new BigDecimal("1100.00"), result.get("balance"));
        verify(ledgerMapper).insert(any(WalletLedger.class));
    }

    @Test
    void recharge_idempotentByBizNo() {
        WalletLedger existing = new WalletLedger();
        existing.setBizNo("RC-1");
        when(ledgerMapper.selectOne(any())).thenReturn(existing);
        when(accountMapper.selectOne(any())).thenReturn(account);

        walletService.recharge(10L, new BigDecimal("100"), "RC-1");

        verify(accountMapper, never()).updateById(any(WalletAccount.class));
        verify(ledgerMapper, never()).insert(any(WalletLedger.class));
    }

    @Test
    void freeze_insufficientBalance() {
        when(ledgerMapper.selectOne(any())).thenReturn(null);
        when(accountMapper.selectOne(any())).thenReturn(account);

        BizException ex = assertThrows(BizException.class,
                () -> walletService.freeze(10L, new BigDecimal("2000"), "FZ-1", "BOUNTY", 1L));
        assertEquals(ErrorCode.WALLET_INSUFFICIENT.getCode(), ex.getCode());
    }

    @Test
    void freeze_movesBalanceToFrozen() {
        when(ledgerMapper.selectOne(any())).thenReturn(null);
        when(accountMapper.selectOne(any())).thenReturn(account);
        when(accountMapper.updateById(any(WalletAccount.class))).thenReturn(1);

        walletService.freeze(10L, new BigDecimal("200"), "FZ-1", "BOUNTY", 9L);

        ArgumentCaptor<WalletAccount> captor = ArgumentCaptor.forClass(WalletAccount.class);
        verify(accountMapper).updateById(captor.capture());
        assertEquals(new BigDecimal("800.00"), captor.getValue().getBalance());
        assertEquals(new BigDecimal("200.00"), captor.getValue().getFrozen());
    }
}
