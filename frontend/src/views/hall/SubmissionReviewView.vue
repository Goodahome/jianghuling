<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listSubmissionReviews } from '@/api/hall'
import type { ReviewSubmissionListItem } from '@/types/models'
import { resolveSubmissionStatusLabel } from '@/utils/labels'
import EmptyState from '@/components/EmptyState.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'

const router = useRouter()
const list = ref<ReviewSubmissionListItem[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await listSubmissionReviews({ status: 'PENDING', page: 1, pageSize: 50 })
    list.value = data.list || []
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
      <JhPageHeader title="验功队列" />

      <div v-loading="loading" class="list">
        <EmptyState v-if="!loading && !list.length" title="暂无待审成果" />
        <button
          v-for="item in list"
          :key="item.submissionId"
          type="button"
          class="item jh-panel"
          @click="goDetail(item.submissionId)"
        >
          <div class="row">
            <strong>{{ item.bountyTitle || `悬赏 #${item.bountyId || '—'}` }}</strong>
            <span class="pill">#{{ item.submissionId }} · {{ resolveSubmissionStatusLabel(item.status) }}</span>
          </div>
          <p class="jh-muted summary">{{ item.summary || '（无摘要）' }}</p>
          <p class="jh-muted time">
            {{ item.claimerNickname || `侠士#${item.claimerUserId}` }} · {{ item.createdAt }}
          </p>
        </button>
      </div>
    </div>
  </section>
</template>

<style scoped>
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
  background: transparent;
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
