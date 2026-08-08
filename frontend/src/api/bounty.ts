import { http } from './request'
import type { BountyStatus, BountyType, Difficulty, PageQuery, PageResult } from '@/types/api'
import type {
  BountyDetail,
  BountyListItem,
  BountyMessage,
  BountySubmissionListItem,
  CancelBountyResult,
  RepublishDraft,
  SettlementPreview,
  SubmissionDetail,
  SubmissionItemInput,
} from '@/types/models'
import { normalizeSubmissionDetail, normalizeSubmissionListItem } from '@/utils/submission'

export function listBounties(
  params: PageQuery & {
    type?: BountyType
    district?: string
    status?: string
    keyword?: string
  },
) {
  return http<PageResult<BountyListItem>>({ url: '/bounties', method: 'GET', params })
}

export function getBounty(id: number | string) {
  return http<BountyDetail>({ url: `/bounties/${id}`, method: 'GET' })
}

export function createBounty(data: {
  type: BountyType
  title: string
  difficulty: Difficulty
  rewardAmount: number
  confirmLowReward?: boolean
  deadlineAt: string
  taskTags: string[]
  warrantFields: Record<string, unknown>
  checklistItemCodes: string[]
  sourceBountyId?: number | null
}) {
  return http<BountyDetail>({ url: '/bounties', method: 'POST', data })
}

export function getRepublishDraft(id: number | string) {
  return http<RepublishDraft>({ url: `/bounties/${id}/republish-draft`, method: 'GET' })
}

export function republishBounty(
  id: number | string,
  data: {
    title?: string
    difficulty?: Difficulty
    rewardAmount?: number
    confirmLowReward?: boolean
    deadlineAt: string
    taskTags?: string[]
    warrantFields?: Record<string, unknown>
    checklistItemCodes?: string[]
  },
) {
  return http<{
    id: number
    sourceBountyId: number
    status: string
    canRepublish: boolean
  }>({
    url: `/bounties/${id}/republish`,
    method: 'POST',
    data,
  })
}

export function listMyPublished(params: PageQuery & { status?: BountyStatus }) {
  return http<PageResult<BountyListItem>>({
    url: '/bounties/mine/published',
    method: 'GET',
    params,
  })
}

export function listMyClaimed(params: PageQuery & { status?: BountyStatus }) {
  return http<PageResult<BountyListItem>>({
    url: '/bounties/mine/claimed',
    method: 'GET',
    params,
  })
}

export function claimBounty(id: number | string) {
  return http<{ claimId: number }>({ url: `/bounties/${id}/claims`, method: 'POST', data: {} })
}

/** 退出揭榜（capabilities.canQuitClaim）；路径对齐 POST /claims */
export function quitClaim(id: number | string) {
  return http<null>({ url: `/bounties/${id}/claims/quit`, method: 'POST', data: {} })
}

export function listMessages(id: number | string, params: PageQuery) {
  return http<PageResult<BountyMessage>>({
    url: `/bounties/${id}/messages`,
    method: 'GET',
    params,
  })
}

export function sendMessage(id: number | string, content: string) {
  return http<BountyMessage>({
    url: `/bounties/${id}/messages`,
    method: 'POST',
    data: { content },
  })
}

export function submitResult(
  id: number | string,
  data: { summary: string; items: SubmissionItemInput[] },
) {
  return http<SubmissionDetail>({ url: `/bounties/${id}/submissions`, method: 'POST', data })
}

/** 本令成果总览（令主全部 / 揭榜侠本人），api.md §8.2 */
export function listBountySubmissions(
  id: number | string,
  params?: PageQuery & { claimId?: number },
) {
  return http<PageResult<BountySubmissionListItem>>({
    url: `/bounties/${id}/submissions`,
    method: 'GET',
    params,
  }).then((page) => ({
    ...page,
    list: (page?.list || []).map(normalizeSubmissionListItem),
  }))
}

/** §8.3 某揭榜关系的成果版本列表 */
export function listClaimSubmissions(bountyId: number | string, claimId: number | string) {
  return http<BountySubmissionListItem[]>({
    url: `/bounties/${bountyId}/claims/${claimId}/submissions`,
    method: 'GET',
  }).then((list) => (list || []).map(normalizeSubmissionListItem))
}

/** §8.4 成果详情（C 端下钻） */
export function getSubmission(submissionId: number | string) {
  return http<SubmissionDetail>({ url: `/submissions/${submissionId}`, method: 'GET' }).then(
    normalizeSubmissionDetail,
  )
}

export function previewSettlement(id: number | string) {
  return http<SettlementPreview>({ url: `/bounties/${id}/settlement/preview`, method: 'GET' })
}

export function submitSettlement(
  id: number | string,
  items: { userId: number; amount: number; chivalryBonus?: number }[],
) {
  return http<null>({ url: `/bounties/${id}/settlement`, method: 'POST', data: { items } })
}

/** §9.3 令主取消；须按 cancelOutcome 分流 */
export function cancelBounty(id: number | string, reason: string) {
  return http<CancelBountyResult>({ url: `/bounties/${id}/cancel`, method: 'POST', data: { reason } })
}

export function submitEvaluation(
  id: number | string,
  data: { toUserId: number; score: number; content?: string },
) {
  return http<null>({ url: `/bounties/${id}/evaluations`, method: 'POST', data })
}

export function listEvaluations(id: number | string) {
  return http<Record<string, unknown>[]>({ url: `/bounties/${id}/evaluations`, method: 'GET' })
}
