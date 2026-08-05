package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.admin.domain.OfficeApplication;
import com.jianghu.ling.admin.mapper.OfficeApplicationMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.office.domain.OfficeDef;
import com.jianghu.ling.office.mapper.OfficeDefMapper;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.user.domain.UserOffice;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.mapper.UserOfficeMapper;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
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
public class AdminOfficeService {

    private final OfficeApplicationMapper officeApplicationMapper;
    private final UserOfficeMapper userOfficeMapper;
    private final UserProfileMapper userProfileMapper;
    private final OfficeDefMapper officeDefMapper;
    private final AuditService auditService;

    public List<Map<String, Object>> listDefs() {
        List<OfficeDef> defs = officeDefMapper.selectList(new LambdaQueryWrapper<OfficeDef>()
                .orderByAsc(OfficeDef::getId));
        List<Map<String, Object>> out = new ArrayList<>();
        for (OfficeDef d : defs) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", d.getId());
            m.put("code", d.getCode());
            m.put("name", d.getName());
            m.put("minLevel", d.getMinLevel());
            m.put("quota", d.getQuota());
            m.put("termDays", d.getTermDays());
            m.put("status", d.getStatus());
            long holders = userOfficeMapper.selectCount(new LambdaQueryWrapper<UserOffice>()
                    .eq(UserOffice::getOfficeCode, d.getCode())
                    .eq(UserOffice::getStatus, "ACTIVE"));
            m.put("holderCount", holders);
            out.add(m);
        }
        return out;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> putDefs(Object body) {
        List<Map<String, Object>> items = new ArrayList<>();
        if (body instanceof List<?> list) {
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    items.add((Map<String, Object>) m);
                }
            }
        } else if (body instanceof Map<?, ?> map) {
            if (map.containsKey("defs")) {
                return putDefs(map.get("defs"));
            }
            items.add((Map<String, Object>) map);
        }
        if (items.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "defs不能为空");
        }
        for (Map<String, Object> item : items) {
            String code = str(item.get("code"));
            if (!StringUtils.hasText(code)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "code必填");
            }
            OfficeDef existing = officeDefMapper.selectOne(new LambdaQueryWrapper<OfficeDef>()
                    .eq(OfficeDef::getCode, code).last("LIMIT 1"));
            if (existing == null) {
                OfficeDef row = new OfficeDef();
                row.setCode(code);
                row.setName(str(item.getOrDefault("name", code)));
                row.setMinLevel(asInt(item.get("minLevel"), 1));
                row.setQuota(asInt(item.get("quota"), 10));
                row.setTermDays(asInt(item.get("termDays"), 90));
                row.setStatus(str(item.getOrDefault("status", "ACTIVE")));
                officeDefMapper.insert(row);
            } else {
                if (item.containsKey("name")) {
                    existing.setName(str(item.get("name")));
                }
                if (item.containsKey("minLevel")) {
                    existing.setMinLevel(asInt(item.get("minLevel"), existing.getMinLevel()));
                }
                if (item.containsKey("quota")) {
                    existing.setQuota(asInt(item.get("quota"), existing.getQuota()));
                }
                if (item.containsKey("termDays")) {
                    existing.setTermDays(asInt(item.get("termDays"), existing.getTermDays()));
                }
                if (item.containsKey("status")) {
                    existing.setStatus(str(item.get("status")));
                }
                officeDefMapper.updateById(existing);
            }
        }
        auditService.log("OFFICE_DEFS_UPDATE", "count=" + items.size());
        return listDefs();
    }

    public PageResult<Map<String, Object>> pageApplications(long page, long pageSize) {
        Page<OfficeApplication> p = officeApplicationMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<OfficeApplication>().orderByDesc(OfficeApplication::getId));
        return PageResult.of(p.getRecords().stream().map(this::toView).toList(), p.getTotal(), page, pageSize);
    }

    @Transactional
    public void approve(Long id) {
        OfficeApplication app = requirePending(id);
        Long adminId = AuthContext.requireAdminId();
        app.setStatus("APPROVED");
        app.setReviewerId(adminId);
        app.setUpdatedAt(LocalDateTime.now());
        officeApplicationMapper.updateById(app);

        OfficeDef def = officeDefMapper.selectOne(new LambdaQueryWrapper<OfficeDef>()
                .eq(OfficeDef::getCode, app.getOfficeCode()).last("LIMIT 1"));
        int termDays = def == null || def.getTermDays() == null ? 90 : def.getTermDays();
        UserOffice office = new UserOffice();
        office.setUserId(app.getUserId());
        office.setOfficeCode(app.getOfficeCode());
        office.setStatus("ACTIVE");
        office.setStartAt(LocalDateTime.now());
        office.setEndAt(LocalDateTime.now().plusDays(termDays));
        office.setCreatedAt(LocalDateTime.now());
        userOfficeMapper.insert(office);
        auditService.log("OFFICE_APPROVE", "appId=" + id + ", userId=" + app.getUserId()
                + ", code=" + app.getOfficeCode());
    }

    @Transactional
    public void reject(Long id, String reason) {
        OfficeApplication app = requirePending(id);
        app.setStatus("REJECTED");
        app.setReason(reason);
        app.setReviewerId(AuthContext.requireAdminId());
        app.setUpdatedAt(LocalDateTime.now());
        officeApplicationMapper.updateById(app);
        auditService.log("OFFICE_REJECT", "appId=" + id + ", reason=" + reason);
    }

    @Transactional
    public void suspendHolder(Long id) {
        UserOffice office = requireHolder(id);
        office.setStatus("SUSPENDED");
        userOfficeMapper.updateById(office);
        auditService.log("OFFICE_SUSPEND", "holderId=" + id);
    }

    @Transactional
    public void revokeHolder(Long id) {
        UserOffice office = requireHolder(id);
        office.setStatus("REVOKED");
        office.setEndAt(LocalDateTime.now());
        userOfficeMapper.updateById(office);
        auditService.log("OFFICE_REVOKE", "holderId=" + id);
    }

    private UserOffice requireHolder(Long id) {
        UserOffice office = userOfficeMapper.selectById(id);
        if (office == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        return office;
    }

    private OfficeApplication requirePending(Long id) {
        OfficeApplication app = officeApplicationMapper.selectById(id);
        if (app == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (!"PENDING".equals(app.getStatus())) {
            throw new BizException(ErrorCode.BIZ_RULE, "申请已处理");
        }
        return app;
    }

    private Map<String, Object> toView(OfficeApplication app) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", app.getId());
        m.put("userId", app.getUserId());
        m.put("nickname", nickname(app.getUserId()));
        m.put("officeCode", app.getOfficeCode());
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

    private String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private int asInt(Object v, Integer def) {
        if (v == null) {
            return def == null ? 0 : def;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return def == null ? 0 : def;
        }
    }
}
