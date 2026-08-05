package com.jianghu.ling.bounty.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("bounty_checklist")
public class BountyChecklist {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bountyId;
    private String itemCode;
    private String itemName;
    private Boolean required;
    private Integer sortNo;
}
