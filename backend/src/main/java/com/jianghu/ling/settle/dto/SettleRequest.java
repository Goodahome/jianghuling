package com.jianghu.ling.settle.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class SettleRequest {
    @NotEmpty
    private List<Item> items;

    @Data
    public static class Item {
        private Long userId;
        private BigDecimal amount;
        private Integer chivalryBonus;
    }
}
