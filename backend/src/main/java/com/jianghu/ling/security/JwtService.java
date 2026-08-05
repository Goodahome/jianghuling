package com.jianghu.ling.security;

import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtProperties properties;
    private final StringRedisTemplate redisTemplate;

    public record TokenResult(String token, long expiresIn, String jti) {
    }

    public TokenResult issue(Long id, PrincipalType type) {
        String jti = UUID.randomUUID().toString().replace("-", "");
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(properties.getExpiresInSeconds());
        String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(id))
                .claim("ptype", type.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key())
                .compact();
        return new TokenResult(token, properties.getExpiresInSeconds(), jti);
    }

    public AuthPrincipal parse(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String jti = claims.getId();
            if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey(jti)))) {
                throw new BizException(ErrorCode.UNAUTHORIZED, "Token已失效");
            }
            Long id = Long.valueOf(claims.getSubject());
            PrincipalType type = PrincipalType.valueOf(claims.get("ptype", String.class));
            return new AuthPrincipal(id, type, jti);
        } catch (ExpiredJwtException e) {
            throw new BizException(ErrorCode.TOKEN_EXPIRED);
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
    }

    public void blacklist(String jti, long ttlSeconds) {
        if (jti == null || jti.isBlank()) {
            return;
        }
        long ttl = Math.max(ttlSeconds, 1);
        redisTemplate.opsForValue().set(blacklistKey(jti), "1", Duration.ofSeconds(ttl));
    }

    private String blacklistKey(String jti) {
        return "jwt:blacklist:" + jti;
    }

    private SecretKey key() {
        byte[] bytes = properties.getSecret().getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(bytes, 0, padded, 0, bytes.length);
            bytes = padded;
        }
        return Keys.hmacShaKeyFor(bytes);
    }
}
