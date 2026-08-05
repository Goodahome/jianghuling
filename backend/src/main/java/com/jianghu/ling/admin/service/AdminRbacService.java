package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.admin.domain.AdminPermission;
import com.jianghu.ling.admin.domain.AdminRole;
import com.jianghu.ling.admin.domain.AdminRolePermission;
import com.jianghu.ling.admin.domain.AdminUser;
import com.jianghu.ling.admin.domain.AdminUserRole;
import com.jianghu.ling.admin.mapper.AdminPermissionMapper;
import com.jianghu.ling.admin.mapper.AdminRoleMapper;
import com.jianghu.ling.admin.mapper.AdminRolePermissionMapper;
import com.jianghu.ling.admin.mapper.AdminUserMapper;
import com.jianghu.ling.admin.mapper.AdminUserRoleMapper;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRbacService {

    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminPermissionMapper adminPermissionMapper;
    private final AdminRolePermissionMapper adminRolePermissionMapper;
    private final AdminUserRoleMapper adminUserRoleMapper;

    public AdminUser requireActiveAdmin(Long adminId) {
        AdminUser admin = adminUserMapper.selectById(adminId);
        if (admin == null) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        if (!"ACTIVE".equals(admin.getStatus())) {
            throw new BizException(ErrorCode.ACCOUNT_BANNED, "账号已停用");
        }
        return admin;
    }

    public List<AdminRole> rolesOf(Long adminId) {
        List<AdminUserRole> links = adminUserRoleMapper.selectList(new LambdaQueryWrapper<AdminUserRole>()
                .eq(AdminUserRole::getAdminId, adminId));
        if (links.isEmpty()) {
            return List.of();
        }
        Set<Long> roleIds = links.stream().map(AdminUserRole::getRoleId).collect(Collectors.toSet());
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>()
                .in(AdminRole::getId, roleIds)
                .eq(AdminRole::getStatus, "ACTIVE"));
    }

    public List<Map<String, Object>> roleViewsOf(Long adminId) {
        return rolesOf(adminId).stream().map(r -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", r.getCode());
            m.put("name", r.getName());
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * 登录/me 用：超管并集含 * 时仅返回 ["*"]；非超管禁止出现 *。
     */
    public List<String> permissionsForResponse(Long adminId) {
        Set<String> raw = rawPermissionCodes(adminId);
        if (raw.contains("*")) {
            return List.of("*");
        }
        return new ArrayList<>(raw);
    }

    public Set<String> rawPermissionCodes(Long adminId) {
        List<AdminRole> roles = rolesOf(adminId);
        if (roles.isEmpty()) {
            return Set.of();
        }
        Set<Long> roleIds = roles.stream().map(AdminRole::getId).collect(Collectors.toSet());
        List<AdminRolePermission> links = adminRolePermissionMapper.selectList(
                new LambdaQueryWrapper<AdminRolePermission>().in(AdminRolePermission::getRoleId, roleIds));
        if (links.isEmpty()) {
            return Set.of();
        }
        Set<Long> permIds = links.stream().map(AdminRolePermission::getPermissionId).collect(Collectors.toSet());
        List<AdminPermission> perms = adminPermissionMapper.selectList(new LambdaQueryWrapper<AdminPermission>()
                .in(AdminPermission::getId, permIds));
        Set<String> codes = new LinkedHashSet<>();
        for (AdminPermission p : perms) {
            if (StringUtils.hasText(p.getCode())) {
                codes.add(p.getCode());
            }
        }
        // 非超管角色不得持有 *
        boolean hasSuper = roles.stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getCode()));
        if (!hasSuper) {
            codes.remove("*");
        }
        return codes;
    }

    public boolean hasPermission(Long adminId, String required) {
        if (!StringUtils.hasText(required)) {
            return true;
        }
        Set<String> codes = rawPermissionCodes(adminId);
        return codes.contains("*") || codes.contains(required);
    }

    public void assertPermission(Long adminId, String required) {
        if (!hasPermission(adminId, required)) {
            throw new BizException(ErrorCode.FORBIDDEN, "无权限: " + required);
        }
    }

    public boolean isSuperAdmin(Long adminId) {
        return rolesOf(adminId).stream().anyMatch(r -> "SUPER_ADMIN".equals(r.getCode()));
    }

    public long countActiveSuperAdmins() {
        AdminRole superRole = adminRoleMapper.selectOne(new LambdaQueryWrapper<AdminRole>()
                .eq(AdminRole::getCode, "SUPER_ADMIN").last("LIMIT 1"));
        if (superRole == null) {
            return 0;
        }
        List<AdminUserRole> links = adminUserRoleMapper.selectList(new LambdaQueryWrapper<AdminUserRole>()
                .eq(AdminUserRole::getRoleId, superRole.getId()));
        long count = 0;
        for (AdminUserRole link : links) {
            AdminUser u = adminUserMapper.selectById(link.getAdminId());
            if (u != null && "ACTIVE".equals(u.getStatus())) {
                count++;
            }
        }
        return count;
    }

    public void bindRoles(Long adminId, List<String> roleCodes) {
        adminUserRoleMapper.delete(new LambdaQueryWrapper<AdminUserRole>()
                .eq(AdminUserRole::getAdminId, adminId));
        if (roleCodes == null || roleCodes.isEmpty()) {
            return;
        }
        for (String code : roleCodes) {
            AdminRole role = adminRoleMapper.selectOne(new LambdaQueryWrapper<AdminRole>()
                    .eq(AdminRole::getCode, code).last("LIMIT 1"));
            if (role == null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "未知角色: " + code);
            }
            AdminUserRole link = new AdminUserRole();
            link.setAdminId(adminId);
            link.setRoleId(role.getId());
            adminUserRoleMapper.insert(link);
        }
    }

    public void ensureSuperAdminRole(Long adminId) {
        if (isSuperAdmin(adminId)) {
            return;
        }
        AdminRole role = adminRoleMapper.selectOne(new LambdaQueryWrapper<AdminRole>()
                .eq(AdminRole::getCode, "SUPER_ADMIN").last("LIMIT 1"));
        if (role == null) {
            return;
        }
        AdminUserRole link = new AdminUserRole();
        link.setAdminId(adminId);
        link.setRoleId(role.getId());
        adminUserRoleMapper.insert(link);
    }

    public Map<String, Object> toAdminAuthView(AdminUser admin) {
        Map<String, Object> view = new LinkedHashMap<>();
        view.put("id", admin.getId());
        view.put("username", admin.getUsername());
        view.put("displayName", admin.getDisplayName());
        view.put("status", admin.getStatus());
        view.put("roles", roleViewsOf(admin.getId()));
        view.put("permissions", permissionsForResponse(admin.getId()));
        view.put("menus", List.of());
        return view;
    }
}
