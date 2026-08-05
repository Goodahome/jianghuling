<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { listNotices } from '@/api/notice'
import type { NoticeCategory } from '@/types/api'
import type { Notice } from '@/types/models'
import { noticeCategoryLabel } from '@/utils/labels'
import EmptyState from '@/components/EmptyState.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'

const category = ref<'' | NoticeCategory>('')
const list = ref<Notice[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await listNotices({
      page: 1,
      pageSize: 50,
      category: category.value || undefined,
    })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <JhPageHeader title="告示栏" subtitle="规则、防骗、遵义租房须知" />
      <el-radio-group v-model="category" class="tabs" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button v-for="(label, key) in noticeCategoryLabel" :key="key" :value="key">
          {{ label }}
        </el-radio-button>
      </el-radio-group>
      <div v-loading="loading" class="list">
        <EmptyState v-if="!loading && !list.length" title="暂无告示" />
        <RouterLink v-for="n in list" :key="n.id" :to="`/notices/${n.id}`" class="item jh-panel">
          <div class="row">
            <strong>{{ n.title }}</strong>
            <span class="jh-muted">{{ noticeCategoryLabel[n.category] }}</span>
          </div>
          <p class="jh-muted">{{ n.summary || n.createdAt }}</p>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
h1 {
  margin: 0 0 6px;
  font-size: 32px;
}
.tabs {
  margin: 14px 0;
}
.list {
  display: grid;
  gap: 10px;
}
.item {
  padding: 14px 16px;
}
.row {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}
</style>
