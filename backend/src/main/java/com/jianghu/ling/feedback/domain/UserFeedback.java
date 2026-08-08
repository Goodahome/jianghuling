package com.jianghu.ling.feedback.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_feedback")
public class UserFeedback {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private String contact;
    private String relatedRef;
    private String attachmentUrlsJson;
    private String status;
    private String handleRemark;
    private LocalDateTime statusChangedAt;
    private Long statusChangedByAdminId;
    private String statusHistoryJson;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
