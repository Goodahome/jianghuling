package com.jianghu.ling.user.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RealNameRequest {
    @NotBlank
    private String realName;
    @NotBlank
    private String idNumber;
}
