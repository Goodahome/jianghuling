package com.jianghu.ling.dispute.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianghu.ling.admin.domain.Dispute;
import com.jianghu.ling.admin.mapper.DisputeMapper;
import com.jianghu.ling.bounty.domain.Bounty;
import com.jianghu.ling.bounty.domain.BountyClaim;
import com.jianghu.ling.bounty.mapper.BountyClaimMapper;
import com.jianghu.ling.bounty.mapper.BountyMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.notify.service.NotifyService;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.settle.domain.Settlement;
import com.jianghu.ling.settle.mapper.SettlementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DisputeService {

    private final DisputeMapper disputeMapper;
    private final BountyMapper bountyMapper;
    private final BountyClaimMapper claimMapper;
    private final SettlementMapper settlementMapper;
    private final NotifyService notifyService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> create(Long bountyId, String reason, List<String> evidenceUrls, String evidenceText) {
        Long userId = AuthContext.requireUserId();
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!"COMPLETED".equals(bounty.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "仅已完结悬赏可发起纠纷");
        }
        boolean participant = userId.equals(bounty.getPublisherId())
                || claimMapper.selectCount(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getBountyId, bountyId)
                .eq(BountyClaim::getUserId, userId)) > 0;
        if (!participant) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        Settlement settlement = settlementMapper.selectOne(new LambdaQueryWrapper<Settlement>()
                .eq(Settlement::getBountyId, bountyId).last("LIMIT 1"));
        if (settlement == null) {
            throw new BizException(ErrorCode.BIZ_RULE, "无结算单");
        }
        if (settlement.getCreatedAt().plusDays(7).isBefore(LocalDateTime.now())) {
            throw new BizException(ErrorCode.BIZ_RULE, "已超过7日纠纷期");
        }
        if (disputeMapper.selectCount(new LambdaQueryWrapper<Dispute>()
                .eq(Dispute::getBountyId, bountyId)
                .eq(Dispute::getStatus, "OPEN")) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "已有进行中的纠纷");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "原因必填");
        }

        Map<String, Object> evidence = new HashMap<>();
        evidence.put("urls", evidenceUrls == null ? List.of() : evidenceUrls);
        evidence.put("text", evidenceText);

        Dispute dispute = new Dispute();
        dispute.setSettlementId(settlement.getId());
        dispute.setBountyId(bountyId);
        dispute.setInitiatorId(userId);
        dispute.setStatus("OPEN");
        dispute.setReason(reason);
        try {
            dispute.setEvidenceJson(objectMapper.writeValueAsString(evidence));
        } catch (Exception e) {
            dispute.setEvidenceJson("{}");
        }
        dispute.setDeadlineAt(LocalDateTime.now().plusDays(7));
        dispute.setCreatedAt(LocalDateTime.now());
        dispute.setUpdatedAt(LocalDateTime.now());
        disputeMapper.insert(dispute);

        bounty.setStatus("IN_DISPUTE");
        bounty.setUpdatedAt(LocalDateTime.now());
        bountyMapper.updateById(bounty);

        notifyService.send(bounty.getPublisherId(), "纠纷已发起",
                "悬赏#" + bountyId + "进入纠纷：" + reason, "DISPUTE", dispute.getId());
        return detail(dispute.getId());
    }

    public Map<String, Object> detail(Long id) {
        Dispute dispute = disputeMapper.selectById(id);
        if (dispute == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        Long userId = AuthContext.requireUserId();
        Bounty bounty = bountyMapper.selectById(dispute.getBountyId());
        boolean ok = userId.equals(dispute.getInitiatorId())
                || (bounty != null && userId.equals(bounty.getPublisherId()))
                || claimMapper.selectCount(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getBountyId, dispute.getBountyId())
                .eq(BountyClaim::getUserId, userId)) > 0;
        if (!ok) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return brief(dispute);
    }

    public PageResult<Map<String, Object>> mine(long page, long pageSize) {
        Long userId = AuthContext.requireUserId();
        Page<Dispute> p = disputeMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Dispute>()
                        .eq(Dispute::getInitiatorId, userId)
                        .orderByDesc(Dispute::getId));
        return PageResult.of(p.getRecords().stream().map(this::brief).toList(), p.getTotal(), page, pageSize);
    }

    private Map<String, Object> brief(Dispute d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("bountyId", d.getBountyId());
        m.put("status", d.getStatus());
        m.put("reason", d.getReason());
        m.put("deadlineAt", d.getDeadlineAt());
        m.put("createdAt", d.getCreatedAt());
        return m;
    }
}
