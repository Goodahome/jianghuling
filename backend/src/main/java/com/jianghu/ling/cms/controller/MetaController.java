package com.jianghu.ling.cms.controller;

import com.jianghu.ling.cms.service.MetaService;
import com.jianghu.ling.common.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/meta")
@RequiredArgsConstructor
public class MetaController {

    private final MetaService metaService;

    @GetMapping("/reward-suggest")
    public ApiResponse<Map<String, Object>> rewardSuggest() {
        return ApiResponse.ok(metaService.rewardSuggest());
    }

    @GetMapping("/warrant-templates")
    public ApiResponse<List<Map<String, Object>>> warrantTemplates() {
        return ApiResponse.ok(metaService.warrantTemplates());
    }

    @GetMapping("/checklist-templates")
    public ApiResponse<List<Map<String, Object>>> checklistTemplates(@RequestParam(required = false) String tags) {
        return ApiResponse.ok(metaService.checklistTemplates(tags));
    }

    @GetMapping("/growth-config")
    public ApiResponse<Map<String, Object>> growthConfig() {
        return ApiResponse.ok(metaService.growthConfig());
    }
}
