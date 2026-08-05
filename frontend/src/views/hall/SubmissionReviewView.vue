<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listSubmissionReviews } from '@/api/hall'
import type { Submission } from '@/types/models'
import EmptyState from '@/components/EmptyState.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'

type Row = Submission & { bountyId?: number; bountyTitle?: string }

const router = useRouter()
const list = ref<Row[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await listSubmissionReviews({ status: 'PENDING', page: 1, pageSize: 50 })
    list.value = (data.list || []) as Row[]
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) {
  router.push(`/hall/submission-reviews/${id}`)
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <JhPageHeader title="验功队列" subtitle="待审成果 · 点进详情可阅清单与举证" />

      <div v-loading="loading" class="list">
        <EmptyState v-if="!loading && !list.length" title="暂无待审成果" />
        <button
          v-for="item in list"
          :key="item.id"
          type="button"
          class="item jh-panel"
          @click="goDetail(Number(item.id))"
        >
          <div class="row">
            <strong>{{ item.bountyTitle || `悬赏 #${item.bountyId || '—'}` }}</strong>
            <span class="pill">成果 #{{ item.id }}</span>
          </div>
          <p class="jh-muted summary">{{ item.contentSummary || '（无摘要）' }}</p>
          <p class="jh-muted time">{{ item.createdAt }}</p>
        </button>
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
  width: 100%;
  text-align: left;
  border: 1px solid var(--jh-line);
  background: #fff;
  cursor: pointer;
  font: inherit;
  color: inherit;
}
.item:hover {
  border-color: var(--jh-seal);
}
.row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}
.pill {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--jh-muted);
  border: 1px solid var(--jh-line);
  padding: 2px 8px;
  border-radius: var(--jh-radius);
}
.summary {
  margin: 0 0 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.time {
  margin: 0;
  font-size: 13px;
}
</style>
