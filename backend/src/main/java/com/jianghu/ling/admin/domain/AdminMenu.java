package com.jianghu.ling.admin.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("admin_menu")
public class AdminMenu {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long parentId;
    private String type;
    private String name;
    private String path;
    private String component;
    private String icon;
    private Integer sort;
    private Boolean visible;
    private String permissionCode;
    private String status;
}
