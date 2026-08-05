package com.jianghu.ling.growth.controller;

import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.growth.dto.ExchangeStaminaRequest;
import com.jianghu.ling.growth.dto.RedeemRequest;
import com.jianghu.ling.growth.service.GrowthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/growth")
@RequiredArgsConstructor
public class GrowthController {

    private final GrowthService growthService;

    @GetMapping("/level")
    public ApiResponse<Map<String, Object>> level() {
        return ApiResponse.ok(growthService.levelProgress());
    }

    @PostMapping("/stamina/exchange")
    public ApiResponse<Map<String, Object>> exchange(@Valid @RequestBody ExchangeStaminaRequest req) {
        return ApiResponse.ok(growthService.exchangeStamina(req.getStaminaPoints()));
    }

    @GetMapping("/products")
    public ApiResponse<PageResult<Map<String, Object>>> products(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(growthService.products(page, pageSize));
    }

    @PostMapping("/products/{productId}/redeem")
    public ApiResponse<Map<String, Object>> redeem(@PathVariable long productId,
                                                   @Valid @RequestBody RedeemRequest req) {
        return ApiResponse.ok(growthService.redeem(productId, req.getQuantity()));
    }

    @GetMapping("/redeem-orders")
    public ApiResponse<PageResult<Map<String, Object>>> redeemOrders(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(growthService.redeemOrders(page, pageSize));
    }
}
