package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminInviteService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/invites")
@RequiredArgsConstructor
public class AdminInviteController {

    private final AdminInviteService adminInviteService;

    @GetMapping
    @RequireAdminPerm("invite:read")
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(adminInviteService.page(page, pageSize));
    }

    @PostMapping
    @RequireAdminPerm("invite:write")
    public ApiResponse<Map<String, Object>> create(@RequestBody(required = false) Map<String, Object> body) {
        Integer count = body == null ? null : toInt(body.get("count"));
        Integer quota = body == null ? null : toInt(body.get("quota"));
        Long ownerUserId = body == null ? null : toLong(body.get("ownerUserId"));
        Integer expireDays = body == null ? null : toInt(body.get("expireDays"));
        return ApiResponse.ok(adminInviteService.batchCreate(count, quota, ownerUserId, expireDays));
    }

    @PostMapping("/{id}/invalidate")
    @RequireAdminPerm("invite:write")
    public ApiResponse<Void> invalidate(@PathVariable Long id) {
        adminInviteService.invalidate(id);
        return ApiResponse.ok();
    }

    private Integer toInt(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).intValue();
        }
        return Integer.parseInt(String.valueOf(v));
    }

    private Long toLong(Object v) {
        if (v == null) {
            return null;
        }
        if (v instanceof Number) {
            return ((Number) v).longValue();
        }
        String s = String.valueOf(v);
        if (s.isBlank() || "null".equalsIgnoreCase(s)) {
            return null;
        }
        return Long.parseLong(s);
    }
}
