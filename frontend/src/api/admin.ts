import { http } from './request'
import type { PageQuery, PageResult } from '@/types/api'
import type { AdminAuthResult, DashboardOverview } from '@/types/models'

export function adminLogin(username: string, password: string) {
  return http<AdminAuthResult>({
    url: '/admin/auth/login',
    method: 'POST',
    data: { username, password },
    headers: { 'X-Admin': '1' },
  })
}

export function adminLogout() {
  return http<null>({
    url: '/admin/auth/logout',
    method: 'POST',
    headers: { 'X-Admin': '1' },
  })
}

export function adminMe() {
  return http<AdminAuthResult['admin']>({
    url: '/admin/auth/me',
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function getDashboard() {
  return http<DashboardOverview>({
    url: '/admin/dashboard/overview',
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminListUsers(params: PageQuery & Record<string, unknown>) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/users',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

export function adminGetUser(id: number | string) {
  return http<Record<string, unknown>>({
    url: `/admin/users/${id}`,
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminUserAction(
  id: number | string,
  action: 'disable' | 'enable' | 'ban' | 'unban',
  data?: Record<string, unknown>,
) {
  return http<null>({
    url: `/admin/users/${id}/${action}`,
    method: 'POST',
    data,
    headers: { 'X-Admin': '1' },
  })
}

export function adminAdjustAssets(
  id: number | string,
  data: { assetType: string; delta: number; reason: string },
) {
  return http<null>({
    url: `/admin/users/${id}/assets/adjust`,
    method: 'POST',
    data,
    headers: { 'X-Admin': '1' },
  })
}

export function adminListInvites(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/invites',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

export function adminCreateInvites(data: Record<string, unknown>) {
  return http<null>({
    url: '/admin/invites',
    method: 'POST',
    data,
    headers: { 'X-Admin': '1' },
  })
}

export function adminInvalidateInvite(id: number | string) {
  return http<null>({
    url: `/admin/invites/${id}/invalidate`,
    method: 'POST',
    headers: { 'X-Admin': '1' },
  })
}

export function adminListBounties(params: PageQuery & Record<string, unknown>) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/bounties',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

export function adminGetBounty(id: number | string) {
  return http<Record<string, unknown>>({
    url: `/admin/bounties/${id}`,
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminForceCloseBounty(id: number | string, reason?: string) {
  return http<null>({
    url: `/admin/bounties/${id}/force-close`,
    method: 'POST',
    data: { reason },
    headers: { 'X-Admin': '1' },
  })
}

export function adminReviewBounty(
  bountyId: number | string,
  data: { result: string; reason?: string },
) {
  return http<null>({
    url: `/admin/bounty-reviews/${bountyId}`,
    method: 'POST',
    data,
    headers: { 'X-Admin': '1' },
  })
}

export function adminReviewSubmission(
  submissionId: number | string,
  data: { result: string; reason?: string },
) {
  return http<null>({
    url: `/admin/submission-reviews/${submissionId}`,
    method: 'POST',
    data,
    headers: { 'X-Admin': '1' },
  })
}

export function adminListLedgers(params: PageQuery & Record<string, unknown>) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/wallet/ledgers',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

export function adminFeeSummary() {
  return http<Record<string, unknown>>({
    url: '/admin/wallet/fee-summary',
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminListDisputes(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/disputes',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

export function adminGetDispute(id: number | string) {
  return http<Record<string, unknown>>({
    url: `/admin/disputes/${id}`,
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminVerdictDispute(id: number | string, data: Record<string, unknown>) {
  return http<null>({
    url: `/admin/disputes/${id}/verdict`,
    method: 'POST',
    data,
    headers: { 'X-Admin': '1' },
  })
}

export function adminListNotices(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/notices',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

export function adminSaveNotice(data: Record<string, unknown>, id?: number | string) {
  return http<null>({
    url: id ? `/admin/notices/${id}` : '/admin/notices',
    method: id ? 'PUT' : 'POST',
    data,
    headers: { 'X-Admin': '1' },
  })
}

export function adminListOfficeApplications(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/offices/applications',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

export function adminApproveOffice(id: number | string) {
  return http<null>({
    url: `/admin/offices/applications/${id}/approve`,
    method: 'POST',
    headers: { 'X-Admin': '1' },
  })
}

export function adminRejectOffice(id: number | string, reason?: string) {
  return http<null>({
    url: `/admin/offices/applications/${id}/reject`,
    method: 'POST',
    data: { reason },
    headers: { 'X-Admin': '1' },
  })
}

export function adminListLordApplications(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/lord/applications',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

export function adminApproveLord(id: number | string) {
  return http<null>({
    url: `/admin/lord/applications/${id}/approve`,
    method: 'POST',
    headers: { 'X-Admin': '1' },
  })
}

export function adminRejectLord(id: number | string, reason?: string) {
  return http<null>({
    url: `/admin/lord/applications/${id}/reject`,
    method: 'POST',
    data: { reason },
    headers: { 'X-Admin': '1' },
  })
}

export function adminListAuditLogs(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/audit-logs',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

export function adminGetSystemConfig() {
  return http<Record<string, unknown>>({
    url: '/admin/configs/system',
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminPutSystemConfig(data: Record<string, unknown>) {
  return http<null>({
    url: '/admin/configs/system',
    method: 'PUT',
    data,
    headers: { 'X-Admin': '1' },
  })
}
