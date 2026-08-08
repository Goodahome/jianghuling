<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBountyReviews, reviewBounty } from '@/api/hall'
import { useHallAttentionStore } from '@/stores/hallAttention'
import type { BountyListItem } from '@/types/models'
import { formatAmount, resolveBountyTypeLabel } from '@/utils/labels'
import StatusTag from '@/components/StatusTag.vue'
import EmptyState from '@/components/EmptyState.vue'
import JhPageHeader from '@/components/JhPageHeader.vue'

const router = useRouter()
const list = ref<BountyListItem[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await listBountyReviews({ status: 'PENDING', page: 1, pageSize: 50 })
    list.value = data.list || []
  } finally {
    loading.value = false
  }
}

function goDetail(id: number) {
  router.push(`/hall/bounty-reviews/${id}`)
}

async function decide(id: number, result: 'APPROVE' | 'REJECT', e: Event) {
  e.preventDefault()
  e.stopPropagation()
  let reason = ''
  if (result === 'REJECT') {
    const { value } = await ElMessageBox.prompt('请填写驳回原因', '驳回发令')
    reason = value
  }
  await reviewBounty(id, { result, reason })
  ElMessage.success(result === 'APPROVE' ? '已通过' : '已驳回')
  void useHallAttentionStore().refresh()
  await load()
}

onMounted(load)
</script>

<template>
  <section class="jh-section">
    <div class="jh-container">
      <JhPageHeader title="令审队列" />

      <div v-loading="loading" class="list">
        <EmptyState v-if="!loading && !list.length" title="暂无待审发令" />
        <button
          v-for="item in list"
          :key="item.id"
          type="button"
          class="item jh-panel"
          @click="goDetail(item.id)"
        >
          <div class="row">
            <strong>{{ item.title }}</strong>
            <StatusTag :status="item.status" />
          </div>
          <p class="jh-muted">
            {{ resolveBountyTypeLabel(item.type, item.typeDisplayName) || item.type }} · {{ formatAmount(item.rewardAmount) }} 两
          </p>
          <div class="ops">
            <el-button size="small" class="jh-btn-seal" @click="decide(item.id, 'APPROVE', $event)">
              通过
            </el-button>
            <el-button size="small" class="jh-btn-ink" @click="decide(item.id, 'REJECT', $event)">
              驳回
            </el-button>
          </div>
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
.ops {
  margin-top: 10px;
  display: flex;
  gap: 8px;
}
</style>
