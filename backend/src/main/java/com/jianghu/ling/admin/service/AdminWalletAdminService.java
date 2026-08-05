package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jianghu.ling.common.api.PageResult;
import com.jianghu.ling.user.domain.User;
import com.jianghu.ling.user.domain.UserProfile;
import com.jianghu.ling.user.mapper.UserMapper;
import com.jianghu.ling.user.mapper.UserProfileMapper;
import com.jianghu.ling.wallet.domain.WalletLedger;
import com.jianghu.ling.wallet.mapper.WalletLedgerMapper;
import com.jianghu.ling.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminWalletAdminService {

    private final WalletLedgerMapper ledgerMapper;
    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;

    public PageResult<Map<String, Object>> pageLedgers(String type, long page, long pageSize) {
        Page<WalletLedger> p = ledgerMapper.selectPage(new Page<>(page, pageSize),
                new LambdaQueryWrapper<WalletLedger>()
                        .eq(StringUtils.hasText(type), WalletLedger::getType, type)
                        .orderByDesc(WalletLedger::getId));
        Set<Long> userIds = p.getRecords().stream()
                .map(WalletLedger::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Map<Long, String> names = resolveDisplayNames(userIds);
        List<Map<String, Object>> list = p.getRecords().stream().map(ledger -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", ledger.getId());
            m.put("bizNo", ledger.getBizNo());
            m.put("userId", ledger.getUserId());
            m.put("userName", names.getOrDefault(ledger.getUserId(), "用户#" + ledger.getUserId()));
            m.put("type", ledger.getType());
            m.put("amount", ledger.getAmount());
            m.put("balanceAfter", ledger.getBalanceAfter());
            m.put("frozenAfter", ledger.getFrozenAfter());
            m.put("refType", ledger.getRefType());
            m.put("refId", ledger.getRefId());
            m.put("remark", ledger.getRemark());
            m.put("createdAt", ledger.getCreatedAt());
            return m;
        }).toList();
        return PageResult.of(list, p.getTotal(), page, pageSize);
    }

    private Map<Long, String> resolveDisplayNames(Set<Long> userIds) {
        Map<Long, String> names = new HashMap<>();
        if (userIds.isEmpty()) {
            return names;
        }
        Map<Long, User> users = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        Map<Long, String> nicknames = userProfileMapper.selectList(new LambdaQueryWrapper<UserProfile>()
                        .in(UserProfile::getUserId, userIds)).stream()
                .filter(p -> StringUtils.hasText(p.getNickname()))
                .collect(Collectors.toMap(UserProfile::getUserId, UserProfile::getNickname, (a, b) -> a));
        for (Long id : userIds) {
            if (nicknames.containsKey(id)) {
                names.put(id, nicknames.get(id));
                continue;
            }
            User u = users.get(id);
            if (u != null && StringUtils.hasText(u.getUsername())) {
                names.put(id, u.getUsername());
            } else {
                names.put(id, "用户#" + id);
            }
        }
        return names;
    }

    public Map<String, Object> feeSummary() {
        BigDecimal total = BigDecimal.ZERO;
        long count = 0;
        for (WalletLedger ledger : ledgerMapper.selectList(new LambdaQueryWrapper<WalletLedger>()
                .eq(WalletLedger::getType, WalletService.PLATFORM_FEE))) {
            total = total.add(ledger.getAmount() == null ? BigDecimal.ZERO : ledger.getAmount());
            count++;
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("feeType", WalletService.PLATFORM_FEE);
        data.put("totalFee", total);
        data.put("count", count);
        data.put("currency", "两");
        data.put("simulated", true);
        return data;
    }
}
