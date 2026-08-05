package com.jianghu.ling.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank
    private String loginType;
    private String username;
    private String password;
    private String phone;
    private String smsCode;
}
