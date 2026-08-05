<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { getDashboard } from '@/api/admin'
import type { DashboardOverview } from '@/types/models'
import { formatAmount } from '@/utils/labels'

const data = ref<DashboardOverview | null>(null)

onMounted(async () => {
  data.value = await getDashboard()
})
</script>

<template>
  <div>
    <h2>运营工作台</h2>
    <el-row :gutter="12" v-if="data">
      <el-col :xs="12" :sm="12" :md="8" v-for="item in [
        { label: '侠士数', value: data.userCount },
        { label: '待审发令', value: data.pendingBountyReviews },
        { label: '待审成果', value: data.pendingSubmissionReviews },
        { label: '纠纷单', value: data.disputeCount },
        { label: '今日揭榜', value: data.todayClaims },
        { label: '托管汇总(两)', value: formatAmount(data.frozenTotal) },
      ]" :key="item.label">
        <el-card shadow="never" style="margin-bottom: 12px">
          <div class="label">{{ item.label }}</div>
          <div class="value">{{ item.value }}</div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.label {
  color: #909399;
  margin-bottom: 8px;
}
.value {
  font-size: 28px;
  font-weight: 700;
}
</style>
