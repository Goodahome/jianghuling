package com.jianghu.ling.bounty;

import com.jianghu.ling.bounty.domain.Bounty;
import com.jianghu.ling.bounty.domain.BountyClaim;
import com.jianghu.ling.bounty.mapper.*;
import com.jianghu.ling.bounty.service.BountyService;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.cms.service.MetaService;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.security.AuthPrincipal;
import com.jianghu.ling.security.PrincipalType;
import com.jianghu.ling.notify.service.NotifyService;
import com.jianghu.ling.settle.mapper.SettlementMapper;
import com.jianghu.ling.user.mapper.UserMapper;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import com.jianghu.ling.user.service.UserAssetService;
import com.jianghu.ling.wallet.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BountyClaimRulesTest {

    @Mock private BountyMapper bountyMapper;
    @Mock private BountyWarrantMapper warrantMapper;
    @Mock private BountyChecklistMapper checklistMapper;
    @Mock private BountyClaimMapper claimMapper;
    @Mock private BountyMessageMapper messageMapper;
    @Mock private SubmissionMapper submissionMapper;
    @Mock private SubmissionItemMapper submissionItemMapper;
    @Mock private SettlementMapper settlementMapper;
    @Mock private MetaService metaService;
    @Mock private ConfigService configService;
    @Mock private WalletService walletService;
    @Mock private UserAssetService userAssetService;
    @Mock private NotifyService notifyService;
    @Mock private UserProfileMapper userProfileMapper;
    @Mock private UserMapper userMapper;
    @Mock private ObjectMapper objectMapper;
    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private BountyService bountyService;

    @AfterEach
    void cleanup() {
        AuthContext.clear();
    }

    @Test
    void claim_selfNotAllowed() {
        AuthContext.set(new AuthPrincipal(1L, PrincipalType.USER, "jti"));
        Bounty bounty = new Bounty();
        bounty.setId(9L);
        bounty.setPublisherId(1L);
        bounty.setStatus("OPEN");
        when(bountyMapper.selectById(9L)).thenReturn(bounty);

        BizException ex = assertThrows(BizException.class, () -> bountyService.claim(9L));
        assertEquals(ErrorCode.CLAIM_NOT_ALLOWED.getCode(), ex.getCode());
    }

    @Test
    void claim_duplicateRejected() {
        AuthContext.set(new AuthPrincipal(2L, PrincipalType.USER, "jti"));
        Bounty bounty = new Bounty();
        bounty.setId(9L);
        bounty.setPublisherId(1L);
        bounty.setStatus("OPEN");
        when(bountyMapper.selectById(9L)).thenReturn(bounty);
        BountyClaim existing = new BountyClaim();
        existing.setId(1L);
        when(claimMapper.selectOne(any())).thenReturn(existing);

        BizException ex = assertThrows(BizException.class, () -> bountyService.claim(9L));
        assertEquals(ErrorCode.CONFLICT.getCode(), ex.getCode());
    }
}
