package com.jianghu.ling.review.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ReviewRequest {
    @NotBlank
    private String result;
    private String reason;
    private List<Object> itemComments;
}
