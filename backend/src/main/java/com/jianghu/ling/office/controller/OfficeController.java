package com.jianghu.ling.office.controller;

import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.office.service.OfficeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/offices")
@RequiredArgsConstructor
public class OfficeController {

    private final OfficeService officeService;

    @GetMapping("/defs")
    public ApiResponse<List<Map<String, Object>>> defs() {
        return ApiResponse.ok(officeService.defs());
    }

    @PostMapping("/applications")
    public ApiResponse<Map<String, Object>> apply(@RequestBody Map<String, String> body) {
        String code = body == null ? null : body.get("officeCode");
        String statement = body == null ? null : body.get("statement");
        return ApiResponse.ok(officeService.apply(code, statement));
    }

    @GetMapping("/mine")
    public ApiResponse<List<Map<String, Object>>> mine() {
        return ApiResponse.ok(officeService.mineOffices());
    }

    @GetMapping("/applications/mine")
    public ApiResponse<List<Map<String, Object>>> mineApplications() {
        return ApiResponse.ok(officeService.mineApplications());
    }
}
