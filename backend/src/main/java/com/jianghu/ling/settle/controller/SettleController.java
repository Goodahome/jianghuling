package com.jianghu.ling.settle.controller;

import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.settle.domain.Evaluation;
import com.jianghu.ling.settle.dto.CancelRequest;
import com.jianghu.ling.settle.dto.EvaluationRequest;
import com.jianghu.ling.settle.dto.SettleRequest;
import com.jianghu.ling.settle.service.SettleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bounties/{id}")
@RequiredArgsConstructor
public class SettleController {

    private final SettleService settleService;

    @GetMapping("/settlement/preview")
    public ApiResponse<Map<String, Object>> preview(@PathVariable("id") Long id) {
        return ApiResponse.ok(settleService.preview(id));
    }

    @PostMapping("/settlement")
    public ApiResponse<Map<String, Object>> settle(@PathVariable("id") Long id,
                                                   @Valid @RequestBody SettleRequest req) {
        return ApiResponse.ok(settleService.settle(id, req));
    }

    @PostMapping("/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable("id") Long id,
                                                   @RequestBody(required = false) CancelRequest req) {
        String reason = req == null ? null : req.getReason();
        return ApiResponse.ok(settleService.cancel(id, reason));
    }

    @PostMapping("/evaluations")
    public ApiResponse<Evaluation> evaluate(@PathVariable("id") Long id,
                                            @Valid @RequestBody EvaluationRequest req) {
        return ApiResponse.ok(settleService.evaluate(id, req.getToUserId(), req.getScore(), req.getContent()));
    }

    @GetMapping("/evaluations")
    public ApiResponse<List<Evaluation>> evaluations(@PathVariable("id") Long id) {
        return ApiResponse.ok(settleService.listEvaluations(id));
    }
}
