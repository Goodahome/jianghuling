<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listMyActions } from '@/api/hall'
import EmptyState from '@/components/EmptyState.vue'
import HallBackBar from '@/components/HallBackBar.vue'

type ActionRow = {
  id?: number
  targetType?: string
  targetId?: number
  result?: string
  reason?: string
  createdAt?: string
}

const list = ref<ActionRow[]>([])
const loading = ref(false)

function typeLabel(type?: string) {
  if (type === 'BOUNTY') return '令审（发令审核）'
  if (type === 'SUBMISSION') return '验功（成果审核）'
  return type || '审核'
}

function resultLabel(result?: string) {
  if (result === 'APPROVE') return '通过'
  if (result === 'REJECT') return '驳回'
  return result || '—'
}

function resultClass(result?: string) {
  if (result === 'APPROVE') return 'ok'
  if (result === 'REJECT') return 'reject'
  return ''
}

function targetLabel(row: ActionRow) {
  if (row.targetType === 'BOUNTY') return `悬赏令 #${row.targetId ?? '—'}`
  if (row.targetType === 'SUBMISSION') return `成果单 #${row.targetId ?? '—'}`
  return `${row.targetType || '对象'} #${row.targetId ?? '—'}`
}

function targetLink(row: ActionRow) {
  if (!row.targetId) return ''
  if (row.targetType === 'BOUNTY') return `/bounties/${row.targetId}`
  if (row.targetType === 'SUBMISSION') return `/hall/submission-reviews/${row.targetId}`
  return ''
}

function formatTime(raw?: string) {
  if (!raw) return '—'
  const d = new Date(raw)
  if (Number.isNaN(d.getTime())) return raw
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(async () => {
  loading.value = true
  try {
    const data = await listMyActions({ page: 1, pageSize: 50 })
    list.value = (data.list || []) as ActionRow[]
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <HallBackBar to="/hall" />
      <h1 class="brand-title">履职记录</h1>
      <p class="jh-muted">你在执事堂做过的令审 / 验功，可点进查看当时对象</p>

      <div v-loading="loading" class="list">
        <EmptyState v-if="!loading && !list.length" title="暂无履职记录" />
        <div v-for="row in list" :key="row.id ?? `${row.targetType}-${row.targetId}-${row.createdAt}`" class="item jh-panel">
          <div class="row">
            <strong>{{ typeLabel(row.targetType) }}</strong>
            <span class="result" :class="resultClass(row.result)">{{ resultLabel(row.result) }}</span>
          </div>
          <p class="target">
            <RouterLink v-if="targetLink(row)" :to="targetLink(row)" class="link">
              {{ targetLabel(row) }}
            </RouterLink>
            <span v-else>{{ targetLabel(row) }}</span>
          </p>
          <p v-if="row.reason" class="reason">原因 / 意见：{{ row.reason }}</p>
          <p v-else class="jh-muted reason-empty">未填写原因</p>
          <p class="jh-muted time">{{ formatTime(row.createdAt) }}</p>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
h1 {
  margin: 0 0 6px;
  font-size: 32px;
}
.list {
  display: grid;
  gap: 10px;
  min-height: 120px;
  margin-top: 14px;
}
.item {
  padding: 14px 16px;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}
.result {
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 600;
  padding: 2px 10px;
  border-radius: var(--jh-radius);
  border: 1px solid var(--jh-line);
  color: var(--jh-ink-soft);
}
.result.ok {
  color: var(--jh-ok);
  border-color: color-mix(in srgb, var(--jh-ok) 35%, #fff);
  background: color-mix(in srgb, var(--jh-ok) 8%, #fff);
}
.result.reject {
  color: var(--jh-seal);
  border-color: color-mix(in srgb, var(--jh-seal) 35%, #fff);
  background: color-mix(in srgb, var(--jh-seal) 8%, #fff);
}
.target {
  margin: 0 0 6px;
  font-size: 15px;
}
.link {
  color: var(--jh-seal);
}
.reason {
  margin: 0 0 4px;
  color: var(--jh-ink-soft);
  font-size: 14px;
  line-height: 1.5;
}
.reason-empty {
  margin: 0 0 4px;
  font-size: 13px;
}
.time {
  margin: 0;
  font-size: 13px;
}
</style>
