package com.jianghu.ling.growth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.admin.domain.PlatformLord;
import com.jianghu.ling.admin.mapper.PlatformLordMapper;
import com.jianghu.ling.cms.domain.UserLevelConfig;
import com.jianghu.ling.cms.service.ConfigService;
import com.jianghu.ling.cms.service.LevelConfigService;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.growth.domain.RedeemOrder;
import com.jianghu.ling.growth.domain.RewardProduct;
import com.jianghu.ling.growth.mapper.RedeemOrderMapper;
import com.jianghu.ling.growth.mapper.RewardProductMapper;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.user.domain.UserAsset;
import com.jianghu.ling.user.service.UserAssetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GrowthService {

    private final UserAssetService userAssetService;
    private final ConfigService configService;
    private final LevelConfigService levelConfigService;
    private final PlatformLordMapper platformLordMapper;
    private final RewardProductMapper rewardProductMapper;
    private final RedeemOrderMapper redeemOrderMapper;

    public Map<String, Object> levelProgress() {
        Long userId = AuthContext.requireUserId();
        UserAsset asset = userAssetService.getOrCreate(userId);
        int chivalry = asset.getChivalry() == null ? 0 : asset.getChivalry();
        int level = userAssetService.levelOf(chivalry);
        String title = userAssetService.levelTitle(chivalry);

        UserLevelConfig current = levelConfigService.byLevel(level);
        UserLevelConfig next = levelConfigService.nextAfter(level);
        Integer nextLevel = next == null ? null : next.getLevel();
        String nextTitle = next == null ? null : next.getTitle();
        Integer nextMin = next == null ? null : next.getMinChivalry();
        double progress = 1.0;
        if (next != null && current != null) {
            int curMin = current.getMinChivalry() == null ? 0 : current.getMinChivalry();
            int nxt = nextMin == null ? curMin : nextMin;
            if (nxt > curMin) {
                progress = Math.min(1.0, (chivalry - curMin) * 1.0 / (nxt - curMin));
            }
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("level", level);
        data.put("levelTitle", title);
        data.put("chivalry", chivalry);
        data.put("nextLevel", nextLevel);
        data.put("nextTitle", nextTitle);
        data.put("nextMinChivalry", nextMin);
        data.put("progress", progress);
        data.put("isLord", isLord(userId));
        return data;
    }

    @Transactional
    public Map<String, Object> exchangeStamina(int staminaPoints) {
        if (staminaPoints < 1) {
            throw new BizException(ErrorCode.PARAM_INVALID, "兑换点数须大于0");
        }
        Long userId = AuthContext.requireUserId();
        int rate = configService.getInt("chivalry_per_stamina", 10);
        int cost = staminaPoints * rate;
        UserAsset asset = userAssetService.getOrCreate(userId);
        if (asset.getChivalry() < cost) {
            throw new BizException(ErrorCode.BIZ_RULE, "侠义值不足");
        }
        userAssetService.addChivalry(userId, -cost);
        userAssetService.adjustStamina(userId, staminaPoints);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("staminaPoints", staminaPoints);
        data.put("chivalryCost", cost);
        data.put("stamina", userAssetService.getOrCreate(userId).getStamina());
        data.put("chivalry", userAssetService.getOrCreate(userId).getChivalry());
        return data;
    }

    public PageResult<Map<String, Object>> products(long page, long pageSize) {
        Page<RewardProduct> p = rewardProductMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<RewardProduct>()
                        .eq(RewardProduct::getStatus, "ACTIVE")
                        .orderByDesc(RewardProduct::getId));
        return PageResult.of(p.getRecords().stream().map(this::productView).toList(), p.getTotal(), page, pageSize);
    }

    @Transactional
    public Map<String, Object> redeem(long productId, int quantity) {
        if (quantity < 1) {
            throw new BizException(ErrorCode.PARAM_INVALID, "数量须大于0");
        }
        RewardProduct product = rewardProductMapper.selectById(productId);
        if (product == null || !"ACTIVE".equals(product.getStatus())) {
            throw new BizException(ErrorCode.NOT_FOUND, "奖品不存在");
        }
        if (product.getStock() == null || product.getStock() < quantity) {
            throw new BizException(ErrorCode.BIZ_RULE, "库存不足");
        }
        int cost = (product.getCostChivalry() == null ? 0 : product.getCostChivalry()) * quantity;
        Long userId = AuthContext.requireUserId();
        UserAsset asset = userAssetService.getOrCreate(userId);
        if (asset.getChivalry() < cost) {
            throw new BizException(ErrorCode.BIZ_RULE, "侠义值不足");
        }
        userAssetService.addChivalry(userId, -cost);
        product.setStock(product.getStock() - quantity);
        product.setUpdatedAt(LocalDateTime.now());
        rewardProductMapper.updateById(product);

        RedeemOrder order = new RedeemOrder();
        order.setUserId(userId);
        order.setProductId(productId);
        order.setQuantity(quantity);
        order.setChivalryCost(cost);
        order.setStatus("DONE");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        redeemOrderMapper.insert(order);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderId", order.getId());
        data.put("productId", productId);
        data.put("quantity", quantity);
        data.put("chivalryCost", cost);
        data.put("status", order.getStatus());
        return data;
    }

    public PageResult<Map<String, Object>> redeemOrders(long page, long pageSize) {
        Long userId = AuthContext.requireUserId();
        Page<RedeemOrder> p = redeemOrderMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<RedeemOrder>()
                        .eq(RedeemOrder::getUserId, userId)
                        .orderByDesc(RedeemOrder::getId));
        return PageResult.of(p.getRecords().stream().map(o -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", o.getId());
            m.put("productId", o.getProductId());
            RewardProduct product = rewardProductMapper.selectById(o.getProductId());
            m.put("productName", product == null ? null : product.getName());
            m.put("quantity", o.getQuantity());
            m.put("chivalryCost", o.getChivalryCost());
            m.put("status", o.getStatus());
            m.put("createdAt", o.getCreatedAt());
            return m;
        }).toList(), p.getTotal(), page, pageSize);
    }

    private boolean isLord(Long userId) {
        try {
            return platformLordMapper.selectCount(new LambdaQueryWrapper<PlatformLord>()
                    .eq(PlatformLord::getUserId, userId)
                    .eq(PlatformLord::getStatus, "ACTIVE")) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> productView(RewardProduct p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", p.getId());
        m.put("name", p.getName());
        m.put("description", p.getDescription());
        m.put("costChivalry", p.getCostChivalry());
        m.put("stock", p.getStock());
        m.put("coverUrl", p.getCoverUrl());
        return m;
    }
}
