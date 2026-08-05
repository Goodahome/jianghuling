package com.jianghu.ling.settle.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("evaluation")
public class Evaluation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bountyId;
    private Long fromUserId;
    private Long toUserId;
    private Integer score;
    private String content;
    private LocalDateTime createdAt;
}
