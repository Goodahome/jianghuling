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
  return http<{ count: number; codes: string[] }>({
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

function adminHeaders() {
  return { 'X-Admin': '1' as const }
}

export function adminGetLevels() {
  return http<Record<string, unknown>[]>({ url: '/admin/configs/levels', method: 'GET', headers: adminHeaders() })
}

export function adminPutLevels(data: unknown) {
  return http<Record<string, unknown>[]>({ url: '/admin/configs/levels', method: 'PUT', data, headers: adminHeaders() })
}

export function adminGetRanksConfig() {
  return http<Record<string, unknown>>({ url: '/admin/configs/ranks', method: 'GET', headers: adminHeaders() })
}

export function adminPutRanksConfig(data: Record<string, unknown>) {
  return http<Record<string, unknown>>({ url: '/admin/configs/ranks', method: 'PUT', data, headers: adminHeaders() })
}

export function adminGetGrowthConfig() {
  return http<Record<string, unknown>>({ url: '/admin/configs/growth', method: 'GET', headers: adminHeaders() })
}

export function adminPutGrowthConfig(data: Record<string, unknown>) {
  return http<Record<string, unknown>>({ url: '/admin/configs/growth', method: 'PUT', data, headers: adminHeaders() })
}

export function adminGetRewardSuggestConfig() {
  return http<Record<string, unknown>>({ url: '/admin/configs/reward-suggest', method: 'GET', headers: adminHeaders() })
}

export function adminPutRewardSuggestConfig(data: Record<string, unknown>) {
  return http<Record<string, unknown>>({
    url: '/admin/configs/reward-suggest',
    method: 'PUT',
    data,
    headers: adminHeaders(),
  })
}

export function adminListProducts(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/products',
    method: 'GET',
    params,
    headers: adminHeaders(),
  })
}

export function adminCreateProduct(data: Record<string, unknown>) {
  return http<Record<string, unknown>>({ url: '/admin/products', method: 'POST', data, headers: adminHeaders() })
}

export function adminUpdateProduct(id: number | string, data: Record<string, unknown>) {
  return http<Record<string, unknown>>({
    url: `/admin/products/${id}`,
    method: 'PUT',
    data,
    headers: adminHeaders(),
  })
}

export function adminDeleteProduct(id: number | string) {
  return http<null>({ url: `/admin/products/${id}`, method: 'DELETE', headers: adminHeaders() })
}

export function adminListRedeemOrders(params: PageQuery & { status?: string }) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/redeem-orders',
    method: 'GET',
    params,
    headers: adminHeaders(),
  })
}

export function adminUpdateRedeemOrder(id: number | string, data: Record<string, unknown>) {
  return http<Record<string, unknown>>({
    url: `/admin/redeem-orders/${id}`,
    method: 'PUT',
    data,
    headers: adminHeaders(),
  })
}

export function adminListChecklistTemplates(params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/checklist-templates',
    method: 'GET',
    params,
    headers: adminHeaders(),
  })
}

export function adminCreateChecklistTemplate(data: Record<string, unknown>) {
  return http<Record<string, unknown>>({
    url: '/admin/checklist-templates',
    method: 'POST',
    data,
    headers: adminHeaders(),
  })
}

export function adminUpdateChecklistTemplate(id: number | string, data: Record<string, unknown>) {
  return http<Record<string, unknown>>({
    url: `/admin/checklist-templates/${id}`,
    method: 'PUT',
    data,
    headers: adminHeaders(),
  })
}

export function adminDeleteChecklistTemplate(id: number | string) {
  return http<null>({ url: `/admin/checklist-templates/${id}`, method: 'DELETE', headers: adminHeaders() })
}

export function adminListWarrantConfigs(params: PageQuery & { templateCode?: string }) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/warrant-field-configs',
    method: 'GET',
    params,
    headers: adminHeaders(),
  })
}

export function adminCreateWarrantConfig(data: Record<string, unknown>) {
  return http<Record<string, unknown>>({
    url: '/admin/warrant-field-configs',
    method: 'POST',
    data,
    headers: adminHeaders(),
  })
}

export function adminUpdateWarrantConfig(id: number | string, data: Record<string, unknown>) {
  return http<Record<string, unknown>>({
    url: `/admin/warrant-field-configs/${id}`,
    method: 'PUT',
    data,
    headers: adminHeaders(),
  })
}

export function adminDeleteWarrantConfig(id: number | string) {
  return http<null>({ url: `/admin/warrant-field-configs/${id}`, method: 'DELETE', headers: adminHeaders() })
}

export function adminListOfficeDefs() {
  return http<Record<string, unknown>[]>({ url: '/admin/offices/defs', method: 'GET', headers: adminHeaders() })
}

export function adminPutOfficeDefs(data: unknown) {
  return http<Record<string, unknown>[]>({
    url: '/admin/offices/defs',
    method: 'PUT',
    data,
    headers: adminHeaders(),
  })
}

export function adminSuspendOfficeHolder(id: number | string) {
  return http<null>({ url: `/admin/offices/holders/${id}/suspend`, method: 'POST', headers: adminHeaders() })
}

export function adminRevokeOfficeHolder(id: number | string) {
  return http<null>({ url: `/admin/offices/holders/${id}/revoke`, method: 'POST', headers: adminHeaders() })
}

export function adminGetLord() {
  return http<Record<string, unknown> | null>({ url: '/admin/lord', method: 'GET', headers: adminHeaders() })
}

export function adminDismissLord(reason?: string) {
  return http<null>({
    url: '/admin/lord/dismiss',
    method: 'POST',
    data: { reason },
    headers: adminHeaders(),
  })
}
