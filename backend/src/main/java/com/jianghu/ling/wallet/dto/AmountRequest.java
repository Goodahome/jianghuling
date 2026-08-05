package com.jianghu.ling.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AmountRequest {
    @NotNull
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;
    @NotBlank
    private String clientRequestId;
}
