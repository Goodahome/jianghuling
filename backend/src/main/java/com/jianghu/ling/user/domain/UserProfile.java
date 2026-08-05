package com.jianghu.ling.user.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_profile")
public class UserProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String bio;
    private String realName;
    private String idNumber;
    private String realNameStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
