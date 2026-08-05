package com.jianghu.ling.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("user_asset")
public class UserAsset {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer chivalry;
    private Integer stamina;
    private LocalDate staminaDate;
    private Integer completedOrders;
    private BigDecimal goodRate;
    private BigDecimal reputationScore;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
