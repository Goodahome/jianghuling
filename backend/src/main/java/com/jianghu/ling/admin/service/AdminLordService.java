package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.admin.domain.LordApplication;
import com.jianghu.ling.admin.domain.PlatformLord;
import com.jianghu.ling.admin.mapper.LordApplicationMapper;
import com.jianghu.ling.admin.mapper.PlatformLordMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminLordService {

    private final LordApplicationMapper lordApplicationMapper;
    private final PlatformLordMapper platformLordMapper;
    private final UserProfileMapper userProfileMapper;
    private final AuditService auditService;

    public Map<String, Object> current() {
        PlatformLord lord = platformLordMapper.selectOne(new LambdaQueryWrapper<PlatformLord>()
                .eq(PlatformLord::getStatus, "ACTIVE")
                .orderByDesc(PlatformLord::getId)
                .last("LIMIT 1"));
        if (lord == null) {
            return null;
        }
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", lord.getId());
        m.put("userId", lord.getUserId());
        m.put("nickname", nickname(lord.getUserId()));
        m.put("startAt", lord.getStartAt());
        m.put("status", lord.getStatus());
        return m;
    }

    @Transactional
    public void dismiss(String reason) {
        PlatformLord lord = platformLordMapper.selectOne(new LambdaQueryWrapper<PlatformLord>()
                .eq(PlatformLord::getStatus, "ACTIVE")
                .orderByDesc(PlatformLord::getId)
                .last("LIMIT 1"));
        if (lord == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "当前无盟主");
        }
        lord.setStatus("DISMISSED");
        lord.setEndAt(LocalDateTime.now());
        platformLordMapper.updateById(lord);
        auditService.log("LORD_DISMISS", "userId=" + lord.getUserId() + ", reason=" + reason);
    }

    public PageResult<Map<String, Object>> pageApplications(long page, long pageSize) {
        Page<LordApplication> p = lordApplicationMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<LordApplication>().orderByDesc(LordApplication::getId));
        return PageResult.of(p.getRecords().stream().map(this::toView).toList(), p.getTotal(), page, pageSize);
    }

    @Transactional
    public void approve(Long id) {
        LordApplication app = requirePending(id);
        Long adminId = AuthContext.requireAdminId();
        app.setStatus("APPROVED");
        app.setReviewerId(adminId);
        app.setUpdatedAt(LocalDateTime.now());
        lordApplicationMapper.updateById(app);

        List<PlatformLord> actives = platformLordMapper.selectList(new LambdaQueryWrapper<PlatformLord>()
                .eq(PlatformLord::getStatus, "ACTIVE"));
        for (PlatformLord old : actives) {
            old.setStatus("DISMISSED");
            old.setEndAt(LocalDateTime.now());
            platformLordMapper.updateById(old);
        }
        PlatformLord lord = new PlatformLord();
        lord.setUserId(app.getUserId());
        lord.setStartAt(LocalDateTime.now());
        lord.setStatus("ACTIVE");
        lord.setCreatedAt(LocalDateTime.now());
        platformLordMapper.insert(lord);
        auditService.log("LORD_APPROVE", "appId=" + id + ", userId=" + app.getUserId());
    }

    @Transactional
    public void reject(Long id, String reason) {
        LordApplication app = requirePending(id);
        app.setStatus("REJECTED");
        app.setReason(reason);
        app.setReviewerId(AuthContext.requireAdminId());
        app.setUpdatedAt(LocalDateTime.now());
        lordApplicationMapper.updateById(app);
        auditService.log("LORD_REJECT", "appId=" + id + ", reason=" + reason);
    }

    private LordApplication requirePending(Long id) {
        LordApplication app = lordApplicationMapper.selectById(id);
        if (app == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!"PENDING".equals(app.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "申请已处理");
        }
        return app;
    }

    private Map<String, Object> toView(LordApplication app) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", app.getId());
        m.put("userId", app.getUserId());
        m.put("nickname", nickname(app.getUserId()));
        m.put("statement", app.getStatement());
        m.put("status", app.getStatus());
        m.put("reason", app.getReason());
        m.put("createdAt", app.getCreatedAt());
        return m;
    }

    private String nickname(Long userId) {
        UserProfile p = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId).last("LIMIT 1"));
        return p == null ? "" : p.getNickname();
    }
}
