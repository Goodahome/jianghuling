package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminWalletAdminService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/wallet")
@RequiredArgsConstructor
public class AdminWalletController {

    private final AdminWalletAdminService adminWalletAdminService;

    @GetMapping("/ledgers")
    @RequireAdminPerm("wallet:read")
    public ApiResponse<PageResult<Map<String, Object>>> ledgers(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(adminWalletAdminService.pageLedgers(type, page, pageSize));
    }

    @GetMapping("/fee-summary")
    @RequireAdminPerm("wallet:read")
    public ApiResponse<Map<String, Object>> feeSummary() {
        return ApiResponse.ok(adminWalletAdminService.feeSummary());
    }
}
