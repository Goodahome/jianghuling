package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminSystemService;
import com.jianghu.ling.admin.service.AuditService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminSystemController {

    private final AdminSystemService adminSystemService;
    private final AuditService auditService;

    @GetMapping("/configs/system")
    @RequireAdminPerm("config:read")
    public ApiResponse<Map<String, Object>> getSystemConfig() {
        return ApiResponse.ok(adminSystemService.getSystemConfig());
    }

    @PutMapping("/configs/system")
    @RequireAdminPerm("config:write")
    public ApiResponse<Void> putSystemConfig(@RequestBody Map<String, Object> body) {
        adminSystemService.putSystemConfig(body);
        return ApiResponse.ok();
    }

    @GetMapping("/audit-logs")
    @RequireAdminPerm("audit:read")
    public ApiResponse<PageResult<Map<String, Object>>> auditLogs(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String operator,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String keyword) {
        return ApiResponse.ok(auditService.page(page, pageSize, operator, action, keyword));
    }
}
