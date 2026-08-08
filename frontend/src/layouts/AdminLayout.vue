<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { adminMenusTree } from '@/api/admin'
import { useAdminAuthStore } from '@/stores/adminAuth'

const router = useRouter()
const route = useRoute()
const adminAuth = useAdminAuthStore()
const drawer = ref(false)

type SideMenuNode = {
  key: string
  label: string
  path?: string
  children?: SideMenuNode[]
}

/** 契约侧栏失败时的兜底（保留 DIR 层级） */
const fallbackMenus: SideMenuNode[] = [
  { key: '/admin', label: '工作台', path: '/admin' },
  { key: '/admin/users', label: '侠士管理', path: '/admin/users' },
  { key: '/admin/invites', label: '邀请管理', path: '/admin/invites' },
  { key: '/admin/bounties', label: '悬赏管理', path: '/admin/bounties' },
  { key: '/admin/wallet', label: '钱庄流水', path: '/admin/wallet' },
  { key: '/admin/disputes', label: '纠纷仲裁', path: '/admin/disputes' },
  { key: '/admin/feedbacks', label: '用户反馈', path: '/admin/feedbacks' },
  { key: '/admin/submission-reviews', label: '成果审核', path: '/admin/submission-reviews' },
  { key: '/admin/notices', label: '告示管理', path: '/admin/notices' },
  { key: '/admin/offices', label: '职司管理', path: '/admin/offices' },
  { key: '/admin/lord', label: '盟主管理', path: '/admin/lord' },
  {
    key: 'dir-ops',
    label: '运营配置',
    children: [
      { key: '/admin/ops', label: '运营参数', path: '/admin/ops' },
      { key: '/admin/products', label: '奖品管理', path: '/admin/products' },
      { key: '/admin/checklist', label: '探子清单', path: '/admin/checklist' },
      { key: '/admin/warrant-config', label: '令状字段', path: '/admin/warrant-config' },
    ],
  },
  { key: '/admin/audit-logs', label: '审计日志', path: '/admin/audit-logs' },
  {
    key: 'dir-rbac',
    label: '权限管理',
    children: [
      { key: '/admin/admins', label: '管理员账号', path: '/admin/admins' },
      { key: '/admin/roles', label: '角色权限', path: '/admin/roles' },
      { key: '/admin/menus', label: '菜单管理', path: '/admin/menus' },
    ],
  },
]

const menus = ref<SideMenuNode[]>([...fallbackMenus])

/** 旧 path → 真实路由；四参分项统一进运营参数页 */
const MENU_PATH_ALIASES: Record<string, string> = {
  '/admin/configs/levels': '/admin/ops',
  '/admin/configs/growth': '/admin/ops',
  '/admin/configs/ranks': '/admin/ops',
  '/admin/configs/reward-suggest': '/admin/ops',
  '/admin/ops?tab=levels': '/admin/ops',
  '/admin/ops?tab=growth': '/admin/ops',
  '/admin/ops?tab=ranks': '/admin/ops',
  '/admin/ops?tab=reward': '/admin/ops',
  '/admin/checklist-templates': '/admin/checklist',
  '/admin/warrant-field-configs': '/admin/warrant-config',
  '/admin/system': '/admin/audit-logs',
}

const OPS_SPLIT_LABELS = new Set(['等级配置', '成长参数', '英雄谱规则', '赏银建议', '运营参数'])

function resolveMenuPath(path: string) {
  return MENU_PATH_ALIASES[path] || path
}

function isOpsParamMenu(label: string, path: string) {
  if (OPS_SPLIT_LABELS.has(label)) return true
  if (path === '/admin/ops' || path.startsWith('/admin/ops?')) return true
  if (path.startsWith('/admin/configs/')) return true
  return false
}

/** v1.8.15：四参侧栏折叠为一条「运营参数」 */
function collapseOpsMenus(nodes: SideMenuNode[]): SideMenuNode[] {
  const out: SideMenuNode[] = []
  let opsInserted = false
  for (const n of nodes) {
    if (n.children?.length) {
      out.push({ ...n, children: collapseOpsMenus(n.children) })
      continue
    }
    if (n.path && isOpsParamMenu(n.label, n.path)) {
      if (!opsInserted) {
        opsInserted = true
        out.push({ key: '/admin/ops', label: '运营参数', path: '/admin/ops' })
      }
      continue
    }
    out.push(n)
  }
  return out
}

