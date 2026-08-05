package com.jianghu.ling.admin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("office_application")
public class OfficeApplication {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String officeCode;
    private String statement;
    private String status;
    private String reason;
    private Long reviewerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
