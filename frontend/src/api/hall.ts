import { http } from './request'
import type { PageQuery, PageResult, ReviewResult, SubmissionReviewFilter } from '@/types/api'
import type {
  BountyListItem,
  ReviewSubmissionListItem,
  SubmissionDetail,
  SubmissionReviewResult,
} from '@/types/models'
import { normalizeSubmissionDetail, normalizeSubmissionListItem } from '@/utils/submission'

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

/** §15.3 待审成果队列 */
export function listSubmissionReviews(
  params: PageQuery & { status?: SubmissionReviewFilter | string },
) {
  return http<PageResult<ReviewSubmissionListItem>>({
    url: '/hall/submission-reviews',
    method: 'GET',
    params,
  }).then((page) => ({
    ...page,
    list: (page?.list || []).map((row) => ({
      ...normalizeSubmissionListItem(row),
      bountyTitle: (row as ReviewSubmissionListItem).bountyTitle ?? null,
    })),
  }))
}

/** §15.3.1 验功详情（正文 = §8.0） */
export function getHallSubmission(submissionId: number | string) {
  return http<SubmissionDetail>({
    url: `/hall/submission-reviews/${submissionId}`,
    method: 'GET',
  }).then(normalizeSubmissionDetail)
}

/** §15.4 审核成果 */
export function reviewSubmission(
  submissionId: number | string,
  data: {
    result: ReviewResult
    reason?: string
    itemComments?: { itemCode: string; comment: string }[]
  },
) {
  return http<SubmissionReviewResult | null>({
    url: `/hall/submission-reviews/${submissionId}`,
    method: 'POST',
    data,
  })
}

export function listMyActions(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/hall/my-actions',
    method: 'GET',
    params,
  })
}
