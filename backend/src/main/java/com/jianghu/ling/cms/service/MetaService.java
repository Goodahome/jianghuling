package com.jianghu.ling.cms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianghu.ling.cms.domain.ChecklistTemplate;
import com.jianghu.ling.cms.domain.RewardSuggestConfig;
import com.jianghu.ling.cms.domain.WarrantFieldConfig;
import com.jianghu.ling.cms.mapper.ChecklistTemplateMapper;
import com.jianghu.ling.cms.mapper.RewardSuggestConfigMapper;
import com.jianghu.ling.cms.mapper.WarrantFieldConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MetaService {

    private static final Map<String, String> TEMPLATE_NAME = Map.of(
            "RENT_SEEK", "租房令状",
            "RENT_OUT", "出租令状",
            "RENT_TRANSFER", "转租令状"
    );
    private static final Map<String, String> TYPE_DISPLAY_NAME = Map.of(
            "RENT_SEEK", "租房悬赏",
            "RENT_OUT", "出租悬赏",
            "RENT_TRANSFER", "转租悬赏"
    );

    private final RewardSuggestConfigMapper rewardSuggestConfigMapper;
    private final ChecklistTemplateMapper checklistTemplateMapper;
    private final WarrantFieldConfigMapper warrantFieldConfigMapper;
    private final ConfigService configService;
    private final LevelConfigService levelConfigService;
    private final ObjectMapper objectMapper;

    public Map<String, Object> rewardSuggest() {
        List<RewardSuggestConfig> list = rewardSuggestConfigMapper.selectList(
                new LambdaQueryWrapper<RewardSuggestConfig>().orderByAsc(RewardSuggestConfig::getSortNo));
        List<Map<String, Object>> difficulties = list.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("code", c.getCode());
            m.put("name", c.getName());
            m.put("suggestMin", c.getSuggestMin());
            m.put("suggestMax", c.getSuggestMax());
            return m;
        }).collect(Collectors.toList());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("minReward", configService.getDecimal("min_reward", "200"));
        data.put("difficulties", difficulties);
        return data;
    }

    public List<Map<String, Object>> warrantTemplates() {
        List<WarrantFieldConfig> fields = warrantFieldConfigMapper.selectList(
                new LambdaQueryWrapper<WarrantFieldConfig>()
                        .eq(WarrantFieldConfig::getStatus, "ACTIVE")
                        .orderByAsc(WarrantFieldConfig::getTemplateCode)
                        .orderByAsc(WarrantFieldConfig::getSortNo));
        if (fields == null || fields.isEmpty()) {
            return fallbackWarrantTemplates();
        }
        Map<String, List<WarrantFieldConfig>> grouped = fields.stream()
                .collect(Collectors.groupingBy(WarrantFieldConfig::getTemplateCode, LinkedHashMap::new, Collectors.toList()));
        // 保证三套模板齐全；库缺 RENT_TRANSFER 时用 fallback 补齐
        LinkedHashMap<String, Map<String, Object>> byCode = new LinkedHashMap<>();
        for (String code : List.of("RENT_SEEK", "RENT_OUT", "RENT_TRANSFER")) {
            List<WarrantFieldConfig> list = grouped.get(code);
            if (list == null || list.isEmpty()) {
                byCode.put(code, fallbackTemplate(code));
            } else {
                byCode.put(code, toTemplateView(code, list));
            }
        }
        // 其它自定义模板（若有）追加在后
        for (Map.Entry<String, List<WarrantFieldConfig>> e : grouped.entrySet()) {
            if (!byCode.containsKey(e.getKey())) {
                byCode.put(e.getKey(), toTemplateView(e.getKey(), e.getValue()));
            }
        }
        return new ArrayList<>(byCode.values());
    }

    public String typeDisplayName(String type) {
        return TYPE_DISPLAY_NAME.getOrDefault(type, type == null ? "" : type);
    }

    public List<Map<String, Object>> checklistTemplates(String tags) {
        Set<String> tagSet = new HashSet<>();
        if (StringUtils.hasText(tags)) {
            for (String t : tags.split(",")) {
                if (StringUtils.hasText(t)) {
                    tagSet.add(t.trim());
                }
            }
        }
        List<ChecklistTemplate> all = checklistTemplateMapper.selectList(new LambdaQueryWrapper<ChecklistTemplate>()
                .eq(ChecklistTemplate::getStatus, "ACTIVE")
                .orderByAsc(ChecklistTemplate::getSortNo));
        List<Map<String, Object>> result = new ArrayList<>();
        for (ChecklistTemplate t : all) {
            List<String> itemTags = parseTags(t.getTagsJson());
            boolean match = tagSet.isEmpty() || itemTags.stream().anyMatch(tagSet::contains);
            if (!match) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("itemCode", t.getItemCode());
            m.put("itemName", t.getItemName());
            m.put("required", t.getRequired());
            m.put("tags", itemTags);
            m.put("preChecked", !tagSet.isEmpty());
            result.add(m);
        }
        return result;
    }

    public Map<String, Object> growthConfig() {
        Map<String, Object> data = new LinkedHashMap<>();
        int claimDayLimit = configService.getInt("claim_day_limit", 10);
        data.put("claimDayLimit", claimDayLimit);
        data.put("dailyClaimLimit", claimDayLimit);
        data.put("dailyFreeStamina", configService.getInt("daily_free_stamina", 5));
        data.put("claimStaminaCost", configService.getInt("claim_stamina_cost", 1));
        data.put("chivalryPerStamina", configService.getInt("chivalry_per_stamina", 10));
        data.put("submitCooldownSeconds", configService.getInt("submit_cooldown_seconds", 600));
        data.put("submitDayLimit", configService.getInt("submit_day_limit", 20));
        data.put("levels", levelConfigService.asViews().stream().map(v -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("level", v.get("level"));
            m.put("title", v.get("title"));
            m.put("minChivalry", v.get("minChivalry"));
            return m;
        }).collect(Collectors.toList()));
        return data;
    }

    public RewardSuggestConfig findDifficulty(String code) {
        return rewardSuggestConfigMapper.selectOne(new LambdaQueryWrapper<RewardSuggestConfig>()
                .eq(RewardSuggestConfig::getCode, code).last("LIMIT 1"));
    }

    public List<ChecklistTemplate> findChecklistByCodes(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        return checklistTemplateMapper.selectList(new LambdaQueryWrapper<ChecklistTemplate>()
                .in(ChecklistTemplate::getItemCode, codes)
                .eq(ChecklistTemplate::getStatus, "ACTIVE")
                .orderByAsc(ChecklistTemplate::getSortNo));
    }

    private List<String> parseTags(String json) {
        if (!StringUtils.hasText(json)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (Exception e) {
            return List.of();
        }
    }

    private Map<String, Object> toTemplateView(String code, List<WarrantFieldConfig> list) {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("type", code);
        t.put("code", code);
        t.put("name", TEMPLATE_NAME.getOrDefault(code, list.get(0).getTemplateName()));
        t.put("displayName", TYPE_DISPLAY_NAME.getOrDefault(code, list.get(0).getTemplateName()));
        List<Map<String, Object>> fieldViews = new ArrayList<>();
        for (WarrantFieldConfig f : list) {
            Map<String, Object> fv = new LinkedHashMap<>();
            fv.put("key", f.getFieldKey());
            fv.put("label", "extra".equals(f.getFieldKey()) ? "补充说明" : f.getLabel());
            fv.put("type", f.getFieldType());
            fv.put("required", Boolean.TRUE.equals(f.getRequired()));
            if (Boolean.TRUE.equals(f.getMaskUntilClaimed())) {
                fv.put("maskUntilClaimed", true);
            }
            fieldViews.add(fv);
        }
        t.put("fields", fieldViews);
        return t;
    }

    private List<Map<String, Object>> fallbackWarrantTemplates() {
        return List.of(
                fallbackTemplate("RENT_SEEK"),
                fallbackTemplate("RENT_OUT"),
                fallbackTemplate("RENT_TRANSFER")
        );
    }

    private Map<String, Object> fallbackTemplate(String code) {
        return switch (code) {
            case "RENT_OUT" -> rentOutTemplate();
            case "RENT_TRANSFER" -> rentTransferTemplate();
            default -> rentSeekTemplate();
        };
    }

    private Map<String, Object> rentSeekTemplate() {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("type", "RENT_SEEK");
        t.put("code", "RENT_SEEK");
        t.put("name", "租房令状");
        t.put("displayName", "租房悬赏");
        t.put("fields", List.of(
                field("district", "区域", "text", true, null),
                field("rentBudgetMin", "预算下限(元/月)", "number", true, null),
                field("rentBudgetMax", "预算上限(元/月)", "number", true, null),
                field("layout", "户型", "text", true, null),
                field("expectMoveInDate", "期望入住", "date", true, null),
                field("acceptAgency", "是否接受中介", "boolean", true, null),
                field("extra", "补充说明", "textarea", false, null)
        ));
        return t;
    }

    private Map<String, Object> rentOutTemplate() {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("type", "RENT_OUT");
        t.put("code", "RENT_OUT");
        t.put("name", "出租令状");
        t.put("displayName", "出租悬赏");
        t.put("fields", List.of(
                field("district", "区域", "text", true, null),
                field("exactAddress", "精确位置", "text", true, Map.of("maskUntilClaimed", true)),
                field("rentPrice", "租金(元/月)", "number", true, null),
                field("layout", "户型", "text", true, null),
                field("availableDate", "可入住日期", "date", true, null),
                field("furniture", "家具家电", "text", false, null),
                field("extra", "补充说明", "textarea", false, null)
        ));
        return t;
    }

    private Map<String, Object> rentTransferTemplate() {
        Map<String, Object> t = new LinkedHashMap<>();
        t.put("type", "RENT_TRANSFER");
        t.put("code", "RENT_TRANSFER");
        t.put("name", "转租令状");
        t.put("displayName", "转租悬赏");
        t.put("fields", List.of(
                field("district", "区域", "text", true, null),
                field("exactAddress", "精确位置", "text", true, Map.of("maskUntilClaimed", true)),
                field("rentPrice", "租金(元/月)", "number", true, null),
                field("layout", "户型", "text", true, null),
                field("availableDate", "可入住日期", "date", true, null),
                field("complianceNote", "转租合规说明", "textarea", false, null),
                field("furniture", "家具家电", "text", false, null),
                field("extra", "补充说明", "textarea", false, null)
        ));
        return t;
    }

    private Map<String, Object> field(String key, String label, String type, boolean required, Map<String, Object> extra) {
        Map<String, Object> f = new LinkedHashMap<>();
        f.put("key", key);
        f.put("label", label);
        f.put("type", type);
        f.put("required", required);
        if (extra != null) {
            f.putAll(extra);
        }
        return f;
    }
}
