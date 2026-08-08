import { http } from './request'
import type { CreateFeedbackRequest, FeedbackStatus, PageQuery, PageResult } from '@/types/api'
import type { FeedbackDetail, FeedbackSummary } from '@/types/models'

/** §14.5.1 POST /feedbacks */
export function createFeedback(data: CreateFeedbackRequest) {
  return http<FeedbackSummary>({ url: '/feedbacks', method: 'POST', data })
}

/** §14.5.2 GET /feedbacks */
export function listMyFeedbacks(params: PageQuery & { status?: FeedbackStatus | '' }) {
  return http<PageResult<FeedbackSummary>>({
    url: '/feedbacks',
    method: 'GET',
    params: {
      page: params.page,
      pageSize: params.pageSize,
      ...(params.status ? { status: params.status } : {}),
    },
  })
}

/** §14.5.3 GET /feedbacks/{id} */
export function getMyFeedback(id: number | string) {
  return http<FeedbackDetail>({ url: `/feedbacks/${id}`, method: 'GET' })
}
