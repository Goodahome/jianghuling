package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.cms.domain.SysConfig;
import com.jianghu.ling.cms.mapper.SysConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminSystemService {

    private final SysConfigMapper sysConfigMapper;
    private final StringRedisTemplate redisTemplate;
    private final AuditService auditService;

    public Map<String, Object> getSystemConfig() {
        List<SysConfig> list = sysConfigMapper.selectList(null);
        Map<String, Object> data = new LinkedHashMap<>();
        for (SysConfig c : list) {
            data.put(c.getConfigKey(), c.getConfigValue());
        }
        return data;
    }

    @Transactional
    public void putSystemConfig(Map<String, Object> body) {
        if (body == null || body.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> e : body.entrySet()) {
            if (e.getKey() == null || e.getValue() == null) {
                continue;
            }
            String key = e.getKey();
            String value = String.valueOf(e.getValue());
            SysConfig existing = sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfig>()
                    .eq(SysConfig::getConfigKey, key).last("LIMIT 1"));
            if (existing == null) {
                SysConfig row = new SysConfig();
                row.setConfigKey(key);
                row.setConfigValue(value);
                row.setRemark("admin");
                sysConfigMapper.insert(row);
            } else {
                existing.setConfigValue(value);
                sysConfigMapper.updateById(existing);
            }
            redisTemplate.delete("config:" + key);
        }
        auditService.log("SYSTEM_CONFIG_UPDATE", "keys=" + body.keySet());
    }
}
