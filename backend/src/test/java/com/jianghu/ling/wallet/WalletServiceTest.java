package com.jianghu.ling.wallet;

import com.jianghu.ling.cms.service.ConfigService;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletAccountMapper accountMapper;
    @Mock
    private WalletLedgerMapper ledgerMapper;
    @Mock
    private ConfigService configService;
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
        when(configService.getBoolean(eq(WalletService.CFG_RECHARGE_ENABLED), eq(false))).thenReturn(true);
        when(configService.getBoolean(eq(WalletService.CFG_WITHDRAW_ENABLED), eq(false))).thenReturn(false);
        when(ledgerMapper.selectOne(any())).thenReturn(null);
        when(accountMapper.selectOne(any())).thenReturn(account);
        when(accountMapper.updateById(any(WalletAccount.class))).thenReturn(1);

        var result = walletService.recharge(10L, new BigDecimal("100"), "RC-1");

        assertEquals(new BigDecimal("1100.00"), result.get("balance"));
        verify(ledgerMapper).insert(any(WalletLedger.class));
    }

    @Test
    void recharge_disabled_returns42004() {
        when(configService.getBoolean(eq(WalletService.CFG_RECHARGE_ENABLED), eq(false))).thenReturn(false);

        BizException ex = assertThrows(BizException.class,
                () -> walletService.recharge(10L, new BigDecimal("100"), "RC-1"));
        assertEquals(ErrorCode.WALLET_FEATURE_DISABLED.getCode(), ex.getCode());
        verify(ledgerMapper, never()).insert(any(WalletLedger.class));
    }

    @Test
    void recharge_idempotentByBizNo() {
        when(configService.getBoolean(eq(WalletService.CFG_RECHARGE_ENABLED), eq(false))).thenReturn(true);
        when(configService.getBoolean(eq(WalletService.CFG_WITHDRAW_ENABLED), eq(false))).thenReturn(false);
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
    void grantRegisterBonus_idempotent() {
        when(configService.getDecimal(eq(WalletService.CFG_REGISTER_GRANT), eq("500")))
                .thenReturn(new BigDecimal("500"));
        WalletLedger existing = new WalletLedger();
        existing.setBizNo("REG_GRANT:10");
        when(ledgerMapper.selectOne(any())).thenReturn(existing);

        walletService.grantRegisterBonus(10L);

        verify(accountMapper, never()).updateById(any(WalletAccount.class));
    }
}
