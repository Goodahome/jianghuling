<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { adminFeeSummary, adminListLedgers } from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])
const fee = ref<Record<string, unknown> | null>(null)

onMounted(async () => {
  const data = await adminListLedgers({ page: 1, pageSize: 50 })
  list.value = data.list || []
  fee.value = await adminFeeSummary()
})
</script>

<template>
  <div>
    <h2>钱庄流水</h2>
    <el-alert
      v-if="fee"
      type="info"
      :closable="false"
      :title="`服务费汇总：${JSON.stringify(fee)}`"
      style="margin-bottom: 12px"
    />
    <el-table :data="list">
      <el-table-column prop="bizNo" label="业务号" />
      <el-table-column prop="userId" label="用户" width="100" />
      <el-table-column prop="type" label="类型" width="140" />
      <el-table-column prop="amount" label="金额" width="100" />
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="remark" label="备注" />
    </el-table>
  </div>
</template>
