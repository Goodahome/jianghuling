package com.jianghu.ling.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.bounty.domain.Bounty;
import com.jianghu.ling.bounty.domain.BountyClaim;
import com.jianghu.ling.bounty.domain.Submission;
import com.jianghu.ling.bounty.mapper.BountyClaimMapper;
import com.jianghu.ling.bounty.mapper.BountyMapper;
import com.jianghu.ling.bounty.mapper.SubmissionMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.common.util.IdempotencyKeys;
import com.jianghu.ling.notify.service.NotifyService;
import com.jianghu.ling.review.domain.ReviewRecord;
import com.jianghu.ling.review.mapper.ReviewRecordMapper;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.user.domain.UserOffice;
import com.jianghu.ling.user.mapper.UserOfficeMapper;
import com.jianghu.ling.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    public static final String OFFICE_DECREE = "DECREE_REVIEWER";
    public static final String OFFICE_FEAT = "FEAT_REVIEWER";

    private final BountyMapper bountyMapper;
    private final BountyClaimMapper claimMapper;
    private final SubmissionMapper submissionMapper;
    private final ReviewRecordMapper reviewRecordMapper;
    private final UserOfficeMapper userOfficeMapper;
    private final WalletService walletService;
    private final NotifyService notifyService;

    public void requireOffice(Long userId, String officeCode) {
        UserOffice office = userOfficeMapper.selectOne(new LambdaQueryWrapper<UserOffice>()
                .eq(UserOffice::getUserId, userId)
                .eq(UserOffice::getOfficeCode, officeCode)
                .eq(UserOffice::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (office == null || (office.getEndAt() != null && office.getEndAt().isBefore(LocalDateTime.now()))) {
            throw new BizException(ErrorCode.OFFICE_FORBIDDEN);
        }
    }

    public PageResult<Map<String, Object>> pendingBounties(String status, long page, long pageSize) {
        Long userId = AuthContext.requireUserId();
        requireOffice(userId, OFFICE_DECREE);
        String st = StringUtils.hasText(status) && !"PENDING".equals(status) ? status : "PENDING_REVIEW";
        if ("PENDING".equals(status)) {
            st = "PENDING_REVIEW";
        }
        Page<Bounty> p = bountyMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Bounty>()
                        .eq(Bounty::getStatus, st)
                        .ne(Bounty::getPublisherId, userId)
                        .orderByAsc(Bounty::getId));
        List<Bounty> records = p.getRecords();
        // also exclude claimed by reviewer
        List<Bounty> filtered = records.stream()
                .filter(b -> claimMapper.selectCount(new LambdaQueryWrapper<BountyClaim>()
                        .eq(BountyClaim::getBountyId, b.getId())
                        .eq(BountyClaim::getUserId, userId)) == 0)
                .toList();

        Map<Long, Long> claimCountMap;
        if (!filtered.isEmpty()) {
            List<Long> bountyIds = filtered.stream().map(Bounty::getId).toList();
            claimCountMap = claimMapper.selectList(
                    new LambdaQueryWrapper<BountyClaim>()
                            .in(BountyClaim::getBountyId, bountyIds)
                            .eq(BountyClaim::getStatus, "ACTIVE")
            ).stream().collect(Collectors.groupingBy(BountyClaim::getBountyId, Collectors.counting()));
        } else {
            claimCountMap = Map.of();
        }

        List<Map<String, Object>> list = filtered.stream()
                .map(b -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", b.getId());
                    m.put("title", b.getTitle());
                    m.put("type", b.getType());
                    m.put("rewardAmount", b.getRewardAmount());
                    m.put("difficulty", b.getDifficulty());
                    m.put("publisherId", b.getPublisherId());
                    m.put("createdAt", b.getCreatedAt());
                    m.put("status", b.getStatus());
                    m.put("claimCount", claimCountMap.getOrDefault(b.getId(), 0L));
                    return m;
                }).collect(Collectors.toList());
        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    @Transactional
    public Map<String, Object> reviewBounty(Long bountyId, String result, String reason, boolean admin, Long overrideBy) {
        Long reviewerId = admin ? AuthContext.requireAdminId() : AuthContext.requireUserId();
        if (!admin) {
            requireOffice(reviewerId, OFFICE_DECREE);
        }
        Bounty bounty = bountyMapper.selectById(bountyId);
        if (bounty == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!admin) {
            assertAvoidance(reviewerId, bounty);
        }
        // 发令审核仅对待审核状态生效（管理员也不例外，避免「通过」重复点）
        if (!"PENDING_REVIEW".equals(bounty.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "仅待审核状态可审核通过/驳回，其它状态请用强制关闭");
        }
        if ("APPROVE".equalsIgnoreCase(result)) {
            bounty.setStatus("OPEN");
            bounty.setUpdatedAt(LocalDateTime.now());
            bountyMapper.updateById(bounty);
            saveRecord("BOUNTY", bountyId, "APPROVE", reason, reviewerId, admin ? "ADMIN" : "HALL", overrideBy);
            notifyService.send(bounty.getPublisherId(), "发令审核通过",
                    "悬赏「" + bounty.getTitle() + "」已张贴", "BOUNTY", bountyId);
        } else if ("REJECT".equalsIgnoreCase(result)) {
            if (!StringUtils.hasText(reason)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "驳回须填写原因");
            }
            bounty.setStatus("REJECTED");
            bounty.setUpdatedAt(LocalDateTime.now());
            bountyMapper.updateById(bounty);
            walletService.unfreezeRefund(bounty.getPublisherId(), bounty.getRewardAmount(),
                    IdempotencyKeys.bizNo("UR"), "BOUNTY", bountyId, "发令审核驳回退款");
            saveRecord("BOUNTY", bountyId, "REJECT", reason, reviewerId, admin ? "ADMIN" : "HALL", overrideBy);
            notifyService.send(bounty.getPublisherId(), "发令审核驳回",
                    "悬赏「" + bounty.getTitle() + "」被驳回：" + reason, "BOUNTY", bountyId);
        } else {
            throw new BizException(ErrorCode.PARAM_INVALID, "result无效");
        }
        return Map.of("bountyId", bountyId, "status", bounty.getStatus());
    }

    public PageResult<Map<String, Object>> pendingSubmissions(String status, long page, long pageSize) {
        Long userId = AuthContext.requireUserId();
        requireOffice(userId, OFFICE_FEAT);
        String st = "PENDING".equalsIgnoreCase(status) || !StringUtils.hasText(status) ? "PENDING" : status;
        Page<Submission> p = submissionMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Submission>()
                        .eq(Submission::getStatus, st)
                        .ne(Submission::getUserId, userId)
                        .orderByAsc(Submission::getId));
        List<Map<String, Object>> list = p.getRecords().stream()
                .filter(s -> {
                    Bounty bounty = bountyMapper.selectById(s.getBountyId());
                    return bounty != null && !userId.equals(bounty.getPublisherId())
                            && claimMapper.selectCount(new LambdaQueryWrapper<BountyClaim>()
                            .eq(BountyClaim::getBountyId, s.getBountyId())
                            .eq(BountyClaim::getUserId, userId)) == 0;
                })
                .map(s -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", s.getId());
                    m.put("bountyId", s.getBountyId());
                    m.put("claimId", s.getClaimId());
                    m.put("userId", s.getUserId());
                    m.put("versionNo", s.getVersionNo());
                    m.put("summary", s.getContentSummary());
                    m.put("status", s.getStatus());
                    m.put("createdAt", s.getCreatedAt());
                    return m;
                }).collect(Collectors.toList());
        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    @Transactional
    public Map<String, Object> reviewSubmission(Long submissionId, String result, String reason, boolean admin, Long overrideBy) {
        Long reviewerId = admin ? AuthContext.requireAdminId() : AuthContext.requireUserId();
        if (!admin) {
            requireOffice(reviewerId, OFFICE_FEAT);
        }
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        Bounty bounty = bountyMapper.selectById(submission.getBountyId());
        if (bounty == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!admin) {
            assertAvoidance(reviewerId, bounty);
            if (reviewerId.equals(submission.getUserId())) {
                throw new BizException(ErrorCode.OFFICE_FORBIDDEN, "不可审核自己的成果");
            }
        }
        if (!"PENDING".equals(submission.getStatus()) && !admin) {
            throw new BizException(ErrorCode.BIZ_RULE, "当前状态不可审核");
        }
        if ("APPROVE".equalsIgnoreCase(result)) {
            submission.setStatus("APPROVED");
            submission.setUpdatedAt(LocalDateTime.now());
            submissionMapper.updateById(submission);
            saveRecord("SUBMISSION", submissionId, "APPROVE", reason, reviewerId, admin ? "ADMIN" : "HALL", overrideBy);
            notifyService.send(submission.getUserId(), "成果审核通过",
                    "你在悬赏#" + bounty.getId() + "的成果已通过", "SUBMISSION", submissionId);
        } else if ("REJECT".equalsIgnoreCase(result)) {
            if (!StringUtils.hasText(reason)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "驳回须填写原因");
            }
            submission.setStatus("REJECTED");
            submission.setRejectReason(reason);
            submission.setUpdatedAt(LocalDateTime.now());
            submissionMapper.updateById(submission);
            saveRecord("SUBMISSION", submissionId, "REJECT", reason, reviewerId, admin ? "ADMIN" : "HALL", overrideBy);
            notifyService.send(submission.getUserId(), "成果审核驳回",
                    "成果被驳回：" + reason, "SUBMISSION", submissionId);
        } else {
            throw new BizException(ErrorCode.PARAM_INVALID, "result无效");
        }
        return Map.of("submissionId", submissionId, "status", submission.getStatus());
    }

    public PageResult<ReviewRecord> myActions(long page, long pageSize) {
        Long userId = AuthContext.requireUserId();
        Page<ReviewRecord> p = reviewRecordMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<ReviewRecord>()
                        .eq(ReviewRecord::getReviewerId, userId)
                        .eq(ReviewRecord::getReviewerRole, "HALL")
                        .orderByDesc(ReviewRecord::getId));
        return PageResult.of(p.getRecords(), p.getTotal(), page, pageSize);
    }

    private void assertAvoidance(Long reviewerId, Bounty bounty) {
        if (reviewerId.equals(bounty.getPublisherId())) {
            throw new BizException(ErrorCode.OFFICE_FORBIDDEN, "不可审核本人发布的令");
        }
        if (claimMapper.selectCount(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getBountyId, bounty.getId())
                .eq(BountyClaim::getUserId, reviewerId)) > 0) {
            throw new BizException(ErrorCode.OFFICE_FORBIDDEN, "不可审核本人揭榜的令");
        }
    }

    private void saveRecord(String type, Long targetId, String result, String reason,
                            Long reviewerId, String role, Long overrideBy) {
        ReviewRecord record = new ReviewRecord();
        record.setTargetType(type);
        record.setTargetId(targetId);
        record.setResult(result);
        record.setReason(reason);
        record.setReviewerId(reviewerId);
        record.setReviewerRole(role);
        record.setOverrideBy(overrideBy);
        record.setCreatedAt(LocalDateTime.now());
        reviewRecordMapper.insert(record);
    }
}
