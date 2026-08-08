package com.jianghu.ling.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    OK(0, "ok"),
    PARAM_INVALID(40001, "参数错误"),
    BIZ_RULE(40002, "业务规则不满足"),
    UNAUTHORIZED(40100, "未登录或Token无效"),
    TOKEN_EXPIRED(40101, "Token过期"),
    FORBIDDEN(40300, "无权限"),
    ACCOUNT_BANNED(40301, "账号封禁"),
    OFFICE_FORBIDDEN(40310, "职司无权或未授予"),
    NOT_FOUND(40400, "资源不存在"),
    CONFLICT(40900, "冲突"),
    RATE_LIMITED(42900, "限流"),
    INTERNAL(50000, "服务内部错误"),

    WALLET_INSUFFICIENT(42001, "余额不足"),
    WALLET_FREEZE_FAIL(42002, "冻结或解冻失败"),
    WALLET_SETTLE_INVALID(42003, "结算分配未分完或不合法"),
    WALLET_FEATURE_DISABLED(42004, "充值/提现暂未开放"),

    BOUNTY_REWARD_TOO_LOW(43001, "赏银低于最低限制"),
    BOUNTY_WARRANT_INVALID(43002, "令状字段不完整"),
    STAMINA_INSUFFICIENT(43003, "体力不足"),
    CLAIM_DAY_LIMIT(43004, "超出每日揭榜上限"),
    CLAIM_NOT_ALLOWED(43005, "不可揭榜"),
    SUBMISSION_INVALID(43006, "成果提交被限流或内容非法"),
    BOUNTY_REPUBLISH_DENIED(43007, "不可再发一令"),
    MESSAGE_NOT_ALLOWED(43008, "当前状态不可发送协作消息"),
    SUBMISSION_STATUS_DENIED(43009, "当前状态不可提交成果"),
    CANCEL_REFUND_WITH_SUBMISSIONS(43010, "本令已有成果提交，禁止全额退回取消（须走分配分支）"),
    CANCEL_ALLOCATE_PENDING(43011, "有成果取消待分配未完成（不可重复取消或错分支结案）"),

    INVITE_INVALID(44001, "邀请码无效/已用尽/过期"),

    FEEDBACK_COOLDOWN(45001, "反馈提交过于频繁（短时冷却未到）"),
    FEEDBACK_DAILY_LIMIT(45002, "今日反馈条数已达上限");

    private final int code;
    private final String message;
}
