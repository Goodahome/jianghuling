package com.jianghu.ling.bounty.controller;

import com.jianghu.ling.bounty.domain.BountyMessage;
import com.jianghu.ling.bounty.dto.CreateBountyRequest;
import com.jianghu.ling.bounty.dto.MessageRequest;
import com.jianghu.ling.bounty.dto.RepublishBountyRequest;
import com.jianghu.ling.bounty.dto.SubmitRequest;
import com.jianghu.ling.bounty.service.BountyService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bounties")
@RequiredArgsConstructor
public class BountyController {

    private final BountyService bountyService;

    @GetMapping
    public ApiResponse<PageResult<Map<String, Object>>> plaza(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(bountyService.plaza(type, district, status, keyword, page, pageSize));
    }

    @GetMapping("/mine/published")
    public ApiResponse<PageResult<Map<String, Object>>> minePublished(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(bountyService.minePublished(status, page, pageSize));
    }

    @GetMapping("/mine/claimed")
    public ApiResponse<PageResult<Map<String, Object>>> mineClaimed(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(bountyService.mineClaimed(status, page, pageSize));
    }

    @GetMapping("/{id}")
    public ApiResponse<Map<String, Object>> detail(@PathVariable Long id) {
        return ApiResponse.ok(bountyService.detail(id));
    }

    @PostMapping
    public ApiResponse<Map<String, Object>> create(@Valid @RequestBody CreateBountyRequest req) {
        return ApiResponse.ok(bountyService.create(req));
    }

    @GetMapping("/{id}/republish-draft")
    public ApiResponse<Map<String, Object>> republishDraft(@PathVariable Long id) {
        return ApiResponse.ok(bountyService.republishDraft(id));
    }

    @PostMapping("/{id}/republish")
    public ApiResponse<Map<String, Object>> republish(@PathVariable Long id,
                                                      @RequestBody(required = false) RepublishBountyRequest req) {
        return ApiResponse.ok(bountyService.republish(id, req));
    }

    @PostMapping("/{id}/claims")
    public ApiResponse<Map<String, Object>> claim(@PathVariable Long id) {
        return ApiResponse.ok(bountyService.claim(id));
    }

    @GetMapping("/{id}/messages")
    public ApiResponse<PageResult<BountyMessage>> messages(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "50") long pageSize) {
        return ApiResponse.ok(bountyService.messages(id, page, pageSize));
    }

    @PostMapping("/{id}/messages")
    public ApiResponse<BountyMessage> sendMessage(@PathVariable Long id, @Valid @RequestBody MessageRequest req) {
        return ApiResponse.ok(bountyService.sendMessage(id, req.getContent()));
    }

    @PostMapping("/{id}/submissions")
    public ApiResponse<Map<String, Object>> submit(@PathVariable Long id, @Valid @RequestBody SubmitRequest req) {
        return ApiResponse.ok(bountyService.submit(id, req));
    }

    @GetMapping("/{id}/claims/{claimId}/submissions")
    public ApiResponse<List<Map<String, Object>>> claimSubmissions(@PathVariable Long id, @PathVariable Long claimId) {
        return ApiResponse.ok(bountyService.claimSubmissions(id, claimId));
    }
}
