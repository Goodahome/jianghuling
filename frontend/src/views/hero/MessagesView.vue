<script setup lang="ts">
import { onMounted, ref } from 'vue'
import type { RouteLocationRaw } from 'vue-router'
import { listMessages, markAllRead, markRead } from '@/api/message'
import { useMessageStore } from '@/stores/message'
import type { SiteMessage } from '@/types/models'
import EmptyState from '@/components/EmptyState.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'

const messageStore = useMessageStore()
const list = ref<SiteMessage[]>([])
const loading = ref(false)
const unreadOnly = ref(false)

/** 悬赏类消息进详情时带 from=mine，面包屑为「我的悬赏 / 悬赏详情」 */
function bountyDetailTo(bountyId: number | string): RouteLocationRaw {
  return { path: `/bounties/${bountyId}`, query: { from: 'mine' } }
}

function bizLink(m: SiteMessage): RouteLocationRaw | '' {
  if (!m.bizType || m.bizId == null) return ''
  if (m.bizType === 'BOUNTY' || m.bizType.includes('BOUNTY')) return bountyDetailTo(m.bizId)
  if (m.bizType.includes('DISPUTE')) return `/disputes/${m.bizId}`
  if (m.bizType === 'WALLET') return '/wallet'
  return ''
}

type ContentPart = { text: string; to?: RouteLocationRaw }

/** 将 content 中「悬赏标题」做成可点链接（约定书名号内为标题） */
function contentParts(m: SiteMessage): ContentPart[] {
  const content = m.content || ''
  const link = bizLink(m)
  if (!link || typeof link === 'string' || !('path' in link) || !String(link.path || '').startsWith('/bounties/')) {
    return [{ text: content }]
  }
  const re = /「([^」]+)」/
  const match = content.match(re)
  if (!match || match.index == null) return [{ text: content }]
  const i = match.index
  const parts: ContentPart[] = []
  if (i > 0) parts.push({ text: content.slice(0, i) })
  parts.push({ text: match[1], to: link })
  const after = content.slice(i + match[0].length)
  if (after) parts.push({ text: after })
  return parts
}

async function load() {
  loading.value = true
  try {
    const data = await listMessages({ page: 1, pageSize: 50, unreadOnly: unreadOnly.value || undefined })
    list.value = data.list || []
    await messageStore.refreshUnread()
  } finally {
    loading.value = false
  }
}

async function onRead(id: number) {
  await markRead(id)
  await load()
}

async function onReadAll() {
  await markAllRead()
  await load()
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow">
      <JhPageHeader title="站内消息" />
      <div class="head">
        <div class="actions">
          <el-checkbox v-model="unreadOnly" @change="load">仅未读</el-checkbox>
          <el-button @click="onReadAll">全部已读</el-button>
        </div>
      </div>
      <div v-loading="loading" class="list">
        <EmptyState v-if="!loading && !list.length" title="暂无消息" />
        <div v-for="m in list" :key="m.id" class="jh-panel item" :class="{ unread: !m.read }">
          <div class="row">
            <div class="title-wrap">
              <span v-if="!m.read" class="dot" aria-hidden="true" />
              <strong :class="{ 'unread-title': !m.read }">{{ m.title }}</strong>
            </div>
            <el-button v-if="!m.read" link type="primary" @click="onRead(m.id)">标为已读</el-button>
          </div>
          <p :class="{ 'unread-body': !m.read }">
            <template v-for="(part, idx) in contentParts(m)" :key="`${m.id}-${idx}`">
              <RouterLink v-if="part.to" :to="part.to" class="bounty-link">{{ part.text }}</RouterLink>
              <template v-else>{{ part.text }}</template>
            </template>
          </p>
          <p class="jh-muted">
            {{ m.createdAt }}
            <RouterLink v-if="bizLink(m)" :to="bizLink(m)" class="jump">查看相关</RouterLink>
          </p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.head {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}
.actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
.list {
  margin-top: 14px;
  display: grid;
  gap: 10px;
}
.item {
  padding: 14px;
  background: transparent;
}
.item.unread {
  border-color: var(--jh-seal);
  background: rgba(178, 58, 45, 0.12);
}
.title-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--jh-seal);
  flex-shrink: 0;
}
.unread-title {
  font-weight: 700;
  color: var(--jh-ink);
}
.unread-body {
  color: var(--jh-ink);
}
.item:not(.unread) strong {
  font-weight: 500;
  color: var(--jh-ink-soft);
}
.item:not(.unread) p {
  color: var(--jh-muted);
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
}
.jump {
  margin-left: 8px;
  color: var(--jh-seal);
}
.bounty-link {
  color: var(--jh-seal);
  text-decoration: underline;
  text-underline-offset: 2px;
  font-weight: 600;
}
.bounty-link:hover {
  opacity: 0.88;
}
</style>
