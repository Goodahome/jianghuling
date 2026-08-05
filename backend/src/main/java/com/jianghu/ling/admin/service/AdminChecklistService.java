package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianghu.ling.cms.domain.ChecklistTemplate;
import com.jianghu.ling.cms.mapper.ChecklistTemplateMapper;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminChecklistService {

    private final ChecklistTemplateMapper checklistTemplateMapper;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public PageResult<Map<String, Object>> page(long page, long pageSize) {
        Page<ChecklistTemplate> p = checklistTemplateMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<ChecklistTemplate>().orderByAsc(ChecklistTemplate::getSortNo)
                        .orderByAsc(ChecklistTemplate::getId));
        return PageResult.of(p.getRecords().stream().map(this::toView).toList(), p.getTotal(), page, pageSize);
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        ChecklistTemplate row = new ChecklistTemplate();
        apply(row, body, true);
        if (!StringUtils.hasText(row.getItemCode()) || !StringUtils.hasText(row.getItemName())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "itemCode/itemName必填");
        }
        if (checklistTemplateMapper.selectCount(new LambdaQueryWrapper<ChecklistTemplate>()
                .eq(ChecklistTemplate::getItemCode, row.getItemCode())) > 0) {
            throw new BizException(ErrorCode.CONFLICT, "itemCode已存在");
        }
        if (row.getRequired() == null) {
            row.setRequired(false);
        }
        if (row.getSortNo() == null) {
            row.setSortNo(0);
        }
        if (!StringUtils.hasText(row.getStatus())) {
            row.setStatus("ACTIVE");
        }
        checklistTemplateMapper.insert(row);
        auditService.log("CHECKLIST_CREATE", "code=" + row.getItemCode());
        return toView(row);
    }

    @Transactional
    public Map<String, Object> update(Long id, Map<String, Object> body) {
        ChecklistTemplate row = checklistTemplateMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        apply(row, body, false);
        checklistTemplateMapper.updateById(row);
        auditService.log("CHECKLIST_UPDATE", "id=" + id);
        return toView(row);
    }

    @Transactional
    public void delete(Long id) {
        ChecklistTemplate row = checklistTemplateMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        row.setStatus("INACTIVE");
        checklistTemplateMapper.updateById(row);
        auditService.log("CHECKLIST_DELETE", "id=" + id);
    }

    private void apply(ChecklistTemplate row, Map<String, Object> body, boolean creating) {
        if (body == null) {
            return;
        }
        if (creating || body.containsKey("itemCode")) {
            row.setItemCode(str(body.get("itemCode")));
        }
        if (body.containsKey("itemName")) {
            row.setItemName(str(body.get("itemName")));
        }
        if (body.containsKey("required")) {
            row.setRequired(asBool(body.get("required")));
        }
        if (body.containsKey("tags") || body.containsKey("tagsJson")) {
            Object tags = body.containsKey("tags") ? body.get("tags") : body.get("tagsJson");
            row.setTagsJson(toTagsJson(tags));
        }
        if (body.containsKey("sortNo")) {
            row.setSortNo(asInt(body.get("sortNo")));
        }
        if (body.containsKey("status")) {
            row.setStatus(str(body.get("status")));
        }
    }

    private Map<String, Object> toView(ChecklistTemplate t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", t.getId());
        m.put("itemCode", t.getItemCode());
        m.put("itemName", t.getItemName());
        m.put("required", Boolean.TRUE.equals(t.getRequired()));
        m.put("tags", parseTags(t.getTagsJson()));
        m.put("tagsJson", t.getTagsJson());
        m.put("sortNo", t.getSortNo());
        m.put("status", t.getStatus());
        return m;
    }

    private String toTagsJson(Object tags) {
        try {
            if (tags instanceof String s) {
                return s;
            }
            return objectMapper.writeValueAsString(tags == null ? List.of() : tags);
        } catch (Exception e) {
            return "[]";
        }
    }

    private List<String> parseTags(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, objectMapper.getTypeFactory()
                    .constructCollectionType(List.class, String.class));
        } catch (Exception e) {
            return List.of();
        }
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
