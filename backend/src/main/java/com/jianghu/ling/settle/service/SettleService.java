package com.jianghu.ling.settle.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.bounty.domain.Bounty;
import com.jianghu.ling.bounty.domain.BountyClaim;
import com.jianghu.ling.bounty.domain.Submission;
import com.jianghu.ling.bounty.mapper.BountyClaimMapper;
import com.jianghu.ling.bounty.mapper.BountyMapper;
import com.jianghu.ling.bounty.mapper.SubmissionMapper;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.common.util.IdempotencyKeys;
import com.jianghu.ling.notify.service.NotifyService;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.settle.domain.Evaluation;
import com.jianghu.ling.settle.domain.Settlement;
import com.jianghu.ling.settle.domain.SettlementItem;
import com.jianghu.ling.settle.dto.SettleRequest;
import com.jianghu.ling.settle.mapper.EvaluationMapper;
import com.jianghu.ling.settle.mapper.SettlementItemMapper;
import com.jianghu.ling.settle.mapper.SettlementMapper;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import com.jianghu.ling.user.service.UserAssetService;
import com.jianghu.ling.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettleService {

    /** 平台费记账用虚拟用户，首次使用时自动建账户 */
    public static final long PLATFORM_USER_ID = 0L;

    private final BountyMapper bountyMapper;
    private final BountyClaimMapper claimMapper;
    private final SubmissionMapper submissionMapper;
    private final SettlementMapper settlementMapper;
    private final SettlementItemMapper settlementItemMapper;
    private final EvaluationMapper evaluationMapper;
    private final ConfigService configService;
    private final WalletService walletService;
    private final UserAssetService userAssetService;
    private final UserProfileMapper userProfileMapper;
    private final NotifyService notifyService;

    public Map<String, Object> preview(Long bountyId) {
        Long userId = AuthContext.requireUserId();
        Bounty bounty = requirePublisher(bountyId, userId);
        BigDecimal feeRate = configService.getDecimal("fee_rate", "0.10");
        BigDecimal rewardB = bounty.getRewardAmount();
        BigDecimal fee = rewardB.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal distributable = rewardB.subtract(fee).setScale(2, RoundingMode.HALF_UP);
        List<BountyClaim> claims = claimMapper.selectList(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getBountyId, bountyId));
        List<Map<String, Object>> claimants = claims.stream().map(c -> {
            Long approved = submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                    .eq(Submission::getClaimId, c.getId())
                    .eq(Submission::getStatus, "APPROVED"));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("userId", c.getUserId());
            m.put("nickname", nickname(c.getUserId()));
            m.put("approvedSubmissionCount", approved);
            return m;
        }).collect(Collectors.toList());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("rewardB", rewardB);
        data.put("feeRate", feeRate);
        data.put("fee", fee);
        data.put("distributable", distributable);
        data.put("claimants", claimants);
        return data;
    }

    @Transactional
    public Map<String, Object> settle(Long bountyId, SettleRequest req) {
        Long userId = AuthContext.requireUserId();
        Bounty bounty = requirePublisher(bountyId, userId);
        if (!"IN_COLLAB".equals(bounty.getStatus()) && !"OPEN".equals(bounty.getStatus())
                && !"PENDING_SETTLE".equals(bounty.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "当前状态不可结算");
        }
        long claimCount = claimMapper.selectCount(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getBountyId, bountyId));
        long approved = submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getBountyId, bountyId)
                .eq(Submission::getStatus, "APPROVED"));
        if (claimCount < 1 || approved < 1) {
            throw new BizException(ErrorCode.BIZ_RULE, "至少1名揭榜人且需有审核通过成果，否则请取消");
        }
        BigDecimal feeRate = configService.getDecimal("fee_rate", "0.10");
        BigDecimal rewardB = bounty.getRewardAmount();
        BigDecimal fee = rewardB.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal distributable = rewardB.subtract(fee).setScale(2, RoundingMode.HALF_UP);
        BigDecimal sum = req.getItems().stream()
                .map(i -> i.getAmount() == null ? BigDecimal.ZERO : i.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        if (sum.compareTo(distributable) != 0) {
            throw new BizException(ErrorCode.WALLET_SETTLE_INVALID);
        }
        Set<Long> claimantIds = claimMapper.selectList(new LambdaQueryWrapper<BountyClaim>()
                        .eq(BountyClaim::getBountyId, bountyId))
                .stream().map(BountyClaim::getUserId).collect(Collectors.toSet());
        for (SettleRequest.Item item : req.getItems()) {
            if (!claimantIds.contains(item.getUserId())) {
                throw new BizException(ErrorCode.WALLET_SETTLE_INVALID, "分配对象须为揭榜人");
            }
        }

        Settlement settlement = new Settlement();
        settlement.setBountyId(bountyId);
        settlement.setRewardB(rewardB);
        settlement.setFeeRate(feeRate);
        settlement.setFee(fee);
        settlement.setDistributable(distributable);
        settlement.setStatus("DONE");
        settlement.setCreatedAt(LocalDateTime.now());
        try {
            settlementMapper.insert(settlement);
        } catch (DuplicateKeyException e) {
            throw new BizException(ErrorCode.CONFLICT, "已结算");
        }

        walletService.settlePay(bounty.getPublisherId(), rewardB, IdempotencyKeys.bizNo("SP"), bountyId);
        walletService.platformFee(PLATFORM_USER_ID, fee, IdempotencyKeys.bizNo("PF"), bountyId);

        int baseChivalry = configService.getInt("chivalry_per_complete", 10);
        for (SettleRequest.Item item : req.getItems()) {
            SettlementItem row = new SettlementItem();
            row.setSettlementId(settlement.getId());
            row.setUserId(item.getUserId());
            row.setAmount(item.getAmount() == null ? BigDecimal.ZERO : item.getAmount());
            row.setChivalryBonus(item.getChivalryBonus() == null ? 0 : item.getChivalryBonus());
            settlementItemMapper.insert(row);
            walletService.settleIncome(item.getUserId(), row.getAmount(),
                    IdempotencyKeys.bizNo("SI"), bountyId);
            userAssetService.onOrderCompleted(item.getUserId());
            userAssetService.addChivalry(item.getUserId(), baseChivalry + row.getChivalryBonus());
            notifyService.send(item.getUserId(), "悬赏结算到账",
                    "悬赏#" + bountyId + "结算入账 " + row.getAmount() + " 两", "SETTLEMENT", settlement.getId());
        }
        userAssetService.onOrderCompleted(bounty.getPublisherId());
        userAssetService.addChivalry(bounty.getPublisherId(), baseChivalry);

        bounty.setStatus("COMPLETED");
        bounty.setUpdatedAt(LocalDateTime.now());
        bountyMapper.updateById(bounty);
        notifyService.send(bounty.getPublisherId(), "悬赏已完结",
                "悬赏「" + bounty.getTitle() + "」结算完成", "SETTLEMENT", settlement.getId());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("settlementId", settlement.getId());
        data.put("status", bounty.getStatus());
        data.put("fee", fee);
        data.put("distributable", distributable);
        return data;
    }

    @Transactional
    public Map<String, Object> cancel(Long bountyId, String reason) {
        Long userId = AuthContext.requireUserId();
        Bounty bounty = requirePublisher(bountyId, userId);
        if (!List.of("PENDING_REVIEW", "OPEN", "IN_COLLAB", "PENDING_SETTLE").contains(bounty.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "当前状态不可取消");
        }
        if ("IN_COLLAB".equals(bounty.getStatus())) {
            long approved = submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                    .eq(Submission::getBountyId, bountyId)
                    .eq(Submission::getStatus, "APPROVED"));
            // 允许取消，全额退回（与超时同类）
            if (approved > 0) {
                // still allow cancel per plan: 未结算且规则允许
            }
        }
        bounty.setStatus("CANCELLED");
        bounty.setCancelReason(StringUtils.hasText(reason) ? reason : "令主取消");
        bounty.setUpdatedAt(LocalDateTime.now());
        bountyMapper.updateById(bounty);
        walletService.unfreezeRefund(bounty.getPublisherId(), bounty.getRewardAmount(),
                IdempotencyKeys.bizNo("UR"), "BOUNTY", bountyId, "悬赏取消退款");
        return Map.of("bountyId", bountyId, "status", "CANCELLED");
    }

    @Transactional
    public Evaluation evaluate(Long bountyId, Long toUserId, Integer score, String content) {
        Long fromUserId = AuthContext.requireUserId();
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null || !"COMPLETED".equals(bounty.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "结算完成后才可互评");
        }
        boolean fromOk = fromUserId.equals(bounty.getPublisherId())
                || claimMapper.selectCount(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getBountyId, bountyId)
                .eq(BountyClaim::getUserId, fromUserId)) > 0;
        boolean toOk = toUserId.equals(bounty.getPublisherId())
                || claimMapper.selectCount(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getBountyId, bountyId)
                .eq(BountyClaim::getUserId, toUserId)) > 0;
        if (!fromOk || !toOk || fromUserId.equals(toUserId)) {
            throw new BizException(ErrorCode.BIZ_RULE, "互评对象非法");
        }
        Evaluation evaluation = new Evaluation();
        evaluation.setBountyId(bountyId);
        evaluation.setFromUserId(fromUserId);
        evaluation.setToUserId(toUserId);
        evaluation.setScore(score);
        evaluation.setContent(content);
        evaluation.setCreatedAt(LocalDateTime.now());
        try {
            evaluationMapper.insert(evaluation);
        } catch (DuplicateKeyException e) {
            throw new BizException(ErrorCode.CONFLICT, "已评价过该侠士");
        }
        refreshGoodRate(toUserId);
        return evaluation;
    }

    public List<Evaluation> listEvaluations(Long bountyId) {
        return evaluationMapper.selectList(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getBountyId, bountyId)
                .orderByDesc(Evaluation::getId));
    }

    private void refreshGoodRate(Long userId) {
        List<Evaluation> all = evaluationMapper.selectList(new LambdaQueryWrapper<Evaluation>()
                .eq(Evaluation::getToUserId, userId));
        if (all.isEmpty()) {
            return;
        }
        long good = all.stream().filter(e -> e.getScore() >= 4).count();
        BigDecimal rate = BigDecimal.valueOf(good * 100.0 / all.size()).setScale(2, RoundingMode.HALF_UP);
        userAssetService.refreshGoodRate(userId, rate);
    }

    private Bounty requirePublisher(Long bountyId, Long userId) {
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!userId.equals(bounty.getPublisherId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return bounty;
    }

    private String nickname(Long userId) {
        UserProfile p = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId).last("LIMIT 1"));
        return p == null ? "" : p.getNickname();
    }
}
