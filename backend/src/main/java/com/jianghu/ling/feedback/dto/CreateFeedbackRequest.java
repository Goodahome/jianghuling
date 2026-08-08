package com.jianghu.ling.feedback.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateFeedbackRequest {
    @NotBlank
    private String type;
    @NotBlank
    @Size(max = 100)
    private String title;
    @NotBlank
    @Size(max = 2000)
    private String content;
    @Size(max = 64)
    private String contact;
    @Size(max = 128)
    private String relatedRef;
    private List<String> attachmentUrls;
}
