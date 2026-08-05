package com.jianghu.ling.rank.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianghu.ling.admin.domain.LordApplication;
import com.jianghu.ling.admin.domain.PlatformLord;
import com.jianghu.ling.admin.mapper.LordApplicationMapper;
import com.jianghu.ling.admin.mapper.PlatformLordMapper;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.security.AuthContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LordService {

    private final LordApplicationMapper lordApplicationMapper;
    private final PlatformLordMapper platformLordMapper;
    private final RankService rankService;
    private final ConfigService configService;
    private final ObjectMapper objectMapper;

    @Transactional
    public Map<String, Object> apply(String statement) {
        Long userId = AuthContext.requireUserId();
        if (!StringUtils.hasText(statement)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "statement必填");
        }
        if (platformLordMapper.selectCount(new LambdaQueryWrapper<PlatformLord>()
                .eq(PlatformLord::getUserId, userId)
                .eq(PlatformLord::getStatus, "ACTIVE")) > 0) {
            throw new BizException(ErrorCode.BIZ_RULE, "你已是现任盟主");
        }
        if (lordApplicationMapper.selectCount(new LambdaQueryWrapper<LordApplication>()
                .eq(LordApplication::getUserId, userId)
                .eq(LordApplication::getStatus, "PENDING")) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "已有待审申请");
        }
        Long topId = rankService.topReputationUserId();
        if (topId == null || !topId.equals(userId)) {
            throw new BizException(ErrorCode.BIZ_RULE, "需为声望榜第1名方可申请");
        }
        LordApplication lastRejected = lordApplicationMapper.selectOne(new LambdaQueryWrapper<LordApplication>()
                .eq(LordApplication::getUserId, userId)
                .eq(LordApplication::getStatus, "REJECTED")
                .orderByDesc(LordApplication::getId)
                .last("LIMIT 1"));
        int cooldownDays = rejectCooldownDays();
        if (lastRejected != null && lastRejected.getUpdatedAt() != null
                && lastRejected.getUpdatedAt().plusDays(cooldownDays).isAfter(LocalDateTime.now())) {
            throw new BizException(ErrorCode.RATE_LIMITED, "驳回后冷却期内不可重复申请");
        }

        LordApplication app = new LordApplication();
        app.setUserId(userId);
        app.setStatement(statement.trim());
        app.setStatus("PENDING");
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        lordApplicationMapper.insert(app);
        return toView(app);
    }

    public Map<String, Object> mine() {
        Long userId = AuthContext.requireUserId();
        LordApplication app = lordApplicationMapper.selectOne(new LambdaQueryWrapper<LordApplication>()
                .eq(LordApplication::getUserId, userId)
                .orderByDesc(LordApplication::getId)
                .last("LIMIT 1"));
        return app == null ? null : toView(app);
    }

    private Map<String, Object> toView(LordApplication app) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", app.getId());
        m.put("statement", app.getStatement());
        m.put("status", app.getStatus());
        m.put("reason", app.getReason());
        m.put("createdAt", app.getCreatedAt());
        m.put("updatedAt", app.getUpdatedAt());
        return m;
    }

    private int rejectCooldownDays() {
        try {
            String raw = configService.get("ranks_config",
                    "{\"rejectCooldownDays\":7}");
            Map<String, Object> cfg = objectMapper.readValue(raw, new TypeReference<>() {
            });
            Object v = cfg.get("rejectCooldownDays");
            if (v instanceof Number n) {
                return n.intValue();
            }
            if (v != null) {
                return Integer.parseInt(String.valueOf(v));
            }
        } catch (Exception ignored) {
            // fallback
        }
        return 7;
    }
}
