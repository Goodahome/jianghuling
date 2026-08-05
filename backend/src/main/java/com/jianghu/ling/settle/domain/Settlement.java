package com.jianghu.ling.settle.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("settlement")
public class Settlement {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bountyId;
    private BigDecimal rewardB;
    private BigDecimal feeRate;
    private BigDecimal fee;
    private BigDecimal distributable;
    private String status;
    private LocalDateTime createdAt;
}
