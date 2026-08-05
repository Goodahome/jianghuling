package com.jianghu.ling.rank.controller;

import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.rank.service.LordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/lord")
@RequiredArgsConstructor
public class LordController {

    private final LordService lordService;

    @PostMapping("/applications")
    public ApiResponse<Map<String, Object>> apply(@RequestBody Map<String, String> body) {
        String statement = body == null ? null : body.get("statement");
        return ApiResponse.ok(lordService.apply(statement));
    }

    @GetMapping("/applications/mine")
    public ApiResponse<Map<String, Object>> mine() {
        return ApiResponse.ok(lordService.mine());
    }
}
