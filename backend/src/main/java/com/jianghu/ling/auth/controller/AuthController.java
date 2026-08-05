package com.jianghu.ling.auth.controller;

import com.jianghu.ling.auth.dto.InviteValidateRequest;
import com.jianghu.ling.auth.dto.LoginRequest;
import com.jianghu.ling.auth.dto.RegisterRequest;
import com.jianghu.ling.auth.dto.SmsSendRequest;
import com.jianghu.ling.auth.service.AuthService;
import com.jianghu.ling.common.api.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sms/send")
    public ApiResponse<Map<String, Object>> sendSms(@Valid @RequestBody SmsSendRequest req) {
        return ApiResponse.ok(authService.sendSms(req.getPhone(), req.getScene()));
    }

    @PostMapping("/invite/validate")
    public ApiResponse<Map<String, Object>> validateInvite(@Valid @RequestBody InviteValidateRequest req) {
        return ApiResponse.ok(authService.validateInvite(req.getInviteCode()));
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req, HttpServletRequest request) {
        return ApiResponse.ok(authService.register(req, request));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        return ApiResponse.ok(authService.login(req, request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok();
    }

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        return ApiResponse.ok(authService.me());
    }
}
