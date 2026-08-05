import { http } from './request'
import type { PageQuery, PageResult, ReviewResult } from '@/types/api'
import type { BountyListItem, Submission } from '@/types/models'

export function listBountyReviews(params: PageQuery & { status?: string }) {
  return http<PageResult<BountyListItem>>({
    url: '/hall/bounty-reviews',
    method: 'GET',
    params,
  })
}

export function reviewBounty(
  bountyId: number | string,
  data: { result: ReviewResult; reason?: string },
) {
  return http<null>({ url: `/hall/bounty-reviews/${bountyId}`, method: 'POST', data })
}

export function listSubmissionReviews(params: PageQuery & { status?: string }) {
  return http<PageResult<Submission & { bountyId?: number; bountyTitle?: string }>>({
    url: '/hall/submission-reviews',
    method: 'GET',
    params,
  })
}

export function reviewSubmission(
  submissionId: number | string,
  data: { result: ReviewResult; reason?: string; itemComments?: unknown[] },
) {
  return http<null>({ url: `/hall/submission-reviews/${submissionId}`, method: 'POST', data })
}

export function listMyActions(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/hall/my-actions',
    method: 'GET',
    params,
  })
}
