package com.jianghu.ling.bounty.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * 再发一令提交体；字段均可选，缺省从原令复制。
 */
@Data
public class RepublishBountyRequest {
    private String title;
    private String difficulty;
    private BigDecimal rewardAmount;
    private Boolean confirmLowReward;
    private OffsetDateTime deadlineAt;
    private List<String> taskTags;
    private Map<String, Object> warrantFields;
    private List<String> checklistItemCodes;
}
