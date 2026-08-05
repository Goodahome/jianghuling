package com.jianghu.ling.cms.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.cms.domain.UserLevelConfig;
import com.jianghu.ling.cms.mapper.UserLevelConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LevelConfigService {

    private final UserLevelConfigMapper userLevelConfigMapper;

    public List<UserLevelConfig> listSorted() {
        List<UserLevelConfig> list = userLevelConfigMapper.selectList(new LambdaQueryWrapper<UserLevelConfig>()
                .orderByAsc(UserLevelConfig::getLevel));
        if (list == null || list.isEmpty()) {
            return defaults();
        }
        return list;
    }

    public List<Map<String, Object>> asViews() {
        List<Map<String, Object>> views = new ArrayList<>();
        for (UserLevelConfig c : listSorted()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("level", c.getLevel());
            m.put("title", c.getTitle());
            m.put("minChivalry", c.getMinChivalry());
            m.put("privilegesJson", c.getPrivilegesJson());
            m.put("sortNo", c.getSortNo());
            if (c.getId() != null) {
                m.put("id", c.getId());
            }
            views.add(m);
        }
        return views;
    }

    public int levelOf(int chivalry) {
        int level = 1;
        for (UserLevelConfig c : listSorted()) {
            if (chivalry >= (c.getMinChivalry() == null ? 0 : c.getMinChivalry())) {
                level = c.getLevel() == null ? level : c.getLevel();
            }
        }
        return level;
    }

    public String levelTitle(int chivalry) {
        String title = "初入江湖";
        for (UserLevelConfig c : listSorted()) {
            if (chivalry >= (c.getMinChivalry() == null ? 0 : c.getMinChivalry())) {
                title = c.getTitle();
            }
        }
        return title;
    }

    public UserLevelConfig nextAfter(int level) {
        return listSorted().stream()
                .filter(c -> c.getLevel() != null && c.getLevel() > level)
                .min(Comparator.comparing(UserLevelConfig::getLevel))
                .orElse(null);
    }

    public UserLevelConfig byLevel(int level) {
        return listSorted().stream()
                .filter(c -> c.getLevel() != null && c.getLevel() == level)
                .findFirst().orElse(null);
    }

    private List<UserLevelConfig> defaults() {
        List<UserLevelConfig> list = new ArrayList<>();
        list.add(def(1, "初入江湖", 0));
        list.add(def(2, "初显身手", 50));
        list.add(def(3, "小有名气", 200));
        list.add(def(4, "名扬江湖", 500));
        return list;
    }

    private UserLevelConfig def(int level, String title, int min) {
        UserLevelConfig c = new UserLevelConfig();
        c.setLevel(level);
        c.setTitle(title);
        c.setMinChivalry(min);
        c.setPrivilegesJson("[]");
        c.setSortNo(level);
        return c;
    }
}
