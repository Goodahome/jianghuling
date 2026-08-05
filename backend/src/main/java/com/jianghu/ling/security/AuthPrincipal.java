package com.jianghu.ling.security;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthPrincipal {
    private Long id;
    private PrincipalType type;
    private String jti;
}
