package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.admin.domain.AdminPermission;
import com.jianghu.ling.admin.domain.AdminRole;
import com.jianghu.ling.admin.domain.AdminRolePermission;
import com.jianghu.ling.admin.mapper.AdminPermissionMapper;
import com.jianghu.ling.admin.mapper.AdminRoleMapper;
import com.jianghu.ling.admin.mapper.AdminRolePermissionMapper;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminRoleAdminService {

    private final AdminRoleMapper adminRoleMapper;
    private final AdminPermissionMapper adminPermissionMapper;
    private final AdminRolePermissionMapper adminRolePermissionMapper;
    private final AuditService auditService;

    public List<Map<String, Object>> listRoles() {
        return adminRoleMapper.selectList(new LambdaQueryWrapper<AdminRole>()
                        .orderByAsc(AdminRole::getId))
                .stream().map(this::toView).collect(Collectors.toList());
    }

    public Map<String, Object> getByCode(String code) {
        return toView(requireRole(code));
    }

    public List<Map<String, Object>> permissionCatalog() {
        return adminPermissionMapper.selectList(new LambdaQueryWrapper<AdminPermission>()
                        .ne(AdminPermission::getCode, "*")
                        .orderByAsc(AdminPermission::getModule)
                        .orderByAsc(AdminPermission::getCode))
                .stream().map(p -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("code", p.getCode());
                    m.put("name", p.getName());
                    m.put("module", p.getModule());
                    m.put("type", p.getType());
                    return m;
                }).collect(Collectors.toList());
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> putPermissions(String code, Map<String, Object> body) {
        AdminRole role = requireRole(code);
        if ("SUPER_ADMIN".equals(role.getCode())) {
            throw new BizException(ErrorCode.BIZ_RULE, "超级管理员权限集只读");
        }
        List<String> codes = new ArrayList<>();
        if (body != null && body.get("permissions") instanceof List<?> list) {
            for (Object o : list) {
                if (o != null) {
                    codes.add(String.valueOf(o));
                }
            }
        }
        Set<String> unique = new LinkedHashSet<>(codes);
        if (unique.contains("*")) {
            throw new BizException(ErrorCode.PARAM_INVALID, "非超管角色不可配置 *");
        }
        List<AdminPermission> perms = new ArrayList<>();
        for (String c : unique) {
            AdminPermission p = adminPermissionMapper.selectOne(new LambdaQueryWrapper<AdminPermission>()
                    .eq(AdminPermission::getCode, c).last("LIMIT 1"));
            if (p == null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "未知权限码: " + c);
            }
            perms.add(p);
        }
        adminRolePermissionMapper.delete(new LambdaQueryWrapper<AdminRolePermission>()
                .eq(AdminRolePermission::getRoleId, role.getId()));
        for (AdminPermission p : perms) {
            AdminRolePermission link = new AdminRolePermission();
            link.setRoleId(role.getId());
            link.setPermissionId(p.getId());
            adminRolePermissionMapper.insert(link);
        }
        auditService.log("ROLE_PERMISSIONS_UPDATE", "code=" + code + ", count=" + perms.size());
        return toView(role);
    }

    private AdminRole requireRole(String code) {
        AdminRole role = adminRoleMapper.selectOne(new LambdaQueryWrapper<AdminRole>()
                .eq(AdminRole::getCode, code).last("LIMIT 1"));
        if (role == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return role;
    }

    private Map<String, Object> toView(AdminRole role) {
        List<AdminRolePermission> links = adminRolePermissionMapper.selectList(
                new LambdaQueryWrapper<AdminRolePermission>().eq(AdminRolePermission::getRoleId, role.getId()));
        List<String> permissions = new ArrayList<>();
        if (!links.isEmpty()) {
            Set<Long> ids = links.stream().map(AdminRolePermission::getPermissionId).collect(Collectors.toSet());
            List<AdminPermission> perms = adminPermissionMapper.selectList(new LambdaQueryWrapper<AdminPermission>()
                    .in(AdminPermission::getId, ids));
            for (AdminPermission p : perms) {
                permissions.add(p.getCode());
            }
            permissions.sort(String::compareTo);
        }
        // 非超管响应禁止 *
        if (!"SUPER_ADMIN".equals(role.getCode())) {
            permissions.removeIf("*"::equals);
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("code", role.getCode());
        m.put("name", role.getName());
        m.put("builtin", Boolean.TRUE.equals(role.getBuiltin()));
        m.put("description", role.getDescription());
        m.put("permissions", permissions);
        m.put("updatedAt", role.getUpdatedAt());
        return m;
    }
}
