package com.jianghu.ling.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.cms.service.LevelConfigService;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.user.domain.UserAsset;
import com.jianghu.ling.user.mapper.UserAssetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserAssetService {

    private final UserAssetMapper userAssetMapper;
    private final ConfigService configService;
    private final LevelConfigService levelConfigService;

    public UserAsset getOrCreate(Long userId) {
        UserAsset asset = userAssetMapper.selectOne(new LambdaQueryWrapper<UserAsset>()
                .eq(UserAsset::getUserId, userId)
                .last("LIMIT 1"));
        if (asset == null) {
            asset = new UserAsset();
            asset.setUserId(userId);
            asset.setChivalry(0);
            asset.setStamina(configService.getInt("daily_free_stamina", 5));
            asset.setStaminaDate(LocalDate.now());
            asset.setCompletedOrders(0);
            asset.setGoodRate(new BigDecimal("100.00"));
            asset.setReputationScore(BigDecimal.ZERO);
            asset.setCreatedAt(LocalDateTime.now());
            asset.setUpdatedAt(LocalDateTime.now());
            userAssetMapper.insert(asset);
        } else {
            ensureDailyStamina(asset);
        }
        return asset;
    }

    public void ensureDailyStamina(UserAsset asset) {
        LocalDate today = LocalDate.now();
        if (asset.getStaminaDate() != null && today.equals(asset.getStaminaDate())) {
            return;
        }
        int free = configService.getInt("daily_free_stamina", 5);
        if (asset.getStamina() == null || asset.getStamina() < free) {
            asset.setStamina(free);
        }
        asset.setStaminaDate(today);
        asset.setUpdatedAt(LocalDateTime.now());
        userAssetMapper.updateById(asset);
    }

    @Transactional
    public void consumeStamina(Long userId, int cost) {
        UserAsset asset = getOrCreate(userId);
        if (asset.getStamina() < cost) {
            throw new BizException(ErrorCode.STAMINA_INSUFFICIENT);
        }
        asset.setStamina(asset.getStamina() - cost);
        asset.setUpdatedAt(LocalDateTime.now());
        userAssetMapper.updateById(asset);
    }

    @Transactional
    public void addChivalry(Long userId, int delta) {
        if (delta == 0) {
            return;
        }
        UserAsset asset = getOrCreate(userId);
        asset.setChivalry(Math.max(0, asset.getChivalry() + delta));
        refreshReputation(asset);
        asset.setUpdatedAt(LocalDateTime.now());
        userAssetMapper.updateById(asset);
    }

    @Transactional
    public void adjustStamina(Long userId, int delta) {
        UserAsset asset = getOrCreate(userId);
        asset.setStamina(Math.max(0, asset.getStamina() + delta));
        asset.setUpdatedAt(LocalDateTime.now());
        userAssetMapper.updateById(asset);
    }

    @Transactional
    public void onOrderCompleted(Long userId) {
        UserAsset asset = getOrCreate(userId);
        asset.setCompletedOrders(asset.getCompletedOrders() + 1);
        refreshReputation(asset);
        asset.setUpdatedAt(LocalDateTime.now());
        userAssetMapper.updateById(asset);
    }

    @Transactional
    public void refreshGoodRate(Long userId, BigDecimal goodRate) {
        UserAsset asset = getOrCreate(userId);
        asset.setGoodRate(goodRate);
        refreshReputation(asset);
        asset.setUpdatedAt(LocalDateTime.now());
        userAssetMapper.updateById(asset);
    }

    private void refreshReputation(UserAsset asset) {
        // 完成单×10 + 好评率×100
        BigDecimal score = BigDecimal.valueOf(asset.getCompletedOrders() * 10L)
                .add(asset.getGoodRate().multiply(BigDecimal.valueOf(100)));
        asset.setReputationScore(score);
    }

    public String levelTitle(int chivalry) {
        return levelConfigService.levelTitle(chivalry);
    }

    public int levelOf(int chivalry) {
        return levelConfigService.levelOf(chivalry);
    }
}
