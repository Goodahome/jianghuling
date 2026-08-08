package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.review.dto.ReviewRequest;
import com.jianghu.ling.review.service.ReviewService;
import com.jianghu.ling.security.AuthContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Admin 独立成果审核入口（api.md §16.12，path=/admin/submission-reviews）。
 */
@RestController
@RequestMapping("/api/v1/admin/submission-reviews")
@RequiredArgsConstructor
public class AdminSubmissionReviewController {

    private final ReviewService reviewService;

    @GetMapping
    @RequireAdminPerm("submission:read")
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long bountyId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(reviewService.adminSubmissionReviews(status, bountyId, keyword, page, pageSize));
    }

    @GetMapping("/{submissionId}")
    @RequireAdminPerm("submission:read")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long submissionId) {
        return ApiResponse.ok(reviewService.adminSubmissionDetail(submissionId));
    }

    @PostMapping("/{submissionId}")
    @RequireAdminPerm("submission:review")
    public ApiResponse<Map<String, Object>> review(@PathVariable Long submissionId,
                                                   @Valid @RequestBody ReviewRequest req) {
        Long adminId = AuthContext.requireAdminId();
        return ApiResponse.ok(reviewService.reviewSubmission(
                submissionId, req.getResult(), req.getReason(), true, adminId));
    }
}
