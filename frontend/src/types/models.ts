import type {
  BountyStatus,
  BountyType,
  CancelOutcome,
  Difficulty,
  FeedbackStatus,
  FeedbackType,
  NoticeCategory,
  OfficeCode,
  SettlementKind,
  SubmissionStatus,
} from './api'

export interface UserBrief {
  id: number
  nickname: string
  avatarUrl: string
  level: number
  levelTitle: string
  offices: OfficeBrief[]
}

export interface OfficeBrief {
  code: OfficeCode | string
  name: string
  status: 'ACTIVE' | 'SUSPENDED' | 'EXPIRED'
}

export interface AuthResult {
  token: string
  expiresIn: number
  user: UserBrief
}

export interface MeProfile extends UserBrief {
  phone?: string
  username?: string
  bio?: string
  city?: string
  balance: number
  frozen: number
  chivalry: number
  stamina: number
  claimCountToday: number
  claimLimitToday: number
  reputationScore: number
  completedOrders: number
  goodRate: number
  isLord: boolean
  realNameStatus?: 'NONE' | 'PENDING' | 'VERIFIED' | 'REJECTED'
}

export interface WalletAccount {
  balance: number
  frozen: number
  currency: string
  simulated: boolean
  rechargeEnabled?: boolean
  withdrawEnabled?: boolean
}

export interface WalletLedger {
  id: number
  bizNo: string
  type: string
  amount: number
  balanceAfter: number
  frozenAfter: number
  remark?: string
  createdAt: string
}

export interface RewardSuggest {
  minReward: number
  difficulties: {
    code: Difficulty
    name: string
    suggestMin: number
    suggestMax: number
  }[]
}

export interface WarrantFieldDef {
  key: string
  label: string
  type: 'text' | 'number' | 'date' | 'select' | 'boolean' | 'textarea'
  required: boolean
  options?: { label: string; value: string | number | boolean }[]
  placeholder?: string
}

export interface WarrantTemplate {
  type: BountyType
  code?: string
  name: string
  /** 界面展示名：租房悬赏 / 出租悬赏 / 转租悬赏 */
  displayName?: string
  complianceNote?: string
  fields: WarrantFieldDef[]
}

export interface ChecklistTemplate {
  itemCode: string
  itemName: string
  required: boolean
  tags: string[]
}

export interface GrowthConfig {
  dailyClaimLimit: number
  dailyFreeStamina: number
  claimStaminaCost: number
  chivalryPerStamina: number
  levels: { level: number; title: string; minChivalry: number }[]
}

export interface Notice {
  id: number
  title: string
  category: NoticeCategory
  summary?: string
  content?: string
  pinned?: boolean
  createdAt: string
}

export interface BountyListItem {
  id: number
  type: BountyType
  /** 令种武侠展示名（优先于本地 labels） */
  typeDisplayName?: string
  title: string
  status: BountyStatus
  city: string
  district?: string
  difficulty: Difficulty
  rewardAmount: number
  deadlineAt: string
  claimCount: number
  publisherNickname?: string
  createdAt: string
  sourceBountyId?: number | null
  canRepublish?: boolean
  /** 我的悬赏：已提交成果条数 */
  submissionCount?: number
  /** 我的悬赏：协作会话未读（他人消息） */
  unreadCollabCount?: number
}

/** 详情 capabilities（api.md §7.9 / v1.0.7） */
export interface BountyCapabilities {
  canCancel: boolean
  canSendMessage: boolean
  canReadMessages: boolean
  canViewSubmissions: boolean
  canSubmit: boolean
  canSettle: boolean
  canQuitClaim: boolean
  canRepublish: boolean
  canDispute: boolean
}

export interface ChecklistItem {
  itemCode: string
  itemName: string
  required: boolean
  sort: number
}

export interface BountyDetail extends BountyListItem {
  publisherId: number
  taskTags: string[]
  warrantFields: Record<string, unknown>
  checklist: ChecklistItem[]
  claimedByMe: boolean
  claimId?: number
  isPublisher: boolean
  description?: string
  /** §7.2：是否存在任意成果提交（取消文案分支） */
  hasSubmissions?: boolean
  /** §7.2：有成果取消后待分配 */
  cancelAllocationPending?: boolean
  /** 当前用户相对本单的能力开关；按钮区以此为准 */
  capabilities?: BountyCapabilities
}

/** GET /bounties/{id}/submissions 列表项（api.md §8.2） */
export interface BountySubmissionListItem {
  submissionId: number
  bountyId: number
  claimId: number
  claimerUserId: number
  claimerNickname: string
  versionNo: number
  status: SubmissionStatus
  summary: string
  createdAt: string
  reviewedAt?: string | null
  reviewReason?: string | null
}

/** 执事堂 / Admin 成果审核列表项（§15.3 / §16.12.1） */
export interface ReviewSubmissionListItem extends BountySubmissionListItem {
  bountyTitle?: string | null
}

