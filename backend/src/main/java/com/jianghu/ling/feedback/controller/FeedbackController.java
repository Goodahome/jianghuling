package com.jianghu.ling.feedback.controller;

import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.feedback.dto.CreateFeedbackRequest;
import com.jianghu.ling.feedback.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {

    private final FeedbackService feedbackService;

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody CreateFeedbackRequest request) {
        return ApiResponse.ok(feedbackService.create(request));
    }

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(feedbackService.myPage(status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(feedbackService.myDetail(id));
    }
}
