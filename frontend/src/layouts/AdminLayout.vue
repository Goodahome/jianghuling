<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminAuthStore } from '@/stores/adminAuth'

const router = useRouter()
const route = useRoute()
const adminAuth = useAdminAuthStore()
const drawer = ref(false)

const menus = [
  { path: '/admin', label: '工作台' },
  { path: '/admin/users', label: '侠士管理' },
  { path: '/admin/invites', label: '邀请管理' },
  { path: '/admin/bounties', label: '悬赏管理' },
  { path: '/admin/wallet', label: '钱庄流水' },
  { path: '/admin/disputes', label: '纠纷仲裁' },
  { path: '/admin/notices', label: '告示管理' },
  { path: '/admin/offices', label: '职司管理' },
  { path: '/admin/lord', label: '盟主管理' },
  { path: '/admin/ops', label: '运营参数' },
  { path: '/admin/products', label: '奖品兑换' },
  { path: '/admin/checklist', label: '探子清单' },
  { path: '/admin/warrant-config', label: '令状字段' },
  { path: '/admin/system', label: '系统配置' },
]

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
</script>

<template>
  <el-container class="admin-layout">
    <el-aside width="230px" class="aside desktop-aside">
      <div class="brand">武林盟</div>
      <el-menu
        class="admin-menu"
        :default-active="route.path"
        router
        background-color="transparent"
        text-color="#c5ccd6"
        active-text-color="#fff"
      >
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">{{ m.label }}</el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
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
    </el-container>

    <el-drawer v-model="drawer" direction="ltr" size="80%" title="武林盟" class="admin-drawer">
      <el-menu
        class="admin-menu admin-menu--light"
        :default-active="route.path"
        router
        @select="drawer = false"
      >
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">{{ m.label }}</el-menu-item>
      </el-menu>
    </el-drawer>
  </el-container>
</template>

<style scoped>
.admin-layout {
  min-height: 100vh;
  min-height: 100dvh;
}
.aside {
  background: #1f2a37;
  border-right: 1px solid rgba(255, 255, 255, 0.06);
}
.brand {
  color: #fff;
  font-size: 22px;
  font-weight: 700;
  padding: 22px 16px 18px;
  text-align: center;
  letter-spacing: 0.08em;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}
.admin-menu {
  border-right: none !important;
  padding: 10px 12px 16px;
  background: transparent !important;
}
.admin-menu :deep(.el-menu-item) {
  height: 44px;
  line-height: 44px;
  margin: 4px 0;
  padding: 0 12px !important;
  justify-content: center;
  text-align: center;
  border-radius: 8px;
  color: #c5ccd6;
  font-size: 14px;
  transition: background-color 0.15s ease, color 0.15s ease;
}
.admin-menu :deep(.el-menu-item:hover) {
  background: rgba(255, 255, 255, 0.08) !important;
  color: #fff !important;
}
.admin-menu :deep(.el-menu-item.is-active) {
  background: rgba(64, 158, 255, 0.22) !important;
  color: #fff !important;
  font-weight: 600;
  box-shadow: inset 3px 0 0 #409eff;
}
.admin-menu--light :deep(.el-menu-item) {
  color: #303133;
}
.admin-menu--light :deep(.el-menu-item:hover) {
  background: #f2f6fc !important;
  color: #303133 !important;
}
.admin-menu--light :deep(.el-menu-item.is-active) {
  background: #ecf5ff !important;
  color: #409eff !important;
  box-shadow: inset 3px 0 0 #409eff;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  height: auto !important;
  min-height: 56px;
  padding: 8px 12px;
}
.title {
  flex: 1;
  font-size: 14px;
  color: #606266;
}
.right {
  display: flex;
  align-items: center;
  gap: 8px;
}
.name {
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}
.menu-trigger {
  display: none;
}
.main {
  padding: 16px;
}

@media (max-width: 768px) {
  .desktop-aside {
    display: none !important;
  }
  .menu-trigger {
    display: inline-flex;
  }
  .main {
    padding: 12px;
  }
  .main :deep(.el-col) {
    max-width: 100% !important;
    flex: 0 0 100% !important;
  }
  .main :deep(.el-table) {
    width: 100%;
  }
}
</style>
