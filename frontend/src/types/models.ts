import type {
  BountyStatus,
  BountyType,
  Difficulty,
  NoticeCategory,
  OfficeCode,
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

export interface SubmissionItemInput {
  itemCode: string
  itemName?: string
  done: boolean
  text?: string
  mediaUrls?: string[]
}

export interface Submission {
  id: number
  claimId: number
  versionNo: number
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  contentSummary: string
  items?: SubmissionItemInput[]
  createdAt: string
}

export interface SettlementPreview {
  rewardB: number
  feeRate: number
  fee: number
  distributable: number
  claimants: {
    userId: number
    nickname: string
    approvedSubmissionCount: number
  }[]
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
