package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.cms.domain.WarrantFieldConfig;
import com.jianghu.ling.cms.mapper.WarrantFieldConfigMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminWarrantConfigService {

    private final WarrantFieldConfigMapper warrantFieldConfigMapper;
    private final AuditService auditService;

    public PageResult<Map<String, Object>> page(String templateCode, long page, long pageSize) {
        LambdaQueryWrapper<WarrantFieldConfig> q = new LambdaQueryWrapper<WarrantFieldConfig>()
                .eq(StringUtils.hasText(templateCode), WarrantFieldConfig::getTemplateCode, templateCode)
                .orderByAsc(WarrantFieldConfig::getTemplateCode)
                .orderByAsc(WarrantFieldConfig::getSortNo)
                .orderByAsc(WarrantFieldConfig::getId);
        Page<WarrantFieldConfig> p = warrantFieldConfigMapper.selectPage(new Page<>(page, pageSize), q);
        return PageResult.of(p.getRecords().stream().map(this::toView).toList(), p.getTotal(), page, pageSize);
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        WarrantFieldConfig row = new WarrantFieldConfig();
        apply(row, body, true);
        validate(row);
        if (warrantFieldConfigMapper.selectCount(new LambdaQueryWrapper<WarrantFieldConfig>()
                .eq(WarrantFieldConfig::getTemplateCode, row.getTemplateCode())
                .eq(WarrantFieldConfig::getFieldKey, row.getFieldKey())) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "字段已存在");
        }
        if (row.getRequired() == null) {
            row.setRequired(false);
        }
        if (row.getMaskUntilClaimed() == null) {
            row.setMaskUntilClaimed(false);
        }
        if (row.getSortNo() == null) {
            row.setSortNo(0);
        }
        if (!StringUtils.hasText(row.getStatus())) {
            row.setStatus("ACTIVE");
        }
        if (!StringUtils.hasText(row.getTemplateName())) {
            row.setTemplateName(row.getTemplateCode());
        }
        warrantFieldConfigMapper.insert(row);
        auditService.log("WARRANT_FIELD_CREATE", row.getTemplateCode() + "." + row.getFieldKey());
        return toView(row);
    }

    @Transactional
    public Map<String, Object> update(Long id, Map<String, Object> body) {
        WarrantFieldConfig row = warrantFieldConfigMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        apply(row, body, false);
        // 保护契约：extra 的 label 固定为补充说明
        if ("extra".equals(row.getFieldKey())) {
            row.setLabel("补充说明");
        }
        warrantFieldConfigMapper.updateById(row);
        auditService.log("WARRANT_FIELD_UPDATE", "id=" + id);
        return toView(row);
    }

    @Transactional
    public void delete(Long id) {
        WarrantFieldConfig row = warrantFieldConfigMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if ("extra".equals(row.getFieldKey())) {
            throw new BizException(ErrorCode.BIZ_RULE, "补充说明字段不可删除");
        }
        row.setStatus("INACTIVE");
        warrantFieldConfigMapper.updateById(row);
        auditService.log("WARRANT_FIELD_DELETE", "id=" + id);
    }

    private void validate(WarrantFieldConfig row) {
        if (!StringUtils.hasText(row.getTemplateCode()) || !StringUtils.hasText(row.getFieldKey())
                || !StringUtils.hasText(row.getLabel())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "templateCode/fieldKey/label必填");
        }
        if ("extra".equals(row.getFieldKey())) {
            row.setLabel("补充说明");
        }
    }

    private void apply(WarrantFieldConfig row, Map<String, Object> body, boolean creating) {
        if (body == null) {
            return;
        }
        if (creating || body.containsKey("templateCode")) {
            row.setTemplateCode(str(body.get("templateCode")));
        }
        if (body.containsKey("templateName")) {
            row.setTemplateName(str(body.get("templateName")));
        }
        if (creating || body.containsKey("fieldKey") || body.containsKey("key")) {
            Object key = body.containsKey("fieldKey") ? body.get("fieldKey") : body.get("key");
            row.setFieldKey(str(key));
        }
        if (body.containsKey("label")) {
            row.setLabel(str(body.get("label")));
        }
        if (body.containsKey("fieldType") || body.containsKey("type")) {
            Object type = body.containsKey("fieldType") ? body.get("fieldType") : body.get("type");
            row.setFieldType(str(type));
        }
        if (body.containsKey("required")) {
            row.setRequired(asBool(body.get("required")));
        }
        if (body.containsKey("maskUntilClaimed")) {
            row.setMaskUntilClaimed(asBool(body.get("maskUntilClaimed")));
        }
        if (body.containsKey("sortNo")) {
            row.setSortNo(asInt(body.get("sortNo")));
        }
        if (body.containsKey("status")) {
            row.setStatus(str(body.get("status")));
        }
    }

    private Map<String, Object> toView(WarrantFieldConfig c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("templateCode", c.getTemplateCode());
        m.put("templateName", c.getTemplateName());
        m.put("fieldKey", c.getFieldKey());
        m.put("key", c.getFieldKey());
        m.put("label", c.getLabel());
        m.put("fieldType", c.getFieldType());
        m.put("type", c.getFieldType());
        m.put("required", Boolean.TRUE.equals(c.getRequired()));
        m.put("maskUntilClaimed", Boolean.TRUE.equals(c.getMaskUntilClaimed()));
        m.put("sortNo", c.getSortNo());
        m.put("status", c.getStatus());
        return m;
    }

    private String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private boolean asBool(Object v) {
        if (v instanceof Boolean b) {
            return b;
        }
        return Boolean.parseBoolean(String.valueOf(v));
    }

    private int asInt(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v));
    }
}
