package com.jianghu.ling.review.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.admin.service.AuditService;
import com.jianghu.ling.bounty.domain.Bounty;
import com.jianghu.ling.bounty.domain.BountyClaim;
import com.jianghu.ling.bounty.domain.Submission;
import com.jianghu.ling.bounty.mapper.BountyClaimMapper;
import com.jianghu.ling.bounty.mapper.BountyMapper;
import com.jianghu.ling.bounty.mapper.SubmissionMapper;
import com.jianghu.ling.bounty.service.BountyService;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.common.util.IdempotencyKeys;
import com.jianghu.ling.notify.service.NotifyService;
import com.jianghu.ling.review.domain.ReviewRecord;
import com.jianghu.ling.review.mapper.ReviewRecordMapper;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.user.domain.UserOffice;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.mapper.UserOfficeMapper;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import com.jianghu.ling.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
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
    private final UserProfileMapper userProfileMapper;
    private final WalletService walletService;
    private final NotifyService notifyService;
    private final BountyService bountyService;
    private final AuditService auditService;

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
        // 先取全量候选再套回避，保证 total 与 list 一致（避免「先分页再过滤」导致统计偏大）
        List<Bounty> candidates = bountyMapper.selectList(new LambdaQueryWrapper<Bounty>()
                .eq(Bounty::getStatus, st)
                .ne(Bounty::getPublisherId, userId)
                .orderByAsc(Bounty::getId));
        Set<Long> claimedIds = claimedBountyIds(userId, candidates.stream().map(Bounty::getId).toList());
        List<Bounty> visible = candidates.stream()
                .filter(b -> !claimedIds.contains(b.getId()))
                .toList();

        Map<Long, Long> claimCountMap;
        if (!visible.isEmpty()) {
            List<Long> bountyIds = visible.stream().map(Bounty::getId).toList();
            claimCountMap = claimMapper.selectList(
                    new LambdaQueryWrapper<BountyClaim>()
                            .in(BountyClaim::getBountyId, bountyIds)
                            .eq(BountyClaim::getStatus, "ACTIVE")
            ).stream().collect(Collectors.groupingBy(BountyClaim::getBountyId, Collectors.counting()));
        } else {
            claimCountMap = Map.of();
        }

        List<Bounty> pageRows = pageSlice(visible, page, pageSize);
        List<Map<String, Object>> list = pageRows.stream()
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
        return PageResult.of(list, visible.size(), page, pageSize);
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

    /** 执事堂成果审核列表（api.md §15.3） */
    public PageResult<Map<String, Object>> pendingSubmissions(String status, long page, long pageSize) {
        Long userId = AuthContext.requireUserId();
        requireOffice(userId, OFFICE_FEAT);
        LambdaQueryWrapper<Submission> q = new LambdaQueryWrapper<Submission>()
                .ne(Submission::getUserId, userId)
                .orderByAsc(Submission::getId);
        applySubmissionStatusFilter(q, status);
        // 先取全量候选再套回避，保证 total 与 list 一致（首页/红点用 total，队列用 list）
        List<Submission> candidates = submissionMapper.selectList(q);
        Set<Long> bountyIds = candidates.stream().map(Submission::getBountyId).collect(Collectors.toSet());
        Map<Long, Bounty> bountyMap = bountyIds.isEmpty() ? Map.of()
                : bountyMapper.selectBatchIds(bountyIds).stream()
                .collect(Collectors.toMap(Bounty::getId, b -> b, (a, b) -> a));
        Set<Long> claimedIds = claimedBountyIds(userId, bountyIds);
        List<Submission> visible = candidates.stream()
                .filter(s -> {
                    Bounty bounty = bountyMap.get(s.getBountyId());
                    return bounty != null
                            && !userId.equals(bounty.getPublisherId())
                            && !claimedIds.contains(s.getBountyId());
                })
                .toList();
        List<Map<String, Object>> list = pageSlice(visible, page, pageSize).stream()
                .map(this::toHallListItem)
                .collect(Collectors.toList());
        return PageResult.of(list, visible.size(), page, pageSize);
    }

    /** 执事堂成果详情（api.md §15.3.1 = §8.0） */
    public Map<String, Object> hallSubmissionDetail(Long submissionId) {
        Long userId = AuthContext.requireUserId();
        requireOffice(userId, OFFICE_FEAT);
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        Bounty bounty = bountyMapper.selectById(submission.getBountyId());
        if (bounty == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        assertAvoidance(userId, bounty);
        if (userId.equals(submission.getUserId())) {
            throw new BizException(ErrorCode.OFFICE_FORBIDDEN, "不可查看自己的成果审核详情");
        }
        return bountyService.buildSubmissionDetailVo(submission);
    }

    /** Admin 成果审核列表（api.md §16.12.1） */
    public PageResult<Map<String, Object>> adminSubmissionReviews(String status, Long bountyId,
                                                                  String keyword, long page, long pageSize) {
        LambdaQueryWrapper<Submission> q = new LambdaQueryWrapper<Submission>()
                .eq(bountyId != null, Submission::getBountyId, bountyId)
                .orderByDesc(Submission::getId);
        applySubmissionStatusFilter(q, status);

        if (StringUtils.hasText(keyword)) {
            String kw = keyword.trim();
            Set<Long> bountyIdsByTitle = bountyMapper.selectList(new LambdaQueryWrapper<Bounty>()
                            .like(Bounty::getTitle, kw))
                    .stream().map(Bounty::getId).collect(Collectors.toSet());
            Set<Long> userIdsByNick = userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                            .like(UserProfile::getNickname, kw))
                    .stream().map(UserProfile::getUserId).collect(Collectors.toSet());
            Long userIdExact = null;
            try {
                userIdExact = Long.parseLong(kw);
            } catch (NumberFormatException ignored) {
                // not a numeric id
            }
            final Long uid = userIdExact;
            q.and(w -> {
                boolean any = false;
                if (!bountyIdsByTitle.isEmpty()) {
                    w.in(Submission::getBountyId, bountyIdsByTitle);
                    any = true;
                }
                if (!userIdsByNick.isEmpty()) {
                    if (any) {
                        w.or();
                    }
                    w.in(Submission::getUserId, userIdsByNick);
                    any = true;
                }
                if (uid != null) {
                    if (any) {
                        w.or();
                    }
                    w.eq(Submission::getUserId, uid);
                    any = true;
                }
                if (!any) {
                    w.eq(Submission::getId, -1L);
                }
            });
        }

        Page<Submission> p = submissionMapper.selectPage(new Page<>(page, pageSize), q);
        List<Map<String, Object>> list = p.getRecords().stream()
                .map(this::toAdminListItem)
                .collect(Collectors.toList());
        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    /** Admin 成果详情（api.md §16.12.2 = §8.0） */
    public Map<String, Object> adminSubmissionDetail(Long submissionId) {
        return bountyService.buildSubmissionDetailVo(submissionId);
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
        boolean wasReviewed = !"PENDING".equals(submission.getStatus());
        if (wasReviewed && !admin) {
            throw new BizException(ErrorCode.BIZ_RULE, "当前状态不可审核");
        }
        if ("APPROVE".equalsIgnoreCase(result)) {
            submission.setStatus("APPROVED");
            submission.setRejectReason(null);
            submission.setUpdatedAt(LocalDateTime.now());
            submissionMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<Submission>()
                    .eq(Submission::getId, submissionId)
                    .set(Submission::getStatus, "APPROVED")
                    .set(Submission::getRejectReason, null)
                    .set(Submission::getUpdatedAt, submission.getUpdatedAt()));
            saveRecord("SUBMISSION", submissionId, "APPROVE", reason, reviewerId, admin ? "ADMIN" : "HALL", overrideBy);
            // biz 指向悬赏，文案含标题「…」；前端可点标题进详情（from=mine）
            notifyService.send(submission.getUserId(), "成果审核通过",
                    "你在悬赏「" + bounty.getTitle() + "」的成果已通过", "BOUNTY", bounty.getId());
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
                    "你在悬赏「" + bounty.getTitle() + "」的成果被驳回：" + reason, "BOUNTY", bounty.getId());
        } else {
            throw new BizException(ErrorCode.PARAM_INVALID, "result无效");
        }
        if (admin && wasReviewed) {
            auditService.log("SUBMISSION_OVERRIDE",
                    "submissionId=" + submissionId + ",result=" + result + ",reason=" + reason);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("submissionId", submissionId);
        data.put("status", submission.getStatus());
        data.put("reviewReason", submission.getRejectReason());
        data.put("reviewedAt", submission.getUpdatedAt());
        return data;
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

    private void applySubmissionStatusFilter(LambdaQueryWrapper<Submission> q, String status) {
        String st = StringUtils.hasText(status) ? status.trim() : "PENDING";
        if ("REVIEWED".equalsIgnoreCase(st)) {
            q.in(Submission::getStatus, List.of("APPROVED", "REJECTED"));
        } else if ("APPROVED".equalsIgnoreCase(st) || "REJECTED".equalsIgnoreCase(st) || "PENDING".equalsIgnoreCase(st)) {
            q.eq(Submission::getStatus, st.toUpperCase(Locale.ROOT));
        } else {
            q.eq(Submission::getStatus, "PENDING");
        }
    }

    private Map<String, Object> toHallListItem(Submission s) {
        Bounty bounty = bountyMapper.selectById(s.getBountyId());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("submissionId", s.getId());
        m.put("bountyId", s.getBountyId());
        m.put("bountyTitle", bounty == null ? null : bounty.getTitle());
        m.put("claimId", s.getClaimId());
        m.put("claimerUserId", s.getUserId());
        m.put("claimerNickname", nickname(s.getUserId()));
        m.put("versionNo", s.getVersionNo());
        m.put("status", s.getStatus());
        m.put("summary", s.getContentSummary());
        m.put("createdAt", s.getCreatedAt());
        // 兼容旧键
        m.put("id", s.getId());
        m.put("userId", s.getUserId());
        return m;
    }

    private Map<String, Object> toAdminListItem(Submission s) {
        Bounty bounty = bountyMapper.selectById(s.getBountyId());
        boolean reviewed = "APPROVED".equals(s.getStatus()) || "REJECTED".equals(s.getStatus());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("submissionId", s.getId());
        m.put("bountyId", s.getBountyId());
        m.put("bountyTitle", bounty == null ? null : bounty.getTitle());
        m.put("claimId", s.getClaimId());
        m.put("claimerUserId", s.getUserId());
        m.put("claimerNickname", nickname(s.getUserId()));
        m.put("versionNo", s.getVersionNo());
        m.put("status", s.getStatus());
        m.put("summary", s.getContentSummary());
        m.put("createdAt", s.getCreatedAt());
        m.put("reviewedAt", reviewed ? s.getUpdatedAt() : null);
        m.put("reviewReason", s.getRejectReason());
        return m;
    }

    private String nickname(Long userId) {
        UserProfile p = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId).last("LIMIT 1"));
        return p == null ? "" : p.getNickname();
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

    /** 当前用户已揭榜的悬赏 id（用于执事堂回避过滤） */
    private Set<Long> claimedBountyIds(Long userId, Collection<Long> bountyIds) {
        if (bountyIds == null || bountyIds.isEmpty()) {
            return Set.of();
        }
        return claimMapper.selectList(new LambdaQueryWrapper<BountyClaim>()
                        .eq(BountyClaim::getUserId, userId)
                        .in(BountyClaim::getBountyId, bountyIds))
                .stream()
                .map(BountyClaim::getBountyId)
                .collect(Collectors.toSet());
    }

    private static <T> List<T> pageSlice(List<T> all, long page, long pageSize) {
        long p = page < 1 ? 1 : page;
        long size = pageSize < 1 ? 20 : pageSize;
        int from = (int) ((p - 1) * size);
        if (from >= all.size()) {
            return List.of();
        }
        int to = (int) Math.min(from + size, all.size());
        return all.subList(from, to);
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