export interface RepublishDraft {
  sourceBountyId: number
  type: BountyType
  title: string
  difficulty: Difficulty
  rewardAmount: number
  deadlineAt?: string | null
  taskTags: string[]
  warrantFields: Record<string, unknown>
  checklistItemCodes: string[]
  suggestMin?: number
  minReward?: number
}

export interface BountyMessage {
  id: number
  bountyId: number
  senderId: number
  senderNickname: string
  content: string
  createdAt: string
}

/** 提交成果 Body 单项（§8.1） */
export interface SubmissionItemInput {
  itemCode: string
  itemName?: string
  done: boolean
  text?: string
  mediaUrls?: string[]
}

/** §8.0 成果详情 items[] 正文项（SSOT） */
export interface SubmissionItemDetail {
  itemCode: string
  itemName?: string | null
  done: boolean
  text?: string | null
  mediaUrls: string[]
}

/**
 * §8.0 SubmissionDetail — C 端 / 执事堂 / Admin 详情共用 SSOT。
 * 主键对外名为 submissionId（勿用裸 id）。
 */
export interface SubmissionDetail {
  submissionId: number
  bountyId: number
  bountyTitle?: string | null
  claimId: number
  claimerUserId: number
  claimerNickname: string
  versionNo: number
  status: SubmissionStatus
  summary: string
  items: SubmissionItemDetail[]
  reviewReason?: string | null
  reviewedAt?: string | null
  createdAt: string
  updatedAt?: string
}

/** @deprecated 请用 SubmissionDetail；保留别名避免旧引用断裂 */
export type Submission = SubmissionDetail

/** §9.3 取消悬赏成功响应 */
export interface CancelBountyResult {
  bountyId: number
  status: BountyStatus | string
  cancelOutcome: CancelOutcome
  hasSubmissions: boolean
  cancelAllocationPending: boolean
  settlementRequired: boolean
}

/** §9.1 结算预览 */
export interface SettlementPreview {
  bountyId?: number
  settlementKind?: SettlementKind
  rewardB: number
  feeRate: number
  fee: number
  distributable: number
  cancelAllocationPending?: boolean
  claimants: {
    userId: number
    nickname: string
    submissionCount?: number
    approvedSubmissionCount: number
  }[]
}

/** §16.12.3 审核成功响应（推荐形） */
export interface SubmissionReviewResult {
  submissionId: number
  status: SubmissionStatus
  reviewReason?: string | null
  reviewedAt?: string | null
}

export interface RankItem {
  rank: number
  userId: number
  nickname: string
  avatarUrl?: string
  score: number
  levelTitle?: string
}

export interface RankPage {
  list: RankItem[]
  total: number
  page: number
  pageSize: number
  lord?: {
    userId: number
    nickname: string
    avatarUrl?: string
    statement?: string
  } | null
}

export interface LevelProgress {
  level: number
  levelTitle: string
  chivalry: number
  nextLevel?: number
  nextTitle?: string
  nextMinChivalry?: number
  progress: number
  isLord: boolean
}

export interface Product {
  id: number
  name: string
  description?: string
  costChivalry: number
  stock: number
  coverUrl?: string
}

export interface OfficeDef {
  code: OfficeCode | string
  name: string
  description?: string
  minLevel: number
  quota: number
  termDays: number
  canApply: boolean
}

export interface SiteMessage {
  id: number
  title: string
  content: string
  read: boolean
  createdAt: string
  bizType?: string
  bizId?: number
}

export interface Dispute {
  id: number
  bountyId: number
  status: string
  reason: string
  deadlineAt?: string
  createdAt: string
}

/** §14.5 提交成功 / 列表摘要 */
export interface FeedbackSummary {
  id: number
  type: FeedbackType | string
  title: string
  status: FeedbackStatus | string
  createdAt: string
  updatedAt?: string
}

/** §14.5.3 C 端详情（不含 handleRemark） */
export interface FeedbackDetail extends FeedbackSummary {
  content: string
  contact?: string
  relatedRef?: string
  attachmentUrls?: string[]
}

/** §16.11.2 Admin 详情 */
export interface AdminFeedbackDetail extends FeedbackDetail {
  submitterId: number
  submitterNickname?: string
  handleRemark?: string | null
  statusChangedAt?: string | null
  statusChangedByAdminId?: number | null
  statusChangedByAdminName?: string | null
  statusHistory?: Array<{
    fromStatus: string | null
    toStatus: string
    adminId?: number | null
    adminName?: string | null
    remark?: string | null
    at: string
  }>
}

/** §16.11.1 Admin 列表项 */
export interface AdminFeedbackListItem {
  id: number
  type: FeedbackType | string
  title: string
  status: FeedbackStatus | string
  submitterId: number
  submitterNickname?: string
  createdAt: string
  updatedAt?: string
}

export interface AdminAuthResult {
  token: string
  expiresIn: number
  admin: {
    id: number
    username: string
    displayName?: string
    permissions: string[]
  }
}

export interface DashboardOverview {
  userCount: number
  pendingBountyReviews: number
  pendingSubmissionReviews: number
  disputeCount: number
  todayClaims: number
  frozenTotal: number
}
