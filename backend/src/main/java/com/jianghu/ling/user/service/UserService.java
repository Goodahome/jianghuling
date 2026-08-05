package com.jianghu.ling.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.config.AppProperties;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.user.domain.InviteCode;
import com.jianghu.ling.user.domain.InviteRelation;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.dto.RealNameRequest;
import com.jianghu.ling.user.dto.UpdateProfileRequest;
import com.jianghu.ling.user.mapper.InviteCodeMapper;
import com.jianghu.ling.user.mapper.InviteRelationMapper;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserProfileMapper userProfileMapper;
    private final InviteCodeMapper inviteCodeMapper;
    private final InviteRelationMapper inviteRelationMapper;
    private final ConfigService configService;
    private final AppProperties appProperties;
    private final StringRedisTemplate redisTemplate;

    @Transactional
    public Map<String, Object> updateProfile(UpdateProfileRequest req) {
        Long userId = AuthContext.requireUserId();
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId).last("LIMIT 1"));
        if (profile == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (StringUtils.hasText(req.getNickname())) {
            profile.setNickname(req.getNickname());
        }
        if (req.getAvatarUrl() != null) {
            profile.setAvatarUrl(req.getAvatarUrl());
        }
        if (req.getBio() != null) {
            profile.setBio(req.getBio());
        }
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileMapper.updateById(profile);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("nickname", profile.getNickname());
        data.put("avatarUrl", profile.getAvatarUrl());
        data.put("bio", profile.getBio());
        return data;
    }

    @Transactional
    public Map<String, Object> submitRealName(RealNameRequest req) {
        Long userId = AuthContext.requireUserId();
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId).last("LIMIT 1"));
        profile.setRealName(req.getRealName());
        profile.setIdNumber(req.getIdNumber());
        profile.setRealNameStatus("PENDING");
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileMapper.updateById(profile);
        return Map.of("status", profile.getRealNameStatus());
    }

    @Transactional
    public Map<String, Object> createInvite() {
        Long userId = AuthContext.requireUserId();
        int dailyQuota = configService.getInt("invite_daily_quota", 3);
        String dayKey = "invite:day:" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ":" + userId;
        Long used = redisTemplate.opsForValue().increment(dayKey);
        if (used != null && used == 1L) {
            redisTemplate.expire(dayKey, java.time.Duration.ofDays(2));
        }
        if (used != null && used > dailyQuota) {
            redisTemplate.opsForValue().decrement(dayKey);
            throw new BizException(ErrorCode.BIZ_RULE, "今日邀请码额度已用尽");
        }
        String code = randomCode();
        InviteCode invite = new InviteCode();
        invite.setCode(code);
        invite.setOwnerUserId(userId);
        invite.setQuota(1);
        invite.setUsedCount(0);
        invite.setStatus("ACTIVE");
        invite.setExpireAt(LocalDateTime.now().plusDays(30));
        invite.setCreatedAt(LocalDateTime.now());
        inviteCodeMapper.insert(invite);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("code", code);
        data.put("link", appProperties.getInviteBaseUrl() + code);
        data.put("remainQuotaToday", Math.max(0, dailyQuota - used.intValue()));
        return data;
    }

    public PageResult<Map<String, Object>> myInvites(long page, long pageSize) {
        Long userId = AuthContext.requireUserId();
        Page<InviteCode> p = inviteCodeMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<InviteCode>()
                        .eq(InviteCode::getOwnerUserId, userId)
                        .orderByDesc(InviteCode::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (InviteCode code : p.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", code.getId());
            row.put("code", code.getCode());
            row.put("quota", code.getQuota());
            row.put("usedCount", code.getUsedCount());
            row.put("status", code.getStatus());
            row.put("expireAt", code.getExpireAt());
            row.put("createdAt", code.getCreatedAt());
            InviteRelation rel = inviteRelationMapper.selectOne(new LambdaQueryWrapper<InviteRelation>()
                    .eq(InviteRelation::getInviteCodeId, code.getId())
                    .last("LIMIT 1"));
            row.put("inviteeId", rel == null ? null : rel.getInviteeId());
            list.add(row);
        }
        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    private String randomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
        }
        return sb.toString();
    }
}
