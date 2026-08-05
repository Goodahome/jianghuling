import { http } from './request'
import type { PageQuery, PageResult } from '@/types/api'
import type { SiteMessage } from '@/types/models'

export function listMessages(params: PageQuery & { unreadOnly?: boolean }) {
  return http<PageResult<SiteMessage>>({ url: '/messages', method: 'GET', params })
}

export function markRead(id: number | string) {
  return http<null>({ url: `/messages/${id}/read`, method: 'POST' })
}

export function markAllRead() {
  return http<null>({ url: '/messages/read-all', method: 'POST' })
}
