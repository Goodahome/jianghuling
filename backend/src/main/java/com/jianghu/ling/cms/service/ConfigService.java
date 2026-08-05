package com.jianghu.ling.cms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.cms.domain.SysConfig;
import com.jianghu.ling.cms.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ConfigService {

    private final SysConfigMapper sysConfigMapper;
    private final StringRedisTemplate redisTemplate;

    public String get(String key, String defaultValue) {
        String cacheKey = "config:" + key;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        SysConfig config = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                .eq(SysConfig::getConfigKey, key)
                .last("LIMIT 1"));
        String value = config == null ? defaultValue : config.getConfigValue();
        redisTemplate.opsForValue().set(cacheKey, value, Duration.ofMinutes(5));
        return value;
    }

    public int getInt(String key, int defaultValue) {
        try {
            return Integer.parseInt(get(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String raw = get(key, String.valueOf(defaultValue));
        if (raw == null) {
            return defaultValue;
        }
        String v = raw.trim().toLowerCase();
        if ("true".equals(v) || "1".equals(v) || "yes".equals(v)) {
            return true;
        }
        if ("false".equals(v) || "0".equals(v) || "no".equals(v)) {
            return false;
        }
        return defaultValue;
    }

    public BigDecimal getDecimal(String key, String defaultValue) {
        return new BigDecimal(get(key, defaultValue));
    }
}
