<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { getNotice } from '@/api/notice'
import type { Notice } from '@/types/models'
import { noticeCategoryLabel } from '@/utils/labels'

const route = useRoute()
const notice = ref<Notice | null>(null)

onMounted(async () => {
  notice.value = await getNotice(route.params.id as string)
})
</script>

<template>
  <section class="jh-section">
    <div class="jh-container narrow jh-panel article" v-if="notice">
      <p class="jh-muted">{{ noticeCategoryLabel[notice.category] }} · {{ notice.createdAt }}</p>
      <h1>{{ notice.title }}</h1>
      <div class="content">{{ notice.content || notice.summary }}</div>
    </div>
  </section>
</template>

<style scoped>
.narrow {
  max-width: 760px;
  padding: 24px;
}
h1 {
  margin: 8px 0 16px;
  font-family: var(--jh-font-display);
  font-size: 30px;
}
.content {
  white-space: pre-wrap;
  line-height: 1.8;
}
</style>
