<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import NoticeBoardShell from '@/components/NoticeBoardShell.vue'

const auth = useAuthStore()
const messageStore = useMessageStore()
const route = useRoute()
const router = useRouter()
const menuOpen = ref(false)

const nav = [
  { to: '/', label: '首页' },
  { to: '/plaza', label: '悬赏榜' },
  { to: '/notices', label: '告示栏' },
  { to: '/ranks', label: '英雄榜' },
  { to: '/mine', label: '我的悬赏', auth: true },
  { to: '/wallet', label: '钱庄', auth: true },
  { to: '/messages', label: '站内消息', auth: true },
]

const visibleNav = computed(() => nav.filter((n) => !n.auth || auth.isLoggedIn))
/** 桌面顶栏：消息已在右侧入口，不重复占位 */
const desktopNav = computed(() => visibleNav.value.filter((n) => n.to !== '/messages'))
const showNav = computed(() => !['login', 'register', 'invite-landing'].includes(String(route.name)))
const unreadBadge = computed(() => {
  const n = messageStore.unreadCount
  if (!n || n <= 0) return ''
  return n > 99 ? '99+' : String(n)
})
const displayName = computed(() => auth.user?.nickname || auth.me?.nickname || '未名侠士')
const levelTitle = computed(() => auth.me?.levelTitle || auth.user?.levelTitle || '侠士')
const nameInitial = computed(() => {
  const n = displayName.value.trim()
  return n ? n.slice(0, 1) : '侠'
})

/** `/` 不能用默认前缀匹配，否则任意页面都会高亮「首页」 */
function isNavActive(to: string) {
  const path = route.path
  if (to === '/') return path === '/'
  return path === to || path.startsWith(`${to}/`)
}

watch(
  () => route.fullPath,
  () => {
    menuOpen.value = false
  },
)

watch(
  () => auth.isLoggedIn,
  (loggedIn) => {
    if (loggedIn) messageStore.startPolling()
    else messageStore.clear()
  },
  { immediate: true },
)

