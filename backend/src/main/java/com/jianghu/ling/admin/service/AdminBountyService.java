package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.bounty.domain.Bounty;
import com.jianghu.ling.bounty.domain.BountyClaim;
import com.jianghu.ling.bounty.domain.BountyMessage;
import com.jianghu.ling.bounty.domain.Submission;
import com.jianghu.ling.bounty.mapper.BountyClaimMapper;
import com.jianghu.ling.bounty.mapper.BountyMapper;
import com.jianghu.ling.bounty.mapper.BountyMessageMapper;
import com.jianghu.ling.bounty.mapper.SubmissionMapper;
import com.jianghu.ling.bounty.service.BountyService;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.common.util.IdempotencyKeys;
import com.jianghu.ling.notify.service.NotifyService;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminBountyService {

    private final BountyMapper bountyMapper;
    private final BountyClaimMapper claimMapper;
    private final SubmissionMapper submissionMapper;
    private final BountyMessageMapper messageMapper;
    private final BountyService bountyService;
    private final WalletService walletService;
    private final NotifyService notifyService;

    public PageResult<Map<String, Object>> page(String status, String keyword, long page, long pageSize) {
        LambdaQueryWrapper<Bounty> q = new LambdaQueryWrapper<Bounty>()
                .eq(StringUtils.hasText(status), Bounty::getStatus, status)
                .like(StringUtils.hasText(keyword), Bounty::getTitle, keyword)
                .orderByDesc(Bounty::getId);
        Page<Bounty> p = bountyMapper.selectPage(new Page<>(page, pageSize), q);
        List<Map<String, Object>> list = p.getRecords().stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("title", b.getTitle());
            m.put("type", b.getType());
            m.put("status", b.getStatus());
            m.put("rewardAmount", b.getRewardAmount());
            m.put("publisherId", b.getPublisherId());
            m.put("deadlineAt", b.getDeadlineAt());
            m.put("createdAt", b.getCreatedAt());
            return m;
        }).toList();
        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    public Map<String, Object> detail(Long id) {
        Map<String, Object> data = bountyService.detail(id);
        data.put("claims", claimMapper.selectList(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getBountyId, id)));
        data.put("submissions", submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getBountyId, id)
                .orderByDesc(Submission::getId)));
        return data;
    }

    @Transactional
    public Map<String, Object> forceClose(Long id, String reason) {
        AuthContext.requireAdminId();
        Bounty bounty = bountyMapper.selectById(id);
        if (bounty == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if ("COMPLETED".equals(bounty.getStatus()) || "CANCELLED".equals(bounty.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "终态不可强制关闭");
        }
        String old = bounty.getStatus();
        bounty.setStatus("CANCELLED");
        bounty.setCancelReason(StringUtils.hasText(reason) ? reason : "管理员强制关闭");
        bounty.setUpdatedAt(LocalDateTime.now());
        bountyMapper.updateById(bounty);
        if (!"REJECTED".equals(old)) {
            walletService.unfreezeRefund(bounty.getPublisherId(), bounty.getRewardAmount(),
                    IdempotencyKeys.bizNo("UR"), "BOUNTY", id, "管理员强制关闭退款");
        }
        notifyService.send(bounty.getPublisherId(), "悬赏被强制关闭",
                "悬赏「" + bounty.getTitle() + "」已被管理员关闭", "BOUNTY", id);
        return Map.of("bountyId", id, "status", "CANCELLED");
    }

    public PageResult<BountyMessage> messages(Long id, long page, long pageSize) {
        Page<BountyMessage> p = messageMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<BountyMessage>()
                        .eq(BountyMessage::getBountyId, id)
                        .orderByAsc(BountyMessage::getId));
        return PageResult.of(p.getRecords(), p.getTotal(), page, pageSize);
    }
}
