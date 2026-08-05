<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { adminFeeSummary, adminListLedgers } from '@/api/admin'
import { ledgerTypeLabel } from '@/utils/labels'

const list = ref<Record<string, unknown>[]>([])
const fee = ref<Record<string, unknown> | null>(null)

function typeText(type: unknown) {
  const key = String(type || '')
  return ledgerTypeLabel[key] || key
}

function userText(row: Record<string, unknown>) {
  const name = row.userName || row.nickname || row.username
  if (name) return String(name)
  return row.userId != null ? `用户#${row.userId}` : '—'
}

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
      :title="`服务费汇总：${fee.totalFee ?? 0} 两（${fee.count ?? 0} 笔）`"
      style="margin-bottom: 12px"
    />
    <el-table :data="list">
      <el-table-column prop="bizNo" label="业务号" min-width="160" />
      <el-table-column label="用户" min-width="120">
        <template #default="{ row }">
          {{ userText(row) }}
        </template>
      </el-table-column>
      <el-table-column label="类型" width="120">
        <template #default="{ row }">
          {{ typeText(row.type) }}
        </template>
      </el-table-column>
      <el-table-column prop="amount" label="金额" width="100" />
      <el-table-column prop="createdAt" label="时间" width="180" />
      <el-table-column prop="remark" label="备注" min-width="140" />
    </el-table>
  </div>
</template>
