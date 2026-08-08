import { http } from './request'
import type { PageQuery, RankType } from '@/types/api'
import type { RankPage } from '@/types/models'

export function getRanks(type: RankType, params: PageQuery) {
  return http<RankPage>({ url: `/ranks/${type}`, method: 'GET', params })
}

export function getMyRank() {
  return http<{
    reputationRank?: number | null
    chivalryRank?: number | null
    completedRank?: number | null
    reputationScore?: number | string | null
    chivalry?: number | null
    completedOrders?: number | null
  }>({ url: '/ranks/me', method: 'GET' })
}

export function applyLord(statement: string) {
  return http<null>({ url: '/lord/applications', method: 'POST', data: { statement } })
}

export function getMyLordApplication() {
  return http<Record<string, unknown> | null>({ url: '/lord/applications/mine', method: 'GET' })
}
