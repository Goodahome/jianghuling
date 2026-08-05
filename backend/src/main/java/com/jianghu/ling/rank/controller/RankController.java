package com.jianghu.ling.rank.controller;

import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.rank.service.RankService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/ranks")
@RequiredArgsConstructor
public class RankController {

    private final RankService rankService;

    @GetMapping("/me")
    public ApiResponse<Map<String, Object>> me() {
        return ApiResponse.ok(rankService.mine());
    }

    @GetMapping("/{type}")
    public ApiResponse<Map<String, Object>> page(
            @PathVariable String type,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "50") long pageSize) {
        return ApiResponse.ok(rankService.page(type, page, pageSize));
    }
}
