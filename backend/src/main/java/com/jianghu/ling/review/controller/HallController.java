package com.jianghu.ling.review.controller;

import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.review.domain.ReviewRecord;
import com.jianghu.ling.review.dto.ReviewRequest;
import com.jianghu.ling.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/hall")
@RequiredArgsConstructor
public class HallController {

    private final ReviewService reviewService;

    @GetMapping("/bounty-reviews")
    public ApiResponse<PageResult<Map<String, Object>>> bountyReviews(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(reviewService.pendingBounties(status, page, pageSize));
    }

    @PostMapping("/bounty-reviews/{bountyId}")
    public ApiResponse<Map<String, Object>> reviewBounty(@PathVariable Long bountyId,
                                                         @Valid @RequestBody ReviewRequest req) {
        return ApiResponse.ok(reviewService.reviewBounty(bountyId, req.getResult(), req.getReason(), false, null));
    }

    @GetMapping("/submission-reviews")
    public ApiResponse<PageResult<Map<String, Object>>> submissionReviews(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(reviewService.pendingSubmissions(status, page, pageSize));
    }

    @PostMapping("/submission-reviews/{submissionId}")
    public ApiResponse<Map<String, Object>> reviewSubmission(@PathVariable Long submissionId,
                                                             @Valid @RequestBody ReviewRequest req) {
        return ApiResponse.ok(reviewService.reviewSubmission(submissionId, req.getResult(), req.getReason(), false, null));
    }

    @GetMapping("/my-actions")
    public ApiResponse<PageResult<ReviewRecord>> myActions(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(reviewService.myActions(page, pageSize));
    }
}
