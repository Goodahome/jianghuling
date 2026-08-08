package com.jianghu.ling.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFeedbackStatusRequest {
    @NotBlank
    private String status;
    @Size(max = 1000)
    private String handleRemark;
}
