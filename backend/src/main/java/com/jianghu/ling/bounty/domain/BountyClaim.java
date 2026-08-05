package com.jianghu.ling.bounty.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bounty_claim")
public class BountyClaim {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bountyId;
    private Long userId;
    private Integer staminaCost;
    private String status;
    private LocalDateTime createdAt;
}
