package com.jianghu.ling.user.controller;

import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.user.dto.RealNameRequest;
import com.jianghu.ling.user.dto.UpdateProfileRequest;
import com.jianghu.ling.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/profile")
    public ApiResponse<Map<String, Object>> updateProfile(@RequestBody UpdateProfileRequest req) {
        return ApiResponse.ok(userService.updateProfile(req));
    }

    @PostMapping("/real-name")
    public ApiResponse<Map<String, Object>> realName(@Valid @RequestBody RealNameRequest req) {
        return ApiResponse.ok(userService.submitRealName(req));
    }

    @PostMapping("/invites")
    public ApiResponse<Map<String, Object>> createInvite() {
        return ApiResponse.ok(userService.createInvite());
    }

    @GetMapping("/invites")
    public ApiResponse<PageResult<Map<String, Object>>> invites(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(userService.myInvites(page, pageSize));
    }
}
