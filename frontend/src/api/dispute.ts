import { http } from './request'
import type { PageQuery, PageResult } from '@/types/api'
import type { Dispute } from '@/types/models'

export function createDispute(
  bountyId: number | string,
  data: { reason: string; evidenceUrls?: string[]; evidenceText?: string },
) {
  return http<Dispute>({ url: `/bounties/${bountyId}/disputes`, method: 'POST', data })
}

export function getDispute(id: number | string) {
  return http<Dispute>({ url: `/disputes/${id}`, method: 'GET' })
}

export function listMyDisputes(params: PageQuery) {
  return http<PageResult<Dispute>>({ url: '/disputes/mine', method: 'GET', params })
}
