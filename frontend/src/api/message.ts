import { http } from './request'
import type { PageQuery, PageResult } from '@/types/api'
import type { SiteMessage } from '@/types/models'

export function listMessages(params: PageQuery & { unreadOnly?: boolean }) {
  return http<PageResult<SiteMessage>>({ url: '/messages', method: 'GET', params })
}

export function getUnreadCount() {
  return http<{ count: number }>({ url: '/messages/unread-count', method: 'GET' })
}

export function markRead(id: number | string) {
  return http<{ id: number; read: boolean }>({ url: `/messages/${id}/read`, method: 'POST' })
}

export function markAllRead() {
  return http<{ updated: number }>({ url: '/messages/read-all', method: 'POST' })
}
