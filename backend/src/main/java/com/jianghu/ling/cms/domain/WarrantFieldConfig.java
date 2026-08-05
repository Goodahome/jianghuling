package com.jianghu.ling.cms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("warrant_field_config")
public class WarrantFieldConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String templateCode;
    private String templateName;
    private String fieldKey;
    private String label;
    private String fieldType;
    private Boolean required;
    private Boolean maskUntilClaimed;
    private Integer sortNo;
    private String status;
}
