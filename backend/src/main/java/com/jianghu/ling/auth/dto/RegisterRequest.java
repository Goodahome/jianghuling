package com.jianghu.ling.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    private String inviteCode;
    @NotBlank
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;
    private String smsCode;
    @NotBlank
    private String username;
    private String password;
    @NotBlank
    private String nickname;
}
