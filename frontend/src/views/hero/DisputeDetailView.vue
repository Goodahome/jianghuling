<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getDispute } from '@/api/dispute'
import type { Dispute } from '@/types/models'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'

const route = useRoute()
const detail = ref<(Dispute & { evidenceUrls?: string[]; evidenceText?: string; verdictJson?: string }) | null>(
  null,
)
const loading = ref(false)
const crumbs = [
  { label: '我的纠纷', to: '/disputes' },
  { label: '纠纷详情' },
]

onMounted(async () => {
  loading.value = true
  try {
    detail.value = (await getDispute(route.params.id as string)) as typeof detail.value
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <section class="jh-section" v-loading="loading">
    <div class="jh-container narrow" v-if="detail">
      <PageBreadcrumb :items="crumbs" />
      <h1 class="brand-title">纠纷详情 #{{ detail.id }}</h1>
      <div class="jh-panel block">
        <p>
          悬赏令
          <RouterLink :to="`/bounties/${detail.bountyId}`">#{{ detail.bountyId }}</RouterLink>
          · 状态 {{ detail.status }}
        </p>
        <p class="jh-muted">发起时间 {{ detail.createdAt }} · 截止 {{ detail.deadlineAt || '-' }}</p>
        <h2>原因</h2>
        <p>{{ detail.reason }}</p>
        <h2 v-if="detail.evidenceText">举证说明</h2>
        <p v-if="detail.evidenceText">{{ detail.evidenceText }}</p>
        <div v-if="detail.evidenceUrls?.length" class="imgs">
          <a v-for="u in detail.evidenceUrls" :key="u" :href="u" target="_blank" rel="noreferrer">
            <img :src="u" alt="举证" />
          </a>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
h1 {
  margin: 0 0 12px;
  font-size: 28px;
}
.block {
  padding: 16px;
}
h2 {
  margin: 16px 0 8px;
  font-size: 16px;
  font-family: var(--jh-font-display);
}
.imgs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}
.imgs img {
  width: 120px;
  height: 90px;
  object-fit: cover;
  border: 1px solid var(--jh-line);
  border-radius: var(--jh-radius);
}
</style>
