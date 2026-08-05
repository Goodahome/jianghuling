package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminNoticeService;
import com.jianghu.ling.cms.domain.Notice;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final AdminNoticeService adminNoticeService;

    @GetMapping
    @RequireAdminPerm("notice:read")
    public ApiResponse<PageResult<Notice>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(adminNoticeService.page(page, pageSize));
    }

    @PostMapping
    @RequireAdminPerm("notice:write")
    public ApiResponse<Notice> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminNoticeService.create(body));
    }

    @PutMapping("/{id}")
    @RequireAdminPerm("notice:write")
    public ApiResponse<Notice> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminNoticeService.update(id, body));
    }
}
