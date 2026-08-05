package com.jianghu.ling.bounty.controller;

import com.jianghu.ling.bounty.service.BountyService;
import com.jianghu.ling.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/submissions")
@RequiredArgsConstructor
public class SubmissionController {

    private final BountyService bountyService;

    @GetMapping("/{submissionId}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long submissionId) {
        return ApiResponse.ok(bountyService.submissionDetail(submissionId));
    }
}
