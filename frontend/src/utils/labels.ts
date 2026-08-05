import type { BountyStatus, BountyType, Difficulty, NoticeCategory } from '@/types/api'

export const bountyStatusLabel: Record<BountyStatus, string> = {
  PENDING_REVIEW: '待审核',
  OPEN: '张贴中',
  IN_COLLAB: '协作中',
  PENDING_SETTLE: '待结算',
  COMPLETED: '已完结',
  REJECTED: '审核驳回',
  CANCELLED: '已取消',
  IN_DISPUTE: '纠纷中',
}

export const bountyTypeLabel: Record<BountyType, string> = {
  RENT_SEEK: '求租',
  RENT_OUT: '出租/转租',
}

export const difficultyLabel: Record<Difficulty, string> = {
  EASY: '简易',
  NORMAL: '普通',
  HARD: '艰辛',
  EXTREME: '超难',
}

export const noticeCategoryLabel: Record<NoticeCategory, string> = {
  RULES: '平台规则',
  ANTI_FRAUD: '防骗须知',
  ZUNYI_RENT: '遵义租房须知',
  ANNOUNCE: '公告',
}

export const ledgerTypeLabel: Record<string, string> = {
  RECHARGE: '充值',
  FREEZE: '托管冻结',
  UNFREEZE_REFUND: '解冻退款',
  SETTLE_PAY: '结算扣款',
  SETTLE_INCOME: '结算入账',
  PLATFORM_FEE: '平台服务费',
  WITHDRAW: '提现',
  ADJUST: '调账',
}

export function formatAmount(v: number | undefined | null) {
  if (v === undefined || v === null || Number.isNaN(v)) return '0'
  return Number(v).toLocaleString('zh-CN')
}

export function clientRequestId(prefix = 'c') {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}
