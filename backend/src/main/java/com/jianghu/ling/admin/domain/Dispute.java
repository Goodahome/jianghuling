package com.jianghu.ling.admin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("dispute")
public class Dispute {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long settlementId;
    private Long bountyId;
    private Long initiatorId;
    private String status;
    private String reason;
    private String evidenceJson;
    private String verdictJson;
    private LocalDateTime deadlineAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
