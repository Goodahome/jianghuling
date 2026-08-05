import { http } from './request'
import type { PageQuery, PageResult } from '@/types/api'

export function updateProfile(data: { nickname?: string; avatarUrl?: string; bio?: string }) {
  return http<null>({ url: '/user/profile', method: 'PUT', data })
}

export function submitRealName(data: { realName: string; idNumber: string }) {
  return http<{ status: string }>({ url: '/user/real-name', method: 'POST', data })
}

export function createInvite() {
  return http<{ code: string; link: string; remainQuotaToday: number }>({
    url: '/user/invites',
    method: 'POST',
  })
}

export function listMyInvites(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/user/invites',
    method: 'GET',
    params,
  })
}
