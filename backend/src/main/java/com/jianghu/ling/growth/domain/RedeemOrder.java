package com.jianghu.ling.growth.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("redeem_order")
public class RedeemOrder {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long productId;
    private Integer quantity;
    private Integer chivalryCost;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
