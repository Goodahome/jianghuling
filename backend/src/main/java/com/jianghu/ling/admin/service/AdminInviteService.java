package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.user.domain.InviteCode;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.mapper.InviteCodeMapper;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class AdminInviteService {

    private final InviteCodeMapper inviteCodeMapper;
    private final UserProfileMapper userProfileMapper;

    public PageResult<Map<String, Object>> page(long page, long pageSize) {
        Page<InviteCode> p = inviteCodeMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<InviteCode>().orderByDesc(InviteCode::getId));
        List<Map<String, Object>> list = new ArrayList<>();
        for (InviteCode code : p.getRecords()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("id", code.getId());
            row.put("code", code.getCode());
            row.put("ownerUserId", code.getOwnerUserId());
            row.put("ownerNickname", resolveOwnerNickname(code.getOwnerUserId()));
            row.put("quota", code.getQuota());
            row.put("usedCount", code.getUsedCount());
            row.put("status", code.getStatus());
            row.put("expireAt", code.getExpireAt());
            row.put("createdAt", code.getCreatedAt());
            list.add(row);
        }
        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    @Transactional
    public Map<String, Object> batchCreate(Integer count, Integer quota, Long ownerUserId, Integer expireDays) {
        int n = count == null || count < 1 ? 1 : Math.min(count, 100);
        int q = quota == null || quota < 1 ? 1 : quota;
        int days = expireDays == null || expireDays < 1 ? 365 : expireDays;
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            InviteCode invite = new InviteCode();
            invite.setCode(randomCode());
            invite.setOwnerUserId(ownerUserId);
            invite.setQuota(q);
            invite.setUsedCount(0);
            invite.setStatus("ACTIVE");
            invite.setExpireAt(LocalDateTime.now().plusDays(days));
            invite.setCreatedAt(LocalDateTime.now());
            inviteCodeMapper.insert(invite);
            codes.add(invite.getCode());
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", n);
        data.put("codes", codes);
        return data;
    }

    @Transactional
    public void invalidate(Long id) {
        InviteCode invite = inviteCodeMapper.selectById(id);
        if (invite == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "邀请码不存在");
        }
        invite.setStatus("INVALID");
        inviteCodeMapper.updateById(invite);
    }

    private String resolveOwnerNickname(Long ownerUserId) {
        if (ownerUserId == null || ownerUserId <= 0) {
            return "平台";
        }
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, ownerUserId)
                .last("LIMIT 1"));
        return profile == null ? String.valueOf(ownerUserId) : profile.getNickname();
    }

    private String randomCode() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        for (int attempt = 0; attempt < 20; attempt++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) {
                sb.append(chars.charAt(ThreadLocalRandom.current().nextInt(chars.length())));
            }
            String code = sb.toString();
            Long exists = inviteCodeMapper.selectCount(new LambdaQueryWrapper<InviteCode>()
                    .eq(InviteCode::getCode, code));
            if (exists == 0) {
                return code;
            }
        }
        throw new BizException(ErrorCode.INTERNAL, "生成邀请码失败，请重试");
    }
}
