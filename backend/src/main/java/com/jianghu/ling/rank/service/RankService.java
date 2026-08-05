package com.jianghu.ling.rank.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.admin.domain.PlatformLord;
import com.jianghu.ling.admin.mapper.PlatformLordMapper;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.user.domain.UserAsset;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.mapper.UserAssetMapper;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import com.jianghu.ling.user.service.UserAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RankService {

    private final UserAssetMapper userAssetMapper;
    private final UserProfileMapper userProfileMapper;
    private final PlatformLordMapper platformLordMapper;
    private final UserAssetService userAssetService;

    public Map<String, Object> page(String type, long page, long pageSize) {
        String rankType = normalizeType(type);
        Page<UserAsset> p = userAssetMapper.selectPage(new Page<>(page, pageSize), orderWrapper(rankType));
        Map<Long, UserProfile> profiles = loadProfiles(p.getRecords().stream()
                .map(UserAsset::getUserId).filter(Objects::nonNull).collect(Collectors.toSet()));

        List<Map<String, Object>> list = new ArrayList<>();
        long base = (page - 1) * pageSize;
        for (int i = 0; i < p.getRecords().size(); i++) {
            UserAsset asset = p.getRecords().get(i);
            UserProfile profile = profiles.get(asset.getUserId());
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("rank", base + i + 1);
            row.put("userId", asset.getUserId());
            row.put("nickname", profile == null ? ("侠士" + asset.getUserId()) : profile.getNickname());
            row.put("avatarUrl", profile == null ? null : profile.getAvatarUrl());
            row.put("score", scoreOf(asset, rankType));
            row.put("levelTitle", userAssetService.levelTitle(asset.getChivalry() == null ? 0 : asset.getChivalry()));
            list.add(row);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("list", list);
        data.put("total", p.getTotal());
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("lord", currentLord());
        return data;
    }

    public Map<String, Object> mine() {
        Long userId = AuthContext.requireUserId();
        UserAsset asset = userAssetService.getOrCreate(userId);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("reputationRank", rankOf(userId, "REPUTATION"));
        data.put("chivalryRank", rankOf(userId, "CHIVALRY"));
        data.put("completedRank", rankOf(userId, "COMPLETED"));
        data.put("reputationScore", asset.getReputationScore() == null ? BigDecimal.ZERO : asset.getReputationScore());
        data.put("chivalry", asset.getChivalry() == null ? 0 : asset.getChivalry());
        data.put("completedOrders", asset.getCompletedOrders() == null ? 0 : asset.getCompletedOrders());
        return data;
    }

    public Long topReputationUserId() {
        UserAsset top = userAssetMapper.selectOne(orderWrapper("REPUTATION").last("LIMIT 1"));
        return top == null ? null : top.getUserId();
    }

    private long rankOf(Long userId, String type) {
        UserAsset me = userAssetMapper.selectOne(new LambdaQueryWrapper<UserAsset>()
                .eq(UserAsset::getUserId, userId).last("LIMIT 1"));
        if (me == null) {
            return 0;
        }
        long ahead;
        if ("CHIVALRY".equals(type)) {
            int score = me.getChivalry() == null ? 0 : me.getChivalry();
            ahead = userAssetMapper.selectCount(new LambdaQueryWrapper<UserAsset>()
                    .and(w -> w.gt(UserAsset::getChivalry, score)
                            .or(x -> x.eq(UserAsset::getChivalry, score).lt(UserAsset::getUserId, userId))));
        } else if ("COMPLETED".equals(type)) {
            int score = me.getCompletedOrders() == null ? 0 : me.getCompletedOrders();
            ahead = userAssetMapper.selectCount(new LambdaQueryWrapper<UserAsset>()
                    .and(w -> w.gt(UserAsset::getCompletedOrders, score)
                            .or(x -> x.eq(UserAsset::getCompletedOrders, score).lt(UserAsset::getUserId, userId))));
        } else {
            BigDecimal score = me.getReputationScore() == null ? BigDecimal.ZERO : me.getReputationScore();
            ahead = userAssetMapper.selectCount(new LambdaQueryWrapper<UserAsset>()
                    .and(w -> w.gt(UserAsset::getReputationScore, score)
                            .or(x -> x.eq(UserAsset::getReputationScore, score).lt(UserAsset::getUserId, userId))));
        }
        return ahead + 1;
    }

    private LambdaQueryWrapper<UserAsset> orderWrapper(String type) {
        LambdaQueryWrapper<UserAsset> q = new LambdaQueryWrapper<>();
        if ("CHIVALRY".equals(type)) {
            q.orderByDesc(UserAsset::getChivalry).orderByAsc(UserAsset::getUserId);
        } else if ("COMPLETED".equals(type)) {
            q.orderByDesc(UserAsset::getCompletedOrders).orderByAsc(UserAsset::getUserId);
        } else {
            q.orderByDesc(UserAsset::getReputationScore).orderByAsc(UserAsset::getUserId);
        }
        return q;
    }

    private BigDecimal scoreOf(UserAsset asset, String type) {
        if ("CHIVALRY".equals(type)) {
            return BigDecimal.valueOf(asset.getChivalry() == null ? 0 : asset.getChivalry());
        }
        if ("COMPLETED".equals(type)) {
            return BigDecimal.valueOf(asset.getCompletedOrders() == null ? 0 : asset.getCompletedOrders());
        }
        return asset.getReputationScore() == null ? BigDecimal.ZERO : asset.getReputationScore();
    }

    private Map<String, Object> currentLord() {
        PlatformLord lord = platformLordMapper.selectOne(new LambdaQueryWrapper<PlatformLord>()
                .eq(PlatformLord::getStatus, "ACTIVE")
                .orderByDesc(PlatformLord::getId)
                .last("LIMIT 1"));
        if (lord == null) {
            return null;
        }
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, lord.getUserId()).last("LIMIT 1"));
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", lord.getUserId());
        m.put("nickname", profile == null ? ("侠士" + lord.getUserId()) : profile.getNickname());
        m.put("avatarUrl", profile == null ? null : profile.getAvatarUrl());
        return m;
    }

    private Map<Long, UserProfile> loadProfiles(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        return userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                        .in(UserProfile::getUserId, userIds))
                .stream().collect(Collectors.toMap(UserProfile::getUserId, x -> x, (a, b) -> a));
    }

    private String normalizeType(String type) {
        if (type == null) {
            throw new BizException(ErrorCode.PARAM_INVALID, "type必填");
        }
        String t = type.trim().toUpperCase();
        if (!Set.of("REPUTATION", "CHIVALRY", "COMPLETED").contains(t)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "非法榜单类型");
        }
        return t;
    }
}
