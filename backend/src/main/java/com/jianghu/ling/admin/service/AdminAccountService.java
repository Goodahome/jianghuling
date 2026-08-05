package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.admin.domain.AdminRole;
import com.jianghu.ling.admin.domain.AdminUser;
import com.jianghu.ling.admin.mapper.AdminRoleMapper;
import com.jianghu.ling.admin.mapper.AdminUserMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminAccountService {

    private final AdminUserMapper adminUserMapper;
    private final AdminRoleMapper adminRoleMapper;
    private final AdminRbacService adminRbacService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public PageResult<Map<String, Object>> page(long page, long pageSize, String keyword, String status) {
        LambdaQueryWrapper<AdminUser> q = new LambdaQueryWrapper<AdminUser>()
                .eq(StringUtils.hasText(status), AdminUser::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w
                        .like(AdminUser::getUsername, keyword)
                        .or()
                        .like(AdminUser::getDisplayName, keyword))
                .orderByDesc(AdminUser::getId);
        Page<AdminUser> p = adminUserMapper.selectPage(new Page<>(page, pageSize), q);
        return PageResult.of(p.getRecords().stream().map(this::toView).toList(), p.getTotal(), page, pageSize);
    }

    public Map<String, Object> detail(Long id) {
        AdminUser admin = adminUserMapper.selectById(id);
        if (admin == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return toView(admin);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> create(Map<String, Object> body) {
        if (body == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        String username = str(body.get("username"));
        String password = str(body.get("password"));
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "username/password必填");
        }
        if (adminUserMapper.selectCount(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, username)) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "用户名已存在");
        }
        List<String> roleCodes = asStringList(body.get("roleCodes"));
        validateRoleCodes(roleCodes);

        AdminUser admin = new AdminUser();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setDisplayName(str(body.getOrDefault("displayName", username)));
        admin.setStatus(str(body.getOrDefault("status", "ACTIVE")));
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.insert(admin);
        adminRbacService.bindRoles(admin.getId(), roleCodes);
        auditService.log("ADMIN_CREATE", "id=" + admin.getId() + ", username=" + username);
        return toView(admin);
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> update(Long id, Map<String, Object> body) {
        AdminUser admin = adminUserMapper.selectById(id);
        if (admin == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (body == null) {
            return toView(admin);
        }
        if (body.containsKey("displayName")) {
            admin.setDisplayName(str(body.get("displayName")));
        }
        if (body.containsKey("status")) {
            String status = str(body.get("status"));
            assertCanChangeSuperStatus(admin, status);
            admin.setStatus(status);
        }
        if (body.containsKey("roleCodes")) {
            List<String> roleCodes = asStringList(body.get("roleCodes"));
            validateRoleCodes(roleCodes);
            assertCanChangeSuperRoles(admin, roleCodes);
            adminRbacService.bindRoles(id, roleCodes);
        }
        admin.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateById(admin);
        auditService.log("ADMIN_UPDATE", "id=" + id);
        return toView(admin);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "newPassword必填");
        }
        AdminUser admin = adminUserMapper.selectById(id);
        if (admin == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        admin.setPasswordHash(passwordEncoder.encode(newPassword));
        admin.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateById(admin);
        auditService.log("ADMIN_RESET_PASSWORD", "id=" + id);
    }

    @Transactional
    public void disable(Long id) {
        AdminUser admin = adminUserMapper.selectById(id);
        if (admin == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        assertCanChangeSuperStatus(admin, "DISABLED");
        admin.setStatus("DISABLED");
        admin.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateById(admin);
        auditService.log("ADMIN_DISABLE", "id=" + id);
    }

    @Transactional
    public void enable(Long id) {
        AdminUser admin = adminUserMapper.selectById(id);
        if (admin == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        admin.setStatus("ACTIVE");
        admin.setUpdatedAt(LocalDateTime.now());
        adminUserMapper.updateById(admin);
        auditService.log("ADMIN_ENABLE", "id=" + id);
    }

    private void assertCanChangeSuperStatus(AdminUser admin, String newStatus) {
        Long selfId = AuthContext.requireAdminId();
        if (selfId.equals(admin.getId()) && !"ACTIVE".equals(newStatus)) {
            throw new BizException(ErrorCode.BIZ_RULE, "不可停用自身账号");
        }
        if (!adminRbacService.isSuperAdmin(admin.getId())) {
            return;
        }
        if (!"ACTIVE".equals(newStatus) && adminRbacService.countActiveSuperAdmins() <= 1) {
            throw new BizException(ErrorCode.BIZ_RULE, "不可停用最后一个超级管理员");
        }
    }

    private void assertCanChangeSuperRoles(AdminUser admin, List<String> roleCodes) {
        boolean wasSuper = adminRbacService.isSuperAdmin(admin.getId());
        boolean willSuper = roleCodes != null && roleCodes.contains("SUPER_ADMIN");
        if (wasSuper && !willSuper && adminRbacService.countActiveSuperAdmins() <= 1) {
            throw new BizException(ErrorCode.BIZ_RULE, "不可去掉最后一个超级管理员角色");
        }
        Long selfId = AuthContext.requireAdminId();
        if (selfId.equals(admin.getId()) && wasSuper && !willSuper) {
            throw new BizException(ErrorCode.BIZ_RULE, "不可去掉自身超管角色");
        }
    }

    private void validateRoleCodes(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "roleCodes必填");
        }
        for (String code : roleCodes) {
            AdminRole role = adminRoleMapper.selectOne(new LambdaQueryWrapper<AdminRole>()
                    .eq(AdminRole::getCode, code).last("LIMIT 1"));
            if (role == null) {
                throw new BizException(ErrorCode.PARAM_INVALID, "未知角色: " + code);
            }
        }
    }

    private Map<String, Object> toView(AdminUser admin) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", admin.getId());
        m.put("username", admin.getUsername());
        m.put("displayName", admin.getDisplayName());
        m.put("status", admin.getStatus());
        List<Map<String, Object>> roles = adminRbacService.roleViewsOf(admin.getId());
        List<String> codes = new ArrayList<>();
        for (Map<String, Object> r : roles) {
            codes.add(String.valueOf(r.get("code")));
        }
        m.put("roleCodes", codes);
        m.put("roles", roles);
        m.put("createdAt", admin.getCreatedAt());
        m.put("updatedAt", admin.getUpdatedAt());
        return m;
    }

    @SuppressWarnings("unchecked")
    private List<String> asStringList(Object v) {
        if (v == null) {
            return List.of();
        }
        if (v instanceof List<?> list) {
            List<String> out = new ArrayList<>();
            for (Object o : list) {
                if (o != null) {
                    out.add(String.valueOf(o));
                }
            }
            return out;
        }
        return List.of(String.valueOf(v));
    }

    private String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }
}
