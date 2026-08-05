package com.jianghu.ling.settle.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("settlement_item")
public class SettlementItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long settlementId;
    private Long userId;
    private BigDecimal amount;
    private Integer chivalryBonus;
}
