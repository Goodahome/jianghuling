package com.jianghu.ling.dispute.controller;

import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.dispute.service.DisputeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class DisputeController {

    private final DisputeService disputeService;

    @PostMapping("/api/v1/bounties/{bountyId}/disputes")
    public ApiResponse<Map<String, Object>> create(@PathVariable Long bountyId,
                                                   @RequestBody Map<String, Object> body) {
        String reason = body == null || body.get("reason") == null ? null : String.valueOf(body.get("reason"));
        String evidenceText = body == null || body.get("evidenceText") == null ? null : String.valueOf(body.get("evidenceText"));
        @SuppressWarnings("unchecked")
        List<String> urls = body == null ? null : (List<String>) body.get("evidenceUrls");
        return ApiResponse.ok(disputeService.create(bountyId, reason, urls, evidenceText));
    }

    @GetMapping("/api/v1/disputes/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(disputeService.detail(id));
    }

    @GetMapping("/api/v1/disputes/mine")
    public ApiResponse<PageResult<Map<String, Object>>> mine(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(disputeService.mine(page, pageSize));
    }
}
