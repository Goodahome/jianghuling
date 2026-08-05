package com.jianghu.ling.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianghu.ling.common.api.ApiResponse;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            boolean pub = isPublic(path, request.getMethod());
            String auth = request.getHeader("Authorization");
            if (auth != null && auth.startsWith("Bearer ")) {
                try {
                    AuthPrincipal principal = jwtService.parse(auth.substring(7).trim());
                    AuthContext.set(principal);
                } catch (BizException e) {
                    if (!pub) {
                        writeError(response, e.getCode(), e.getMessage());
                        return;
                    }
                }
            } else if (!pub) {
                writeError(response, ErrorCode.UNAUTHORIZED);
                return;
            }

            AuthPrincipal principal = AuthContext.get();
            if (!pub && principal != null) {
                if (path.startsWith("/api/v1/admin/") && principal.getType() != PrincipalType.ADMIN) {
                    writeError(response, ErrorCode.FORBIDDEN);
                    return;
                }
                if (path.startsWith("/api/v1/") && !path.startsWith("/api/v1/admin/")
                        && principal.getType() == PrincipalType.ADMIN) {
                    writeError(response, ErrorCode.FORBIDDEN);
                    return;
                }
            }
            filterChain.doFilter(request, response);
        } catch (BizException e) {
            writeError(response, e.getCode(), e.getMessage());
        } finally {
            AuthContext.clear();
        }
    }

    private boolean isPublic(String path, String method) {
        if (path.startsWith("/actuator") || path.startsWith("/files/")) {
            return true;
        }
        if (!path.startsWith("/api/v1/")) {
            return false;
        }
        if (path.equals("/api/v1/auth/sms/send")
                || path.equals("/api/v1/auth/invite/validate")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/admin/auth/login")) {
            return true;
        }
        if ("GET".equalsIgnoreCase(method)) {
            if (path.startsWith("/api/v1/meta/") || path.startsWith("/api/v1/notices")) {
                return true;
            }
            if (path.startsWith("/api/v1/bounties/mine")) {
                return false;
            }
            // plaza list or detail: /api/v1/bounties or /api/v1/bounties/{id}
            if (path.equals("/api/v1/bounties") || path.matches("/api/v1/bounties/\\d+")) {
                return true;
            }
            // 英雄谱公开；/ranks/me 需登录
            if (path.matches("/api/v1/ranks/(REPUTATION|CHIVALRY|COMPLETED)")) {
                return true;
            }
        }
        return false;
    }


    private void writeError(HttpServletResponse response, ErrorCode code) throws IOException {
        writeError(response, code.getCode(), code.getMessage());
    }

    private void writeError(HttpServletResponse response, int code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(code, message));
    }
}
