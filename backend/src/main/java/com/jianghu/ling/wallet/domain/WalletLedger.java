package com.jianghu.ling.wallet.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet_ledger")
public class WalletLedger {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String bizNo;
    private Long userId;
    private String type;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private BigDecimal frozenAfter;
    private String refType;
    private Long refId;
    private String remark;
    private LocalDateTime createdAt;
}
