<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const route = useRoute()
const router = useRouter()
const menuOpen = ref(false)

const nav = [
  { to: '/', label: '悬赏广场' },
  { to: '/notices', label: '告示栏' },
  { to: '/ranks', label: '英雄谱' },
  { to: '/mine', label: '我的悬赏', auth: true },
  { to: '/wallet', label: '钱庄', auth: true },
  { to: '/growth', label: '成长兑换', auth: true },
  { to: '/messages', label: '站内消息', auth: true },
  { to: '/profile', label: '侠士资料', auth: true },
]

const visibleNav = computed(() => nav.filter((n) => !n.auth || auth.isLoggedIn))
const showNav = computed(() => !['login', 'register', 'invite-landing'].includes(String(route.name)))

watch(
  () => route.fullPath,
  () => {
    menuOpen.value = false
  },
)

async function onLogout() {
  menuOpen.value = false
  await auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="hero-app">
    <header v-if="showNav" class="topbar">
      <div class="jh-container bar-inner">
        <RouterLink to="/" class="brand brand-title">江湖令</RouterLink>

        <nav class="nav desktop-only">
          <RouterLink
            v-for="item in visibleNav.slice(0, 5)"
            :key="item.to"
            :to="item.to"
            class="nav-link"
          >
            {{ item.label }}
          </RouterLink>
        </nav>

        <div class="actions desktop-only">
          <template v-if="auth.isLoggedIn">
            <RouterLink to="/messages" class="ghost">消息</RouterLink>
            <RouterLink v-if="auth.hasOffice" to="/hall" class="ghost">执事堂</RouterLink>
            <RouterLink to="/profile" class="user">{{ auth.user?.nickname || '侠士' }}</RouterLink>
            <button class="link-btn" type="button" @click="onLogout">登出</button>
          </template>
          <template v-else>
            <RouterLink to="/login" class="ghost">登录</RouterLink>
            <RouterLink to="/register" class="cta">持令入江湖</RouterLink>
          </template>
        </div>

        <button
          class="menu-btn mobile-only"
          type="button"
          :aria-expanded="menuOpen"
          aria-label="打开菜单"
          @click="menuOpen = !menuOpen"
        >
          <span />
          <span />
          <span />
        </button>
      </div>
    </header>

    <div v-if="showNav && menuOpen" class="drawer-mask mobile-only" @click="menuOpen = false" />
    <aside v-if="showNav" class="drawer mobile-only" :class="{ open: menuOpen }">
      <div class="drawer-head">
        <strong class="brand-title">江湖令</strong>
        <button type="button" class="link-btn" @click="menuOpen = false">关闭</button>
      </div>
      <nav class="drawer-nav">
        <RouterLink v-for="item in visibleNav" :key="item.to" :to="item.to" class="drawer-link">
          {{ item.label }}
        </RouterLink>
        <RouterLink v-if="auth.hasOffice" to="/hall" class="drawer-link">执事堂</RouterLink>
        <RouterLink v-if="!auth.isLoggedIn" to="/login" class="drawer-link">登录</RouterLink>
        <RouterLink v-if="!auth.isLoggedIn" to="/register" class="drawer-link accent">持令入江湖</RouterLink>
        <button v-if="auth.isLoggedIn" type="button" class="drawer-link as-btn" @click="onLogout">
          登出
        </button>
      </nav>
    </aside>

    <main class="page-main">
      <RouterView />
    </main>

    <nav v-if="showNav" class="tabbar mobile-only">
      <RouterLink to="/" class="tab">广场</RouterLink>
      <RouterLink to="/notices" class="tab">告示</RouterLink>
      <RouterLink to="/ranks" class="tab">英雄谱</RouterLink>
      <RouterLink :to="auth.isLoggedIn ? '/mine' : '/login'" class="tab">我的</RouterLink>
      <RouterLink :to="auth.isLoggedIn ? '/wallet' : '/register'" class="tab">
        {{ auth.isLoggedIn ? '钱庄' : '入江湖' }}
      </RouterLink>
    </nav>

    <footer v-if="showNav" class="footer">
      <div class="jh-container">
        <p class="brand-title">江湖令</p>
        <p class="jh-muted">天下有悬赏，江湖有侠士。 · 遵义试点 · 模拟银两非真实货币</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  backdrop-filter: blur(10px);
  background: rgba(247, 245, 242, 0.92);
  border-bottom: 1px solid var(--jh-line);
  padding-top: env(safe-area-inset-top);
}
.bar-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  min-height: 56px;
}
.brand {
  font-size: 26px;
  color: var(--jh-ink);
  white-space: nowrap;
}
.nav {
  display: flex;
  gap: 14px;
  flex: 1;
  flex-wrap: wrap;
}
.nav-link {
  color: var(--jh-ink-soft);
  font-size: 14px;
  padding: 6px 0;
  border-bottom: 2px solid transparent;
}
.nav-link.router-link-active {
  color: var(--jh-seal);
  border-bottom-color: var(--jh-seal);
}
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}
.ghost {
  color: var(--jh-muted);
}
.cta,
.user {
  color: #fff;
  background: var(--jh-seal);
  padding: 8px 12px;
  border-radius: 999px;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user {
  background: var(--jh-ink-soft);
}
.link-btn {
  border: none;
  background: transparent;
  color: var(--jh-muted);
  cursor: pointer;
  padding: 0;
  font: inherit;
}
.menu-btn {
  margin-left: auto;
  width: 44px;
  height: 44px;
  border: none;
  background: transparent;
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  padding: 10px;
  cursor: pointer;
}
.menu-btn span {
  display: block;
  height: 2px;
  background: var(--jh-ink);
  border-radius: 2px;
}
.drawer-mask {
  position: fixed;
  inset: 0;
  background: rgba(28, 36, 48, 0.45);
  z-index: 40;
}
.drawer {
  position: fixed;
  top: 0;
  right: 0;
  width: min(320px, 86vw);
  height: 100%;
  background: #fff;
  z-index: 50;
  transform: translateX(100%);
  transition: transform 0.22s ease;
  padding: calc(16px + env(safe-area-inset-top)) 16px 24px;
  box-shadow: -8px 0 24px rgba(0, 0, 0, 0.12);
}
.drawer.open {
  transform: translateX(0);
}
.drawer-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.drawer-nav {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.drawer-link {
  display: block;
  padding: 14px 12px;
  border-radius: 10px;
  font-size: 16px;
  color: var(--jh-ink);
}
.drawer-link.router-link-active,
.drawer-link.accent {
  background: rgba(178, 58, 45, 0.08);
  color: var(--jh-seal);
}
.drawer-link.as-btn {
  width: 100%;
  text-align: left;
  border: none;
  background: transparent;
  cursor: pointer;
  font: inherit;
}
.tabbar {
  display: none;
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 25;
  background: rgba(255, 255, 255, 0.96);
  border-top: 1px solid var(--jh-line);
  padding: 6px 4px calc(6px + env(safe-area-inset-bottom));
  grid-template-columns: repeat(5, 1fr);
}
.tab {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  font-size: 12px;
  color: var(--jh-muted);
}
.tab.router-link-active {
  color: var(--jh-seal);
  font-weight: 600;
}
.footer {
  border-top: 1px solid var(--jh-line);
  padding: 28px 0 40px;
  margin-top: 24px;
}
.footer .brand-title {
  font-size: 22px;
  margin: 0 0 6px;
}
.mobile-only {
  display: none;
}
.desktop-only {
  display: flex;
}

@media (max-width: 768px) {
  .desktop-only {
    display: none !important;
  }
  .mobile-only {
    display: flex;
  }
  .menu-btn {
    display: flex;
  }
  .drawer.mobile-only {
    display: block;
  }
  .drawer-mask.mobile-only {
    display: block;
  }
  .tabbar {
    display: grid;
  }
  .page-main {
    padding-bottom: calc(64px + env(safe-area-inset-bottom));
  }
  .footer {
    padding-bottom: calc(88px + env(safe-area-inset-bottom));
  }
  .brand {
    font-size: 24px;
  }
}
</style>
