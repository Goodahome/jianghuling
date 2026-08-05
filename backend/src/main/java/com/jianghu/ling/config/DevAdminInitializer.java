package com.jianghu.ling.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.admin.domain.AdminUser;
import com.jianghu.ling.admin.mapper.AdminUserMapper;
import com.jianghu.ling.admin.service.AdminRbacService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@Profile("dev")
@Order(100)
@RequiredArgsConstructor
public class DevAdminInitializer implements ApplicationRunner {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final AdminRbacService adminRbacService;

    @Override
    public void run(ApplicationArguments args) {
        AdminUser admin = adminUserMapper.selectOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getUsername, "admin")
                .last("LIMIT 1"));
        if (admin == null) {
            admin = new AdminUser();
            admin.setUsername("admin");
            admin.setDisplayName("武林盟主事");
            admin.setStatus("ACTIVE");
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setCreatedAt(LocalDateTime.now());
            admin.setUpdatedAt(LocalDateTime.now());
            adminUserMapper.insert(admin);
            log.info("Created default admin user: admin / admin123");
        } else if (!passwordEncoder.matches("admin123", admin.getPasswordHash())) {
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            admin.setUpdatedAt(LocalDateTime.now());
            adminUserMapper.updateById(admin);
            log.info("Reset default admin password to admin123 (dev profile)");
        }
        try {
            adminRbacService.ensureSuperAdminRole(admin.getId());
        } catch (Exception e) {
            log.warn("Bind SUPER_ADMIN skipped (run patch_rbac.sql first): {}", e.getMessage());
        }
    }
}
