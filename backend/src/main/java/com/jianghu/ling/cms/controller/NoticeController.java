package com.jianghu.ling.cms.controller;

import com.jianghu.ling.cms.domain.Notice;
import com.jianghu.ling.cms.service.NoticeService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public ApiResponse<PageResult<Notice>> page(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(noticeService.page(category, page, pageSize));
    }

    @GetMapping("/top")
    public ApiResponse<List<Notice>> top(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "3") int limit) {
        return ApiResponse.ok(noticeService.top(category, limit));
    }

    @GetMapping("/{id}")
    public ApiResponse<Notice> detail(@PathVariable Long id) {
        return ApiResponse.ok(noticeService.detail(id));
    }
}
