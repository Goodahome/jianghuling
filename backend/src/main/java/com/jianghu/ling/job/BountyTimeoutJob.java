package com.jianghu.ling.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.bounty.domain.Bounty;
import com.jianghu.ling.bounty.mapper.BountyMapper;
import com.jianghu.ling.common.util.IdempotencyKeys;
import com.jianghu.ling.notify.service.NotifyService;
import com.jianghu.ling.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BountyTimeoutJob {

    private final BountyMapper bountyMapper;
    private final WalletService walletService;
    private final NotifyService notifyService;

    @Scheduled(fixedDelayString = "${app.jobs.timeout-ms:60000}")
    @Transactional
    public void cancelExpired() {
        List<Bounty> list = bountyMapper.selectList(new LambdaQueryWrapper<Bounty>()
                .in(Bounty::getStatus, List.of("OPEN", "IN_COLLAB", "PENDING_REVIEW"))
                .lt(Bounty::getDeadlineAt, LocalDateTime.now())
                .last("LIMIT 100"));
        for (Bounty bounty : list) {
            try {
                bounty.setStatus("CANCELLED");
                bounty.setCancelReason("超时自动取消");
                bounty.setUpdatedAt(LocalDateTime.now());
                bountyMapper.updateById(bounty);
                walletService.unfreezeRefund(bounty.getPublisherId(), bounty.getRewardAmount(),
                        IdempotencyKeys.bizNo("UR"), "BOUNTY", bounty.getId(), "超时自动退款");
                notifyService.send(bounty.getPublisherId(), "悬赏超时取消",
                        "悬赏「" + bounty.getTitle() + "」已超时取消，赏银已退回", "BOUNTY", bounty.getId());
                log.info("Auto cancelled bounty {}", bounty.getId());
            } catch (Exception e) {
                log.error("Failed to cancel bounty {}", bounty.getId(), e);
            }
        }
    }
}
