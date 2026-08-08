<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listNotices } from '@/api/notice'
import type { NoticeCategory } from '@/types/api'
import type { Notice } from '@/types/models'
import { noticeCategoryLabel } from '@/utils/labels'
import EmptyState from '@/components/EmptyState.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'
import JhBoardPoster from '@/components/JhBoardPoster.vue'
import JhBoardPosterGrid from '@/components/JhBoardPosterGrid.vue'
import '@/components/JhBoardPosterMeta.css'

const categoryTab = ref('ALL')
const list = ref<Notice[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const cat = categoryTab.value === 'ALL' ? undefined : (categoryTab.value as NoticeCategory)
    const data = await listNotices({
      page: 1,
      pageSize: 50,
      category: cat,
    })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

function onCategoryTab() {
  void load()
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <JhPageHeader title="告示栏" />
      <el-tabs v-model="categoryTab" class="tabs" @tab-change="onCategoryTab">
        <el-tab-pane label="全部" name="ALL" />
        <el-tab-pane
          v-for="(label, key) in noticeCategoryLabel"
          :key="key"
          :label="label"
          :name="key"
        />
      </el-tabs>

      <JhBoardPosterGrid :loading="loading" :empty="!loading && !list.length">
        <template #empty>
          <EmptyState title="暂无告示" description="尚无张贴的告示。" />
        </template>
        <JhBoardPoster
          v-for="(n, index) in list"
          :key="n.id"
          :to="`/notices/${n.id}`"
          :title="n.title"
          :index="index"
          seal="告"
        >
          <template #top>
            <span class="jh-poster-type">{{ noticeCategoryLabel[n.category] }}</span>
            <span v-if="n.pinned" class="pin">置顶</span>
          </template>
          <p>{{ n.summary || '点开阅全文' }}</p>
          <template #bottom>
            <span class="jh-poster-deadline">{{ n.createdAt?.slice(0, 10) || '' }}</span>
          </template>
        </JhBoardPoster>
      </JhBoardPosterGrid>
    </div>
  </section>
</template>

<style scoped>
.tabs {
  margin-bottom: 14px;
}
.tabs :deep(.el-tabs__item) {
  color: rgba(247, 240, 221, 0.75);
}
.tabs :deep(.el-tabs__item.is-active) {
  color: var(--jh-gold-bright);
}
.tabs :deep(.el-tabs__active-bar) {
  background-color: var(--jh-gold);
}
.tabs :deep(.el-tabs__nav-wrap::after) {
  background-color: rgba(196, 163, 90, 0.25);
}
.pin {
  font-size: 12px;
  color: var(--jh-seal);
  border: 1px solid rgba(178, 58, 45, 0.45);
  padding: 0 6px;
  border-radius: 2px;
  line-height: 1.6;
}

@media (max-width: 768px) {
  .tabs :deep(.el-tabs__item) {
    padding: 0 10px;
    font-size: 13px;
  }
}
</style>
