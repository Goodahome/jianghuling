package com.jianghu.ling.office.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.admin.domain.OfficeApplication;
import com.jianghu.ling.admin.mapper.OfficeApplicationMapper;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.office.domain.OfficeDef;
import com.jianghu.ling.office.mapper.OfficeDefMapper;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.user.domain.UserAsset;
import com.jianghu.ling.user.domain.UserOffice;
import com.jianghu.ling.user.mapper.UserOfficeMapper;
import com.jianghu.ling.user.service.UserAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OfficeService {

    private final OfficeDefMapper officeDefMapper;
    private final OfficeApplicationMapper officeApplicationMapper;
    private final UserOfficeMapper userOfficeMapper;
    private final UserAssetService userAssetService;

    public List<Map<String, Object>> defs() {
        Long userId = AuthContext.requireUserId();
        UserAsset asset = userAssetService.getOrCreate(userId);
        int chivalry = asset.getChivalry() == null ? 0 : asset.getChivalry();
        int level = userAssetService.levelOf(chivalry);
        List<OfficeDef> defs = officeDefMapper.selectList(new LambdaQueryWrapper<OfficeDef>()
                .eq(OfficeDef::getStatus, "ACTIVE"));
        return defs.stream().map(d -> {
            long holders = userOfficeMapper.selectCount(new LambdaQueryWrapper<UserOffice>()
                    .eq(UserOffice::getOfficeCode, d.getCode())
                    .eq(UserOffice::getStatus, "ACTIVE"));
            boolean holding = userOfficeMapper.selectCount(new LambdaQueryWrapper<UserOffice>()
                    .eq(UserOffice::getUserId, userId)
                    .eq(UserOffice::getOfficeCode, d.getCode())
                    .eq(UserOffice::getStatus, "ACTIVE")) > 0;
            boolean pending = officeApplicationMapper.selectCount(new LambdaQueryWrapper<OfficeApplication>()
                    .eq(OfficeApplication::getUserId, userId)
                    .eq(OfficeApplication::getOfficeCode, d.getCode())
                    .eq(OfficeApplication::getStatus, "PENDING")) > 0;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", d.getCode());
            m.put("name", d.getName());
            m.put("description", d.getName());
            m.put("minLevel", d.getMinLevel());
            m.put("quota", d.getQuota());
            m.put("termDays", d.getTermDays());
            m.put("canApply", !holding && !pending && level >= d.getMinLevel() && holders < d.getQuota());
            return m;
        }).collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> apply(String officeCode, String statement) {
        Long userId = AuthContext.requireUserId();
        if (!StringUtils.hasText(officeCode)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "officeCode必填");
        }
        OfficeDef def = officeDefMapper.selectOne(new LambdaQueryWrapper<OfficeDef>()
                .eq(OfficeDef::getCode, officeCode)
                .eq(OfficeDef::getStatus, "ACTIVE")
                .last("LIMIT 1"));
        if (def == null) {
            throw new BizException(ErrorCode.NOT_FOUND, "职司不存在");
        }
        UserAsset asset = userAssetService.getOrCreate(userId);
        int chivalry = asset.getChivalry() == null ? 0 : asset.getChivalry();
        if (userAssetService.levelOf(chivalry) < def.getMinLevel()) {
            throw new BizException(ErrorCode.BIZ_RULE, "等级不足");
        }
        if (officeApplicationMapper.selectCount(new LambdaQueryWrapper<OfficeApplication>()
                .eq(OfficeApplication::getUserId, userId)
                .eq(OfficeApplication::getOfficeCode, officeCode)
                .eq(OfficeApplication::getStatus, "PENDING")) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "已有待审申请");
        }
        OfficeApplication app = new OfficeApplication();
        app.setUserId(userId);
        app.setOfficeCode(officeCode);
        app.setStatement(statement);
        app.setStatus("PENDING");
        app.setCreatedAt(LocalDateTime.now());
        app.setUpdatedAt(LocalDateTime.now());
        officeApplicationMapper.insert(app);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", app.getId());
        data.put("status", app.getStatus());
        return data;
    }

    public List<Map<String, Object>> mineOffices() {
        Long userId = AuthContext.requireUserId();
        List<UserOffice> list = userOfficeMapper.selectList(new LambdaQueryWrapper<UserOffice>()
                .eq(UserOffice::getUserId, userId)
                .orderByDesc(UserOffice::getId));
        LocalDateTime now = LocalDateTime.now();
        Map<String, String> names = officeDefMapper.selectList(null).stream()
                .collect(Collectors.toMap(OfficeDef::getCode, OfficeDef::getName, (a, b) -> a));
        return list.stream().map(o -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", o.getOfficeCode());
            m.put("name", names.getOrDefault(o.getOfficeCode(), o.getOfficeCode()));
            String status = o.getStatus();
            if ("ACTIVE".equals(status) && o.getEndAt() != null && o.getEndAt().isBefore(now)) {
                status = "EXPIRED";
            }
            m.put("status", status);
            m.put("startAt", o.getStartAt());
            m.put("endAt", o.getEndAt());
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> mineApplications() {
        Long userId = AuthContext.requireUserId();
        return officeApplicationMapper.selectList(new LambdaQueryWrapper<OfficeApplication>()
                        .eq(OfficeApplication::getUserId, userId)
                        .orderByDesc(OfficeApplication::getId))
                .stream().map(a -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", a.getId());
                    m.put("officeCode", a.getOfficeCode());
                    m.put("statement", a.getStatement());
                    m.put("status", a.getStatus());
                    m.put("reason", a.getReason());
                    m.put("createdAt", a.getCreatedAt());
                    return m;
                }).collect(Collectors.toList());
    }
}