async function onLogout() {
  menuOpen.value = false
  messageStore.clear()
  await auth.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <div class="hero-app">
    <NoticeBoardShell title="江湖令">
      <header v-if="showNav" class="topbar">
        <div class="jh-container bar-inner">
          <RouterLink to="/" class="brand brand-title">江湖令</RouterLink>

          <nav class="nav desktop-only">
            <RouterLink
              v-for="item in desktopNav"
              :key="item.to"
              :to="item.to"
              class="nav-link"
              :class="{ 'is-active': isNavActive(item.to) }"
              active-class=""
              exact-active-class=""
            >
              {{ item.label }}
            </RouterLink>
          </nav>

          <div class="actions desktop-only">
            <template v-if="auth.isLoggedIn">
              <RouterLink to="/messages" class="ghost with-badge">
                消息
                <span v-if="unreadBadge" class="badge">{{ unreadBadge }}</span>
              </RouterLink>
              <RouterLink v-if="auth.hasOffice" to="/hall" class="ghost">执事堂</RouterLink>
              <button class="ghost" type="button" @click="onLogout">暂别江湖</button>
              <RouterLink
                to="/profile"
                class="user-chip"
                :title="`${displayName} · ${levelTitle}`"
              >
                <span class="user-avatar" aria-hidden="true">{{ nameInitial }}</span>
                <span class="user-meta">
                  <span class="user-name">{{ displayName }}</span>
                  <span class="user-title">{{ levelTitle }}</span>
                </span>
              </RouterLink>
            </template>
            <template v-else>
              <RouterLink to="/login" class="ghost">踏入江湖</RouterLink>
              <RouterLink to="/register" class="cta">初入江湖</RouterLink>
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
            <span v-if="unreadBadge" class="menu-dot" />
          </button>
        </div>
      </header>

      <div v-if="showNav && menuOpen" class="drawer-mask mobile-only" @click="menuOpen = false" />
      <aside v-if="showNav" class="drawer mobile-only" :class="{ open: menuOpen }">
        <div class="drawer-head">
          <button type="button" class="ghost" @click="menuOpen = false">关闭</button>
        </div>
        <RouterLink
          v-if="auth.isLoggedIn"
          to="/profile"
          class="user-chip drawer-chip"
          active-class=""
          exact-active-class=""
          @click="menuOpen = false"
        >
          <span class="user-avatar" aria-hidden="true">{{ nameInitial }}</span>
          <span class="user-meta">
            <span class="user-name">{{ displayName }}</span>
            <span class="user-title">{{ levelTitle }}</span>
          </span>
        </RouterLink>
        <nav class="drawer-nav">
          <RouterLink
            v-for="item in visibleNav"
            :key="item.to"
            :to="item.to"
            class="nav-link"
            :class="{ 'is-active': isNavActive(item.to) }"
            active-class=""
            exact-active-class=""
          >
            <span>{{ item.label }}</span>
            <span v-if="item.to === '/messages' && unreadBadge" class="badge">{{ unreadBadge }}</span>
          </RouterLink>
          <RouterLink
            v-if="auth.hasOffice"
            to="/hall"
            class="nav-link"
            :class="{ 'is-active': isNavActive('/hall') }"
            active-class=""
            exact-active-class=""
          >
            执事堂
          </RouterLink>
          <RouterLink v-if="!auth.isLoggedIn" to="/login" class="ghost" active-class="" exact-active-class="">踏入江湖</RouterLink>
          <RouterLink v-if="!auth.isLoggedIn" to="/register" class="cta" active-class="" exact-active-class="">初入江湖</RouterLink>
          <button v-if="auth.isLoggedIn" type="button" class="ghost" @click="onLogout">
            暂别江湖
          </button>
        </nav>
      </aside>

      <main class="page-main">
        <RouterView />
      </main>

      <footer v-if="showNav" class="footer">
        <div class="jh-container">
          <p class="brand-title">江湖令</p>
          <p class="jh-muted">天下有悬赏，江湖有侠士。 · 内测中 · 模拟银两非真实货币</p>
          <p class="footer-legal">
            <RouterLink to="/legal/user-agreement">用户协议</RouterLink>
            <span>·</span>
            <RouterLink to="/legal/privacy">隐私政策</RouterLink>
          </p>
        </div>
      </footer>
    </NoticeBoardShell>
  </div>
</template>

<style scoped>
.topbar {
  position: relative;
  z-index: 30;
  flex: 0 0 auto;
  background: transparent;
  border-bottom: none;
  padding: calc(6px + env(safe-area-inset-top)) 0 8px;
}
.bar-inner {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 52px;
  flex-wrap: wrap;
}
/* 空白木牌刻字 */
.brand {
  font-family: var(--jh-font-display);
  font-size: 22px;
  letter-spacing: 0.22em;
  text-indent: 0.12em;
  white-space: nowrap;
  padding: 8px 16px;
  color: #3a2a18;
  background:
    linear-gradient(180deg, #f7f0dd 0%, #e8d9b8 100%);
  border: 1px solid rgba(90, 66, 40, 0.45);
  border-radius: var(--jh-wood-radius);
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
  background:
    linear-gradient(180deg, #fbf6e8 0%, #eadfc8 100%);
  border: 1px solid rgba(90, 66, 40, 0.35);
  border-radius: var(--jh-wood-radius);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.55),
    0 1px 0 rgba(42, 34, 24, 0.15);
  text-shadow:
    0 1px 0 rgba(255, 255, 255, 0.4),
    0 -0.5px 0 rgba(42, 34, 24, 0.2);
  transition: transform 0.15s ease, border-color 0.15s ease;
}
.nav-link:hover {
  transform: translateY(-1px);
  border-color: rgba(138, 107, 42, 0.65);
}
.actions {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}
.ghost {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--jh-font-display);
  font-size: 14px;
  color: #4a3824;
  padding: 7px 10px;
  background: linear-gradient(180deg, #fbf6e8, #eadfc8);
  border: 1px solid rgba(90, 66, 40, 0.35);
  border-radius: var(--jh-wood-radius);
  letter-spacing: 0.08em;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.35);
  cursor: pointer;
  white-space: nowrap;
}
.with-badge {
  position: relative;
  display: inline-flex;
  align-items: center;
}
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  margin-left: 4px;
  border-radius: 9px;
  background: var(--jh-seal);
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  line-height: 1;
}
.cta {
  font-family: var(--jh-font-display);
  color: #f7f0dd;
  background: linear-gradient(180deg, #c45a4a, var(--jh-seal));
  padding: 7px 12px;
  border: 1px solid rgba(90, 30, 24, 0.45);
  border-radius: var(--jh-wood-radius);
  letter-spacing: 0.08em;
  white-space: nowrap;
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
  border-radius: var(--jh-wood-radius);
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
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.25);
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
  letter-spacing: 0.04em;
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
.drawer-chip {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  gap: 8px;
  margin: 0 0 10px;
  text-decoration: none;
  width: fit-content;
  max-width: 100%;
}
.link-btn {
  border: none;
  background: transparent;
  color: #efe6d0;
  cursor: pointer;
  padding: 0 4px;
  font: inherit;
  text-shadow: 0 1px 1px rgba(0, 0, 0, 0.35);
  white-space: nowrap;
}
.menu-btn {
  margin-left: auto;
  width: 44px;
  height: 44px;
  border: 1px solid rgba(90, 66, 40, 0.45);
  border-radius: var(--jh-wood-radius);
  background: linear-gradient(180deg, #fbf6e8, #eadfc8);
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 5px;
  padding: 10px;
  cursor: pointer;
  position: relative;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.55);
}
.menu-btn span:not(.menu-dot) {
  display: block;
  height: 2px;
  background: #3a2a18;
  border-radius: 2px;
}
.menu-dot {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--jh-seal);
}
.drawer-mask {
  /* 相对告示板木面，不盖瓦顶 */
  position: absolute;
  inset: 0;
  background: rgba(20, 14, 8, 0.28);
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
  width: min(240px, 78%);
  height: auto;
  max-height: 100%;
  background: transparent;
  z-index: 50;
  transform: translateX(100%);
  transition: transform 0.22s ease;
  padding: 12px 12px 16px;
  border: none;
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
  justify-content: flex-end;
  align-items: center;
  flex-shrink: 0;
  margin-bottom: 10px;
}
.drawer-nav {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  gap: 8px;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
  align-items: stretch;
}
.drawer-nav .nav-link,
.drawer-nav .ghost,
.drawer-nav .cta {
  width: 100%;
  box-sizing: border-box;
  justify-content: space-between;
  text-align: left;
}
.page-main {
  flex: 1 1 auto;
  min-height: 0;
  padding: 0 0 8px;
}
.footer {
  flex: 0 0 auto;
  min-height: 52px;
  border-top: 1px solid rgba(196, 163, 90, 0.3);
  padding: 8px 0 calc(8px + env(safe-area-inset-bottom, 0px));
  margin-top: 0;
  color: rgba(247, 240, 221, 0.85);
  background: linear-gradient(180deg, rgba(28, 20, 10, 0.18), rgba(18, 12, 8, 0.28));
}
.footer .jh-container {
  display: flex;
  align-items: center;
  gap: 10px 16px;
  min-height: 35px;
}
.footer .jh-container > * {
  margin: 0;
}
.footer .brand-title {
  flex-shrink: 0;
  font-size: 18px;
  color: var(--jh-gold-bright);
}
.footer .jh-muted {
  flex: 1 1 auto;
  min-width: 0;
  font-size: 12px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: rgba(247, 240, 221, 0.65);
}
.footer-legal {
  display: flex;
  flex: 0 0 auto;
  gap: 8px;
  align-items: center;
  white-space: nowrap;
  font-size: 12px;
  color: rgba(247, 240, 221, 0.45);
}
.footer-legal a {
  color: rgba(247, 240, 221, 0.78);
}
.footer-legal a:hover {
  color: var(--jh-gold-bright);
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
    flex-shrink: 0;
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
  .page-main {
    padding-bottom: 6px;
    min-width: 0;
    max-width: 100%;
    overflow-x: clip;
  }
  .footer {
    min-height: 48px;
    padding: 7px 0 calc(7px + env(safe-area-inset-bottom, 0px));
  }
  .footer .jh-container {
    display: grid;
    grid-template-columns: auto 1fr;
    align-items: center;
    row-gap: 4px;
  }
  .footer .brand-title {
    font-size: 16px;
  }
  .footer .jh-muted,
  .footer-legal {
    font-size: 11px;
  }
  .footer-legal {
    grid-column: 1 / -1;
  }
  .drawer {
    width: min(220px, 72%);
    max-width: 220px;
    max-height: calc(var(--jh-viewport-min) - 5.5rem);
    max-height: calc(var(--jh-viewport-min) - 5.5rem - env(safe-area-inset-bottom, 0px));
    background: transparent;
    border: none;
    padding: 10px 10px 14px;
    padding-bottom: calc(14px + env(safe-area-inset-bottom, 0px));
    overflow: hidden;
  }
  .drawer-head {
    margin-bottom: 8px;
  }
  .drawer-nav {
    gap: 8px;
  }
  .drawer-nav .nav-link,
  .drawer-nav .ghost,
  .drawer-nav .cta {
    font-size: 14px;
    padding: 8px 12px;
  }
}
</style>
