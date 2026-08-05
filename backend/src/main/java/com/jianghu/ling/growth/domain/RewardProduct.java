package com.jianghu.ling.growth.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reward_product")
public class RewardProduct {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Integer costChivalry;
    private Integer stock;
    private String coverUrl;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
