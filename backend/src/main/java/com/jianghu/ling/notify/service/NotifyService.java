package com.jianghu.ling.notify.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.notify.domain.SiteMessage;
import com.jianghu.ling.notify.mapper.SiteMessageMapper;
import com.jianghu.ling.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class NotifyService {

    private final SiteMessageMapper siteMessageMapper;

    public void send(Long userId, String title, String content, String bizType, Long bizId) {
        SiteMessage msg = new SiteMessage();
        msg.setUserId(userId);
        msg.setTitle(title);
        msg.setContent(content);
        msg.setBizType(bizType);
        msg.setBizId(bizId);
        msg.setReadFlag(false);
        msg.setCreatedAt(LocalDateTime.now());
        siteMessageMapper.insert(msg);
    }

    public PageResult<Map<String, Object>> pageMine(long page, long pageSize, Boolean unreadOnly) {
        Long userId = AuthContext.requireUserId();
        LambdaQueryWrapper<SiteMessage> q = new LambdaQueryWrapper<SiteMessage>()
                .eq(SiteMessage::getUserId, userId)
                .eq(Boolean.TRUE.equals(unreadOnly), SiteMessage::getReadFlag, false)
                .orderByDesc(SiteMessage::getId);
        Page<SiteMessage> p = siteMessageMapper.selectPage(new Page<>(page, pageSize), q);
        return PageResult.of(p.getRecords().stream().map(this::toView).toList(), p.getTotal(), page, pageSize);
    }

    public Map<String, Object> unreadCount() {
        Long userId = AuthContext.requireUserId();
        long count = siteMessageMapper.selectCount(new LambdaQueryWrapper<SiteMessage>()
                .eq(SiteMessage::getUserId, userId)
                .eq(SiteMessage::getReadFlag, false));
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("count", count);
        return data;
    }

    @Transactional
    public Map<String, Object> detail(Long id) {
        Long userId = AuthContext.requireUserId();
        SiteMessage msg = siteMessageMapper.selectById(id);
        if (msg == null || !userId.equals(msg.getUserId())) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!Boolean.TRUE.equals(msg.getReadFlag())) {
            msg.setReadFlag(true);
            siteMessageMapper.updateById(msg);
        }
        return toView(msg);
    }

    @Transactional
    public Map<String, Object> markRead(Long id) {
        Long userId = AuthContext.requireUserId();
        SiteMessage msg = siteMessageMapper.selectById(id);
        if (msg == null || !userId.equals(msg.getUserId())) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        msg.setReadFlag(true);
        siteMessageMapper.updateById(msg);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", msg.getId());
        data.put("read", true);
        return data;
    }

    @Transactional
    public Map<String, Object> markAllRead() {
        Long userId = AuthContext.requireUserId();
        long unread = siteMessageMapper.selectCount(new LambdaQueryWrapper<SiteMessage>()
                .eq(SiteMessage::getUserId, userId)
                .eq(SiteMessage::getReadFlag, false));
        if (unread > 0) {
            siteMessageMapper.update(null, new LambdaUpdateWrapper<SiteMessage>()
                    .eq(SiteMessage::getUserId, userId)
                    .eq(SiteMessage::getReadFlag, false)
                    .set(SiteMessage::getReadFlag, true));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("updated", unread);
        return data;
    }

    private Map<String, Object> toView(SiteMessage msg) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", msg.getId());
        m.put("title", msg.getTitle());
        m.put("content", msg.getContent());
        m.put("read", Boolean.TRUE.equals(msg.getReadFlag()));
        m.put("createdAt", msg.getCreatedAt());
        m.put("bizType", msg.getBizType());
        m.put("bizId", msg.getBizId());
        return m;
    }
}
