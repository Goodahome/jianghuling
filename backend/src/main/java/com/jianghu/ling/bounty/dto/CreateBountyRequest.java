package com.jianghu.ling.bounty.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Data
public class CreateBountyRequest {
    @NotBlank
    private String type;
    @NotBlank
    private String title;
    @NotBlank
    private String difficulty;
    @NotNull
    private BigDecimal rewardAmount;
    private Boolean confirmLowReward;
    @NotNull
    private OffsetDateTime deadlineAt;
    private List<String> taskTags;
    private Map<String, Object> warrantFields;
    private List<String> checklistItemCodes;
    /** 可选：经「创建+来源」等价再发路径时传入；须满足 §7.8 终态约束 */
    private Long sourceBountyId;
}
