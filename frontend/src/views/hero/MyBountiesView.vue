<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { listMyClaimed, listMyPublished } from '@/api/bounty'
import type { BountyListItem } from '@/types/models'
import { bountyTypeLabel, formatAmount } from '@/utils/labels'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'

const router = useRouter()
const tab = ref('published')
const list = ref<BountyListItem[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data =
      tab.value === 'published'
        ? await listMyPublished({ page: 1, pageSize: 50 })
        : await listMyClaimed({ page: 1, pageSize: 50 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

function goRepublish(id: number, e: Event) {
  e.preventDefault()
  e.stopPropagation()
  router.push({ path: '/bounties/publish', query: { republishFrom: String(id) } })
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <JhPageHeader title="我的悬赏" subtitle="我发布的与我揭榜的" />
      <el-tabs v-model="tab" @tab-change="load">
        <el-tab-pane label="我发布的" name="published" />
        <el-tab-pane label="我揭榜的" name="claimed" />
      </el-tabs>
      <div v-loading="loading" class="list">
        <EmptyState v-if="!loading && !list.length" title="暂无记录" />
        <RouterLink
          v-for="item in list"
          :key="item.id"
          :to="`/bounties/${item.id}`"
          class="item jh-panel"
        >
          <div class="row">
            <strong>{{ item.title }}</strong>
            <StatusTag :status="item.status" />
          </div>
          <p class="jh-muted">
            {{ bountyTypeLabel[item.type] }} · {{ formatAmount(item.rewardAmount) }} 两
          </p>
          <div v-if="tab === 'published' && item.canRepublish" class="ops">
            <el-button size="small" type="primary" @click="goRepublish(item.id, $event)">
              再发一令
            </el-button>
          </div>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<style scoped>
h1 {
  margin: 0 0 12px;
  font-size: 32px;
}
.list {
  display: grid;
  gap: 10px;
  min-height: 120px;
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
.ops {
  margin-top: 10px;
}
</style>
