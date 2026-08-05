package com.jianghu.ling.growth.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExchangeStaminaRequest {
    @NotNull
    @Min(1)
    private Integer staminaPoints;
}
