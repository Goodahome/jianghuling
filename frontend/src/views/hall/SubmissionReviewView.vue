<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { listSubmissionReviews, reviewSubmission } from '@/api/hall'

const list = ref<Record<string, unknown>[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await listSubmissionReviews({ status: 'PENDING', page: 1, pageSize: 50 })
    list.value = (data.list || []) as unknown as Record<string, unknown>[]
  } finally {
    loading.value = false
  }
}

async function decide(id: number, result: 'APPROVE' | 'REJECT') {
  let reason = ''
  if (result === 'REJECT') {
    const { value } = await ElMessageBox.prompt('请填写驳回原因', '驳回成果')
    reason = value
  }
  await reviewSubmission(id, { result, reason })
  ElMessage.success('已处理')
  await load()
}

onMounted(load)
</script>

<template>
  <div v-loading="loading">
    <h2>验功队列</h2>
    <el-table :data="list">
      <el-table-column prop="id" label="成果ID" width="90" />
      <el-table-column prop="bountyTitle" label="悬赏" />
      <el-table-column prop="contentSummary" label="摘要" />
      <el-table-column prop="createdAt" label="提交时间" width="180" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button type="success" size="small" @click="decide(Number(row.id), 'APPROVE')">通过</el-button>
          <el-button type="danger" size="small" @click="decide(Number(row.id), 'REJECT')">驳回</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
