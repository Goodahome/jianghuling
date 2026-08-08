import type {
  BountyStatus,
  BountyType,
  Difficulty,
  FeedbackStatus,
  FeedbackType,
  NoticeCategory,
  SubmissionStatus,
} from '@/types/api'

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

/** 场景化状态文案：榜=悬赏中；我的=进行中 */
export type BountyStatusScene = 'default' | 'plaza' | 'mine'

export function resolveBountyStatusLabel(
  status: BountyStatus | string | undefined | null,
  scene: BountyStatusScene = 'default',
) {
  if (!status) return '—'
  if (status === 'IN_COLLAB') {
    if (scene === 'plaza') return '悬赏中'
    if (scene === 'mine') return '进行中'
  }
  if (status in bountyStatusLabel) return bountyStatusLabel[status as BountyStatus]
  return String(status)
}

/** 我的悬赏列表排序：进行中优先 */
export function mineBountySortRank(status: string | undefined | null): number {
  switch (status) {
    case 'IN_COLLAB':
      return 0
    case 'PENDING_SETTLE':
      return 1
    case 'OPEN':
      return 2
    case 'PENDING_REVIEW':
      return 3
    case 'IN_DISPUTE':
      return 4
    case 'COMPLETED':
      return 5
    case 'CANCELLED':
      return 6
    case 'REJECTED':
      return 7
    default:
      return 9
  }
}

/** 令种界面武侠名（api.md §5.2）；禁止「求租」「出租/转租」 */
export const bountyTypeLabel: Record<BountyType, string> = {
  RENT_SEEK: '租房悬赏',
  RENT_OUT: '出租悬赏',
  RENT_TRANSFER: '转租悬赏',
}

export function resolveBountyTypeLabel(
  type: BountyType | string | undefined | null,
  typeDisplayName?: string | null,
) {
  const fromApi = (typeDisplayName || '').trim()
  if (fromApi) return fromApi
  if (type && type in bountyTypeLabel) return bountyTypeLabel[type as BountyType]
  return type || ''
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

/** §14.5.0 反馈类型展示名 */
export const feedbackTypeLabel: Record<FeedbackType, string> = {
  BUG: '缺陷反馈',
  SUGGEST: '功能建议',
  COMPLAINT: '投诉举报',
  OTHER: '其他',
}

/** §14.5.0 反馈状态展示名 */
export const feedbackStatusLabel: Record<FeedbackStatus, string> = {
  NEW: '待处理',
  PROCESSING: '处理中',
  RESOLVED: '已完结',
  CLOSED: '已关闭',
}

export function resolveFeedbackTypeLabel(type: string | undefined | null) {
  if (type && type in feedbackTypeLabel) return feedbackTypeLabel[type as FeedbackType]
  return type || '—'
}

export function resolveFeedbackStatusLabel(status: string | undefined | null) {
  if (status && status in feedbackStatusLabel) return feedbackStatusLabel[status as FeedbackStatus]
  return status || '—'
}

/** §8.0 成果状态展示 */
export const submissionStatusLabel: Record<SubmissionStatus, string> = {
  PENDING: '待审核',
  APPROVED: '已通过',
  REJECTED: '已驳回',
}

export function resolveSubmissionStatusLabel(status: string | undefined | null) {
  if (status && status in submissionStatusLabel) {
    return submissionStatusLabel[status as SubmissionStatus]
  }
  return status || '—'
}

export const ledgerTypeLabel: Record<string, string> = {
  REGISTER_GRANT: '注册赠银',
  INVITE_REWARD: '邀新奖励',
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

/** 令状字段 key → 中文兜底（模板未加载时） */
export const warrantFieldFallbackLabel: Record<string, string> = {
  district: '遵义区域',
  rentBudgetMin: '租金预算下限（元/月）',
  rentBudgetMax: '租金预算上限（元/月）',
  layout: '户型与面积意向',
  expectMoveInDate: '期望入住日',
  acceptAgency: '是否接受中介房',
  extra: '补充说明',
  exactAddress: '大致/精确位置',
  rentPrice: '租金（元/月）',
  availableDate: '可入住日',
  furniture: '家具家电',
}

export function formatWarrantValue(val: unknown): string {
  if (val === true) return '是'
  if (val === false) return '否'
  if (val === null || val === undefined) return ''
  if (typeof val === 'string') return val.trim()
  return String(val)
}

export function isWarrantValueEmpty(val: unknown): boolean {
  if (val === true || val === false) return false
  if (val === null || val === undefined) return true
  if (typeof val === 'string') return val.trim() === ''
  return false
}

export function clientRequestId(prefix = 'c') {
  return `${prefix}-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
}
