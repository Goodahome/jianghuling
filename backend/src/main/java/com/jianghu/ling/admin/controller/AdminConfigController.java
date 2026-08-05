package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminConfigService;
import com.jianghu.ling.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/configs")
@RequiredArgsConstructor
public class AdminConfigController {

    private final AdminConfigService adminConfigService;

    @GetMapping("/levels")
    @RequireAdminPerm("config:read")
    public ApiResponse<List<Map<String, Object>>> getLevels() {
        return ApiResponse.ok(adminConfigService.getLevels());
    }

    @PutMapping("/levels")
    @RequireAdminPerm("config:write")
    public ApiResponse<List<Map<String, Object>>> putLevels(@RequestBody Object body) {
        return ApiResponse.ok(adminConfigService.putLevels(body));
    }

    @GetMapping("/ranks")
    @RequireAdminPerm("config:read")
    public ApiResponse<Map<String, Object>> getRanks() {
        return ApiResponse.ok(adminConfigService.getRanks());
    }

    @PutMapping("/ranks")
    @RequireAdminPerm("config:write")
    public ApiResponse<Map<String, Object>> putRanks(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminConfigService.putRanks(body));
    }

    @GetMapping("/growth")
    @RequireAdminPerm("config:read")
    public ApiResponse<Map<String, Object>> getGrowth() {
        return ApiResponse.ok(adminConfigService.getGrowth());
    }

    @PutMapping("/growth")
    @RequireAdminPerm("config:write")
    public ApiResponse<Map<String, Object>> putGrowth(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminConfigService.putGrowth(body));
    }

    @GetMapping("/reward-suggest")
    @RequireAdminPerm("config:read")
    public ApiResponse<Map<String, Object>> getRewardSuggest() {
        return ApiResponse.ok(adminConfigService.getRewardSuggest());
    }

    @PutMapping("/reward-suggest")
    @RequireAdminPerm("config:write")
    public ApiResponse<Map<String, Object>> putRewardSuggest(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminConfigService.putRewardSuggest(body));
    }
}
