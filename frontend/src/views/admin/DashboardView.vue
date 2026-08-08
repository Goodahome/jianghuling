<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { getDashboard } from '@/api/admin'
import type { DashboardOverview } from '@/types/models'
import { formatAmount } from '@/utils/labels'

const router = useRouter()
const data = ref<DashboardOverview | null>(null)

onMounted(async () => {
  data.value = await getDashboard()
})

function go(item: { label: string }) {
  if (item.label === '待审发令') {
    router.push({ path: '/admin/bounties', query: { status: 'PENDING_REVIEW' } })
    return
  }
  if (item.label === '待审成果') {
    router.push({ path: '/admin/bounties' })
    return
  }
  if (item.label === '纠纷单') {
    router.push('/admin/disputes')
    return
  }
  if (item.label === '侠士数') {
    router.push('/admin/users')
  }
}
</script>

<template>
  <div>
    <h2>运营工作台</h2>
    <el-row :gutter="12" v-if="data">
      <el-col
        :xs="12"
        :sm="12"
        :md="8"
        v-for="item in [
          { label: '侠士数', value: data.userCount },
          { label: '待审发令', value: data.pendingBountyReviews },
          { label: '待审成果', value: data.pendingSubmissionReviews },
          { label: '纠纷单', value: data.disputeCount },
          { label: '今日揭榜', value: data.todayClaims },
          { label: '托管汇总(两)', value: formatAmount(data.frozenTotal) },
        ]"
        :key="item.label"
      >
        <el-card
          shadow="hover"
          class="stat-card"
          :class="{ clickable: ['待审发令', '待审成果', '纠纷单', '侠士数'].includes(item.label) }"
          @click="go(item)"
        >
          <div class="label">{{ item.label }}</div>
          <div class="value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stat-card {
  margin-bottom: 12px;
}
.stat-card.clickable {
  cursor: pointer;
}
.label {
  color: #909399;
  margin-bottom: 8px;
}
.value {
  font-size: 28px;
  font-weight: 700;
}
</style>
