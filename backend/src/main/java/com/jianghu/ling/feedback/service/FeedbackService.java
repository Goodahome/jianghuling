package com.jianghu.ling.feedback.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.feedback.domain.UserFeedback;
import com.jianghu.ling.feedback.dto.CreateFeedbackRequest;
import com.jianghu.ling.feedback.mapper.UserFeedbackMapper;
import com.jianghu.ling.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
    private static final DateTimeFormatter DAY_FMT = DateTimeFormatter.BASIC_ISO_DATE;
    private static final int MAX_ATTACHMENTS = 3;

    private final UserFeedbackMapper userFeedbackMapper;
    private final ConfigService configService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> create(CreateFeedbackRequest req) {
        Long userId = AuthContext.requireUserId();
        if (req == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String type = req.getType() == null ? null : req.getType().trim().toUpperCase();
        if (!FeedbackStatusRules.isValidType(type)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "type无效");
        }
        String title = trimToNull(req.getTitle());
        String content = trimToNull(req.getContent());
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "标题和内容必填");
        }
        if (title.length() > 100 || content.length() > 2000) {
            throw new BizException(ErrorCode.PARAM_INVALID, "标题或内容超长");
        }
        String contact = trimToNull(req.getContact());
        if (contact != null && contact.length() > 64) {
            throw new BizException(ErrorCode.PARAM_INVALID, "contact超长");
        }
        String relatedRef = trimToNull(req.getRelatedRef());
        if (relatedRef != null && relatedRef.length() > 128) {
            throw new BizException(ErrorCode.PARAM_INVALID, "relatedRef超长");
        }
        List<String> urls = normalizeAttachmentUrls(req.getAttachmentUrls());

        consumeRateLimit(userId);

        LocalDateTime now = LocalDateTime.now(SHANGHAI);
        UserFeedback row = new UserFeedback();
        row.setUserId(userId);
        row.setType(type);
        row.setTitle(title);
        row.setContent(content);
        row.setContact(contact);
        row.setRelatedRef(relatedRef);
        row.setAttachmentUrlsJson(writeJson(urls));
        row.setStatus("NEW");
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        row.setStatusHistoryJson(writeJson(List.of(
                FeedbackStatusRules.historyEntry(null, "NEW", null, null, null, now))));
        userFeedbackMapper.insert(row);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", row.getId());
        data.put("type", row.getType());
        data.put("title", row.getTitle());
        data.put("status", row.getStatus());
        data.put("createdAt", row.getCreatedAt());
        return data;
    }

    public PageResult<Map<String, Object>> myPage(String status, long page, long pageSize) {
        Long userId = AuthContext.requireUserId();
        LambdaQueryWrapper<UserFeedback> q = new LambdaQueryWrapper<UserFeedback>()
                .eq(UserFeedback::getUserId, userId)
                .orderByDesc(UserFeedback::getId);
        if (StringUtils.hasText(status)) {
            String s = status.trim().toUpperCase();
            if (!FeedbackStatusRules.isValidStatus(s)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "status无效");
            }
            q.eq(UserFeedback::getStatus, s);
        }
        Page<UserFeedback> p = userFeedbackMapper.selectPage(new Page<>(page, pageSize), q);
        return PageResult.of(p.getRecords().stream().map(this::toUserListItem).toList(),
                p.getTotal(), page, pageSize);
    }

    public Map<String, Object> myDetail(Long id) {
        Long userId = AuthContext.requireUserId();
        UserFeedback row = userFeedbackMapper.selectById(id);
        if (row == null || !userId.equals(row.getUserId())) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return toUserDetail(row);
    }

    private void consumeRateLimit(Long userId) {
        int cooldown = configService.getInt("feedback.cooldownSeconds", 60);
        String cdKey = cooldownKey(userId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(cdKey))) {
            throw new BizException(ErrorCode.FEEDBACK_COOLDOWN);
        }
        int dayLimit = configService.getInt("feedback.dailyLimit", 10);
        String dayKey = dayKey(userId);
        Long count = redisTemplate.opsForValue().increment(dayKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(dayKey, Duration.ofDays(2));
        }
        if (count != null && count > dayLimit) {
            redisTemplate.opsForValue().decrement(dayKey);
            throw new BizException(ErrorCode.FEEDBACK_DAILY_LIMIT);
        }
        redisTemplate.opsForValue().set(cdKey, "1", Duration.ofSeconds(Math.max(1, cooldown)));
    }

    private String cooldownKey(Long userId) {
        return "feedback:cd:" + userId;
    }

    private String dayKey(Long userId) {
        String day = LocalDate.now(SHANGHAI).format(DAY_FMT);
        return "feedback:day:" + day + ":" + userId;
    }

    private List<String> normalizeAttachmentUrls(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return Collections.emptyList();
        }
        if (raw.size() > MAX_ATTACHMENTS) {
            throw new BizException(ErrorCode.PARAM_INVALID, "附件最多3个");
        }
        List<String> urls = new ArrayList<>();
        for (String u : raw) {
            if (!StringUtils.hasText(u)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "附件URL非法");
            }
            String url = u.trim();
            if (!url.startsWith("/files/")) {
                throw new BizException(ErrorCode.PARAM_INVALID, "附件URL非法");
            }
            urls.add(url);
        }
        return urls;
    }

    private Map<String, Object> toUserListItem(UserFeedback row) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", row.getId());
        m.put("type", row.getType());
        m.put("title", row.getTitle());
        m.put("status", row.getStatus());
        m.put("createdAt", row.getCreatedAt());
        m.put("updatedAt", row.getUpdatedAt());
        return m;
    }

    private Map<String, Object> toUserDetail(UserFeedback row) {
        Map<String, Object> m = toUserListItem(row);
        m.put("content", row.getContent());
        m.put("contact", row.getContact());
        m.put("relatedRef", row.getRelatedRef());
        m.put("attachmentUrls", readUrlList(row.getAttachmentUrlsJson()));
        return m;
    }

    private List<String> readUrlList(String json) {
        if (!StringUtils.hasText(json)) {
            return Collections.emptyList();
        }
        try {
            List<String> list = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return list == null ? Collections.emptyList() : list;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new BizException(ErrorCode.INTERNAL, "序列化失败");
        }
    }

    private static String trimToNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
