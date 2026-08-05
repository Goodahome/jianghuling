package com.jianghu.ling.wallet.controller;

import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.wallet.domain.WalletLedger;
import com.jianghu.ling.wallet.dto.AmountRequest;
import com.jianghu.ling.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping("/account")
    public ApiResponse<Map<String, Object>> account() {
        return ApiResponse.ok(walletService.accountView(AuthContext.requireUserId()));
    }

    @PostMapping("/recharge")
    public ApiResponse<Map<String, Object>> recharge(@Valid @RequestBody AmountRequest req) {
        String bizNo = "RC-" + req.getClientRequestId();
        return ApiResponse.ok(walletService.recharge(AuthContext.requireUserId(), req.getAmount(), bizNo));
    }

    @PostMapping("/withdraw")
    public ApiResponse<Map<String, Object>> withdraw(@Valid @RequestBody AmountRequest req) {
        String bizNo = "WD-" + req.getClientRequestId();
        return ApiResponse.ok(walletService.withdraw(AuthContext.requireUserId(), req.getAmount(), bizNo));
    }

    @GetMapping("/ledgers")
    public ApiResponse<PageResult<WalletLedger>> ledgers(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "20") long pageSize,
            @RequestParam(required = false) String type) {
        return ApiResponse.ok(walletService.pageLedgers(AuthContext.requireUserId(), type, page, pageSize));
    }
}
