<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getNotice } from '@/api/notice'
import type { Notice } from '@/types/models'
import { noticeCategoryLabel } from '@/utils/labels'
import PageBreadcrumb from '@/components/PageBreadcrumb.vue'

const route = useRoute()
const notice = ref<Notice | null>(null)
const crumbs = [
  { label: '告示栏', to: '/notices' },
  { label: '告示详情' },
]

onMounted(async () => {
  notice.value = await getNotice(route.params.id as string)
})
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow" v-if="notice">
      <PageBreadcrumb :items="crumbs" />
      <div class="jh-panel article">
        <p class="jh-muted">{{ noticeCategoryLabel[notice.category] }} · {{ notice.createdAt }}</p>
        <h1>{{ notice.title }}</h1>
        <div class="content">{{ notice.content || notice.summary }}</div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.article {
  padding: 24px;
}
h1 {
  margin: 8px 0 16px;
  font-family: var(--jh-font-doc);
  font-size: clamp(22px, 5vw, 28px);
  color: var(--jh-ink);
  text-shadow: none;
  letter-spacing: 0.04em;
  font-weight: 600;
}
.content {
  white-space: pre-wrap;
  line-height: 1.85;
  color: var(--jh-ink);
  font-family: var(--jh-font-body);
  font-size: 15px;
}
</style>
