package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.growth.domain.RedeemOrder;
import com.jianghu.ling.growth.domain.RewardProduct;
import com.jianghu.ling.growth.mapper.RedeemOrderMapper;
import com.jianghu.ling.growth.mapper.RewardProductMapper;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final RewardProductMapper rewardProductMapper;
    private final RedeemOrderMapper redeemOrderMapper;
    private final UserProfileMapper userProfileMapper;
    private final AuditService auditService;

    public PageResult<Map<String, Object>> pageProducts(long page, long pageSize) {
        Page<RewardProduct> p = rewardProductMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<RewardProduct>().orderByDesc(RewardProduct::getId));
        return PageResult.of(p.getRecords().stream().map(this::productView).toList(), p.getTotal(), page, pageSize);
    }

    @Transactional
    public Map<String, Object> createProduct(Map<String, Object> body) {
        RewardProduct row = new RewardProduct();
        applyProduct(row, body, true);
        if (!StringUtils.hasText(row.getName())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "name必填");
        }
        if (row.getCostChivalry() == null) {
            row.setCostChivalry(0);
        }
        if (row.getStock() == null) {
            row.setStock(0);
        }
        if (!StringUtils.hasText(row.getStatus())) {
            row.setStatus("ACTIVE");
        }
        row.setCreatedAt(LocalDateTime.now());
        row.setUpdatedAt(LocalDateTime.now());
        rewardProductMapper.insert(row);
        auditService.log("PRODUCT_CREATE", "id=" + row.getId());
        return productView(row);
    }

    @Transactional
    public Map<String, Object> updateProduct(Long id, Map<String, Object> body) {
        RewardProduct row = rewardProductMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        applyProduct(row, body, false);
        row.setUpdatedAt(LocalDateTime.now());
        rewardProductMapper.updateById(row);
        auditService.log("PRODUCT_UPDATE", "id=" + id);
        return productView(row);
    }

    @Transactional
    public void deleteProduct(Long id) {
        RewardProduct row = rewardProductMapper.selectById(id);
        if (row == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        row.setStatus("INACTIVE");
        row.setUpdatedAt(LocalDateTime.now());
        rewardProductMapper.updateById(row);
        auditService.log("PRODUCT_DELETE", "id=" + id);
    }

    public PageResult<Map<String, Object>> pageOrders(String status, long page, long pageSize) {
        LambdaQueryWrapper<RedeemOrder> q = new LambdaQueryWrapper<RedeemOrder>()
                .eq(StringUtils.hasText(status), RedeemOrder::getStatus, status)
                .orderByDesc(RedeemOrder::getId);
        Page<RedeemOrder> p = redeemOrderMapper.selectPage(new Page<>(page, pageSize), q);
        return PageResult.of(p.getRecords().stream().map(this::orderView).toList(), p.getTotal(), page, pageSize);
    }

    @Transactional
    public Map<String, Object> updateOrder(Long id, Map<String, Object> body) {
        RedeemOrder order = redeemOrderMapper.selectById(id);
        if (order == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        if (body != null && body.containsKey("status")) {
            String st = String.valueOf(body.get("status"));
            if (!Set.of("DONE", "PENDING", "SHIPPED", "CANCELLED").contains(st)) {
                throw new BizException(ErrorCode.PARAM_INVALID, "非法状态");
            }
            order.setStatus(st);
            order.setUpdatedAt(LocalDateTime.now());
            redeemOrderMapper.updateById(order);
            auditService.log("REDEEM_ORDER_UPDATE", "id=" + id + ", status=" + st);
        }
        return orderView(order);
    }

    private void applyProduct(RewardProduct row, Map<String, Object> body, boolean creating) {
        if (body == null) {
            return;
        }
        if (body.containsKey("name") || creating) {
            row.setName(str(body.get("name")));
        }
        if (body.containsKey("description")) {
            row.setDescription(str(body.get("description")));
        }
        if (body.containsKey("costChivalry")) {
            row.setCostChivalry(asInt(body.get("costChivalry")));
        }
        if (body.containsKey("stock")) {
            row.setStock(asInt(body.get("stock")));
        }
        if (body.containsKey("coverUrl")) {
            row.setCoverUrl(str(body.get("coverUrl")));
        }
        if (body.containsKey("status")) {
            row.setStatus(str(body.get("status")));
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
        m.put("status", p.getStatus());
        m.put("createdAt", p.getCreatedAt());
        m.put("updatedAt", p.getUpdatedAt());
        return m;
    }

    private Map<String, Object> orderView(RedeemOrder o) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", o.getId());
        m.put("userId", o.getUserId());
        m.put("nickname", nickname(o.getUserId()));
        m.put("productId", o.getProductId());
        RewardProduct product = rewardProductMapper.selectById(o.getProductId());
        m.put("productName", product == null ? null : product.getName());
        m.put("quantity", o.getQuantity());
        m.put("chivalryCost", o.getChivalryCost());
        m.put("status", o.getStatus());
        m.put("createdAt", o.getCreatedAt());
        m.put("updatedAt", o.getUpdatedAt());
        return m;
    }

    private String nickname(Long userId) {
        UserProfile p = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId).last("LIMIT 1"));
        return p == null ? "" : p.getNickname();
    }

    private String str(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private int asInt(Object v) {
        if (v instanceof Number n) {
            return n.intValue();
        }
        return Integer.parseInt(String.valueOf(v));
    }
}
