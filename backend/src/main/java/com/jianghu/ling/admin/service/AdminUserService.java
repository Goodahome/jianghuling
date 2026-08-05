package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.admin.dto.AssetAdjustRequest;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.common.error.BizException;
import com.jianghu.ling.common.error.ErrorCode;
import com.jianghu.ling.common.util.IdempotencyKeys;
import com.jianghu.ling.security.AuthContext;
import com.jianghu.ling.user.domain.LoginLog;
import com.jianghu.ling.user.domain.User;
import com.jianghu.ling.user.domain.UserAsset;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.mapper.LoginLogMapper;
import com.jianghu.ling.user.mapper.UserMapper;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import com.jianghu.ling.user.service.UserAssetService;
import com.jianghu.ling.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserAssetService userAssetService;
    private final WalletService walletService;
    private final LoginLogMapper loginLogMapper;

    public PageResult<Map<String, Object>> page(String keyword, String status, long page, long pageSize) {
        LambdaQueryWrapper<User> q = new LambdaQueryWrapper<User>()
                .eq(StringUtils.hasText(status), User::getStatus, status)
                .and(StringUtils.hasText(keyword), w -> w.like(User::getUsername, keyword)
                        .or().like(User::getPhone, keyword))
                .orderByDesc(User::getId);
        Page<User> p = userMapper.selectPage(new Page<>(page, pageSize), q);
        return PageResult.of(p.getRecords().stream().map(u -> {
            UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                    .eq(UserProfile::getUserId, u.getId()).last("LIMIT 1"));
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", u.getId());
            m.put("username", u.getUsername());
            m.put("phone", u.getPhone());
            m.put("nickname", profile == null ? "" : profile.getNickname());
            m.put("status", u.getStatus());
            m.put("city", u.getCity());
            m.put("createdAt", u.getCreatedAt());
            return m;
        }).toList(), p.getTotal(), page, pageSize);
    }

    public Map<String, Object> detail(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, id).last("LIMIT 1"));
        UserAsset asset = userAssetService.getOrCreate(id);
        Map<String, Object> wallet = walletService.accountView(id);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("id", user.getId());
        data.put("username", user.getUsername());
        data.put("phone", user.getPhone());
        data.put("status", user.getStatus());
        data.put("remark", user.getRemark());
        data.put("profile", profile);
        data.put("asset", asset);
        data.put("wallet", wallet);
        data.put("level", userAssetService.levelOf(asset.getChivalry()));
        data.put("levelTitle", userAssetService.levelTitle(asset.getChivalry()));
        return data;
    }

    @Transactional
    public void setStatus(Long id, String status) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Transactional
    public void remark(Long id, String remark) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        user.setRemark(remark);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Transactional
    public void adjustAsset(Long id, AssetAdjustRequest req) {
        AuthContext.requireAdminId();
        userMapper.selectById(id);
        String type = req.getAssetType();
        if ("BALANCE".equalsIgnoreCase(type)) {
            walletService.adjustBalance(id, req.getDelta(), IdempotencyKeys.bizNo("ADJ"), req.getReason());
        } else if ("CHIVALRY".equalsIgnoreCase(type)) {
            userAssetService.addChivalry(id, req.getDelta().intValue());
        } else if ("STAMINA".equalsIgnoreCase(type)) {
            userAssetService.adjustStamina(id, req.getDelta().intValue());
        } else {
            throw new BizException(ErrorCode.PARAM_INVALID, "assetType无效");
        }
    }

    public PageResult<LoginLog> loginLogs(Long userId, long page, long pageSize) {
        Page<LoginLog> p = loginLogMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<LoginLog>()
                        .eq(LoginLog::getUserId, userId)
                        .orderByDesc(LoginLog::getId));
        return PageResult.of(p.getRecords(), p.getTotal(), page, pageSize);
    }

    public Map<String, Object> realName(Long userId) {
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId).last("LIMIT 1"));
        if (profile == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("realName", profile.getRealName());
        data.put("idNumber", profile.getIdNumber());
        data.put("status", profile.getRealNameStatus());
        return data;
    }

    @Transactional
    public Map<String, Object> updateRealNameStatus(Long userId, String status) {
        UserProfile profile = userProfileMapper.selectOne(new LambdaQueryWrapper<UserProfile>()
                .eq(UserProfile::getUserId, userId).last("LIMIT 1"));
        if (profile == null) {
            throw new BizException(ErrorCode.NOT_FOUND);
        }
        profile.setRealNameStatus(status);
        profile.setUpdatedAt(LocalDateTime.now());
        userProfileMapper.updateById(profile);
        return Map.of("status", status);
    }
}
