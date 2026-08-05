package com.jianghu.ling.bounty.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("submission_item")
public class SubmissionItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long submissionId;
    private String checklistItemCode;
    private Boolean done;
    private String text;
    private String mediaUrlsJson;
}
