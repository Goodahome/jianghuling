package com.jianghu.ling.bounty.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("bounty_message")
public class BountyMessage {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bountyId;
    private Long senderId;
    private String content;
    private LocalDateTime createdAt;
}
