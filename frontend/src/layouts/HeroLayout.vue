<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useMessageStore } from '@/stores/message'
import { useMineAttentionStore } from '@/stores/mineAttention'
import { useHallAttentionStore } from '@/stores/hallAttention'
import NoticeBoardShell from '@/components/NoticeBoardShell.vue'

const auth = useAuthStore()
const messageStore = useMessageStore()
const mineAttention = useMineAttentionStore()
const hallAttention = useHallAttentionStore()
const route = useRoute()
const router = useRouter()
const menuOpen = ref(false)
/** 打开消息页前的路径，再次点击消息可返回 */
const messagesReturnTo = ref<string | null>(null)

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
const onMessagesPage = computed(() => route.path === '/messages' || route.name === 'messages')
const unreadBadge = computed(() => {
  const n = messageStore.unreadCount
  if (!n || n <= 0) return ''
  return n > 99 ? '99+' : String(n)
})
const mineAttentionDot = computed(() => auth.isLoggedIn && mineAttention.hasAttention)
const hallAttentionDot = computed(() => auth.isLoggedIn && auth.hasOffice && hallAttention.hasAttention)
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
  if (to === '/messages') return onMessagesPage.value
  return path === to || path.startsWith(`${to}/`)
}

function toggleMessages() {
  menuOpen.value = false
  if (onMessagesPage.value) {
    const back = messagesReturnTo.value || '/'
    messagesReturnTo.value = null
    router.push(back)
    return
  }
  messagesReturnTo.value = route.fullPath
  router.push('/messages')
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
    if (loggedIn) {
      messageStore.startPolling()
      mineAttention.startPolling()
      if (auth.hasOffice) hallAttention.startPolling()
      else hallAttention.clear()
    } else {
      messageStore.clear()
      mineAttention.clear()
      hallAttention.clear()
    }
  },
  { immediate: true },
)

watch(
  () => auth.hasOffice,
  (has) => {
    if (!auth.isLoggedIn) return
    if (has) hallAttention.startPolling()
    else hallAttention.clear()
  },
)

async function onLogout() {
  menuOpen.value = false
  messageStore.clear()
  mineAttention.clear()
  hallAttention.clear()
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
              <span
                v-if="item.to === '/mine' && mineAttentionDot"
                class="nav-dot"
                aria-label="我的悬赏有更新"
              />
            </RouterLink>
          </nav>

          <div class="actions desktop-only">
            <template v-if="auth.isLoggedIn">
              <button
                type="button"
                class="ghost with-badge"
                :class="{ 'is-active': onMessagesPage }"
                @click="toggleMessages"
              >
                消息
                <span v-if="unreadBadge" class="badge">{{ unreadBadge }}</span>
              </button>
              <RouterLink
                v-if="auth.hasOffice"
                to="/hall"
                class="ghost with-badge"
                :class="{ 'is-active': isNavActive('/hall') }"
              >
                执事堂
                <span
                  v-if="hallAttentionDot"
                  class="nav-dot"
                  aria-label="执事堂有待审"
                />
              </RouterLink>
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
            :class="{ 'is-open': menuOpen }"
            type="button"
            :aria-expanded="menuOpen"
            :aria-label="menuOpen ? '收起菜单' : '打开菜单'"
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
          <template v-for="item in visibleNav" :key="item.to">
            <button
              v-if="item.to === '/messages'"
              type="button"
              class="nav-link"
              :class="{ 'is-active': onMessagesPage }"
              @click="toggleMessages"
            >
              <span>{{ item.label }}</span>
              <span v-if="unreadBadge" class="badge">{{ unreadBadge }}</span>
            </button>
            <RouterLink
              v-else
              :to="item.to"
              class="nav-link"
              :class="{ 'is-active': isNavActive(item.to) }"
              active-class=""
              exact-active-class=""
            >
              <span>{{ item.label }}</span>
              <span
                v-if="item.to === '/mine' && mineAttentionDot"
                class="nav-dot"
                aria-label="我的悬赏有更新"
              />
            </RouterLink>
          </template>
          <RouterLink
            v-if="auth.hasOffice"
            to="/hall"
            class="nav-link"
            :class="{ 'is-active': isNavActive('/hall') }"
            active-class=""
            exact-active-class=""
          >
            <span>执事堂</span>
            <span
              v-if="hallAttentionDot"
              class="nav-dot"
              aria-label="执事堂有待审"
            />
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
            <RouterLink to="/feedbacks">意见反馈</RouterLink>
            <span>·</span>
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
  position: relative;
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
  position: relative;
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
.nav-dot {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #c62828;
  box-shadow: 0 0 0 1px rgba(251, 246, 232, 0.95);
  pointer-events: none;
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
  margin-left: auto;
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
  width: 34px;
  height: 34px;
  border: 1px solid rgba(90, 66, 40, 0.4);
  border-radius: var(--jh-wood-radius);
  background: linear-gradient(180deg, #fbf6e8, #eadfc8);
  display: none;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  padding: 7px;
  cursor: pointer;
  position: relative;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.55);
}
.menu-btn span:not(.menu-dot) {
  display: block;
  height: 1.5px;
  background: #3a2a18;
  border-radius: 2px;
  transition: transform 0.2s ease, opacity 0.2s ease;
  transform-origin: center;
}
.menu-btn.is-open span:not(.menu-dot):nth-child(1) {
  transform: translateY(5.5px) rotate(45deg);
}
.menu-btn.is-open span:not(.menu-dot):nth-child(2) {
  opacity: 0;
}
.menu-btn.is-open span:not(.menu-dot):nth-child(3) {
  transform: translateY(-5.5px) rotate(-45deg);
}
.menu-dot {
  position: absolute;
  top: 5px;
  right: 5px;
  width: 6px;
  height: 6px;
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
  /* 落在 board-face 内；顶栏下展开，由同一菜单钮开关 */
  position: absolute;
  top: 3.6rem;
  right: 0;
  bottom: auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  width: min(240px, 78%);
  height: auto;
  max-height: calc(100% - 3.6rem);
  background: transparent;
  z-index: 50;
  transform: translateX(100%);
  transition: transform 0.22s ease;
  padding: 8px 12px 16px;
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
.drawer-nav {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  align-self: stretch;
  gap: 8px;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
  align-items: flex-end;
}
.drawer-nav .nav-link,
.drawer-nav .ghost,
.drawer-nav .cta {
  width: 50%;
  max-width: 50%;
  box-sizing: border-box;
  justify-content: center;
  text-align: center;
  font: inherit;
  cursor: pointer;
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
    display: flex;
    flex-direction: column;
    align-items: flex-end;
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
  /* 仅折叠菜单内：名称与导航同列靠右；顶栏侠士芯片不受影响 */
  .drawer .drawer-chip {
    margin-left: auto;
    max-width: 100%;
  }
  .drawer-nav {
    width: 100%;
    gap: 8px;
    align-items: flex-end;
  }
  .drawer-nav .nav-link,
  .drawer-nav .ghost,
  .drawer-nav .cta {
    width: 50%;
    max-width: 50%;
    font-size: 14px;
    padding: 8px 10px;
  }
}
</style>
