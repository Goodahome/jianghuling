package com.jianghu.ling.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("invite_code")
public class InviteCode {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private Long ownerUserId;
    private Integer quota;
    private Integer usedCount;
    private String status;
    private LocalDateTime expireAt;
    private LocalDateTime createdAt;
}
