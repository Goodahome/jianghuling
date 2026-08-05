package com.jianghu.ling.notify.service;

import com.jianghu.ling.notify.domain.SiteMessage;
import com.jianghu.ling.notify.mapper.SiteMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
}
