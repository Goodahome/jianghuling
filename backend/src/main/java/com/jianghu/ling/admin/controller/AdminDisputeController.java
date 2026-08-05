package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminDisputeService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/disputes")
@RequiredArgsConstructor
public class AdminDisputeController {

    private final AdminDisputeService adminDisputeService;

    @GetMapping
    @RequireAdminPerm("dispute:read")
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(adminDisputeService.page(page, pageSize));
    }

    @GetMapping("/{id}")
    @RequireAdminPerm("dispute:read")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(adminDisputeService.detail(id));
    }

    @PostMapping("/{id}/verdict")
    @RequireAdminPerm("dispute:verdict")
    public ApiResponse<Void> verdict(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        adminDisputeService.verdict(id, body);
        return ApiResponse.ok();
    }
}
