package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminAccountService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/admins")
@RequiredArgsConstructor
public class AdminAccountController {

    private final AdminAccountService adminAccountService;

    @GetMapping
    @RequireAdminPerm("admin:read")
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status) {
        return ApiResponse.ok(adminAccountService.page(page, pageSize, keyword, status));
    }

    @GetMapping("/{id}")
    @RequireAdminPerm("admin:read")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(adminAccountService.detail(id));
    }

    @PostMapping
    @RequireAdminPerm("admin:write")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminAccountService.create(body));
    }

    @PutMapping("/{id}")
    @RequireAdminPerm("admin:write")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminAccountService.update(id, body));
    }

    @PostMapping("/{id}/reset-password")
    @RequireAdminPerm("admin:write")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminAccountService.resetPassword(id, body == null ? null : body.get("newPassword"));
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/disable")
    @RequireAdminPerm("admin:write")
    public ApiResponse<Void> disable(@PathVariable Long id) {
        adminAccountService.disable(id);
        return ApiResponse.ok();
    }

    @PostMapping("/{id}/enable")
    @RequireAdminPerm("admin:write")
    public ApiResponse<Void> enable(@PathVariable Long id) {
        adminAccountService.enable(id);
        return ApiResponse.ok();
    }
}
