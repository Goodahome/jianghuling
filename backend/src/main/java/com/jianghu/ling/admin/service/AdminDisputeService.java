package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianghu.ling.admin.domain.Dispute;
import com.jianghu.ling.admin.mapper.DisputeMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.common.util.IdempotencyKeys;
import com.jianghu.ling.bounty.domain.Bounty;
import com.jianghu.ling.bounty.mapper.BountyMapper;
import com.jianghu.ling.settle.domain.Settlement;
import com.jianghu.ling.settle.mapper.SettlementMapper;
import com.jianghu.ling.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminDisputeService {

    private final DisputeMapper disputeMapper;
    private final BountyMapper bountyMapper;
    private final SettlementMapper settlementMapper;
    private final WalletService walletService;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public PageResult<Map<String, Object>> page(long page, long pageSize) {
        Page<Dispute> p = disputeMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Dispute>().orderByDesc(Dispute::getId));
        return PageResult.of(p.getRecords().stream().map(this::brief).toList(), p.getTotal(), page, pageSize);
    }

    public Map<String, Object> detail(Long id) {
        Dispute dispute = disputeMapper.selectById(id);
        if (dispute == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        Map<String, Object> data = brief(dispute);
        data.put("evidenceJson", dispute.getEvidenceJson());
        data.put("verdictJson", dispute.getVerdictJson());
        data.put("settlementId", dispute.getSettlementId());
        data.put("initiatorId", dispute.getInitiatorId());
        data.put("deadlineAt", dispute.getDeadlineAt());
        data.put("createdAt", dispute.getCreatedAt());
        return data;
    }

    @Transactional
    public void verdict(Long id, Map<String, Object> body) {
        Dispute dispute = disputeMapper.selectById(id);
        if (dispute == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!"OPEN".equals(dispute.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "纠纷已结案");
        }
        String action = body == null ? null : String.valueOf(body.get("action"));
        String comment = body == null || body.get("comment") == null ? "" : String.valueOf(body.get("comment"));
        if (!StringUtils.hasText(action)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "action必填");
        }
        action = action.trim().toUpperCase();
        if (!"KEEP".equals(action) && !"REALLOCATE".equals(action)
                && !"REFUND".equals(action) && !"PUNISH".equals(action)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "action无效");
        }

        // MVP：KEEP/PUNISH 仅结案；REFUND 尝试把悬赏标记并记审计（资金复杂再分配后续批次）
        if ("REFUND".equals(action)) {
            Bounty bounty = bountyMapper.selectById(dispute.getBountyId());
            Settlement settlement = settlementMapper.selectOne(new LambdaQueryWrapper<Settlement>()
                    .eq(Settlement::getBountyId, dispute.getBountyId()).last("LIMIT 1"));
            if (bounty != null && settlement != null && "IN_DISPUTE".equals(bounty.getStatus())) {
                // 简化：记平台调账审计，实际资金回滚留给完整纠纷实现
                walletService.adjustBalance(bounty.getPublisherId(), settlement.getRewardB(),
                        IdempotencyKeys.bizNo("DR"), "纠纷退款(简化)" + comment);
            }
        }

        Map<String, Object> verdict = new HashMap<>();
        verdict.put("action", action);
        verdict.put("comment", comment);
        verdict.put("at", LocalDateTime.now().toString());
        try {
            dispute.setVerdictJson(objectMapper.writeValueAsString(verdict));
        } catch (Exception e) {
            dispute.setVerdictJson("{\"action\":\"" + action + "\"}");
        }
        dispute.setStatus("CLOSED");
        dispute.setUpdatedAt(LocalDateTime.now());
        disputeMapper.updateById(dispute);

        Bounty bounty = bountyMapper.selectById(dispute.getBountyId());
        if (bounty != null && "IN_DISPUTE".equals(bounty.getStatus())) {
            bounty.setStatus("COMPLETED");
            bounty.setUpdatedAt(LocalDateTime.now());
            bountyMapper.updateById(bounty);
        }
        auditService.log("DISPUTE_VERDICT", "id=" + id + ", action=" + action + ", " + comment);
    }

    private Map<String, Object> brief(Dispute d) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", d.getId());
        m.put("bountyId", d.getBountyId());
        m.put("status", d.getStatus());
        m.put("reason", d.getReason());
        return m;
    }
}
