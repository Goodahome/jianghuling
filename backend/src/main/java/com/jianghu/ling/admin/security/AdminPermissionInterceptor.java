package com.jianghu.ling.admin.security;

import com.jianghu.ling.admin.service.AdminRbacService;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.security.AuthPrincipal;
import com.jianghu.ling.security.PrincipalType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AdminPermissionInterceptor implements HandlerInterceptor {

    private final AdminRbacService adminRbacService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method)) {
            return true;
        }
        String path = request.getRequestURI();
        if (!path.startsWith("/api/v1/admin/")) {
            return true;
        }
        if (path.equals("/api/v1/admin/auth/login")) {
            return true;
        }

        AuthPrincipal principal = AuthContext.get();
        if (principal == null || principal.getType() != PrincipalType.ADMIN) {
            // JwtAuthFilter 已拦；双保险
            throw new BizException(ErrorCode.FORBIDDEN);
        }

        // 停用账号
        adminRbacService.requireActiveAdmin(principal.getId());

        if (path.equals("/api/v1/admin/auth/logout")
                || path.equals("/api/v1/admin/auth/me")
                || path.equals("/api/v1/admin/menus/tree")) {
            return true;
        }

        RequireAdminPerm ann = method.getMethodAnnotation(RequireAdminPerm.class);
        if (ann == null) {
            ann = method.getBeanType().getAnnotation(RequireAdminPerm.class);
        }
        if (ann == null || ann.value().length == 0) {
            // 未标注的 admin 接口默认拒绝（防漏挂权限）
            throw new BizException(ErrorCode.FORBIDDEN, "无权限: (missing annotation)");
        }
        for (String code : ann.value()) {
            if (adminRbacService.hasPermission(principal.getId(), code)) {
                return true;
            }
        }
        throw new BizException(ErrorCode.FORBIDDEN, "无权限: " + String.join("|", ann.value()));
    }
}
