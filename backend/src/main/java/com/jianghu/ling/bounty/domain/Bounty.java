package com.jianghu.ling.bounty.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    /** 有成果取消后待分配；对应表字段 cancel_allocation_pending */
    @TableField("cancel_allocation_pending")
    private Boolean cancelAllocationPending;
    /** 再发一令来源悬赏 ID；普通发令为 null */
    private Long sourceBountyId;
    /** 表字段 remind_24h_sent；默认驼峰会变成 remind24h_sent */
    @TableField("remind_24h_sent")
    private Boolean remind24hSent;
    @TableField("remind_2h_sent")
    private Boolean remind2hSent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
