package com.jianghu.ling.admin.controller;

import com.jianghu.ling.admin.security.RequireAdminPerm;
import com.jianghu.ling.admin.service.AdminProductService;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final AdminProductService adminProductService;

    @GetMapping("/products")
    @RequireAdminPerm("product:read")
    public ApiResponse<PageResult<Map<String, Object>>> products(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(adminProductService.pageProducts(page, pageSize));
    }

    @PostMapping("/products")
    @RequireAdminPerm("product:write")
    public ApiResponse<Map<String, Object>> createProduct(@RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminProductService.createProduct(body));
    }

    @PutMapping("/products/{id}")
    @RequireAdminPerm("product:write")
    public ApiResponse<Map<String, Object>> updateProduct(@PathVariable Long id,
                                                          @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminProductService.updateProduct(id, body));
    }

    @DeleteMapping("/products/{id}")
    @RequireAdminPerm("product:write")
    public ApiResponse<Void> deleteProduct(@PathVariable Long id) {
        adminProductService.deleteProduct(id);
        return ApiResponse.ok();
    }

    @GetMapping("/redeem-orders")
    @RequireAdminPerm("product:read")
    public ApiResponse<PageResult<Map<String, Object>>> orders(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize) {
        return ApiResponse.ok(adminProductService.pageOrders(status, page, pageSize));
    }

    @PutMapping("/redeem-orders/{id}")
    @RequireAdminPerm("product:write")
    public ApiResponse<Map<String, Object>> updateOrder(@PathVariable Long id,
                                                        @RequestBody Map<String, Object> body) {
        return ApiResponse.ok(adminProductService.updateOrder(id, body));
    }
}
