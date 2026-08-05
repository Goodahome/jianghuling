package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminRoleAdminService;
import com.jianghu.ling.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/roles")
@RequiredArgsConstructor
public class AdminRoleController {

    private final AdminRoleAdminService adminRoleAdminService;

    @GetMapping
    @RequireAdminPerm("role:read")
    public ApiResponse<List<Map<String, Object>>> list() {
        return ApiResponse.ok(adminRoleAdminService.listRoles());
    }

    @GetMapping("/permission-catalog")
    @RequireAdminPerm("role:read")
    public ApiResponse<List<Map<String, Object>>> catalog() {
        return ApiResponse.ok(adminRoleAdminService.permissionCatalog());
    }

    @GetMapping("/{code}")
    @RequireAdminPerm("role:read")
    public ApiResponse<Map<String, Object>> detail(@PathVariable String code) {
        return ApiResponse.ok(adminRoleAdminService.getByCode(code));
    }

    @PutMapping("/{code}/permissions")
    @RequireAdminPerm("role:write")
    public ApiResponse<Map<String, Object>> putPermissions(@PathVariable String code,
                                                           @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminRoleAdminService.putPermissions(code, body));
    }
}
