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

export type BountyType = 'RENT_SEEK' | 'RENT_OUT' | 'RENT_TRANSFER'
export type Difficulty = 'EASY' | 'NORMAL' | 'HARD' | 'EXTREME'
export type NoticeCategory = 'RULES' | 'ANTI_FRAUD' | 'ZUNYI_RENT' | 'ANNOUNCE'
export type RankType = 'REPUTATION' | 'CHIVALRY' | 'COMPLETED'
export type OfficeCode = 'DECREE_REVIEWER' | 'FEAT_REVIEWER'
export type ReviewResult = 'APPROVE' | 'REJECT'
export type LoginType = 'PASSWORD' | 'SMS'
export type SmsScene = 'REGISTER' | 'LOGIN'

/** §8.0 / §8.2 成果审核状态 */
export type SubmissionStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

/** §9.3.1 令主取消分支 */
export type CancelOutcome = 'ALLOCATE' | 'REFUND'

/** §9.1 结算预览分支 */
export type SettlementKind = 'COMPLETE' | 'CANCEL_ALLOCATE'

/** §15.3 / §16.12.1 列表筛选：REVIEWED = APPROVED∪REJECTED */
export type SubmissionReviewFilter = SubmissionStatus | 'REVIEWED'

/** §14.5.0 用户反馈类型 */
export type FeedbackType = 'BUG' | 'SUGGEST' | 'COMPLAINT' | 'OTHER'

/** §14.5.0 用户反馈状态 */
export type FeedbackStatus = 'NEW' | 'PROCESSING' | 'RESOLVED' | 'CLOSED'

/** §14.5.1 提交反馈 Body */
export interface CreateFeedbackRequest {
  type: FeedbackType
  title: string
  content: string
  contact?: string
  relatedRef?: string
  attachmentUrls?: string[]
}

/** §16.11.3 改状态 Body */
export interface UpdateFeedbackStatusRequest {
  status: FeedbackStatus
  handleRemark?: string
}
