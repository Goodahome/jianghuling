package com.jianghu.ling.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AssetAdjustRequest {
    @NotBlank
    private String assetType;
    @NotNull
    private BigDecimal delta;
    @NotBlank
    private String reason;
}
