import { http } from './request'
import type { PageQuery, PageResult, UpdateFeedbackStatusRequest } from '@/types/api'
import type {
  AdminAuthResult,
  AdminFeedbackDetail,
  AdminFeedbackListItem,
  BountyDetail,
  DashboardOverview,
  ReviewSubmissionListItem,
  SubmissionDetail,
  SubmissionReviewResult,
} from '@/types/models'
import { normalizeSubmissionDetail, normalizeSubmissionListItem } from '@/utils/submission'

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

/** §16.3.2 备注 */
export function adminUpdateUserRemark(id: number | string, remark: string) {
  return http<null>({
    url: `/admin/users/${id}/remark`,
    method: 'PUT',
    data: { remark },
    headers: { 'X-Admin': '1' },
  })
}

/** §16.3.4 登录日志 */
export function adminListUserLoginLogs(id: number | string, params: PageQuery) {
  return http<PageResult<Record<string, unknown>>>({
    url: `/admin/users/${id}/login-logs`,
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

/** §16.3.5 实名 */
export function adminGetUserRealName(id: number | string) {
  return http<{ realName?: string; idNumber?: string; status?: string }>({
    url: `/admin/users/${id}/real-name`,
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminUpdateUserRealName(id: number | string, status: string) {
  return http<{ status: string }>({
    url: `/admin/users/${id}/real-name`,
    method: 'PUT',
    data: { status },
    headers: { 'X-Admin': '1' },
  })
}

/** §16.10.1 管理员账号 */
export function adminListAdmins(params: PageQuery & { keyword?: string; status?: string }) {
  return http<PageResult<Record<string, unknown>>>({
    url: '/admin/admins',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

export function adminGetAdmin(id: number | string) {
  return http<Record<string, unknown>>({
    url: `/admin/admins/${id}`,
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminCreateAdmin(data: Record<string, unknown>) {
  return http<Record<string, unknown>>({
    url: '/admin/admins',
    method: 'POST',
    data,
    headers: { 'X-Admin': '1' },
  })
}

export function adminUpdateAdmin(id: number | string, data: Record<string, unknown>) {
  return http<Record<string, unknown>>({
    url: `/admin/admins/${id}`,
    method: 'PUT',
    data,
    headers: { 'X-Admin': '1' },
  })
}

export function adminResetAdminPassword(id: number | string, newPassword: string) {
  return http<null>({
    url: `/admin/admins/${id}/reset-password`,
    method: 'POST',
    data: { newPassword },
    headers: { 'X-Admin': '1' },
  })
}

export function adminDisableAdmin(id: number | string) {
  return http<null>({
    url: `/admin/admins/${id}/disable`,
    method: 'POST',
    headers: { 'X-Admin': '1' },
  })
}

export function adminEnableAdmin(id: number | string) {
  return http<null>({
    url: `/admin/admins/${id}/enable`,
    method: 'POST',
    headers: { 'X-Admin': '1' },
  })
}

/** §16.10.2 角色 */
export function adminListRoles() {
  return http<Record<string, unknown>[]>({
    url: '/admin/roles',
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminGetRole(code: string) {
  return http<Record<string, unknown>>({
    url: `/admin/roles/${code}`,
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminPutRolePermissions(code: string, permissions: string[]) {
  return http<Record<string, unknown>>({
    url: `/admin/roles/${code}/permissions`,
    method: 'PUT',
    data: { permissions },
    headers: { 'X-Admin': '1' },
  })
}

export function adminPermissionCatalog() {
  return http<Record<string, unknown>[]>({
    url: '/admin/roles/permission-catalog',
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

/** §16.10.3 菜单 */
export function adminMenusTree() {
  return http<Record<string, unknown>[]>({
    url: '/admin/menus/tree',
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminMenusAll() {
  return http<Record<string, unknown>[]>({
    url: '/admin/menus/all',
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminCreateMenu(data: Record<string, unknown>) {
  return http<Record<string, unknown>>({
    url: '/admin/menus',
    method: 'POST',
    data,
    headers: { 'X-Admin': '1' },
  })
}

export function adminUpdateMenu(id: number | string, data: Record<string, unknown>) {
  return http<Record<string, unknown>>({
    url: `/admin/menus/${id}`,
    method: 'PUT',
    data,
    headers: { 'X-Admin': '1' },
  })
}

export function adminDeleteMenu(id: number | string) {
  return http<null>({
    url: `/admin/menus/${id}`,
    method: 'DELETE',
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
  return http<BountyDetail & {
    claims?: Array<Record<string, unknown>>
    submissions?: Array<Record<string, unknown>>
  }>({
    url: `/admin/bounties/${id}`,
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

export function adminListBountyMessages(
  id: number | string,
  params: PageQuery = { page: 1, pageSize: 50 },
) {
  return http<PageResult<Record<string, unknown>>>({
    url: `/admin/bounties/${id}/messages`,
    method: 'GET',
    params,
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

/** §16.12.1 成果审核列表 */
export function adminListSubmissionReviews(
  params: PageQuery & {
    status?: string
    bountyId?: number | string
    keyword?: string
  },
) {
  return http<PageResult<ReviewSubmissionListItem>>({
    url: '/admin/submission-reviews',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  }).then((page) => ({
    ...page,
    list: (page?.list || []).map((row) => ({
      ...normalizeSubmissionListItem(row),
      bountyTitle: (row as ReviewSubmissionListItem).bountyTitle ?? null,
    })),
  }))
}

/** §16.12.2 成果审核详情（§8.0） */
export function adminGetSubmission(submissionId: number | string) {
  return http<SubmissionDetail>({
    url: `/admin/submission-reviews/${submissionId}`,
    method: 'GET',
    headers: { 'X-Admin': '1' },
  }).then(normalizeSubmissionDetail)
}

/** §16.12.3 通过/驳回（可改判） */
export function adminReviewSubmission(
  submissionId: number | string,
  data: {
    result: string
    reason?: string | null
    itemComments?: { itemCode: string; comment: string }[]
  },
) {
  return http<SubmissionReviewResult | null>({
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

export function adminListAuditLogs(
  params: PageQuery & { operator?: string; action?: string; keyword?: string },
) {
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

/** §16.11.1 用户反馈列表 */
export function adminListFeedbacks(
  params: PageQuery & { status?: string; type?: string; keyword?: string },
) {
  return http<PageResult<AdminFeedbackListItem>>({
    url: '/admin/feedbacks',
    method: 'GET',
    params,
    headers: { 'X-Admin': '1' },
  })
}

/** §16.11.2 用户反馈详情 */
export function adminGetFeedback(id: number | string) {
  return http<AdminFeedbackDetail>({
    url: `/admin/feedbacks/${id}`,
    method: 'GET',
    headers: { 'X-Admin': '1' },
  })
}

/** §16.11.3 改状态 */
export function adminUpdateFeedbackStatus(id: number | string, data: UpdateFeedbackStatusRequest) {
  return http<AdminFeedbackDetail>({
    url: `/admin/feedbacks/${id}/status`,
    method: 'PUT',
    data,
    headers: { 'X-Admin': '1' },
  })
}
