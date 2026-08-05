package com.jianghu.ling.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {
    private String secret = "change-me-jianghu-ling-jwt-secret-key-32bytes";
    private long expiresInSeconds = 7200;
}
