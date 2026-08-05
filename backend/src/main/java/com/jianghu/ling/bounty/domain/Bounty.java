package com.jianghu.ling.bounty.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("bounty")
public class Bounty {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long publisherId;
    private String type;
    private String title;
    private String status;
    private String city;
    private String district;
    private String difficulty;
    private BigDecimal rewardAmount;
    private LocalDateTime deadlineAt;
    private String taskTagsJson;
    private String frozenBizNo;
    private String cancelReason;
    private Boolean remind24hSent;
    private Boolean remind2hSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
