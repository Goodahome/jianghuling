package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.admin.domain.AuditLog;
import com.jianghu.ling.admin.mapper.AuditLogMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.security.AuthPrincipal;
import com.jianghu.ling.security.PrincipalType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogMapper auditLogMapper;

    public void log(String action, String detail) {
        AuditLog row = new AuditLog();
        row.setOperator(currentOperator());
        row.setAction(action);
        row.setDetail(detail);
        row.setCreatedAt(LocalDateTime.now());
        auditLogMapper.insert(row);
    }

    public PageResult<Map<String, Object>> page(long page, long pageSize,
                                                  String operator, String action, String keyword) {
        LambdaQueryWrapper<AuditLog> q = new LambdaQueryWrapper<AuditLog>().orderByDesc(AuditLog::getId);
        if (operator != null && !operator.isBlank()) {
            q.like(AuditLog::getOperator, operator.trim());
        }
        if (action != null && !action.isBlank()) {
            q.like(AuditLog::getAction, action.trim());
        }
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim();
            q.and(w -> w.like(AuditLog::getAction, kw).or().like(AuditLog::getDetail, kw));
        }
        Page<AuditLog> p = auditLogMapper.selectPage(new Page<>(page, pageSize), q);
        return PageResult.of(p.getRecords().stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("operator", a.getOperator());
            m.put("action", a.getAction());
            m.put("detail", a.getDetail());
            m.put("createdAt", a.getCreatedAt());
            return m;
        }).toList(), p.getTotal(), page, pageSize);
    }

    private String currentOperator() {
        AuthPrincipal principal = AuthContext.get();
        if (principal == null) {
            return "system";
        }
        if (principal.getType() == PrincipalType.ADMIN) {
            return "admin:" + principal.getId();
        }
        return "user:" + principal.getId();
    }
}
