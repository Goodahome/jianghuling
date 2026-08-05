package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.dto.AdminLoginRequest;
import com.jianghu.ling.admin.service.AdminAuthService;
import com.jianghu.ling.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody AdminLoginRequest req,
                                                  HttpServletRequest request) {
        return ApiResponse.ok(adminAuthService.login(req, request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        adminAuthService.logout();
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        return ApiResponse.ok(adminAuthService.me());
    }
}
