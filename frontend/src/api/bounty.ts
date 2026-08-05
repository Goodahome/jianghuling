import { http } from './request'
import type { BountyStatus, BountyType, Difficulty, PageQuery, PageResult } from '@/types/api'
import type {
  BountyDetail,
  BountyListItem,
  BountyMessage,
  SettlementPreview,
  Submission,
  SubmissionItemInput,
} from '@/types/models'

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
}) {
  return http<BountyDetail>({ url: '/bounties', method: 'POST', data })
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
  return http<Submission>({ url: `/bounties/${id}/submissions`, method: 'POST', data })
}

export function listClaimSubmissions(bountyId: number | string, claimId: number | string) {
  return http<Submission[]>({
    url: `/bounties/${bountyId}/claims/${claimId}/submissions`,
    method: 'GET',
  })
}

export function getSubmission(submissionId: number | string) {
  return http<Submission>({ url: `/submissions/${submissionId}`, method: 'GET' })
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

export function cancelBounty(id: number | string, reason: string) {
  return http<null>({ url: `/bounties/${id}/cancel`, method: 'POST', data: { reason } })
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
