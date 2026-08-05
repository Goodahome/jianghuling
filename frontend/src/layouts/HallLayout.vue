<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import NoticeBoardShell from '@/components/NoticeBoardShell.vue'

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
const displayName = computed(() => auth.user?.nickname || auth.me?.nickname || '未名侠士')
const levelTitle = computed(() => auth.me?.levelTitle || auth.user?.levelTitle || '侠士')
const nameInitial = computed(() => {
  const n = displayName.value.trim()
  return n ? n.slice(0, 1) : '侠'
})

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
    <NoticeBoardShell title="执事堂">
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
            <RouterLink to="/" class="ghost">返回江湖</RouterLink>
            <div class="user-chip" :title="`${displayName} · ${levelTitle}`">
              <span class="user-avatar" aria-hidden="true">{{ nameInitial }}</span>
              <span class="user-meta">
                <span class="user-name">{{ displayName }}</span>
                <span class="user-title">{{ levelTitle }}</span>
              </span>
            </div>
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
          <div class="drawer-user">
            <span class="user-avatar" aria-hidden="true">{{ nameInitial }}</span>
            <span class="user-meta">
              <span class="user-name">{{ displayName }}</span>
              <span class="user-title">{{ levelTitle }}</span>
            </span>
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
              @click="menuOpen = false"
            >
              {{ m.label }}
            </RouterLink>
            <RouterLink to="/" class="drawer-link" active-class="" exact-active-class="" @click="menuOpen = false">
              返回江湖
            </RouterLink>
          </nav>
      </aside>

      <main class="page-main">
        <RouterView />
      </main>

      <footer class="footer">
        <div class="jh-container">
          <p class="brand-title">执事堂</p>
          <p class="jh-muted">职司履职之所 · 与侠士同江湖</p>
        </div>
      </footer>
    </NoticeBoardShell>
  </div>
</template>

