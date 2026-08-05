package com.jianghu.ling.job;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jianghu.ling.bounty.domain.Bounty;
import com.jianghu.ling.bounty.mapper.BountyMapper;
import com.jianghu.ling.notify.service.NotifyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class BountyRemindJob {

    private final BountyMapper bountyMapper;
    private final NotifyService notifyService;

    @Scheduled(fixedDelayString = "${app.jobs.remind-ms:300000}")
    public void remind() {
        LocalDateTime now = LocalDateTime.now();
        List<Bounty> list = bountyMapper.selectList(new LambdaQueryWrapper<Bounty>()
                .in(Bounty::getStatus, List.of("OPEN", "IN_COLLAB"))
                .gt(Bounty::getDeadlineAt, now)
                .last("LIMIT 200"));
        for (Bounty bounty : list) {
            LocalDateTime deadline = bounty.getDeadlineAt();
            if (!Boolean.TRUE.equals(bounty.getRemind24hSent())
                    && !deadline.isAfter(now.plusHours(24))
                    && deadline.isAfter(now.plusHours(2))) {
                notifyService.send(bounty.getPublisherId(), "截止提醒 T-24h",
                        "悬赏「" + bounty.getTitle() + "」将在24小时内截止", "BOUNTY", bounty.getId());
                bounty.setRemind24hSent(true);
                bountyMapper.updateById(bounty);
            }
            if (!Boolean.TRUE.equals(bounty.getRemind2hSent())
                    && !deadline.isAfter(now.plusHours(2))) {
                notifyService.send(bounty.getPublisherId(), "截止提醒 T-2h",
                        "悬赏「" + bounty.getTitle() + "」将在2小时内截止", "BOUNTY", bounty.getId());
                bounty.setRemind2hSent(true);
                bountyMapper.updateById(bounty);
            }
        }
    }
}
