package com.jianghu.ling.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.admin.domain.AdminUser;
import com.jianghu.ling.admin.mapper.AdminUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevAdminInitializer implements ApplicationRunner {

    private final AdminUserMapper adminUserMapper;
    private final PasswordEncoder passwordEncoder;

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
            adminUserMapper.insert(admin);
            log.info("Created default admin user: admin / admin123");
            return;
        }
        // ensure known password for local联调
        if (!passwordEncoder.matches("admin123", admin.getPasswordHash())) {
            admin.setPasswordHash(passwordEncoder.encode("admin123"));
            adminUserMapper.updateById(admin);
            log.info("Reset default admin password to admin123 (dev profile)");
        }
    }
}
