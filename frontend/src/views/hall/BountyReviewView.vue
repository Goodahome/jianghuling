<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listBountyReviews, reviewBounty } from '@/api/hall'
import type { BountyListItem } from '@/types/models'
import { formatAmount } from '@/utils/labels'

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

async function decide(id: number, result: 'APPROVE' | 'REJECT') {
  let reason = ''
  if (result === 'REJECT') {
    const { value } = await ElMessageBox.prompt('请填写驳回原因', '驳回发令')
    reason = value
  }
  await reviewBounty(id, { result, reason })
  ElMessage.success(result === 'APPROVE' ? '已通过' : '已驳回')
  await load()
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <h2>令审队列</h2>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column label="赏银" width="120">
        <template #default="{ row }">{{ formatAmount(row.rewardAmount) }} 两</template>
      </el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="decide(row.id, 'APPROVE')">通过</el-button>
          <el-button type="danger" size="small" @click="decide(row.id, 'REJECT')">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
