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

    private final BountyMapper bountyMapper;
    private final BountyWarrantMapper warrantMapper;
    private final BountyChecklistMapper checklistMapper;
    private final BountyClaimMapper claimMapper;
    private final BountyMessageMapper messageMapper;
    private final SubmissionMapper submissionMapper;
    private final SubmissionItemMapper submissionItemMapper;
    private final MetaService metaService;
    private final ConfigService configService;
    private final WalletService walletService;
    private final UserAssetService userAssetService;
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
        List<Map<String, Object>> list = p.getRecords().stream().map(this::briefView).collect(Collectors.toList());
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
        if ("RENT_OUT".equals(bounty.getType()) && !claimed && !publisher) {
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
        if (!"RENT_SEEK".equals(req.getType()) && !"RENT_OUT".equals(req.getType())) {
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
        return PageResult.of(p.getRecords().stream().map(this::briefView).toList(), p.getTotal(), page, pageSize);
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
        return PageResult.of(p.getRecords().stream().map(this::briefView).toList(), p.getTotal(), page, pageSize);
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

    public PageResult<BountyMessage> messages(Long bountyId, long page, long pageSize) {
        assertParticipant(bountyId, AuthContext.requireUserId());
        Page<BountyMessage> p = messageMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<BountyMessage>()
                        .eq(BountyMessage::getBountyId, bountyId)
                        .orderByAsc(BountyMessage::getId));
        return PageResult.of(p.getRecords(), p.getTotal(), page, pageSize);
    }

    @Transactional
    public BountyMessage sendMessage(Long bountyId, String content) {
        Long userId = AuthContext.requireUserId();
        assertParticipant(bountyId, userId);
        if (!StringUtils.hasText(content) || content.length() > 1000) {
            throw new BizException(ErrorCode.PARAM_INVALID, "消息内容非法");
        }
        BountyMessage msg = new BountyMessage();
        msg.setBountyId(bountyId);
        msg.setSenderId(userId);
        msg.setContent(content.trim());
        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);
        return msg;
    }

    @Transactional
    public Map<String, Object> submit(Long bountyId, SubmitRequest req) {
        Long userId = AuthContext.requireUserId();
        Bounty bounty = requireBounty(bountyId);
        if (!"IN_COLLAB".equals(bounty.getStatus()) && !"OPEN".equals(bounty.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "当前状态不可提交成果");
        }
        BountyClaim claim = findClaim(bountyId, userId);
        if (claim == null) {
            throw new BizException(ErrorCode.FORBIDDEN, "未揭榜不可提交");
        }
        checkSubmitRate(claim.getId(), userId);
        List<BountyChecklist> checklist = checklistMapper.selectList(new LambdaQueryWrapper<BountyChecklist>()
                .eq(BountyChecklist::getBountyId, bountyId));
        Set<String> required = checklist.stream().filter(BountyChecklist::getRequired)
                .map(BountyChecklist::getItemCode).collect(Collectors.toSet());
        Map<String, SubmitRequest.Item> itemMap = new HashMap<>();
        for (SubmitRequest.Item item : req.getItems()) {
            itemMap.put(item.getItemCode(), item);
        }
        boolean anyContent = false;
        for (SubmitRequest.Item item : req.getItems()) {
            if (Boolean.TRUE.equals(item.getDone())
                    || StringUtils.hasText(item.getText())
                    || (item.getMediaUrls() != null && !item.getMediaUrls().isEmpty())) {
                anyContent = true;
            }
        }
        if (!anyContent && !StringUtils.hasText(req.getSummary())) {
            throw new BizException(ErrorCode.SUBMISSION_INVALID, "提交内容不能为空");
        }
        for (String code : required) {
            SubmitRequest.Item item = itemMap.get(code);
            if (item == null || !Boolean.TRUE.equals(item.getDone())) {
                throw new BizException(ErrorCode.SUBMISSION_INVALID, "必验项未覆盖: " + code);
            }
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
            row.setDone(Boolean.TRUE.equals(item.getDone()));
            row.setText(item.getText());
            row.setMediaUrlsJson(writeJson(item.getMediaUrls() == null ? List.of() : item.getMediaUrls()));
            submissionItemMapper.insert(row);
        }
        markSubmitCooldown(claim.getId(), userId);
        return submissionDetail(submission.getId());
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
        return list.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("versionNo", s.getVersionNo());
            m.put("status", s.getStatus());
            m.put("summary", s.getContentSummary());
            m.put("createdAt", s.getCreatedAt());
            return m;
        }).toList();
    }

    public Map<String, Object> submissionDetail(Long submissionId) {
        Submission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        Long userId = optionalUserId();
        if (userId != null) {
            Bounty bounty = requireBounty(submission.getBountyId());
            boolean ok = userId.equals(bounty.getPublisherId()) || userId.equals(submission.getUserId());
            // hall/admin checked elsewhere; for hero API allow participants
            if (!ok) {
                // still allow if has office? leave for hall APIs
            }
        }
        List<SubmissionItem> items = submissionItemMapper.selectList(new LambdaQueryWrapper<SubmissionItem>()
                .eq(SubmissionItem::getSubmissionId, submissionId));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", submission.getId());
        data.put("bountyId", submission.getBountyId());
        data.put("claimId", submission.getClaimId());
        data.put("userId", submission.getUserId());
        data.put("versionNo", submission.getVersionNo());
        data.put("status", submission.getStatus());
        data.put("summary", submission.getContentSummary());
        data.put("rejectReason", submission.getRejectReason());
        data.put("createdAt", submission.getCreatedAt());
        data.put("items", items.stream().map(i -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("itemCode", i.getChecklistItemCode());
            m.put("done", i.getDone());
            m.put("text", i.getText());
            m.put("mediaUrls", readJsonList(i.getMediaUrlsJson()));
            return m;
        }).toList());
        return data;
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

    private void validateWarrant(String type, Map<String, Object> fields) {
        if (fields == null || fields.isEmpty()) {
            throw new BizException(ErrorCode.BOUNTY_WARRANT_INVALID, "令状字段不能为空");
        }
        List<String> required = "RENT_SEEK".equals(type)
                ? List.of("district", "rentBudgetMin", "rentBudgetMax", "layout", "expectMoveInDate", "acceptAgency")
                : List.of("district", "exactAddress", "rentPrice", "layout", "availableDate");
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
