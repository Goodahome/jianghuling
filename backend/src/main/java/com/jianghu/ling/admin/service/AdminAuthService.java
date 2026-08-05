package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.admin.domain.AdminUser;
import com.jianghu.ling.admin.dto.AdminLoginRequest;
import com.jianghu.ling.admin.mapper.AdminUserMapper;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.security.AuthPrincipal;
import com.jianghu.ling.security.JwtService;
import com.jianghu.ling.security.PrincipalType;
import com.jianghu.ling.user.domain.LoginLog;
import com.jianghu.ling.user.mapper.LoginLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginLogMapper loginLogMapper;

    public Map<String, Object> login(AdminLoginRequest req, HttpServletRequest request) {
        AdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, req.getUsername())
                .last("LIMIT 1"));
        if (admin == null || !"ACTIVE".equals(admin.getStatus())
                || !passwordEncoder.matches(req.getPassword(), admin.getPasswordHash())) {
            writeLog(null, request, "FAIL");
            throw new BizException(ErrorCode.PARAM_INVALID, "用户名或密码错误");
        }
        JwtService.TokenResult token = jwtService.issue(admin.getId(), PrincipalType.ADMIN);
        writeLog(admin.getId(), request, "SUCCESS");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token.token());
        data.put("expiresIn", token.expiresIn());
        data.put("permissions", List.of("*"));
        data.put("admin", Map.of(
                "id", admin.getId(),
                "username", admin.getUsername(),
                "displayName", admin.getDisplayName()
        ));
        return data;
    }

    public void logout() {
        AuthPrincipal principal = AuthContext.get();
        if (principal != null) {
            jwtService.blacklist(principal.getJti(), 7200);
        }
    }

    public Map<String, Object> me() {
        Long adminId = AuthContext.requireAdminId();
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null || !"ACTIVE".equals(admin.getStatus())) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", admin.getId());
        data.put("username", admin.getUsername());
        data.put("displayName", admin.getDisplayName());
        data.put("permissions", List.of("*"));
        return data;
    }

    private void writeLog(Long adminId, HttpServletRequest request, String result) {
        LoginLog log = new LoginLog();
        log.setAdminId(adminId);
        log.setIp(request == null ? null : request.getRemoteAddr());
        log.setUserAgent(request == null ? null : request.getHeader("User-Agent"));
        log.setResult(result);
        log.setCreatedAt(LocalDateTime.now());
        loginLogMapper.insert(log);
    }
}
