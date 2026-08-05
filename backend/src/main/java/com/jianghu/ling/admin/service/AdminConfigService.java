package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jianghu.ling.cms.domain.RewardSuggestConfig;
import com.jianghu.ling.cms.domain.SysConfig;
import com.jianghu.ling.cms.domain.UserLevelConfig;
import com.jianghu.ling.cms.mapper.RewardSuggestConfigMapper;
import com.jianghu.ling.cms.mapper.SysConfigMapper;
import com.jianghu.ling.cms.mapper.UserLevelConfigMapper;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.cms.service.LevelConfigService;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminConfigService {

    private static final String RANKS_KEY = "ranks_config";

    private final UserLevelConfigMapper userLevelConfigMapper;
    private final LevelConfigService levelConfigService;
    private final RewardSuggestConfigMapper rewardSuggestConfigMapper;
    private final SysConfigMapper sysConfigMapper;
    private final ConfigService configService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;

    public List<Map<String, Object>> getLevels() {
        return levelConfigService.asViews();
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> putLevels(Object body) {
        List<Map<String, Object>> items = asList(body);
        if (items.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "levels不能为空");
        }
        userLevelConfigMapper.delete(null);
        int sort = 1;
        for (Map<String, Object> item : items) {
            UserLevelConfig row = new UserLevelConfig();
            row.setLevel(asInt(item.get("level"), sort));
            row.setTitle(String.valueOf(item.getOrDefault("title", "等级" + row.getLevel())));
            row.setMinChivalry(asInt(item.get("minChivalry"), 0));
            Object priv = item.get("privilegesJson");
            row.setPrivilegesJson(priv == null ? "[]" : String.valueOf(priv));
            row.setSortNo(asInt(item.get("sortNo"), sort));
            userLevelConfigMapper.insert(row);
            sort++;
        }
        auditService.log("CONFIG_LEVELS_UPDATE", "count=" + items.size());
        return getLevels();
    }

    public Map<String, Object> getRanks() {
        String raw = configService.get(RANKS_KEY,
                "{\"refreshMinutes\":10,\"excludeBanned\":true,\"lordTopDisplay\":true,\"eligibleForLordTopN\":1,\"rejectCooldownDays\":7}");
        try {
            return objectMapper.readValue(raw, new TypeReference<>() {
            });
        } catch (Exception e) {
            Map<String, Object> fallback = new LinkedHashMap<>();
            fallback.put("refreshMinutes", 10);
            fallback.put("excludeBanned", true);
            fallback.put("lordTopDisplay", true);
            fallback.put("eligibleForLordTopN", 1);
            fallback.put("rejectCooldownDays", 7);
            return fallback;
        }
    }

    @Transactional
    public Map<String, Object> putRanks(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            throw new BizException(ErrorCode.PARAM_INVALID, "ranks配置不能为空");
        }
        try {
            upsertSys(RANKS_KEY, objectMapper.writeValueAsString(body), "英雄谱规则");
        } catch (Exception e) {
            throw new BizException(ErrorCode.PARAM_INVALID, "ranks配置非法");
        }
        auditService.log("CONFIG_RANKS_UPDATE", body.keySet().toString());
        return getRanks();
    }

    public Map<String, Object> getGrowth() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("claimDayLimit", configService.getInt("claim_day_limit", 10));
        data.put("dailyFreeStamina", configService.getInt("daily_free_stamina", 5));
        data.put("claimStaminaCost", configService.getInt("claim_stamina_cost", 1));
        data.put("chivalryPerStamina", configService.getInt("chivalry_per_stamina", 10));
        data.put("submitCooldownSeconds", configService.getInt("submit_cooldown_seconds", 600));
        data.put("submitDayLimit", configService.getInt("submit_day_limit", 20));
        data.put("chivalryPerComplete", configService.getInt("chivalry_per_complete", 10));
        data.put("inviteDailyQuota", configService.getInt("invite_daily_quota", 3));
        data.put("minReward", configService.getDecimal("min_reward", "200"));
        data.put("feeRate", configService.getDecimal("fee_rate", "0.10"));
        return data;
    }

    @Transactional
    public Map<String, Object> putGrowth(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return getGrowth();
        }
        putIfPresent(body, "claimDayLimit", "claim_day_limit");
        putIfPresent(body, "dailyFreeStamina", "daily_free_stamina");
        putIfPresent(body, "claimStaminaCost", "claim_stamina_cost");
        putIfPresent(body, "chivalryPerStamina", "chivalry_per_stamina");
        putIfPresent(body, "submitCooldownSeconds", "submit_cooldown_seconds");
        putIfPresent(body, "submitDayLimit", "submit_day_limit");
        putIfPresent(body, "chivalryPerComplete", "chivalry_per_complete");
        putIfPresent(body, "inviteDailyQuota", "invite_daily_quota");
        putIfPresent(body, "minReward", "min_reward");
        putIfPresent(body, "feeRate", "fee_rate");
        auditService.log("CONFIG_GROWTH_UPDATE", body.keySet().toString());
        return getGrowth();
    }

    public Map<String, Object> getRewardSuggest() {
        List<RewardSuggestConfig> list = rewardSuggestConfigMapper.selectList(
                new LambdaQueryWrapper<RewardSuggestConfig>().orderByAsc(RewardSuggestConfig::getSortNo));
        List<Map<String, Object>> difficulties = new ArrayList<>();
        for (RewardSuggestConfig c : list) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("code", c.getCode());
            m.put("name", c.getName());
            m.put("suggestMin", c.getSuggestMin());
            m.put("suggestMax", c.getSuggestMax());
            m.put("sortNo", c.getSortNo());
            difficulties.add(m);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("minReward", configService.getDecimal("min_reward", "200"));
        data.put("difficulties", difficulties);
        return data;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public Map<String, Object> putRewardSuggest(Map<String, Object> body) {
        if (body == null) {
            throw new BizException(ErrorCode.PARAM_INVALID);
        }
        if (body.containsKey("minReward")) {
            upsertSys("min_reward", String.valueOf(body.get("minReward")), "最低赏银");
        }
        Object diffs = body.get("difficulties");
        if (diffs instanceof List<?> list) {
            rewardSuggestConfigMapper.delete(null);
            int sort = 1;
            for (Object o : list) {
                if (!(o instanceof Map<?, ?> raw)) {
                    continue;
                }
                Map<String, Object> item = (Map<String, Object>) raw;
                String code = String.valueOf(item.get("code"));
                if (!StringUtils.hasText(code) || "null".equals(code)) {
                    throw new BizException(ErrorCode.PARAM_INVALID, "difficulty.code必填");
                }
                RewardSuggestConfig row = new RewardSuggestConfig();
                row.setCode(code);
                row.setName(String.valueOf(item.getOrDefault("name", code)));
                row.setSuggestMin(asDecimal(item.get("suggestMin"), "200"));
                row.setSuggestMax(asDecimal(item.get("suggestMax"), "300"));
                row.setSortNo(asInt(item.get("sortNo"), sort));
                rewardSuggestConfigMapper.insert(row);
                sort++;
            }
        }
        auditService.log("CONFIG_REWARD_SUGGEST_UPDATE", "ok");
        return getRewardSuggest();
    }

    private void putIfPresent(Map<String, Object> body, String jsonKey, String configKey) {
        if (!body.containsKey(jsonKey) || body.get(jsonKey) == null) {
            return;
        }
        upsertSys(configKey, String.valueOf(body.get(jsonKey)), "growth");
    }

    private void upsertSys(String key, String value, String remark) {
        SysConfig existing = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key).last("LIMIT 1"));
        if (existing == null) {
            SysConfig row = new SysConfig();
            row.setConfigKey(key);
            row.setConfigValue(value);
            row.setRemark(remark);
            sysConfigMapper.insert(row);
        } else {
            existing.setConfigValue(value);
            sysConfigMapper.updateById(existing);
        }
        redisTemplate.delete("config:" + key);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> asList(Object body) {
        if (body instanceof List<?> list) {
            List<Map<String, Object>> out = new ArrayList<>();
            for (Object o : list) {
                if (o instanceof Map<?, ?> m) {
                    out.add((Map<String, Object>) m);
                }
            }
            return out;
        }
        if (body instanceof Map<?, ?> map && map.containsKey("levels")) {
            return asList(map.get("levels"));
        }
        return List.of();
    }

    private int asInt(Object v, int def) {
        if (v == null) {
            return def;
        }
        if (v instanceof Number n) {
            return n.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (Exception e) {
            return def;
        }
    }

    private BigDecimal asDecimal(Object v, String def) {
        if (v == null) {
            return new BigDecimal(def);
        }
        return new BigDecimal(String.valueOf(v));
    }
}
