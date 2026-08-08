package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianghu.ling.admin.domain.AdminUser;
import com.jianghu.ling.admin.mapper.AdminUserMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.feedback.domain.UserFeedback;
import com.jianghu.ling.feedback.dto.UpdateFeedbackStatusRequest;
import com.jianghu.ling.feedback.mapper.UserFeedbackMapper;
import com.jianghu.ling.feedback.service.FeedbackStatusRules;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.user.domain.User;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.mapper.UserMapper;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminFeedbackService {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");

    private final UserFeedbackMapper userFeedbackMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserMapper userMapper;
    private final AdminUserMapper adminUserMapper;
    private final AuditService auditService;
    private final ObjectMapper objectMapper;

    public PageResult<Map<String, Object>> page(long page, long pageSize,
                                                 String status, String type, String keyword) {
        LambdaQueryWrapper<UserFeedback> q = new LambdaQueryWrapper<UserFeedback>()
                .orderByDesc(UserFeedback::getId);
        if (StringUtils.hasText(status)) {
            String s = status.trim().toUpperCase();
            if (!FeedbackStatusRules.isValidStatus(s)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "status无效");
            }
            q.eq(UserFeedback::getStatus, s);
        }
        if (StringUtils.hasText(type)) {
            String t = type.trim().toUpperCase();
            if (!FeedbackStatusRules.isValidType(t)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "type无效");
            }
            q.eq(UserFeedback::getType, t);
        }
        if (StringUtils.hasText(keyword)) {
            applyKeyword(q, keyword.trim());
        }
        Page<UserFeedback> p = userFeedbackMapper.selectPage(new Page<>(page, pageSize), q);
        Map<Long, String> nickMap = loadNicknames(p.getRecords().stream()
                .map(UserFeedback::getUserId).collect(Collectors.toSet()));
        List<Map<String, Object>> list = p.getRecords().stream()
                .map(row -> toAdminListItem(row, nickMap.getOrDefault(row.getUserId(), "")))
                .toList();
        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    public Map<String, Object> detail(Long id) {
        UserFeedback row = requireFeedback(id);
        return toAdminDetail(row);
    }

    @Transactional
    public Map<String, Object> updateStatus(Long id, UpdateFeedbackStatusRequest req) {
        Long adminId = AuthContext.requireAdminId();
        if (req == null || !StringUtils.hasText(req.getStatus())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status必填");
        }
        String toStatus = req.getStatus().trim().toUpperCase();
        if (!FeedbackStatusRules.isValidStatus(toStatus)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "status无效");
        }
        UserFeedback row = requireFeedback(id);
        String fromStatus = row.getStatus();
        if (!FeedbackStatusRules.canTransit(fromStatus, toStatus)) {
            throw new BizException(ErrorCode.BIZ_RULE, "非法状态流转或已终态");
        }

        AdminUser admin = adminUserMapper.selectById(adminId);
        String adminName = admin == null ? null : admin.getDisplayName();
        LocalDateTime now = LocalDateTime.now(SHANGHAI);

        String remark = req.getHandleRemark();
        if (remark != null) {
            remark = remark.trim();
            if (remark.length() > 1000) {
                throw new BizException(ErrorCode.PARAM_INVALID, "handleRemark超长");
            }
            row.setHandleRemark(remark.isEmpty() ? null : remark);
        }

        row.setStatus(toStatus);
        row.setStatusChangedAt(now);
        row.setStatusChangedByAdminId(adminId);
        row.setUpdatedAt(now);

        List<Map<String, Object>> history = readHistory(row.getStatusHistoryJson());
        history.add(FeedbackStatusRules.historyEntry(
                fromStatus, toStatus, adminId, adminName, row.getHandleRemark(), now));
        row.setStatusHistoryJson(writeJson(history));
        userFeedbackMapper.updateById(row);

        Map<String, Object> auditPayload = new LinkedHashMap<>();
        auditPayload.put("feedbackId", id);
        auditPayload.put("fromStatus", fromStatus);
        auditPayload.put("toStatus", toStatus);
        auditPayload.put("adminId", adminId);
        auditPayload.put("at", now.toString());
        auditService.log("FEEDBACK_STATUS_CHANGE", writeJson(auditPayload));

        return toAdminDetail(row);
    }

    private void applyKeyword(LambdaQueryWrapper<UserFeedback> q, String keyword) {
        Set<Long> userIds = new HashSet<>();
        if (keyword.matches("\\d+")) {
            try {
                userIds.add(Long.parseLong(keyword));
            } catch (NumberFormatException ignored) {
                // ignore
            }
        }
        List<UserProfile> profiles = userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                .like(UserProfile::getNickname, keyword)
                .last("LIMIT 200"));
        for (UserProfile p : profiles) {
            userIds.add(p.getUserId());
        }
        q.and(w -> {
            w.like(UserFeedback::getTitle, keyword);
            if (!userIds.isEmpty()) {
                w.or().in(UserFeedback::getUserId, userIds);
            }
            w.or().apply("CAST(user_id AS CHAR) LIKE CONCAT('%', {0}, '%')", keyword);
        });
    }

    private UserFeedback requireFeedback(Long id) {
        UserFeedback row = userFeedbackMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return row;
    }

    private Map<String, Object> toAdminListItem(UserFeedback row, String nickname) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", row.getId());
        m.put("type", row.getType());
        m.put("title", row.getTitle());
        m.put("status", row.getStatus());
        m.put("submitterId", row.getUserId());
        m.put("submitterNickname", nickname == null ? "" : nickname);
        m.put("createdAt", row.getCreatedAt());
        m.put("updatedAt", row.getUpdatedAt());
        return m;
    }

    private Map<String, Object> toAdminDetail(UserFeedback row) {
        String nickname = resolveNickname(row.getUserId());
        Map<String, Object> m = toAdminListItem(row, nickname);
        m.put("content", row.getContent());
        m.put("contact", row.getContact());
        m.put("relatedRef", row.getRelatedRef());
        m.put("attachmentUrls", readUrlList(row.getAttachmentUrlsJson()));
        m.put("handleRemark", row.getHandleRemark());
        m.put("statusChangedAt", row.getStatusChangedAt());
        m.put("statusChangedByAdminId", row.getStatusChangedByAdminId());
        String adminName = null;
        if (row.getStatusChangedByAdminId() != null) {
            AdminUser admin = adminUserMapper.selectById(row.getStatusChangedByAdminId());
            adminName = admin == null ? null : admin.getDisplayName();
        }
        m.put("statusChangedByAdminName", adminName);
        m.put("statusHistory", readHistory(row.getStatusHistoryJson()));
        return m;
    }

    private String resolveNickname(Long userId) {
        if (userId == null) {
            return "";
        }
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId).last("LIMIT 1"));
        if (profile != null && StringUtils.hasText(profile.getNickname())) {
            return profile.getNickname();
        }
        User user = userMapper.selectById(userId);
        if (user != null && StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return "";
    }

    private Map<Long, String> loadNicknames(Set<Long> userIds) {
        Map<Long, String> map = new HashMap<>();
        if (userIds == null || userIds.isEmpty()) {
            return map;
        }
        List<UserProfile> profiles = userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                .in(UserProfile::getUserId, userIds));
        for (UserProfile p : profiles) {
            if (StringUtils.hasText(p.getNickname())) {
                map.put(p.getUserId(), p.getNickname());
            }
        }
        Set<Long> missing = new HashSet<>(userIds);
        missing.removeAll(map.keySet());
        if (!missing.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(missing);
            for (User u : users) {
                if (StringUtils.hasText(u.getUsername())) {
                    map.putIfAbsent(u.getId(), u.getUsername());
                }
            }
        }
        for (Long id : userIds) {
            map.putIfAbsent(id, "");
        }
        return map;
    }

    private List<String> readUrlList(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            List<String> list = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return list == null ? List.of() : list;
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<Map<String, Object>> readHistory(String json) {
        if (!StringUtils.hasText(json)) {
            return new ArrayList<>();
        }
        try {
            List<Map<String, Object>> list = objectMapper.readValue(json,
                    new TypeReference<List<Map<String, Object>>>() {});
            return list == null ? new ArrayList<>() : new ArrayList<>(list);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BizException(ErrorCode.INTERNAL, "序列化失败");
        }
    }
}