<style scoped>
.topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  background: transparent;
  border-bottom: none;
  padding: calc(10px + env(safe-area-inset-top)) 0 12px;
}
.bar-inner {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 64px;
  flex-wrap: wrap;
}
.brand {
  font-family: var(--jh-font-display);
  font-size: 22px;
  letter-spacing: 0.22em;
  text-indent: 0.12em;
  white-space: nowrap;
  padding: 8px 16px;
  color: #3a2a18;
  background: linear-gradient(180deg, #f7f0dd 0%, #e8d9b8 100%);
  border: 1px solid rgba(90, 66, 40, 0.45);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.65),
    inset 0 -2px 4px rgba(90, 66, 40, 0.12),
    0 2px 0 rgba(42, 34, 24, 0.2);
  text-shadow:
    0 1px 0 rgba(255, 255, 255, 0.35),
    0 -1px 0 rgba(42, 34, 24, 0.25);
}
.nav {
  display: flex;
  gap: 8px;
  flex: 1;
  flex-wrap: wrap;
  align-items: center;
}
.nav-link {
  font-family: var(--jh-font-display);
  font-size: 14px;
  letter-spacing: 0.12em;
  color: #4a3824;
  padding: 8px 12px;
  background: linear-gradient(180deg, #fbf6e8 0%, #eadfc8 100%);
  border: 1px solid rgba(90, 66, 40, 0.35);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.55),
    0 1px 0 rgba(42, 34, 24, 0.15);
  text-shadow:
    0 1px 0 rgba(255, 255, 255, 0.4),
    0 -0.5px 0 rgba(42, 34, 24, 0.2);
}
.nav-link.is-active {
  color: var(--jh-seal);
  border-color: rgba(178, 58, 45, 0.55);
}
.actions {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  max-width: 168px;
  min-width: 0;
  padding: 4px 10px 4px 4px;
  background: linear-gradient(180deg, #fbf6e8 0%, #eadfc8 100%);
  border: 1px solid rgba(90, 66, 40, 0.4);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.55),
    0 1px 0 rgba(42, 34, 24, 0.15);
}
.user-avatar {
  flex-shrink: 0;
  width: 28px;
  height: 28px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  font-family: var(--jh-font-display);
  font-size: 14px;
  color: #f7f0dd;
  background: linear-gradient(160deg, #c45a4a, var(--jh-seal));
  border: 1px solid rgba(90, 30, 24, 0.35);
}
.user-meta {
  min-width: 0;
  display: flex;
  flex-direction: column;
  line-height: 1.15;
}
.user-name {
  font-family: var(--jh-font-display);
  font-size: 14px;
  color: #3a2a18;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-title {
  font-size: 11px;
  letter-spacing: 0.08em;
  color: var(--jh-seal);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ghost {
  font-family: var(--jh-font-display);
  color: #4a3824;
  padding: 7px 10px;
  background: linear-gradient(180deg, #fbf6e8, #eadfc8);
  border: 1px solid rgba(90, 66, 40, 0.35);
  letter-spacing: 0.08em;
}
.menu-btn {
  margin-left: auto;
  width: 44px;
  height: 44px;
  border: 1px solid rgba(90, 66, 40, 0.45);
  background: linear-gradient(180deg, #fbf6e8, #eadfc8);
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  padding: 10px;
  cursor: pointer;
  flex-shrink: 0;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.55);
}
.menu-btn span {
  display: block;
  height: 2px;
  background: #3a2a18;
  border-radius: 2px;
}
.drawer-mask {
  /* 相对告示板木面，不盖瓦顶 */
  position: absolute;
  inset: 0;
  background: rgba(28, 36, 48, 0.45);
  z-index: 40;
}
.drawer {
  /* 落在 board-face 内，避免 fixed 顶到视口被瓦面遮挡 */
  position: absolute;
  top: 0;
  right: 0;
  bottom: auto;
  display: flex;
  flex-direction: column;
  width: min(260px, 72%);
  height: auto;
  max-height: 100%;
  background: #fff;
  z-index: 50;
  transform: translateX(100%);
  transition: transform 0.22s ease;
  padding: 16px 16px 24px;
  border-left: 1px solid var(--jh-line);
  box-sizing: border-box;
  overflow: hidden;
}
.drawer:not(.open) {
  pointer-events: none;
}
.drawer.open {
  transform: translateX(0);
  pointer-events: auto;
}
.drawer-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-shrink: 0;
  margin-bottom: 16px;
}
.drawer-user {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: 10px;
  margin: 0 0 12px;
  padding: 10px 12px;
  background: linear-gradient(180deg, #fbf6e8, #eadfc8);
  border: 1px solid rgba(90, 66, 40, 0.35);
}
.drawer-user .user-avatar {
  width: 36px;
  height: 36px;
  font-size: 16px;
}
.drawer-user .user-name {
  font-size: 16px;
}
.drawer-nav {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  gap: 4px;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
}
.drawer-link {
  display: block;
  padding: 14px 12px;
  border-radius: var(--jh-radius);
  font-size: 16px;
  color: var(--jh-ink);
  flex-shrink: 0;
}
.drawer-link.is-active {
  background: var(--jh-mist);
  color: var(--jh-seal);
}
.link-btn {
  border: none;
  background: transparent;
  color: #5a4630;
  cursor: pointer;
  font: inherit;
  padding: 0 4px;
}
.page-main {
  min-height: 50vh;
}
.footer {
  border-top: 1px solid rgba(196, 163, 90, 0.3);
  padding: 28px 0 40px;
  margin-top: 24px;
  color: rgba(247, 240, 221, 0.85);
}
.footer .brand-title {
  font-size: 22px;
  margin: 0 0 6px;
  color: var(--jh-gold-bright);
}
.footer .jh-muted {
  color: rgba(247, 240, 221, 0.65);
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
  .topbar {
    padding: calc(8px + env(safe-area-inset-top)) 0 8px;
  }
  .bar-inner {
    flex-wrap: nowrap;
    gap: 8px;
    min-height: 52px;
    min-width: 0;
  }
  .brand {
    font-size: 18px;
    letter-spacing: 0.12em;
    text-indent: 0.06em;
    padding: 6px 10px;
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
  }
  .drawer {
    width: min(220px, 68%);
    max-width: 220px;
    max-height: calc(100vh - 5.75rem);
    max-height: calc(100dvh - 5.75rem - env(safe-area-inset-bottom, 0px));
    background: linear-gradient(180deg, #fbf6e8 0%, #f3ead4 100%);
    border-left: 1px solid rgba(90, 66, 40, 0.35);
    padding: 12px 12px 14px;
    padding-bottom: calc(14px + env(safe-area-inset-bottom, 0px));
    overflow: hidden;
  }
  .drawer-head {
    margin-bottom: 12px;
  }
  .drawer-head .brand-title {
    color: #3a2a18;
  }
  .drawer-user {
    margin-bottom: 10px;
    padding: 9px 11px;
  }
  .drawer-nav {
    gap: 2px;
  }
  .drawer-link {
    padding: 11px 11px;
    font-size: 15px;
  }
  .page-main {
    padding-bottom: env(safe-area-inset-bottom);
    min-width: 0;
    max-width: 100%;
    overflow-x: clip;
  }
  .footer {
    padding: 20px 0 28px;
  }
}
</style>
