package com.jianghu.ling.cms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.cms.domain.Notice;
import com.jianghu.ling.cms.mapper.NoticeMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeService {

    private final NoticeMapper noticeMapper;

    public PageResult<Notice> page(String category, long page, long pageSize) {
        Page<Notice> p = noticeMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, "PUBLISHED")
                        .eq(StringUtils.hasText(category), Notice::getCategory, category)
                        .orderByDesc(Notice::getPinned)
                        .orderByDesc(Notice::getId));
        return PageResult.of(p.getRecords(), p.getTotal(), page, pageSize);
    }

    public Notice detail(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null || !"PUBLISHED".equals(notice.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return notice;
    }

    public List<Notice> top(String category, int limit) {
        return noticeMapper.selectList(new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, "PUBLISHED")
                .eq(StringUtils.hasText(category), Notice::getCategory, category)
                .eq(Notice::getPinned, true)
                .orderByDesc(Notice::getId)
                .last("LIMIT " + Math.max(1, Math.min(limit, 20))));
    }
}
