package com.jianghu.ling.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.bounty.domain.Bounty;
import com.jianghu.ling.bounty.domain.Submission;
import com.jianghu.ling.bounty.mapper.BountyMapper;
import com.jianghu.ling.bounty.mapper.SubmissionMapper;
import com.jianghu.ling.user.mapper.UserMapper;
import com.jianghu.ling.wallet.domain.WalletAccount;
import com.jianghu.ling.wallet.mapper.WalletAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserMapper userMapper;
    private final BountyMapper bountyMapper;
    private final SubmissionMapper submissionMapper;
    private final WalletAccountMapper walletAccountMapper;
    private final StringRedisTemplate redisTemplate;

    public Map<String, Object> overview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userCount", userMapper.selectCount(null));
        data.put("pendingBountyReviews", bountyMapper.selectCount(new LambdaQueryWrapper<Bounty>()
                .eq(Bounty::getStatus, "PENDING_REVIEW")));
        data.put("pendingSubmissionReviews", submissionMapper.selectCount(new LambdaQueryWrapper<Submission>()
                .eq(Submission::getStatus, "PENDING")));
        data.put("disputeCount", 0);
        data.put("todayClaims", todayClaimSum());
        BigDecimal frozen = walletAccountMapper.selectList(null).stream()
                .map(WalletAccount::getFrozen)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        data.put("totalFrozen", frozen);
        data.put("frozenTotal", frozen); // 与前端 DashboardOverview.frozenTotal 对齐
        return data;
    }

    private long todayClaimSum() {
        String prefix = "claim:day:" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ":";
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        long sum = 0;
        for (String key : keys) {
            String v = redisTemplate.opsForValue().get(key);
            if (v != null) {
                sum += Long.parseLong(v);
            }
        }
        return sum;
    }
}
