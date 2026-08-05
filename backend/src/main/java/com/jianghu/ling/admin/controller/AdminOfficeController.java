package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminOfficeService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/offices")
@RequiredArgsConstructor
public class AdminOfficeController {

    private final AdminOfficeService adminOfficeService;

    @GetMapping("/defs")
    @RequireAdminPerm("office:read")
    public ApiResponse<List<Map<String, Object>>> defs() {
        return ApiResponse.ok(adminOfficeService.listDefs());
    }

    @PutMapping("/defs")
    @RequireAdminPerm("office:write")
    public ApiResponse<List<Map<String, Object>>> putDefs(@RequestBody Object body) {
        return ApiResponse.ok(adminOfficeService.putDefs(body));
    }

    @GetMapping("/applications")
    @RequireAdminPerm("office:read")
    public ApiResponse<PageResult<Map<String, Object>>> applications(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(adminOfficeService.pageApplications(page, pageSize));
    }

    @PostMapping("/applications/{id}/approve")
    @RequireAdminPerm("office:write")
    public ApiResponse<Void> approve(@PathVariable Long id) {
        adminOfficeService.approve(id);
        return ApiResponse.ok();
    }

    @PostMapping("/applications/{id}/reject")
    @RequireAdminPerm("office:write")
    public ApiResponse<Void> reject(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        adminOfficeService.reject(id, reason);
        return ApiResponse.ok();
    }

    @PostMapping("/holders/{id}/suspend")
    @RequireAdminPerm("office:write")
    public ApiResponse<Void> suspend(@PathVariable Long id) {
        adminOfficeService.suspendHolder(id);
        return ApiResponse.ok();
    }

    @PostMapping("/holders/{id}/revoke")
    @RequireAdminPerm("office:write")
    public ApiResponse<Void> revoke(@PathVariable Long id) {
        adminOfficeService.revokeHolder(id);
        return ApiResponse.ok();
    }
}