function mapMenuTree(nodes: Record<string, unknown>[]): SideMenuNode[] {
  const out: SideMenuNode[] = []
  for (const n of nodes) {
    const type = String(n.type || '')
    const visible = n.visible !== false
    if (!visible || type === 'BUTTON') continue

    const rawChildren = Array.isArray(n.children) ? (n.children as Record<string, unknown>[]) : []

    if (type === 'DIR') {
      const children = collapseOpsMenus(mapMenuTree(rawChildren))
      if (!children.length) continue
      out.push({
        key: `dir-${n.id ?? n.name}`,
        label: String(n.name || '目录'),
        children,
      })
      continue
    }

    if (type === 'MENU') {
      const path = resolveMenuPath(String(n.path || ''))
      if (!path) continue
      let label = String(n.name || path)
      if (path === '/admin/audit-logs' || label === '系统配置') {
        label = '审计日志'
      }
      if (isOpsParamMenu(label, path)) {
        out.push({ key: '/admin/ops', label: '运营参数', path: '/admin/ops' })
      } else {
        out.push({ key: path, label, path })
      }
    }
  }
  return out
}

async function loadMenus() {
  try {
    const tree = await adminMenusTree()
    const mapped = mapMenuTree(tree || [])
    if (mapped.length) {
      const hasDash = mapped.some((m) => m.path === '/admin' || m.path === '/admin/')
      menus.value = hasDash ? mapped : [{ key: '/admin', label: '工作台', path: '/admin' }, ...mapped]
      return
    }
  } catch {
    /* 无菜单权限或接口失败 → 兜底 */
  }
  menus.value = [...fallbackMenus]
}

/** 二级详情/编辑页高亮对应菜单项 */
const activeMenu = computed(() => {
  const p = route.path
  if (p.startsWith('/admin/bounties')) return '/admin/bounties'
  if (p.startsWith('/admin/notices')) return '/admin/notices'
  if (p.startsWith('/admin/users')) return '/admin/users'
  if (p.startsWith('/admin/disputes')) return '/admin/disputes'
  if (p.startsWith('/admin/feedbacks')) return '/admin/feedbacks'
  if (p.startsWith('/admin/admins')) return '/admin/admins'
  if (p.startsWith('/admin/roles')) return '/admin/roles'
  if (p.startsWith('/admin/menus')) return '/admin/menus'
  if (p.startsWith('/admin/ops') || p.startsWith('/admin/configs/')) return '/admin/ops'
  if (p.startsWith('/admin/checklist')) return '/admin/checklist'
  if (p.startsWith('/admin/warrant-config')) return '/admin/warrant-config'
  if (p.startsWith('/admin/products')) return '/admin/products'
  if (p.startsWith('/admin/audit-logs') || p.startsWith('/admin/system')) return '/admin/audit-logs'
  return p
})

/** 当前页所属目录默认展开 */
const defaultOpeneds = computed(() => {
  const active = activeMenu.value
  const open: string[] = []
  for (const m of menus.value) {
    if (!m.children?.length) continue
    if (m.children.some((c) => c.path === active || c.key === active)) {
      open.push(m.key)
    }
  }
  return open
})

watch(
  () => route.fullPath,
  () => {
    drawer.value = false
  },
)

async function logout() {
  await adminAuth.logout()
  router.push({ name: 'admin-login' })
}

onMounted(loadMenus)
</script>

