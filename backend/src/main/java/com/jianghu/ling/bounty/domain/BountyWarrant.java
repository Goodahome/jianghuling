package com.jianghu.ling.bounty.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("bounty_warrant")
public class BountyWarrant {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long bountyId;
    private String templateCode;
    private String fieldsJson;
}
