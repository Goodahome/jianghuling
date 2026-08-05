package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminWarrantConfigService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/warrant-field-configs")
@RequiredArgsConstructor
public class AdminWarrantConfigController {

    private final AdminWarrantConfigService adminWarrantConfigService;

    @GetMapping
    @RequireAdminPerm("warrant_config:read")
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(required = false) String templateCode,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "100") long pageSize) {
        return ApiResponse.ok(adminWarrantConfigService.page(templateCode, page, pageSize));
    }

    @PostMapping
    @RequireAdminPerm("warrant_config:write")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminWarrantConfigService.create(body));
    }

    @PutMapping("/{id}")
    @RequireAdminPerm("warrant_config:write")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminWarrantConfigService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @RequireAdminPerm("warrant_config:write")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminWarrantConfigService.delete(id);
        return ApiResponse.ok();
    }
}
