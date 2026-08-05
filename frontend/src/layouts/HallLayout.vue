<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const router = useRouter()
const route = useRoute()
const drawer = ref(false)

const menus = [
  { path: '/hall', label: '概览' },
  { path: '/hall/bounty-reviews', label: '令审队列' },
  { path: '/hall/submission-reviews', label: '验功队列' },
  { path: '/hall/actions', label: '操作记录' },
]

watch(
  () => route.fullPath,
  () => {
    drawer.value = false
  },
)
</script>

<template>
  <el-container class="hall-layout">
    <el-aside width="220px" class="aside desktop-aside">
      <div class="brand" @click="router.push('/')">执事堂</div>
      <el-menu :default-active="route.path" router>
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">{{ m.label }}</el-menu-item>
      </el-menu>
      <div class="back">
        <el-button text type="primary" @click="router.push('/')">返回侠士端</el-button>
      </div>
    </el-aside>

    <el-container>
      <el-header class="header">
        <el-button class="menu-trigger" text @click="drawer = true">菜单</el-button>
        <span class="title">职司工作台</span>
        <el-button text type="primary" @click="router.push('/')">侠士端</el-button>
      </el-header>
      <el-main class="main">
        <RouterView />
      </el-main>
    </el-container>

    <el-drawer v-model="drawer" direction="ltr" size="78%" title="执事堂">
      <el-menu :default-active="route.path" router @select="drawer = false">
        <el-menu-item v-for="m in menus" :key="m.path" :index="m.path">{{ m.label }}</el-menu-item>
      </el-menu>
    </el-drawer>
  </el-container>
</template>

<style scoped>
.hall-layout {
  min-height: 100vh;
  min-height: 100dvh;
  background: #f5f7fa;
}
.aside {
  background: #fff;
  border-right: 1px solid #ebeef5;
  display: flex;
  flex-direction: column;
}
.brand {
  font-size: 20px;
  font-weight: 700;
  padding: 20px 16px;
  cursor: pointer;
}
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  color: #606266;
  height: auto !important;
  min-height: 56px;
  padding: 8px 12px;
}
.title {
  flex: 1;
  text-align: center;
  font-size: 14px;
}
.back {
  margin-top: auto;
  padding: 12px;
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
  .title {
    font-size: 13px;
  }
  .main {
    padding: 12px;
  }
  .main :deep(.el-table) {
    width: 100%;
    overflow-x: auto;
  }
}
</style>
