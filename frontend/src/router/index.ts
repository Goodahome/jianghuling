import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getAdminToken, getToken } from '@/api/request'
import { useAuthStore } from '@/stores/auth'
import { useAdminAuthStore } from '@/stores/adminAuth'

const routes: RouteRecordRaw[] = [
  {
    path: '/',
    component: () => import('@/layouts/HeroLayout.vue'),
    children: [
      { path: '', name: 'home', component: () => import('@/views/hero/HomeView.vue'), meta: { title: '首页' } },
      { path: 'plaza', name: 'plaza', component: () => import('@/views/hero/BountyPlazaView.vue'), meta: { title: '悬赏榜' } },
      { path: 'login', name: 'login', component: () => import('@/views/hero/LoginView.vue'), meta: { guest: true, title: '登录' } },
      { path: 'register', name: 'register', component: () => import('@/views/hero/RegisterView.vue'), meta: { guest: true, title: '邀请注册' } },
      { path: 'r/:code', name: 'invite-landing', component: () => import('@/views/hero/RegisterView.vue'), meta: { guest: true, title: '邀请注册' } },
      {
        path: 'legal/:doc',
        name: 'legal-doc',
        component: () => import('@/views/hero/LegalDocView.vue'),
        meta: { title: '法律条款' },
      },
      { path: 'bounties/publish', name: 'bounty-publish', component: () => import('@/views/hero/PublishBountyView.vue'), meta: { auth: true, title: '张贴悬赏' } },
      { path: 'bounties/:id', name: 'bounty-detail', component: () => import('@/views/hero/BountyDetailView.vue'), meta: { title: '悬赏详情' } },
      { path: 'bounties/:id/chat', name: 'bounty-chat', component: () => import('@/views/hero/CollabChatView.vue'), meta: { auth: true, title: '协作会话' } },
      { path: 'bounties/:id/submit', name: 'bounty-submit', component: () => import('@/views/hero/SubmitResultView.vue'), meta: { auth: true, title: '提交成果' } },
      {
        path: 'bounties/:id/submissions/:submissionId',
        name: 'bounty-submission-detail',
        component: () => import('@/views/hero/SubmissionDetailView.vue'),
        meta: { auth: true, title: '成果详情' },
      },
      { path: 'bounties/:id/settle', name: 'bounty-settle', component: () => import('@/views/hero/SettlementView.vue'), meta: { auth: true, title: '完结分配' } },
      { path: 'mine', name: 'mine', component: () => import('@/views/hero/MyBountiesView.vue'), meta: { auth: true, title: '我的悬赏' } },
      { path: 'wallet', name: 'wallet', component: () => import('@/views/hero/WalletView.vue'), meta: { auth: true, title: '模拟钱庄' } },
      { path: 'profile', name: 'profile', component: () => import('@/views/hero/ProfileView.vue'), meta: { auth: true, title: '侠士资料' } },
      { path: 'notices', name: 'notices', component: () => import('@/views/hero/NoticesView.vue'), meta: { title: '告示栏' } },
      { path: 'notices/:id', name: 'notice-detail', component: () => import('@/views/hero/NoticeDetailView.vue'), meta: { title: '告示详情' } },
      { path: 'ranks', name: 'ranks', component: () => import('@/views/hero/RankView.vue'), meta: { title: '英雄榜' } },
      { path: 'growth', name: 'growth', component: () => import('@/views/hero/GrowthView.vue'), meta: { auth: true, title: '成长兑换' } },
      { path: 'offices', name: 'offices', component: () => import('@/views/hero/OfficeApplyView.vue'), meta: { auth: true, title: '职司申请' } },
      { path: 'messages', name: 'messages', component: () => import('@/views/hero/MessagesView.vue'), meta: { auth: true, title: '站内消息' } },
      { path: 'invites', name: 'invites', component: () => import('@/views/hero/InviteView.vue'), meta: { auth: true, title: '邀请同道' } },
      { path: 'disputes', name: 'disputes', component: () => import('@/views/hero/DisputeListView.vue'), meta: { auth: true, title: '我的纠纷' } },
      { path: 'disputes/:id', name: 'dispute-detail', component: () => import('@/views/hero/DisputeDetailView.vue'), meta: { auth: true, title: '纠纷详情' } },
      { path: 'feedbacks', name: 'feedbacks', component: () => import('@/views/hero/FeedbackListView.vue'), meta: { auth: true, title: '意见反馈' } },
      {
        path: 'feedbacks/:id',
        name: 'feedback-detail',
        component: () => import('@/views/hero/FeedbackDetailView.vue'),
        meta: { auth: true, title: '反馈详情' },
      },
    ],
  },
  {
    path: '/hall',
    component: () => import('@/layouts/HallLayout.vue'),
    meta: { auth: true, hall: true },
    children: [
      { path: '', name: 'hall-home', component: () => import('@/views/hall/HallHomeView.vue'), meta: { title: '执事堂' } },
      { path: 'bounty-reviews', name: 'hall-bounty-reviews', component: () => import('@/views/hall/BountyReviewView.vue'), meta: { title: '令审', office: 'DECREE_REVIEWER' } },
      {
        path: 'bounty-reviews/:id',
        name: 'hall-bounty-review-detail',
        component: () => import('@/views/hall/BountyReviewDetailView.vue'),
        meta: { title: '令审详情', office: 'DECREE_REVIEWER' },
      },
      { path: 'submission-reviews', name: 'hall-submission-reviews', component: () => import('@/views/hall/SubmissionReviewView.vue'), meta: { title: '验功', office: 'FEAT_REVIEWER' } },
      {
        path: 'submission-reviews/:id',
        name: 'hall-submission-review-detail',
        component: () => import('@/views/hall/SubmissionReviewDetailView.vue'),
        meta: { title: '验功详情', office: 'FEAT_REVIEWER' },
      },
      { path: 'actions', name: 'hall-actions', component: () => import('@/views/hall/MyActionsView.vue'), meta: { title: '履职记录' } },
    ],
  },
  {
    path: '/admin/login',
    name: 'admin-login',
    component: () => import('@/views/admin/AdminLoginView.vue'),
    meta: { title: '武林盟登录' },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    meta: { admin: true },
    children: [
      { path: '', name: 'admin-dashboard', component: () => import('@/views/admin/DashboardView.vue'), meta: { title: '工作台' } },
      { path: 'users', name: 'admin-users', component: () => import('@/views/admin/UsersView.vue'), meta: { title: '侠士管理' } },
      {
        path: 'users/:id',
        name: 'admin-user-detail',
        component: () => import('@/views/admin/UserDetailView.vue'),
        meta: { title: '侠士详情' },
      },
      { path: 'invites', name: 'admin-invites', component: () => import('@/views/admin/InvitesView.vue'), meta: { title: '邀请管理' } },
      { path: 'bounties', name: 'admin-bounties', component: () => import('@/views/admin/BountiesView.vue'), meta: { title: '悬赏管理' } },
      {
        path: 'bounties/:id',
        name: 'admin-bounty-detail',
        component: () => import('@/views/admin/AdminBountyDetailView.vue'),
        meta: { title: '悬赏详情' },
      },
      { path: 'wallet', name: 'admin-wallet', component: () => import('@/views/admin/WalletLedgersView.vue'), meta: { title: '钱庄流水' } },
      { path: 'disputes', name: 'admin-disputes', component: () => import('@/views/admin/DisputesView.vue'), meta: { title: '纠纷仲裁' } },
      {
        path: 'disputes/:id',
        name: 'admin-dispute-detail',
        component: () => import('@/views/admin/DisputeDetailView.vue'),
        meta: { title: '纠纷详情' },
      },
      {
        path: 'feedbacks',
        name: 'admin-feedbacks',
        component: () => import('@/views/admin/FeedbacksAdminView.vue'),
        meta: { title: '用户反馈' },
      },
      {
        path: 'feedbacks/:id',
        name: 'admin-feedback-detail',
        component: () => import('@/views/admin/FeedbackDetailAdminView.vue'),
        meta: { title: '反馈详情' },
      },
      {
        path: 'submission-reviews',
        name: 'admin-submission-reviews',
        component: () => import('@/views/admin/SubmissionReviewsAdminView.vue'),
        meta: { title: '成果审核' },
      },
      {
        path: 'submission-reviews/:id',
        name: 'admin-submission-review-detail',
        component: () => import('@/views/admin/SubmissionReviewDetailAdminView.vue'),
        meta: { title: '成果审核详情' },
      },
      { path: 'notices', name: 'admin-notices', component: () => import('@/views/admin/NoticesAdminView.vue'), meta: { title: '告示管理' } },
      {
        path: 'notices/new',
        name: 'admin-notice-new',
        component: () => import('@/views/admin/NoticeEditorView.vue'),
        meta: { title: '发布告示' },
      },
      {
        path: 'notices/:id/edit',
        name: 'admin-notice-edit',
        component: () => import('@/views/admin/NoticeEditorView.vue'),
        meta: { title: '编辑告示' },
      },
      { path: 'offices', name: 'admin-offices', component: () => import('@/views/admin/OfficesAdminView.vue'), meta: { title: '职司管理' } },
      { path: 'lord', name: 'admin-lord', component: () => import('@/views/admin/LordAdminView.vue'), meta: { title: '盟主管理' } },
      { path: 'ops', name: 'admin-ops', component: () => import('@/views/admin/OpsConfigView.vue'), meta: { title: '运营参数' } },
      /* v1.8.13：兼容种子菜单旧 path，redirect 到真实路由 */
      { path: 'configs/levels', redirect: { path: '/admin/ops', query: { tab: 'levels' } } },
      { path: 'configs/growth', redirect: { path: '/admin/ops', query: { tab: 'growth' } } },
      { path: 'configs/ranks', redirect: { path: '/admin/ops', query: { tab: 'ranks' } } },
      { path: 'configs/reward-suggest', redirect: { path: '/admin/ops', query: { tab: 'reward' } } },
      { path: 'products', name: 'admin-products', component: () => import('@/views/admin/ProductsAdminView.vue'), meta: { title: '奖品兑换' } },
      { path: 'checklist', name: 'admin-checklist', component: () => import('@/views/admin/ChecklistAdminView.vue'), meta: { title: '探子清单' } },
      { path: 'checklist-templates', redirect: '/admin/checklist' },
      { path: 'warrant-config', name: 'admin-warrant-config', component: () => import('@/views/admin/WarrantConfigAdminView.vue'), meta: { title: '令状字段' } },
      { path: 'warrant-field-configs', redirect: '/admin/warrant-config' },
      { path: 'admins', name: 'admin-admins', component: () => import('@/views/admin/AdminsView.vue'), meta: { title: '管理员账号' } },
      { path: 'roles', name: 'admin-roles', component: () => import('@/views/admin/RolesView.vue'), meta: { title: '角色权限' } },
      { path: 'menus', name: 'admin-menus', component: () => import('@/views/admin/MenusView.vue'), meta: { title: '菜单管理' } },
      {
        path: 'audit-logs',
        name: 'admin-audit-logs',
        component: () => import('@/views/admin/AuditLogsView.vue'),
        meta: { title: '审计日志' },
      },
      /* 旧「系统配置」与运营参数重复，改为审计日志 */
      { path: 'system', redirect: '/admin/audit-logs' },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to) => {
  document.title = `${String(to.meta.title || '江湖令')} · 江湖令`

  if (to.meta.admin || to.path.startsWith('/admin')) {
    if (to.name === 'admin-login') return true
    if (!getAdminToken()) {
      return { name: 'admin-login', query: { redirect: to.fullPath } }
    }
    const adminStore = useAdminAuthStore()
    if (!adminStore.admin) {
      try {
        await adminStore.fetchMe()
      } catch {
        await adminStore.logout()
        return { name: 'admin-login', query: { redirect: to.fullPath } }
      }
    }
    return true
  }

  const auth = useAuthStore()

  // 公开页刷新后也要恢复用户资料（昵称、职司等）
  if (getToken() && !auth.me) {
    try {
      await auth.fetchMe()
    } catch {
      await auth.logout()
      if (to.meta.auth || to.meta.hall) {
        return { name: 'login', query: { redirect: to.fullPath } }
      }
    }
  }

  if (to.meta.auth || to.meta.hall) {
    if (!getToken()) {
      return { name: 'login', query: { redirect: to.fullPath } }
    }
    if (to.meta.hall && !auth.hasOffice) {
      return { name: 'offices' }
    }
    const needOffice = to.meta.office as string | undefined
    if (needOffice && !auth.hasOfficeCode(needOffice)) {
      return { name: 'hall-home' }
    }
  }

  if (to.meta.guest && getToken()) {
    return { name: 'home' }
  }

  return true
})

export default router