<template>
  <el-container class="admin-layout">
    <el-aside width="230px" class="aside desktop-aside">
      <div class="brand">武林盟</div>
      <el-menu
        class="admin-menu"
        :key="activeMenu + '-' + defaultOpeneds.join(',') + '-' + menus.length"
        :default-active="activeMenu"
        :default-openeds="defaultOpeneds"
        router
        background-color="transparent"
        text-color="#c5ccd6"
        active-text-color="#fff"
      >
        <template v-for="m in menus" :key="m.key">
          <el-sub-menu v-if="m.children?.length" :index="m.key">
            <template #title>{{ m.label }}</template>
            <el-menu-item v-for="c in m.children" :key="c.key" :index="c.path || c.key">
              {{ c.label }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else-if="m.path" :index="m.path">{{ m.label }}</el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <el-container class="content-shell">
      <el-header class="header">
        <el-button class="menu-trigger" text @click="drawer = true">菜单</el-button>
        <span class="title">运营后台 · L0</span>
        <div class="right">
          <span class="name">{{ adminAuth.admin?.displayName || adminAuth.admin?.username }}</span>
          <el-button link type="danger" @click="logout">退出</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <RouterView />
      </el-main>
      <el-footer class="footer">
        <div class="footer-inner">
          <span class="footer-brand">武林盟后台</span>
          <span class="footer-text">内测环境 · 顶部导航与底栏固定，内容区独立滚动</span>
        </div>
      </el-footer>
    </el-container>

    <el-drawer v-model="drawer" direction="ltr" size="80%" title="武林盟" class="admin-drawer">
      <el-menu
        class="admin-menu admin-menu--light"
        :key="'d-' + activeMenu + '-' + defaultOpeneds.join(',')"
        :default-active="activeMenu"
        :default-openeds="defaultOpeneds"
        router
        @select="drawer = false"
      >
        <template v-for="m in menus" :key="m.key">
          <el-sub-menu v-if="m.children?.length" :index="m.key">
            <template #title>{{ m.label }}</template>
            <el-menu-item v-for="c in m.children" :key="c.key" :index="c.path || c.key">
              {{ c.label }}
            </el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else-if="m.path" :index="m.path">{{ m.label }}</el-menu-item>
        </template>
      </el-menu>
    </el-drawer>
  </el-container>
</template>

<style scoped>
.admin-layout {
  --admin-header-h: 56px;
  --admin-footer-h: 44px;
  height: 100dvh;
  max-height: 100dvh;
  overflow: hidden;
  background: #f3f5f8;
}
.aside {
  background: linear-gradient(180deg, #1f2a37 0%, #17212b 100%);
  color: #fff;
  border-right: 1px solid #101820;
  overflow: auto;
}
.brand {
  height: var(--admin-header-h);
  display: flex;
  align-items: center;
  padding: 0 18px;
  font-family: var(--jh-font-display);
  font-size: 22px;
  letter-spacing: 0.08em;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.admin-menu {
  border-right: none;
  padding: 8px;
}
.admin-menu :deep(.el-menu-item),
.admin-menu :deep(.el-sub-menu__title) {
  border-radius: 8px;
  margin-bottom: 4px;
}
.admin-menu :deep(.el-menu-item.is-active) {
  background: rgba(178, 58, 45, 0.92) !important;
}
.admin-menu :deep(.el-sub-menu .el-menu-item) {
  min-width: 0;
  padding-left: 40px !important;
}
.content-shell {
  min-width: 0;
  height: 100%;
  overflow: hidden;
}
.header {
  height: var(--admin-header-h);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background: #fff;
  border-bottom: 1px solid #e6ebf1;
  flex-shrink: 0;
}
.title {
  font-weight: 600;
  color: #303133;
}
.right {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-left: auto;
}
.name {
  color: #606266;
  font-size: 14px;
}
.menu-trigger {
  display: none;
}
.main {
  --el-main-padding: 16px;
  height: calc(100dvh - var(--admin-header-h) - var(--admin-footer-h));
  overflow: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
}
.footer {
  height: var(--admin-footer-h);
  padding: 0 16px;
  background: #fff;
  border-top: 1px solid #e6ebf1;
  flex-shrink: 0;
  display: flex;
  align-items: center;
}
.footer-inner {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: #909399;
}
.footer-brand {
  font-weight: 600;
  color: #606266;
}
.admin-menu--light {
  border-right: none;
}
.admin-menu--light :deep(.el-menu-item.is-active) {
  background: rgba(178, 58, 45, 0.12) !important;
  color: var(--jh-seal) !important;
}

@media (max-width: 900px) {
  .desktop-aside {
    display: none;
  }
  .menu-trigger {
    display: inline-flex;
  }
  .admin-layout {
    --admin-footer-h: 48px;
  }
  .main {
    --el-main-padding: 12px;
  }
  .footer-text {
    display: none;
  }
}
</style>
