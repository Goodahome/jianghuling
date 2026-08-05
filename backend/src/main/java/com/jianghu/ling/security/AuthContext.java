package com.jianghu.ling.security;

import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;

public final class AuthContext {

    private static final ThreadLocal<AuthPrincipal> HOLDER = new ThreadLocal<>();

    private AuthContext() {
    }

    public static void set(AuthPrincipal principal) {
        HOLDER.set(principal);
    }

    public static AuthPrincipal get() {
        return HOLDER.get();
    }

    public static Long requireUserId() {
        AuthPrincipal p = HOLDER.get();
        if (p == null || p.getType() != PrincipalType.USER) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return p.getId();
    }

    public static Long requireAdminId() {
        AuthPrincipal p = HOLDER.get();
        if (p == null || p.getType() != PrincipalType.ADMIN) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        return p.getId();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
