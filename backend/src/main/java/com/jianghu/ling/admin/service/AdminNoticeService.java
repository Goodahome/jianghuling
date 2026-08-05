package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.cms.domain.Notice;
import com.jianghu.ling.cms.mapper.NoticeMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminNoticeService {

    private final NoticeMapper noticeMapper;
    private final AuditService auditService;

    public PageResult<Notice> page(long page, long pageSize) {
        Page<Notice> p = noticeMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Notice>().orderByDesc(Notice::getId));
        return PageResult.of(p.getRecords(), p.getTotal(), page, pageSize);
    }

    @Transactional
    public Notice create(Map<String, Object> body) {
        Notice notice = new Notice();
        apply(notice, body);
        if (!StringUtils.hasText(notice.getTitle()) || !StringUtils.hasText(notice.getContent())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "标题和内容必填");
        }
        if (!StringUtils.hasText(notice.getCategory())) {
            notice.setCategory("ANNOUNCE");
        }
        if (!StringUtils.hasText(notice.getStatus())) {
            notice.setStatus("PUBLISHED");
        }
        if (notice.getPinned() == null) {
            notice.setPinned(false);
        }
        notice.setCreatedAt(LocalDateTime.now());
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.insert(notice);
        auditService.log("NOTICE_CREATE", "id=" + notice.getId() + ", title=" + notice.getTitle());
        return notice;
    }

    @Transactional
    public Notice update(Long id, Map<String, Object> body) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        apply(notice, body);
        notice.setUpdatedAt(LocalDateTime.now());
        noticeMapper.updateById(notice);
        auditService.log("NOTICE_UPDATE", "id=" + id);
        return notice;
    }

    private void apply(Notice notice, Map<String, Object> body) {
        if (body == null) {
            return;
        }
        if (body.containsKey("title")) {
            notice.setTitle(String.valueOf(body.get("title")));
        }
        if (body.containsKey("category")) {
            notice.setCategory(String.valueOf(body.get("category")));
        }
        if (body.containsKey("content")) {
            notice.setContent(String.valueOf(body.get("content")));
        }
        if (body.containsKey("pinned")) {
            Object p = body.get("pinned");
            notice.setPinned(p instanceof Boolean ? (Boolean) p : Boolean.parseBoolean(String.valueOf(p)));
        }
        if (body.containsKey("status")) {
            notice.setStatus(String.valueOf(body.get("status")));
        }
    }
}
