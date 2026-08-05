package com.jianghu.ling.office.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("office_def")
public class OfficeDef {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private Integer minLevel;
    private Integer quota;
    private Integer termDays;
    private String status;
}
