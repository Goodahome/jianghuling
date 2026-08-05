package com.jianghu.ling.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InviteValidateRequest {
    @NotBlank
    private String inviteCode;
}
