<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const auth = useAuthStore()
const menuOpen = ref(false)

const allMenus = [
  { path: '/hall', label: '堂前概览', exact: true },
  { path: '/hall/bounty-reviews', label: '令审队列', need: 'DECREE_REVIEWER' as const },
  { path: '/hall/submission-reviews', label: '验功队列', need: 'FEAT_REVIEWER' as const },
  { path: '/hall/actions', label: '履职记录' },
]

const menus = computed(() =>
  allMenus.filter((m) => {
    if (!m.need) return true
    return auth.hasOfficeCode(m.need)
  }),
)

const pageTitle = computed(() => String(route.meta.title || '执事堂'))

function isActive(path: string, exact?: boolean) {
  if (exact) return route.path === path
  return route.path === path || route.path.startsWith(`${path}/`)
}

watch(
  () => route.fullPath,
  () => {
    menuOpen.value = false
  },
)
</script>

<template>
  <div class="hall-app">
    <header class="topbar">
      <div class="jh-container bar-inner">
        <RouterLink to="/hall" class="brand brand-title">执事堂</RouterLink>
        <nav class="nav desktop-only">
          <RouterLink
            v-for="m in menus"
            :key="m.path"
            :to="m.path"
            class="nav-link"
            :class="{ 'is-active': isActive(m.path, m.exact) }"
            active-class=""
            exact-active-class=""
          >
            {{ m.label }}
          </RouterLink>
        </nav>
        <div class="actions desktop-only">
          <span class="who">{{ auth.user?.nickname || '职司' }}</span>
          <RouterLink to="/" class="ghost">返回江湖</RouterLink>
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

    <div v-if="menuOpen" class="drawer-mask mobile-only" @click="menuOpen = false" />
    <aside class="drawer mobile-only" :class="{ open: menuOpen }">
      <div class="drawer-head">
        <strong class="brand-title">执事堂</strong>
        <button type="button" class="link-btn" @click="menuOpen = false">关闭</button>
      </div>
      <nav class="drawer-nav">
        <RouterLink
          v-for="m in menus"
          :key="m.path"
          :to="m.path"
          class="drawer-link"
          :class="{ 'is-active': isActive(m.path, m.exact) }"
          active-class=""
          exact-active-class=""
        >
          {{ m.label }}
        </RouterLink>
        <RouterLink to="/" class="drawer-link" active-class="" exact-active-class="">返回江湖</RouterLink>
      </nav>
    </aside>

    <main class="page-main">
      <RouterView />
    </main>

    <footer class="footer">
      <div class="jh-container">
        <p class="brand-title">执事堂</p>
        <p class="jh-muted">职司履职之所 · 与侠士同江湖 · 非武林盟运营后台</p>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  background: #fff;
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
.nav-link.is-active {
  color: var(--jh-seal);
  border-bottom-color: var(--jh-seal);
}
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}
.who {
  color: var(--jh-muted);
  max-width: 96px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ghost {
  color: var(--jh-seal);
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
  border-left: 1px solid var(--jh-line);
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
  border-radius: var(--jh-radius);
  font-size: 16px;
  color: var(--jh-ink);
}
.drawer-link.is-active {
  background: var(--jh-mist);
  color: var(--jh-seal);
}
.link-btn {
  border: none;
  background: transparent;
  color: var(--jh-muted);
  cursor: pointer;
  font: inherit;
}
.page-main {
  min-height: 50vh;
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
  .page-main {
    padding-bottom: env(safe-area-inset-bottom);
  }
  .brand {
    font-size: 24px;
  }
}
</style>
