package com.jianghu.ling.bounty.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianghu.ling.bounty.domain.*;
import com.jianghu.ling.bounty.dto.CreateBountyRequest;
import com.jianghu.ling.bounty.dto.RepublishBountyRequest;
import com.jianghu.ling.bounty.dto.SubmitRequest;
import com.jianghu.ling.bounty.mapper.*;
import com.jianghu.ling.cms.domain.ChecklistTemplate;
import com.jianghu.ling.cms.domain.RewardSuggestConfig;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.cms.service.MetaService;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.common.util.IdempotencyKeys;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.security.AuthPrincipal;
import com.jianghu.ling.security.PrincipalType;
import com.jianghu.ling.user.domain.User;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.mapper.UserMapper;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import com.jianghu.ling.notify.service.NotifyService;
import com.jianghu.ling.settle.domain.Settlement;
import com.jianghu.ling.settle.mapper.SettlementMapper;
import com.jianghu.ling.user.service.UserAssetService;
import com.jianghu.ling.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BountyService {

    private static final Set<String> REPUBLISHABLE_STATUSES = Set.of("REJECTED", "CANCELLED", "COMPLETED");
    private static final Set<String> MESSAGE_SUBMIT_ALLOWED_STATUSES = Set.of("IN_COLLAB", "PENDING_SETTLE");
    private static final Set<String> CANCELABLE_STATUSES =
            Set.of("PENDING_REVIEW", "OPEN", "IN_COLLAB", "PENDING_SETTLE");
    private static final Set<String> PUBLISHER_READONLY_TERMINAL =
            Set.of("COMPLETED", "CANCELLED", "IN_DISPUTE");
    private static final Set<String> CLAIMER_READONLY_STATUSES =
            Set.of("IN_COLLAB", "PENDING_SETTLE", "COMPLETED", "CANCELLED", "IN_DISPUTE");

    private final BountyMapper bountyMapper;
    private final BountyWarrantMapper warrantMapper;
    private final BountyChecklistMapper checklistMapper;
    private final BountyClaimMapper claimMapper;
    private final BountyMessageMapper messageMapper;
    private final SubmissionMapper submissionMapper;
    private final SubmissionItemMapper submissionItemMapper;
    private final SettlementMapper settlementMapper;
    private final MetaService metaService;
    private final ConfigService configService;
    private final WalletService walletService;
    private final UserAssetService userAssetService;
    private final NotifyService notifyService;
    private final UserProfileMapper userProfileMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;

    public PageResult<Map<String, Object>> plaza(String type, String district, String status,
                                                 String keyword, long page, long pageSize) {
        List<String> statuses;
        if (StringUtils.hasText(status)) {
            statuses = Arrays.stream(status.split(",")).map(String::trim).filter(StringUtils::hasText).toList();
        } else {
            statuses = List.of("OPEN", "IN_COLLAB");
        }
        LambdaQueryWrapper<Bounty> q = new LambdaQueryWrapper<Bounty>()
                .in(Bounty::getStatus, statuses)
                .eq(StringUtils.hasText(type), Bounty::getType, type)
                .eq(StringUtils.hasText(district), Bounty::getDistrict, district)
                .like(StringUtils.hasText(keyword), Bounty::getTitle, keyword)
                .orderByDesc(Bounty::getId);
        Page<Bounty> p = bountyMapper.selectPage(new Page<>(page, pageSize), q);
        List<Bounty> records = p.getRecords();
        List<Map<String, Object>> list = records.stream().map(this::briefView).collect(Collectors.toList());

        if (!records.isEmpty()) {
            List<Long> bountyIds = records.stream().map(Bounty::getId).toList();
            Map<Long, Long> claimCountMap = claimMapper.selectList(
                    new LambdaQueryWrapper<BountyClaim>()
                            .in(BountyClaim::getBountyId, bountyIds)
                            .eq(BountyClaim::getStatus, "ACTIVE")
            ).stream().collect(Collectors.groupingBy(BountyClaim::getBountyId, Collectors.counting()));
            for (Map<String, Object> m : list) {
                Long id = (Long) m.get("id");
                m.put("claimCount", claimCountMap.getOrDefault(id, 0L));
            }
        }

        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    public Map<String, Object> detail(Long id) {
        Bounty bounty = requireBounty(id);
        Long viewerId = optionalUserId();
        boolean claimed = viewerId != null && findClaim(id, viewerId) != null;
        boolean publisher = viewerId != null && viewerId.equals(bounty.getPublisherId());
        Map<String, Object> data = briefView(bounty);
        data.put("taskTags", readJsonList(bounty.getTaskTagsJson()));
        Map<String, Object> fields = readJsonMap(optionalWarrant(id).map(BountyWarrant::getFieldsJson).orElse("{}"));
        if (("RENT_OUT".equals(bounty.getType()) || "RENT_TRANSFER".equals(bounty.getType()))
                && !claimed && !publisher) {
            fields.put("exactAddress", "***揭榜后可见***");
        }
        data.put("warrantFields", fields);
        data.put("checklist", checklistMapper.selectList(new LambdaQueryWrapper<BountyChecklist>()
                .eq(BountyChecklist::getBountyId, id)
                .orderByAsc(BountyChecklist::getSortNo)));
        data.put("claimCount", claimMapper.selectCount(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getBountyId, id)));
        data.put("claimedByMe", claimed);
        data.put("publisherNickname", nickname(bounty.getPublisherId()));
        data.put("hasSubmissions", countSubmissions(bounty.getId()) > 0);
        data.put("cancelAllocationPending", Boolean.TRUE.equals(bounty.getCancelAllocationPending()));
        Map<String, Object> capabilities = computeCapabilities(bounty, viewerId);
        data.put("capabilities", capabilities);
        data.put("canRepublish", capabilities.get("canRepublish"));
        return data;
    }

    @Transactional
    public Map<String, Object> create(CreateBountyRequest req) {
        Long userId = AuthContext.requireUserId();
        Long sourceId = req.getSourceBountyId();
        if (sourceId != null) {
            // 等价再发路径：校验终态/归属，仍走新建+冻结，禁止复活原单
            Bounty source = requireBounty(sourceId);
            assertCanRepublish(source, userId);
        }
        Bounty bounty = createNewBounty(userId, req, sourceId);
        return detail(bounty.getId());
    }

    public Map<String, Object> republishDraft(Long sourceId) {
        Long userId = AuthContext.requireUserId();
        Bounty source = requireBounty(sourceId);
        assertCanRepublish(source, userId);

        Map<String, Object> warrantFields = readJsonMap(
                optionalWarrant(sourceId).map(BountyWarrant::getFieldsJson).orElse("{}"));
        List<String> checklistCodes = checklistMapper.selectList(new LambdaQueryWrapper<BountyChecklist>()
                        .eq(BountyChecklist::getBountyId, sourceId)
                        .orderByAsc(BountyChecklist::getSortNo))
                .stream().map(BountyChecklist::getItemCode).toList();

        BigDecimal minReward = configService.getDecimal("min_reward", "200");
        RewardSuggestConfig diff = metaService.findDifficulty(source.getDifficulty());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("sourceBountyId", source.getId());
        data.put("type", source.getType());
        data.put("title", source.getTitle());
        data.put("difficulty", source.getDifficulty());
        data.put("rewardAmount", source.getRewardAmount());
        data.put("deadlineAt", null);
        data.put("taskTags", readJsonList(source.getTaskTagsJson()));
        data.put("warrantFields", warrantFields);
        data.put("checklistItemCodes", checklistCodes);
        data.put("suggestMin", diff == null ? null : diff.getSuggestMin());
        data.put("minReward", minReward);
        return data;
    }

    @Transactional
    public Map<String, Object> republish(Long sourceId, RepublishBountyRequest body) {
        Long userId = AuthContext.requireUserId();
        Bounty source = requireBounty(sourceId);
        assertCanRepublish(source, userId);
        // 记录原状态，提交后校验未被改写
        String sourceStatusBefore = source.getStatus();

        CreateBountyRequest req = mergeRepublishRequest(source, body);
        Bounty created = createNewBounty(userId, req, sourceId);

        Bounty sourceAfter = bountyMapper.selectById(sourceId);
        if (sourceAfter == null || !sourceStatusBefore.equals(sourceAfter.getStatus())) {
            throw new BizException(ErrorCode.INTERNAL, "原单状态异常，再发已中止");
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", created.getId());
        data.put("sourceBountyId", sourceId);
        data.put("status", created.getStatus());
        data.put("type", created.getType());
        data.put("title", created.getTitle());
        data.put("rewardAmount", created.getRewardAmount());
        data.put("deadlineAt", created.getDeadlineAt());
        data.put("frozen", StringUtils.hasText(created.getFrozenBizNo()));
        data.put("canRepublish", false);
        return data;
    }

    private Bounty createNewBounty(Long userId, CreateBountyRequest req, Long sourceBountyId) {
        if (!isValidBountyType(req.getType())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "type无效");
        }
        BigDecimal minReward = configService.getDecimal("min_reward", "200");
        if (req.getRewardAmount().compareTo(minReward) < 0) {
            throw new BizException(ErrorCode.BOUNTY_REWARD_TOO_LOW);
        }
        RewardSuggestConfig diff = metaService.findDifficulty(req.getDifficulty());
        if (diff == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "difficulty无效");
        }
        if (req.getRewardAmount().compareTo(diff.getSuggestMin()) < 0
                && !Boolean.TRUE.equals(req.getConfirmLowReward())) {
            throw new BizException(ErrorCode.BIZ_RULE, "赏银低于建议下限，请确认 confirmLowReward=true");
        }
        validateWarrant(req.getType(), req.getWarrantFields());
        if (req.getDeadlineAt() == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "截止时间必填");
        }
        LocalDateTime deadline = req.getDeadlineAt().toLocalDateTime();
        if (!deadline.isAfter(LocalDateTime.now())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "截止时间必须晚于当前");
        }

        String district = String.valueOf(req.getWarrantFields().get("district"));
        Bounty bounty = new Bounty();
        bounty.setPublisherId(userId);
        bounty.setType(req.getType());
        bounty.setTitle(req.getTitle());
        bounty.setStatus("PENDING_REVIEW");
        bounty.setCity("遵义");
        bounty.setDistrict(district);
        bounty.setDifficulty(req.getDifficulty());
        bounty.setRewardAmount(req.getRewardAmount());
        bounty.setDeadlineAt(deadline);
        bounty.setTaskTagsJson(writeJson(req.getTaskTags() == null ? List.of() : req.getTaskTags()));
        bounty.setSourceBountyId(sourceBountyId);
        bounty.setRemind24hSent(false);
        bounty.setRemind2hSent(false);
        bounty.setCreatedAt(LocalDateTime.now());
        bounty.setUpdatedAt(LocalDateTime.now());
        bountyMapper.insert(bounty);

        String freezeBizNo = IdempotencyKeys.bizNo("FZ");
        walletService.freeze(userId, req.getRewardAmount(), freezeBizNo, "BOUNTY", bounty.getId());
        bounty.setFrozenBizNo(freezeBizNo);
        bountyMapper.updateById(bounty);

        BountyWarrant warrant = new BountyWarrant();
        warrant.setBountyId(bounty.getId());
        warrant.setTemplateCode(req.getType());
        warrant.setFieldsJson(writeJson(req.getWarrantFields()));
        warrantMapper.insert(warrant);

        List<String> codes = req.getChecklistItemCodes() == null ? List.of() : req.getChecklistItemCodes();
        List<ChecklistTemplate> templates = metaService.findChecklistByCodes(codes);
        int sort = 1;
        for (ChecklistTemplate t : templates) {
            BountyChecklist item = new BountyChecklist();
            item.setBountyId(bounty.getId());
            item.setItemCode(t.getItemCode());
            item.setItemName(t.getItemName());
            item.setRequired(t.getRequired());
            item.setSortNo(sort++);
            checklistMapper.insert(item);
        }
        return bounty;
    }

    private CreateBountyRequest mergeRepublishRequest(Bounty source, RepublishBountyRequest body) {
        if (body == null) {
            body = new RepublishBountyRequest();
        }
        Map<String, Object> sourceFields = readJsonMap(
                optionalWarrant(source.getId()).map(BountyWarrant::getFieldsJson).orElse("{}"));
        List<String> sourceChecklist = checklistMapper.selectList(new LambdaQueryWrapper<BountyChecklist>()
                        .eq(BountyChecklist::getBountyId, source.getId())
                        .orderByAsc(BountyChecklist::getSortNo))
                .stream().map(BountyChecklist::getItemCode).toList();

        CreateBountyRequest req = new CreateBountyRequest();
        req.setType(source.getType());
        req.setTitle(StringUtils.hasText(body.getTitle()) ? body.getTitle() : source.getTitle());
        req.setDifficulty(StringUtils.hasText(body.getDifficulty()) ? body.getDifficulty() : source.getDifficulty());
        req.setRewardAmount(body.getRewardAmount() != null ? body.getRewardAmount() : source.getRewardAmount());
        req.setConfirmLowReward(body.getConfirmLowReward());
        req.setDeadlineAt(body.getDeadlineAt());
        req.setTaskTags(body.getTaskTags() != null ? body.getTaskTags() : readJsonList(source.getTaskTagsJson()));
        req.setWarrantFields(body.getWarrantFields() != null ? body.getWarrantFields() : sourceFields);
        req.setChecklistItemCodes(body.getChecklistItemCodes() != null ? body.getChecklistItemCodes() : sourceChecklist);
        req.setSourceBountyId(source.getId());
        return req;
    }

    private void assertCanRepublish(Bounty source, Long userId) {
        if (!userId.equals(source.getPublisherId())
                || !REPUBLISHABLE_STATUSES.contains(source.getStatus())) {
            throw new BizException(ErrorCode.BOUNTY_REPUBLISH_DENIED);
        }
        User user = userMapper.selectById(userId);
        if (user == null || !"ACTIVE".equals(user.getStatus())) {
            throw new BizException(ErrorCode.BOUNTY_REPUBLISH_DENIED);
        }
    }

    private boolean computeCanRepublish(Bounty bounty, Long viewerId) {
        if (viewerId == null || !viewerId.equals(bounty.getPublisherId())) {
            return false;
        }
        if (!REPUBLISHABLE_STATUSES.contains(bounty.getStatus())) {
            return false;
        }
        User user = userMapper.selectById(viewerId);
        return user != null && "ACTIVE".equals(user.getStatus());
    }

    public PageResult<Map<String, Object>> minePublished(String status, long page, long pageSize) {
        Long userId = AuthContext.requireUserId();
        LambdaQueryWrapper<Bounty> q = new LambdaQueryWrapper<Bounty>()
                .eq(Bounty::getPublisherId, userId)
                .eq(StringUtils.hasText(status), Bounty::getStatus, status)
                .orderByDesc(Bounty::getId);
        Page<Bounty> p = bountyMapper.selectPage(new Page<>(page, pageSize), q);
        List<Bounty> records = p.getRecords();
        List<Map<String, Object>> list = records.stream().map(this::briefView).collect(Collectors.toList());

        if (!records.isEmpty()) {
            List<Long> bountyIds = records.stream().map(Bounty::getId).toList();
            Map<Long, Long> claimCountMap = claimMapper.selectList(
                    new LambdaQueryWrapper<BountyClaim>()
                            .in(BountyClaim::getBountyId, bountyIds)
                            .eq(BountyClaim::getStatus, "ACTIVE")
            ).stream().collect(Collectors.groupingBy(BountyClaim::getBountyId, Collectors.counting()));
            for (Map<String, Object> m : list) {
                Long id = (Long) m.get("id");
                m.put("claimCount", claimCountMap.getOrDefault(id, 0L));
            }
            enrichMineListExtras(list, userId);
        }

        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    public PageResult<Map<String, Object>> mineClaimed(String status, long page, long pageSize) {
        Long userId = AuthContext.requireUserId();
        List<BountyClaim> claims = claimMapper.selectList(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getUserId, userId)
                .orderByDesc(BountyClaim::getId));
        if (claims.isEmpty()) {
            return PageResult.empty(page, pageSize);
        }
        List<Long> bountyIds = claims.stream().map(BountyClaim::getBountyId).toList();
        LambdaQueryWrapper<Bounty> q = new LambdaQueryWrapper<Bounty>()
                .in(Bounty::getId, bountyIds)
                .eq(StringUtils.hasText(status), Bounty::getStatus, status)
                .orderByDesc(Bounty::getId);
        Page<Bounty> p = bountyMapper.selectPage(new Page<>(page, pageSize), q);
        List<Bounty> records2 = p.getRecords();
        List<Map<String, Object>> list2 = records2.stream().map(this::briefView).collect(Collectors.toList());

        if (!records2.isEmpty()) {
            List<Long> ids2 = records2.stream().map(Bounty::getId).toList();
            Map<Long, Long> claimCountMap2 = claimMapper.selectList(
                    new LambdaQueryWrapper<BountyClaim>()
                            .in(BountyClaim::getBountyId, ids2)
                            .eq(BountyClaim::getStatus, "ACTIVE")
            ).stream().collect(Collectors.groupingBy(BountyClaim::getBountyId, Collectors.counting()));
            for (Map<String, Object> m : list2) {
                Long id = (Long) m.get("id");
                m.put("claimCount", claimCountMap2.getOrDefault(id, 0L));
            }
            enrichMineListExtras(list2, userId);
        }

        return PageResult.of(list2, p.getTotal(), page, pageSize);
    }

    /** 我的悬赏列表附加：成果数 + 协作未读（他人消息） */
    private void enrichMineListExtras(List<Map<String, Object>> list, Long userId) {
        if (list == null || list.isEmpty() || userId == null) {
            return;
        }
        List<Long> ids = list.stream()
                .map(m -> (Long) m.get("id"))
                .filter(Objects::nonNull)
                .toList();
        if (ids.isEmpty()) {
            return;
        }
        Map<Long, Long> submissionCountMap = submissionMapper.selectList(
                new LambdaQueryWrapper<Submission>().in(Submission::getBountyId, ids)
        ).stream().collect(Collectors.groupingBy(Submission::getBountyId, Collectors.counting()));

        for (Map<String, Object> m : list) {
            Long id = (Long) m.get("id");
            if (id == null) {
                continue;
            }
            m.put("submissionCount", submissionCountMap.getOrDefault(id, 0L));
            m.put("unreadCollabCount", countUnreadCollab(userId, id));
        }
    }

    private String collabReadKey(Long userId, Long bountyId) {
        return "collab:lastRead:" + userId + ":" + bountyId;
    }

    private void markCollabRead(Long userId, Long bountyId) {
        BountyMessage latest = messageMapper.selectOne(new LambdaQueryWrapper<BountyMessage>()
                .eq(BountyMessage::getBountyId, bountyId)
                .orderByDesc(BountyMessage::getId)
                .last("LIMIT 1"));
        long watermark = latest == null || latest.getId() == null ? 0L : latest.getId();
        redisTemplate.opsForValue().set(collabReadKey(userId, bountyId), String.valueOf(watermark), Duration.ofDays(30));
    }

    private long countUnreadCollab(Long userId, Long bountyId) {
        long lastRead = 0L;
        try {
            String raw = redisTemplate.opsForValue().get(collabReadKey(userId, bountyId));
            if (StringUtils.hasText(raw)) {
                lastRead = Long.parseLong(raw.trim());
            }
        } catch (Exception ignored) {
            lastRead = 0L;
        }
        return messageMapper.selectCount(new LambdaQueryWrapper<BountyMessage>()
                .eq(BountyMessage::getBountyId, bountyId)
                .gt(BountyMessage::getId, lastRead)
                .ne(BountyMessage::getSenderId, userId));
    }

    @Transactional
    public Map<String, Object> claim(Long bountyId) {
        Long userId = AuthContext.requireUserId();
        Bounty bounty = requireBounty(bountyId);
        if (userId.equals(bounty.getPublisherId())) {
            throw new BizException(ErrorCode.CLAIM_NOT_ALLOWED, "不可揭自己的令");
        }
        if (!"OPEN".equals(bounty.getStatus()) && !"IN_COLLAB".equals(bounty.getStatus())) {
            throw new BizException(ErrorCode.CLAIM_NOT_ALLOWED);
        }
        if (findClaim(bountyId, userId) != null) {
            throw new BizException(ErrorCode.CONFLICT, "已揭过该令");
        }
        int dayLimit = configService.getInt("claim_day_limit", 10);
        String dayKey = claimDayKey(userId);
        Long count = redisTemplate.opsForValue().increment(dayKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(dayKey, Duration.ofDays(2));
        }
        if (count != null && count > dayLimit) {
            redisTemplate.opsForValue().decrement(dayKey);
            throw new BizException(ErrorCode.CLAIM_DAY_LIMIT);
        }
        int cost = configService.getInt("claim_stamina_cost", 1);
        try {
            userAssetService.consumeStamina(userId, cost);
        } catch (BizException e) {
            redisTemplate.opsForValue().decrement(dayKey);
            throw e;
        }
        BountyClaim claim = new BountyClaim();
        claim.setBountyId(bountyId);
        claim.setUserId(userId);
        claim.setStaminaCost(cost);
        claim.setStatus("ACTIVE");
        claim.setCreatedAt(LocalDateTime.now());
        try {
            claimMapper.insert(claim);
        } catch (DuplicateKeyException e) {
            redisTemplate.opsForValue().decrement(dayKey);
            throw new BizException(ErrorCode.CONFLICT, "已揭过该令");
        }
        if ("OPEN".equals(bounty.getStatus())) {
            bounty.setStatus("IN_COLLAB");
            bounty.setUpdatedAt(LocalDateTime.now());
            bountyMapper.updateById(bounty);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("claimId", claim.getId());
        data.put("bountyId", bountyId);
        data.put("status", bounty.getStatus());
        return data;
    }

    /**
     * 退出揭榜：ACTIVE + IN_COLLAB；返还体力；不回退当日次数；同令不可再揭（api.md §7.5.1）。
     */
    @Transactional
    public Map<String, Object> quitClaim(Long bountyId, String reason) {
        Long userId = AuthContext.requireUserId();
        Bounty bounty = requireBounty(bountyId);
        BountyClaim claim = findClaim(bountyId, userId);
        if (claim == null) {
            throw new BizException(ErrorCode.FORBIDDEN, "非揭榜人不可退出");
        }
        if (!"ACTIVE".equals(claim.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "当前揭榜状态不可退出");
        }
        if (!"IN_COLLAB".equals(bounty.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "当前悬赏状态不可退出揭榜");
        }

        claim.setStatus("QUIT");
        claimMapper.updateById(claim);

        int refund = claim.getStaminaCost() == null ? 0 : Math.max(0, claim.getStaminaCost());
        if (refund > 0) {
            userAssetService.adjustStamina(userId, refund);
        }

        String reasonText = StringUtils.hasText(reason) ? reason.trim() : "未填写原因";
        notifyService.send(bounty.getPublisherId(), "揭榜侠已退出",
                "悬赏「" + bounty.getTitle() + "」有揭榜侠退出：" + reasonText,
                "BOUNTY_CLAIM_QUIT", claim.getId());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("claimId", claim.getId());
        data.put("status", "QUIT");
        data.put("staminaRefunded", refund);
        return data;
    }

    public PageResult<Map<String, Object>> messages(Long bountyId, long page, long pageSize) {
        // 可读：令主或任意揭榜关系；共享流禁止按发送方过滤（api.md §7.6）
        Long userId = AuthContext.requireUserId();
        assertParticipant(bountyId, userId);
        Page<BountyMessage> p = messageMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<BountyMessage>()
                        .eq(BountyMessage::getBountyId, bountyId)
                        .orderByAsc(BountyMessage::getId));
        List<Map<String, Object>> list = p.getRecords().stream().map(this::messageView).toList();
        // 打开会话即记已读水位（供「我的悬赏」未读角标）
        markCollabRead(userId, bountyId);
        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    @Transactional
    public Map<String, Object> sendMessage(Long bountyId, String content) {
        Long userId = AuthContext.requireUserId();
        // 可写：令主或 ACTIVE 揭榜，且状态 IN_COLLAB|PENDING_SETTLE（api.md §7.7 / §7.9.4）
        assertCanWriteMessage(bountyId, userId);
        String body = content == null ? "" : content.trim();
        if (!StringUtils.hasText(body) || body.length() > 1000) {
            throw new BizException(ErrorCode.PARAM_INVALID, "消息内容非法");
        }
        BountyMessage msg = new BountyMessage();
        msg.setBountyId(bountyId);
        msg.setSenderId(userId);
        msg.setContent(body);
        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);
        return messageView(msg);
    }

    @Transactional
    public Map<String, Object> submit(Long bountyId, SubmitRequest req) {
        Long userId = AuthContext.requireUserId();
        Bounty bounty = requireBounty(bountyId);
        BountyClaim claim = findClaim(bountyId, userId);
        if (claim == null || !"ACTIVE".equals(claim.getStatus())) {
            throw new BizException(ErrorCode.FORBIDDEN, "未揭榜或已退出");
        }
        if (Boolean.TRUE.equals(bounty.getCancelAllocationPending())
                || !MESSAGE_SUBMIT_ALLOWED_STATUSES.contains(bounty.getStatus())) {
            throw new BizException(ErrorCode.SUBMISSION_STATUS_DENIED);
        }
        checkSubmitRate(claim.getId(), userId);
        List<BountyChecklist> checklist = checklistMapper.selectList(new LambdaQueryWrapper<BountyChecklist>()
                .eq(BountyChecklist::getBountyId, bountyId));
        Map<String, String> requiredLabels = checklist.stream()
                .filter(BountyChecklist::getRequired)
                .collect(Collectors.toMap(
                        BountyChecklist::getItemCode,
                        c -> StringUtils.hasText(c.getItemName()) ? c.getItemName() : c.getItemCode(),
                        (a, b) -> a,
                        LinkedHashMap::new));
        Map<String, SubmitRequest.Item> itemMap = new HashMap<>();
        for (SubmitRequest.Item item : req.getItems()) {
            itemMap.put(item.getItemCode(), item);
        }
        boolean anyContent = StringUtils.hasText(req.getSummary());
        for (SubmitRequest.Item item : req.getItems()) {
            boolean filled = itemHasContent(item);
            item.setDone(filled);
            if (filled) {
                anyContent = true;
            }
        }
        if (!anyContent) {
            throw new BizException(ErrorCode.SUBMISSION_INVALID, "提交内容不能为空");
        }
        List<String> missingRequired = new ArrayList<>();
        for (Map.Entry<String, String> entry : requiredLabels.entrySet()) {
            SubmitRequest.Item item = itemMap.get(entry.getKey());
            if (item == null || !itemHasContent(item)) {
                missingRequired.add(entry.getValue());
            }
        }
        if (!missingRequired.isEmpty()) {
            throw new BizException(ErrorCode.SUBMISSION_INVALID,
                    "以下必验项请填写说明或上传凭证：" + String.join("、", missingRequired));
        }
        Long versionCount = submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getClaimId, claim.getId()));
        Submission submission = new Submission();
        submission.setBountyId(bountyId);
        submission.setClaimId(claim.getId());
        submission.setUserId(userId);
        submission.setVersionNo(versionCount.intValue() + 1);
        submission.setStatus("PENDING");
        submission.setContentSummary(req.getSummary());
        submission.setCreatedAt(LocalDateTime.now());
        submission.setUpdatedAt(LocalDateTime.now());
        submissionMapper.insert(submission);
        for (SubmitRequest.Item item : req.getItems()) {
            SubmissionItem row = new SubmissionItem();
            row.setSubmissionId(submission.getId());
            row.setChecklistItemCode(item.getItemCode());
            row.setDone(itemHasContent(item));
            row.setText(item.getText());
            row.setMediaUrlsJson(writeJson(item.getMediaUrls() == null ? List.of() : item.getMediaUrls()));
            submissionItemMapper.insert(row);
        }
        markSubmitCooldown(claim.getId(), userId);
        return submissionDetail(submission.getId());
    }

    /**
     * 本令成果总览：令主看全部；揭榜侠仅本人 claim；路人 403（api.md §8.2）。
     */
    public PageResult<Map<String, Object>> listSubmissions(Long bountyId, Long claimIdFilter,
                                                           long page, long pageSize) {
        Long userId = AuthContext.requireUserId();
        Bounty bounty = requireBounty(bountyId);
        boolean isPublisher = userId.equals(bounty.getPublisherId());
        BountyClaim myClaim = findClaim(bountyId, userId);
        if (!isPublisher && myClaim == null) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }

        LambdaQueryWrapper<Submission> q = new LambdaQueryWrapper<Submission>()
                .eq(Submission::getBountyId, bountyId)
                .orderByDesc(Submission::getId);
        if (!isPublisher) {
            q.eq(Submission::getClaimId, myClaim.getId());
        } else if (claimIdFilter != null) {
            q.eq(Submission::getClaimId, claimIdFilter);
        }

        Page<Submission> p = submissionMapper.selectPage(new Page<>(page, pageSize), q);
        List<Submission> records = p.getRecords();
        Set<Long> claimIds = records.stream().map(Submission::getClaimId).collect(Collectors.toSet());
        Map<Long, BountyClaim> claimMap = claimIds.isEmpty() ? Map.of()
                : claimMapper.selectList(new LambdaQueryWrapper<BountyClaim>().in(BountyClaim::getId, claimIds))
                .stream().collect(Collectors.toMap(BountyClaim::getId, c -> c, (a, b) -> a));

        List<Map<String, Object>> list = records.stream()
                .map(s -> toSubmissionListItem(s, claimMap.get(s.getClaimId())))
                .toList();
        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    public List<Map<String, Object>> claimSubmissions(Long bountyId, Long claimId) {
        Long userId = AuthContext.requireUserId();
        Bounty bounty = requireBounty(bountyId);
        BountyClaim claim = claimMapper.selectById(claimId);
        if (claim == null || !claim.getBountyId().equals(bountyId)) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!userId.equals(bounty.getPublisherId()) && !userId.equals(claim.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        List<Submission> list = submissionMapper.selectList(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getClaimId, claimId)
                .orderByDesc(Submission::getId));
        return list.stream().map(s -> toSubmissionListItem(s, claim)).toList();
    }

    /**
     * C 端成果详情：令主或提交人（api.md §8.4）；完整字段见 §8.0。
     */
    public Map<String, Object> submissionDetail(Long submissionId) {
        Long userId = AuthContext.requireUserId();
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        Bounty bounty = requireBounty(submission.getBountyId());
        if (!userId.equals(bounty.getPublisherId()) && !userId.equals(submission.getUserId())) {
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        return buildSubmissionDetailVo(submission);
    }

    /** 共享 §8.0 SubmissionDetail（无鉴权；供 Hall/Admin/提交成功响应复用） */
    public Map<String, Object> buildSubmissionDetailVo(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return buildSubmissionDetailVo(submission);
    }

    public Map<String, Object> buildSubmissionDetailVo(Submission submission) {
        Bounty bounty = requireBounty(submission.getBountyId());
        List<BountyChecklist> checklist = checklistMapper.selectList(new LambdaQueryWrapper<BountyChecklist>()
                .eq(BountyChecklist::getBountyId, submission.getBountyId()));
        Map<String, String> itemNameMap = new LinkedHashMap<>();
        for (BountyChecklist c : checklist) {
            if (c.getItemCode() == null) {
                continue;
            }
            if (StringUtils.hasText(c.getItemName())) {
                itemNameMap.put(c.getItemCode(), c.getItemName());
            }
        }
        List<SubmissionItem> items = submissionItemMapper.selectList(new LambdaQueryWrapper<SubmissionItem>()
                .eq(SubmissionItem::getSubmissionId, submission.getId()));
        // 本单快照缺中文名时，回退 checklist_template
        List<String> missingCodes = items.stream()
                .map(SubmissionItem::getChecklistItemCode)
                .filter(code -> StringUtils.hasText(code) && !itemNameMap.containsKey(code))
                .distinct()
                .toList();
        if (!missingCodes.isEmpty()) {
            for (ChecklistTemplate t : metaService.findChecklistByCodes(missingCodes)) {
                if (StringUtils.hasText(t.getItemCode()) && StringUtils.hasText(t.getItemName())) {
                    itemNameMap.putIfAbsent(t.getItemCode(), t.getItemName());
                }
            }
        }
        boolean reviewed = "APPROVED".equals(submission.getStatus()) || "REJECTED".equals(submission.getStatus());
        Long claimerUserId = submission.getUserId();
        String claimerNickname = nickname(claimerUserId);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("submissionId", submission.getId());
        data.put("bountyId", submission.getBountyId());
        data.put("bountyTitle", bounty.getTitle());
        data.put("claimId", submission.getClaimId());
        data.put("claimerUserId", claimerUserId);
        data.put("claimerNickname", claimerNickname);
        data.put("versionNo", submission.getVersionNo());
        data.put("status", submission.getStatus());
        data.put("summary", submission.getContentSummary());
        data.put("items", items.stream().map(i -> {
            String code = i.getChecklistItemCode();
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("itemCode", code);
            m.put("itemName", itemNameMap.getOrDefault(code, code));
            m.put("done", Boolean.TRUE.equals(i.getDone()));
            m.put("text", i.getText());
            m.put("mediaUrls", readJsonList(i.getMediaUrlsJson()));
            return m;
        }).toList());
        data.put("reviewReason", submission.getRejectReason());
        data.put("reviewedAt", reviewed ? submission.getUpdatedAt() : null);
        data.put("createdAt", submission.getCreatedAt());
        data.put("updatedAt", submission.getUpdatedAt());
        // 兼容旧键
        data.put("id", submission.getId());
        data.put("userId", claimerUserId);
        data.put("rejectReason", submission.getRejectReason());
        return data;
    }

    private Map<String, Object> toSubmissionListItem(Submission s, BountyClaim claim) {
        Long claimerUserId = claim != null ? claim.getUserId() : s.getUserId();
        boolean reviewed = "APPROVED".equals(s.getStatus()) || "REJECTED".equals(s.getStatus());
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("submissionId", s.getId());
        m.put("bountyId", s.getBountyId());
        m.put("claimId", s.getClaimId());
        m.put("claimerUserId", claimerUserId);
        m.put("claimerNickname", nickname(claimerUserId));
        m.put("versionNo", s.getVersionNo());
        m.put("status", s.getStatus());
        m.put("summary", s.getContentSummary());
        m.put("createdAt", s.getCreatedAt());
        m.put("reviewedAt", reviewed ? s.getUpdatedAt() : null);
        m.put("reviewReason", s.getRejectReason());
        return m;
    }

    public Bounty requireBounty(Long id) {
        Bounty bounty = bountyMapper.selectById(id);
        if (bounty == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "悬赏不存在");
        }
        return bounty;
    }

    public BountyClaim findClaim(Long bountyId, Long userId) {
        return claimMapper.selectOne(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getBountyId, bountyId)
                .eq(BountyClaim::getUserId, userId)
                .last("LIMIT 1"));
    }

    public void assertParticipant(Long bountyId, Long userId) {
        Bounty bounty = requireBounty(bountyId);
        if (userId.equals(bounty.getPublisherId()) || findClaim(bountyId, userId) != null) {
            return;
        }
        throw new BizException(ErrorCode.FORBIDDEN);
    }

    private void assertCanWriteMessage(Long bountyId, Long userId) {
        Bounty bounty = requireBounty(bountyId);
        boolean isPublisher = userId.equals(bounty.getPublisherId());
        BountyClaim claim = findClaim(bountyId, userId);
        boolean activeClaimer = claim != null && "ACTIVE".equals(claim.getStatus());
        if (!isPublisher && !activeClaimer) {
            // 非参与人 / 已退出
            throw new BizException(ErrorCode.FORBIDDEN);
        }
        if (Boolean.TRUE.equals(bounty.getCancelAllocationPending())
                || !MESSAGE_SUBMIT_ALLOWED_STATUSES.contains(bounty.getStatus())) {
            throw new BizException(ErrorCode.MESSAGE_NOT_ALLOWED);
        }
    }

    /** api.md §7.9 角色×状态能力矩阵（含 cancelAllocationPending 取消分支） */
    private Map<String, Object> computeCapabilities(Bounty bounty, Long viewerId) {
        Map<String, Object> caps = emptyCapabilities();
        if (viewerId == null) {
            return caps;
        }
        String status = bounty.getStatus() == null ? "" : bounty.getStatus();
        boolean cancelPending = Boolean.TRUE.equals(bounty.getCancelAllocationPending());
        boolean isPublisher = viewerId.equals(bounty.getPublisherId());
        BountyClaim claim = findClaim(bounty.getId(), viewerId);
        boolean activeClaimer = claim != null && "ACTIVE".equals(claim.getStatus());
        boolean participant = isPublisher || claim != null;

        if (isPublisher) {
            if (cancelPending) {
                caps.put("canCancel", false);
                caps.put("canSendMessage", false);
                caps.put("canReadMessages", true);
                caps.put("canViewSubmissions", true);
                caps.put("canSubmit", false);
                caps.put("canSettle", true);
                caps.put("canQuitClaim", false);
                caps.put("canRepublish", false);
                caps.put("canDispute", false);
                return caps;
            }
            boolean collabLike = MESSAGE_SUBMIT_ALLOWED_STATUSES.contains(status);
            boolean readonlyTerminal = PUBLISHER_READONLY_TERMINAL.contains(status);
            caps.put("canCancel", CANCELABLE_STATUSES.contains(status));
            caps.put("canSendMessage", collabLike);
            caps.put("canReadMessages", collabLike || readonlyTerminal);
            caps.put("canViewSubmissions", collabLike || readonlyTerminal);
            caps.put("canSubmit", false);
            caps.put("canSettle", computeCanSettleForPublisher(bounty));
            caps.put("canQuitClaim", false);
            caps.put("canRepublish", computeCanRepublish(bounty, viewerId));
            caps.put("canDispute", computeCanDispute(bounty, participant));
            return caps;
        }

        if (activeClaimer) {
            if (cancelPending) {
                caps.put("canCancel", false);
                caps.put("canSendMessage", false);
                caps.put("canReadMessages", true);
                caps.put("canViewSubmissions", true);
                caps.put("canSubmit", false);
                caps.put("canSettle", false);
                caps.put("canQuitClaim", false);
                caps.put("canRepublish", false);
                caps.put("canDispute", false);
                return caps;
            }
            boolean collabLike = MESSAGE_SUBMIT_ALLOWED_STATUSES.contains(status);
            caps.put("canCancel", false);
            caps.put("canSendMessage", collabLike);
            caps.put("canReadMessages", CLAIMER_READONLY_STATUSES.contains(status));
            caps.put("canViewSubmissions", CLAIMER_READONLY_STATUSES.contains(status));
            caps.put("canSubmit", collabLike);
            caps.put("canSettle", false);
            caps.put("canQuitClaim", "IN_COLLAB".equals(status));
            caps.put("canRepublish", false);
            caps.put("canDispute", computeCanDispute(bounty, true));
            return caps;
        }

        if (claim != null) {
            // 已退出等历史揭榜：会话/成果只读；写能力关闭（api.md §7.5.1 / §7.9.3）
            caps.put("canSendMessage", false);
            caps.put("canSubmit", false);
            caps.put("canQuitClaim", false);
            caps.put("canReadMessages", true);
            caps.put("canViewSubmissions", true);
            caps.put("canDispute", computeCanDispute(bounty, true));
        }
        return caps;
    }

    private Map<String, Object> emptyCapabilities() {
        Map<String, Object> caps = new LinkedHashMap<>();
        caps.put("canCancel", false);
        caps.put("canSendMessage", false);
        caps.put("canReadMessages", false);
        caps.put("canViewSubmissions", false);
        caps.put("canSubmit", false);
        caps.put("canSettle", false);
        caps.put("canQuitClaim", false);
        caps.put("canRepublish", false);
        caps.put("canDispute", false);
        return caps;
    }

    private boolean computeCanSettleForPublisher(Bounty bounty) {
        if (Boolean.TRUE.equals(bounty.getCancelAllocationPending())) {
            return true;
        }
        String status = bounty.getStatus();
        if ("PENDING_SETTLE".equals(status)) {
            return true;
        }
        if (!"IN_COLLAB".equals(status)) {
            return false;
        }
        long claimCount = claimMapper.selectCount(new LambdaQueryWrapper<BountyClaim>()
                .eq(BountyClaim::getBountyId, bounty.getId()));
        long approved = submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getBountyId, bounty.getId())
                .eq(Submission::getStatus, "APPROVED"));
        return claimCount >= 1 && approved >= 1;
    }

    private long countSubmissions(Long bountyId) {
        return submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getBountyId, bountyId));
    }

    private boolean computeCanDispute(Bounty bounty, boolean participant) {
        if (!participant || !"COMPLETED".equals(bounty.getStatus())) {
            return false;
        }
        Settlement settlement = settlementMapper.selectOne(new LambdaQueryWrapper<Settlement>()
                .eq(Settlement::getBountyId, bounty.getId())
                .last("LIMIT 1"));
        if (settlement == null || settlement.getCreatedAt() == null) {
            return false;
        }
        return !settlement.getCreatedAt().plusDays(7).isBefore(LocalDateTime.now());
    }

    private Map<String, Object> messageView(BountyMessage msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", msg.getId());
        m.put("bountyId", msg.getBountyId());
        m.put("senderId", msg.getSenderId());
        m.put("senderNickname", nickname(msg.getSenderId()));
        m.put("content", msg.getContent());
        m.put("createdAt", msg.getCreatedAt());
        return m;
    }

    private void validateWarrant(String type, Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new BizException(ErrorCode.BOUNTY_WARRANT_INVALID, "令状字段不能为空");
        }
        List<String> required;
        if ("RENT_SEEK".equals(type)) {
            required = List.of("district", "rentBudgetMin", "rentBudgetMax", "layout", "expectMoveInDate", "acceptAgency");
        } else if ("RENT_OUT".equals(type) || "RENT_TRANSFER".equals(type)) {
            required = List.of("district", "exactAddress", "rentPrice", "layout", "availableDate");
        } else {
            throw new BizException(ErrorCode.PARAM_INVALID, "type无效");
        }
        for (String key : required) {
            Object val = fields.get(key);
            if (val == null) {
                throw new BizException(ErrorCode.BOUNTY_WARRANT_INVALID, "缺少字段: " + key);
            }
            // boolean false 合法；其余按字符串判空
            if (val instanceof Boolean) {
                continue;
            }
            if (String.valueOf(val).isBlank()) {
                throw new BizException(ErrorCode.BOUNTY_WARRANT_INVALID, "缺少字段: " + key);
            }
        }
    }

    private static boolean isValidBountyType(String type) {
        return "RENT_SEEK".equals(type) || "RENT_OUT".equals(type) || "RENT_TRANSFER".equals(type);
    }

    /** 清单项是否完成：有文字说明或已上传凭证即视为完成（不再依赖客户端勾选 done） */
    private static boolean itemHasContent(SubmitRequest.Item item) {
        if (item == null) {
            return false;
        }
        if (StringUtils.hasText(item.getText())) {
            return true;
        }
        return item.getMediaUrls() != null && !item.getMediaUrls().isEmpty();
    }

    private void checkSubmitRate(Long claimId, Long userId) {
        int cooldown = configService.getInt("submit_cooldown_seconds", 600);
        String cdKey = "submit:cd:" + claimId;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cdKey))) {
            throw new BizException(ErrorCode.SUBMISSION_INVALID, "提交冷却中");
        }
        int dayLimit = configService.getInt("submit_day_limit", 20);
        String dayKey = "submit:day:" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ":" + userId;
        Long count = redisTemplate.opsForValue().increment(dayKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(dayKey, Duration.ofDays(2));
        }
        if (count != null && count > dayLimit) {
            redisTemplate.opsForValue().decrement(dayKey);
            throw new BizException(ErrorCode.SUBMISSION_INVALID, "超出每日提交上限");
        }
        // cooldown set after success
    }

    private void markSubmitCooldown(Long claimId, Long userId) {
        int cooldown = configService.getInt("submit_cooldown_seconds", 600);
        redisTemplate.opsForValue().set("submit:cd:" + claimId, "1", Duration.ofSeconds(cooldown));
    }

    private String claimDayKey(Long userId) {
        return "claim:day:" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ":" + userId;
    }

    private Map<String, Object> briefView(Bounty bounty) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", bounty.getId());
        m.put("type", bounty.getType());
        m.put("typeDisplayName", metaService.typeDisplayName(bounty.getType()));
        m.put("title", bounty.getTitle());
        m.put("status", bounty.getStatus());
        m.put("city", bounty.getCity());
        m.put("district", bounty.getDistrict());
        m.put("difficulty", bounty.getDifficulty());
        m.put("rewardAmount", bounty.getRewardAmount());
        m.put("deadlineAt", bounty.getDeadlineAt());
        m.put("publisherId", bounty.getPublisherId());
        m.put("sourceBountyId", bounty.getSourceBountyId());
        m.put("canRepublish", computeCanRepublish(bounty, optionalUserId()));
        m.put("createdAt", bounty.getCreatedAt());
        return m;
    }

    private Optional<BountyWarrant> optionalWarrant(Long bountyId) {
        return Optional.ofNullable(warrantMapper.selectOne(new LambdaQueryWrapper<BountyWarrant>()
                .eq(BountyWarrant::getBountyId, bountyId).last("LIMIT 1")));
    }

    private Long optionalUserId() {
        AuthPrincipal p = AuthContext.get();
        if (p != null && p.getType() == PrincipalType.USER) {
            return p.getId();
        }
        return null;
    }

    private String nickname(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId).last("LIMIT 1"));
        return profile == null ? "" : profile.getNickname();
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new BizException(ErrorCode.INTERNAL, "JSON序列化失败");
        }
    }

    private Map<String, Object> readJsonMap(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private List<String> readJsonList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }
}
