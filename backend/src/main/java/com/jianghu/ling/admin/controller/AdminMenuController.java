package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminMenuService;
import com.jianghu.ling.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/menus")
@RequiredArgsConstructor
public class AdminMenuController {

    private final AdminMenuService adminMenuService;

    /** 仅需 Admin 登录；拦截器白名单放行 */
    @GetMapping("/tree")
    public ApiResponse<List<Map<String, Object>>> tree() {
        return ApiResponse.ok(adminMenuService.treeForCurrentUserPruned());
    }

    @GetMapping("/all")
    @RequireAdminPerm("menu:read")
    public ApiResponse<List<Map<String, Object>>> all() {
        return ApiResponse.ok(adminMenuService.treeAll());
    }

    @PostMapping
    @RequireAdminPerm("menu:write")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminMenuService.create(body));
    }

    @PutMapping("/{id}")
    @RequireAdminPerm("menu:write")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminMenuService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @RequireAdminPerm("menu:write")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminMenuService.delete(id);
        return ApiResponse.ok();
    }
}
