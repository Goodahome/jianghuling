package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminLordService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/lord")
@RequiredArgsConstructor
public class AdminLordController {

    private final AdminLordService adminLordService;

    @GetMapping
    @RequireAdminPerm("lord:read")
    public ApiResponse<Map<String, Object>> current() {
        return ApiResponse.ok(adminLordService.current());
    }

    @PostMapping("/dismiss")
    @RequireAdminPerm("lord:write")
    public ApiResponse<Void> dismiss(@RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        adminLordService.dismiss(reason);
        return ApiResponse.ok();
    }

    @GetMapping("/applications")
    @RequireAdminPerm("lord:read")
    public ApiResponse<PageResult<Map<String, Object>>> applications(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(adminLordService.pageApplications(page, pageSize));
    }

    @PostMapping("/applications/{id}/approve")
    @RequireAdminPerm("lord:write")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        adminLordService.approve(id);
        return ApiResponse.ok();
    }

    @PostMapping("/applications/{id}/reject")
    @RequireAdminPerm("lord:write")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        adminLordService.reject(id, reason);
        return ApiResponse.ok();
    }
}
