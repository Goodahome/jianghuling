package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.dto.AssetAdjustRequest;
import com.jianghu.ling.admin.service.AdminUserService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.user.domain.LoginLog;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(adminUserService.page(keyword, status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(adminUserService.detail(id));
    }

    @PostMapping("/{id}/disable")
    public ApiResponse<Void> disable(@PathVariable Long id) {
        adminUserService.setStatus(id, "DISABLED");
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/enable")
    public ApiResponse<Void> enable(@PathVariable Long id) {
        adminUserService.setStatus(id, "ACTIVE");
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/ban")
    public ApiResponse<Void> ban(@PathVariable Long id) {
        adminUserService.setStatus(id, "BANNED");
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/unban")
    public ApiResponse<Void> unban(@PathVariable Long id) {
        adminUserService.setStatus(id, "ACTIVE");
        return ApiResponse.ok();
    }

    @PutMapping("/{id}/remark")
    public ApiResponse<Void> remark(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminUserService.remark(id, body.get("remark"));
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/assets/adjust")
    public ApiResponse<Void> adjust(@PathVariable Long id, @Valid @RequestBody AssetAdjustRequest req) {
        adminUserService.adjustAsset(id, req);
        return ApiResponse.ok();
    }

    @GetMapping("/{id}/login-logs")
    public ApiResponse<PageResult<LoginLog>> loginLogs(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(adminUserService.loginLogs(id, page, pageSize));
    }

    @GetMapping("/{id}/real-name")
    public ApiResponse<Map<String, Object>> realName(@PathVariable Long id) {
        return ApiResponse.ok(adminUserService.realName(id));
    }

    @PutMapping("/{id}/real-name")
    public ApiResponse<Map<String, Object>> updateRealName(@PathVariable Long id,
                                                           @RequestBody Map<String, String> body) {
        return ApiResponse.ok(adminUserService.updateRealNameStatus(id, body.get("status")));
    }
}
