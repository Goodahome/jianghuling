/** 与 docs/api.md 对齐的通用类型 */

export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  list: T[]
  total: number
  page: number
  pageSize: number
}

export interface PageQuery {
  page?: number
  pageSize?: number
}

export type BountyStatus =
  | 'PENDING_REVIEW'
  | 'OPEN'
  | 'IN_COLLAB'
  | 'PENDING_SETTLE'
  | 'COMPLETED'
  | 'REJECTED'
  | 'CANCELLED'
  | 'IN_DISPUTE'

export type BountyType = 'RENT_SEEK' | 'RENT_OUT'
export type Difficulty = 'EASY' | 'NORMAL' | 'HARD' | 'EXTREME'
export type NoticeCategory = 'RULES' | 'ANTI_FRAUD' | 'ZUNYI_RENT' | 'ANNOUNCE'
export type RankType = 'REPUTATION' | 'CHIVALRY' | 'COMPLETED'
export type OfficeCode = 'DECREE_REVIEWER' | 'FEAT_REVIEWER'
export type ReviewResult = 'APPROVE' | 'REJECT'
export type LoginType = 'PASSWORD' | 'SMS'
export type SmsScene = 'REGISTER' | 'LOGIN'
