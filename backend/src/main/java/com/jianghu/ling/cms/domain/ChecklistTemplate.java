package com.jianghu.ling.cms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("checklist_template")
public class ChecklistTemplate {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String itemCode;
    private String itemName;
    private Boolean required;
    private String tagsJson;
    private Integer sortNo;
    private String status;
}
