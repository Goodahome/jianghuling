import { http } from './request'
import type { NoticeCategory, PageQuery, PageResult } from '@/types/api'
import type { Notice } from '@/types/models'

export function listNotices(params: PageQuery & { category?: NoticeCategory }) {
  return http<PageResult<Notice>>({ url: '/notices', method: 'GET', params })
}

export function getNotice(id: number | string) {
  return http<Notice>({ url: `/notices/${id}`, method: 'GET' })
}

export function getTopNotices(category?: NoticeCategory, limit = 3) {
  return http<Notice[]>({
    url: '/notices/top',
    method: 'GET',
    params: { category, limit },
  })
}
