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
  { to: '/plaza', label: '悬赏广场' },
  { to: '/notices', label: '告示栏' },
  { to: '/ranks', label: '英雄榜' },
  { to: '/mine', label: '我的悬赏', auth: true },
  { to: '/wallet', label: '钱庄', auth: true },
  { to: '/growth', label: '成长兑换', auth: true },
  { to: '/messages', label: '站内消息', auth: true },
  { to: '/profile', label: '侠士资料', auth: true },
]

const visibleNav = computed(() => nav.filter((n) => !n.auth || auth.isLoggedIn))
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
              v-for="item in visibleNav.slice(0, 5)"
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
              <button class="ghost" type="button" @click="onLogout">隐退江湖</button>
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
            <span v-if="unreadBadge" class="menu-dot" />
          </button>
        </div>
      </header>

      <div v-if="showNav && menuOpen" class="drawer-mask mobile-only" @click="menuOpen = false" />
      <aside v-if="showNav" class="drawer mobile-only" :class="{ open: menuOpen }">
        <div class="drawer-head">
          <strong class="brand-title">江湖令</strong>
          <button type="button" class="link-btn" @click="menuOpen = false">关闭</button>
        </div>
        <RouterLink
          v-if="auth.isLoggedIn"
          to="/profile"
          class="drawer-user"
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
            class="drawer-link"
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
            class="drawer-link"
            :class="{ 'is-active': isNavActive('/hall') }"
            active-class=""
            exact-active-class=""
          >
            执事堂
          </RouterLink>
          <RouterLink v-if="!auth.isLoggedIn" to="/login" class="drawer-link" active-class="" exact-active-class="">登录</RouterLink>
          <RouterLink v-if="!auth.isLoggedIn" to="/register" class="drawer-link accent" active-class="" exact-active-class="">持令入江湖</RouterLink>
          <button v-if="auth.isLoggedIn" type="button" class="ghost drawer-ghost" @click="onLogout">
            隐退江湖
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
        </div>
      </footer>
    </NoticeBoardShell>

    <nav v-if="showNav" class="tabbar mobile-only">
      <RouterLink to="/" class="tab" :class="{ 'is-active': isNavActive('/') }" active-class="" exact-active-class="">首页</RouterLink>
      <RouterLink to="/plaza" class="tab" :class="{ 'is-active': isNavActive('/plaza') }" active-class="" exact-active-class="">广场</RouterLink>
      <RouterLink to="/notices" class="tab" :class="{ 'is-active': isNavActive('/notices') }" active-class="" exact-active-class="">告示</RouterLink>
      <RouterLink
        :to="auth.isLoggedIn ? '/mine' : '/login'"
        class="tab"
        :class="{ 'is-active': isNavActive(auth.isLoggedIn ? '/mine' : '/login') }"
        active-class=""
        exact-active-class=""
      >
        我的
      </RouterLink>
      <RouterLink
        :to="auth.isLoggedIn ? '/messages' : '/register'"
        class="tab with-badge"
        :class="{ 'is-active': isNavActive(auth.isLoggedIn ? '/messages' : '/register') }"
        active-class=""
        exact-active-class=""
      >
        {{ auth.isLoggedIn ? '消息' : '入江湖' }}
        <span v-if="auth.isLoggedIn && unreadBadge" class="badge tab-badge">{{ unreadBadge }}</span>
      </RouterLink>
    </nav>
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
.nav-link.is-active {
  color: var(--jh-seal);
  border-color: rgba(178, 58, 45, 0.55);
  box-shadow:
    inset 0 0 0 1px rgba(178, 58, 45, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.5),
    0 1px 0 rgba(42, 34, 24, 0.15);
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
  letter-spacing: 0.08em;
  text-shadow: 0 1px 0 rgba(255, 255, 255, 0.35);
  cursor: pointer;
  white-space: nowrap;
}
.drawer-ghost {
  width: 100%;
  margin-top: 8px;
  min-height: 44px;
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
.drawer-user {
  display: flex;
  align-items: center;
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
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding: 14px 12px;
  border-radius: var(--jh-radius);
  font-size: 16px;
  color: var(--jh-ink);
}
.drawer-link.is-active,
.drawer-link.accent {
  background: var(--jh-mist);
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
  background: rgba(251, 246, 232, 0.96);
  border-top: 1px solid rgba(138, 107, 42, 0.35);
  padding: 6px 4px calc(6px + env(safe-area-inset-bottom));
  grid-template-columns: repeat(5, 1fr);
}
.tab {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 44px;
  font-size: 12px;
  color: var(--jh-muted);
}
.tab.is-active {
  color: var(--jh-seal);
  font-weight: 600;
}
.tab-badge {
  position: absolute;
  top: 2px;
  right: calc(50% - 28px);
  margin-left: 0;
  min-width: 16px;
  height: 16px;
  font-size: 10px;
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
