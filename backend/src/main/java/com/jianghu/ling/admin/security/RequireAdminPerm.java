package com.jianghu.ling.admin.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 管理员接口所需权限码；多个时满足其一即可（OR）。
 * 持有 {@code *}（仅超管角色）视为全放行。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequireAdminPerm {
    String[] value();
}
