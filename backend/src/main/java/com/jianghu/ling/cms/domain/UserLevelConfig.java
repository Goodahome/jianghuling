package com.jianghu.ling.cms.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_level_config")
public class UserLevelConfig {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer level;
    private String title;
    private Integer minChivalry;
    private String privilegesJson;
    private Integer sortNo;
}
