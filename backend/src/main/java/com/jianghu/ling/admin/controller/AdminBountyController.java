package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminBountyService;
import com.jianghu.ling.bounty.domain.BountyMessage;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.review.dto.ReviewRequest;
import com.jianghu.ling.review.service.ReviewService;
import com.jianghu.ling.security.AuthContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminBountyController {

    private final AdminBountyService adminBountyService;
    private final ReviewService reviewService;

    @GetMapping("/bounties")
    @RequireAdminPerm("bounty:read")
    public ApiResponse<PageResult<Map<String, Object>>> page(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(adminBountyService.page(status, keyword, page, pageSize));
    }

    @GetMapping("/bounties/{id}")
    @RequireAdminPerm("bounty:read")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(adminBountyService.detail(id));
    }

    @PostMapping("/bounties/{id}/force-close")
    @RequireAdminPerm("bounty:write")
    public ApiResponse<Map<String, Object>> forceClose(@PathVariable Long id,
                                                       @RequestBody(required = false) Map<String, String> body) {
        String reason = body == null ? null : body.get("reason");
        return ApiResponse.ok(adminBountyService.forceClose(id, reason));
    }

    @PostMapping("/bounty-reviews/{bountyId}")
    @RequireAdminPerm("bounty:review")
    public ApiResponse<Map<String, Object>> reviewBounty(@PathVariable Long bountyId,
                                                         @Valid @RequestBody ReviewRequest req) {
        Long adminId = AuthContext.requireAdminId();
        return ApiResponse.ok(reviewService.reviewBounty(bountyId, req.getResult(), req.getReason(), true, adminId));
    }

    @PostMapping("/submission-reviews/{submissionId}")
    @RequireAdminPerm("submission:review")
    public ApiResponse<Map<String, Object>> reviewSubmission(@PathVariable Long submissionId,
                                                             @Valid @RequestBody ReviewRequest req) {
        Long adminId = AuthContext.requireAdminId();
        return ApiResponse.ok(reviewService.reviewSubmission(submissionId, req.getResult(), req.getReason(), true, adminId));
    }

    @GetMapping("/bounties/{id}/messages")
    @RequireAdminPerm("bounty:read")
    public ApiResponse<PageResult<BountyMessage>> messages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "50") long pageSize) {
        return ApiResponse.ok(adminBountyService.messages(id, page, pageSize));
    }
}
