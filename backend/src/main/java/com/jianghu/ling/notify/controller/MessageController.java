package com.jianghu.ling.notify.controller;

import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.notify.service.NotifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final NotifyService notifyService;

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) Boolean unreadOnly) {
        return ApiResponse.ok(notifyService.pageMine(page, pageSize, unreadOnly));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Object>> unreadCount() {
        return ApiResponse.ok(notifyService.unreadCount());
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(notifyService.detail(id));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Map<String, Object>> read(@PathVariable Long id) {
        return ApiResponse.ok(notifyService.markRead(id));
    }

    @PostMapping("/read-all")
    public ApiResponse<Map<String, Object>> readAll() {
        return ApiResponse.ok(notifyService.markAllRead());
    }
}
