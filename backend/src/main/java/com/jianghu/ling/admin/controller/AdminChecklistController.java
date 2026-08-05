package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminChecklistService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/checklist-templates")
@RequiredArgsConstructor
public class AdminChecklistController {

    private final AdminChecklistService adminChecklistService;

    @GetMapping
    @RequireAdminPerm("checklist:read")
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "50") long pageSize) {
        return ApiResponse.ok(adminChecklistService.page(page, pageSize));
    }

    @PostMapping
    @RequireAdminPerm("checklist:write")
    public ApiResponse<Map<String, Object>> create(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminChecklistService.create(body));
    }

    @PutMapping("/{id}")
    @RequireAdminPerm("checklist:write")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminChecklistService.update(id, body));
    }

    @DeleteMapping("/{id}")
    @RequireAdminPerm("checklist:write")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminChecklistService.delete(id);
        return ApiResponse.ok();
    }
}
