package com.jianghu.ling.bounty;

import com.jianghu.ling.bounty.domain.Bounty;
import com.jianghu.ling.bounty.mapper.BountyClaimMapper;
import com.jianghu.ling.bounty.mapper.BountyMapper;
import com.jianghu.ling.bounty.mapper.SubmissionMapper;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.notify.service.NotifyService;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.security.AuthPrincipal;
import com.jianghu.ling.security.PrincipalType;
import com.jianghu.ling.settle.mapper.EvaluationMapper;
import com.jianghu.ling.settle.mapper.SettlementItemMapper;
import com.jianghu.ling.settle.mapper.SettlementMapper;
import com.jianghu.ling.settle.service.SettleService;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import com.jianghu.ling.user.service.UserAssetService;
import com.jianghu.ling.wallet.service.WalletService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BountyCancelRulesTest {

    @Mock private BountyMapper bountyMapper;
    @Mock private BountyClaimMapper claimMapper;
    @Mock private SubmissionMapper submissionMapper;
    @Mock private SettlementMapper settlementMapper;
    @Mock private SettlementItemMapper settlementItemMapper;
    @Mock private EvaluationMapper evaluationMapper;
    @Mock private ConfigService configService;
    @Mock private WalletService walletService;
    @Mock private UserAssetService userAssetService;
    @Mock private UserProfileMapper userProfileMapper;
    @Mock private NotifyService notifyService;

    @InjectMocks
    private SettleService settleService;

    @AfterEach
    void cleanup() {
        AuthContext.clear();
    }

    @Test
    void cancel_withAnySubmission_allocateNotRefund() {
        AuthContext.set(new AuthPrincipal(1L, PrincipalType.USER, "jti"));
        Bounty bounty = baseBounty("IN_COLLAB");
        when(bountyMapper.selectById(88L)).thenReturn(bounty);
        when(submissionMapper.selectCount(any())).thenReturn(1L);

        Map<String, Object> data = settleService.cancel(88L, "已租到");

        assertEquals("ALLOCATE", data.get("cancelOutcome"));
        assertEquals(true, data.get("hasSubmissions"));
        assertEquals(true, data.get("cancelAllocationPending"));
        assertEquals(true, data.get("settlementRequired"));
        assertEquals("PENDING_SETTLE", data.get("status"));
        assertTrue(Boolean.TRUE.equals(bounty.getCancelAllocationPending()));
        assertEquals("PENDING_SETTLE", bounty.getStatus());
        verify(walletService, never()).unfreezeRefund(anyLong(), any(), anyString(), anyString(), anyLong(), anyString());
        verify(bountyMapper).updateById(bounty);
    }

    @Test
    void cancel_withoutSubmission_refundAndCancelled() {
        AuthContext.set(new AuthPrincipal(1L, PrincipalType.USER, "jti"));
        Bounty bounty = baseBounty("IN_COLLAB");
        when(bountyMapper.selectById(88L)).thenReturn(bounty);
        when(submissionMapper.selectCount(any())).thenReturn(0L);

        Map<String, Object> data = settleService.cancel(88L, "改主意了");

        assertEquals("REFUND", data.get("cancelOutcome"));
        assertEquals(false, data.get("hasSubmissions"));
        assertEquals(false, data.get("cancelAllocationPending"));
        assertEquals(false, data.get("settlementRequired"));
        assertEquals("CANCELLED", data.get("status"));
        verify(walletService).unfreezeRefund(eq(1L), eq(new BigDecimal("350.00")), anyString(), eq("BOUNTY"), eq(88L), anyString());
    }

    @Test
    void cancel_pendingAllocate_cannotRepeat() {
        AuthContext.set(new AuthPrincipal(1L, PrincipalType.USER, "jti"));
        Bounty bounty = baseBounty("PENDING_SETTLE");
        bounty.setCancelAllocationPending(true);
        when(bountyMapper.selectById(88L)).thenReturn(bounty);

        BizException ex = assertThrows(BizException.class, () -> settleService.cancel(88L, "再取消"));
        assertEquals(ErrorCode.BIZ_RULE.getCode(), ex.getCode());
        verify(walletService, never()).unfreezeRefund(anyLong(), any(), anyString(), anyString(), anyLong(), anyString());
    }

    @Test
    void cancel_collabWithoutReason_rejected() {
        AuthContext.set(new AuthPrincipal(1L, PrincipalType.USER, "jti"));
        Bounty bounty = baseBounty("IN_COLLAB");
        when(bountyMapper.selectById(88L)).thenReturn(bounty);

        BizException ex = assertThrows(BizException.class, () -> settleService.cancel(88L, "  "));
        assertEquals(ErrorCode.PARAM_INVALID.getCode(), ex.getCode());
    }

    @Test
    void hasSubmissions_countsPendingRejectedApproved() {
        // 有成果判定：任意一条 submission 均算（不依赖 APPROVED）
        AuthContext.set(new AuthPrincipal(1L, PrincipalType.USER, "jti"));
        Bounty bounty = baseBounty("IN_COLLAB");
        when(bountyMapper.selectById(88L)).thenReturn(bounty);
        when(submissionMapper.selectCount(any())).thenReturn(1L); // 可为 PENDING/REJECTED

        Map<String, Object> data = settleService.cancel(88L, "有待审也算有成果");
        assertEquals("ALLOCATE", data.get("cancelOutcome"));
        assertEquals(true, data.get("hasSubmissions"));
    }

    private static Bounty baseBounty(String status) {
        Bounty bounty = new Bounty();
        bounty.setId(88L);
        bounty.setPublisherId(1L);
        bounty.setStatus(status);
        bounty.setRewardAmount(new BigDecimal("350.00"));
        bounty.setTitle("测试令");
        bounty.setCancelAllocationPending(false);
        return bounty;
    }
}
