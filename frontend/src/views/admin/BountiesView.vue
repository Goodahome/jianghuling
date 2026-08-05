<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  adminForceCloseBounty,
  adminListBounties,
  adminReviewBounty,
  adminReviewSubmission,
} from '@/api/admin'

const list = ref<Record<string, unknown>[]>([])
const query = reactive({ page: 1, pageSize: 20, status: '' })

async function load() {
  const data = await adminListBounties(query)
  list.value = data.list || []
}

async function forceClose(id: number) {
  const { value } = await ElMessageBox.prompt('关闭原因', '强制下架')
  await adminForceCloseBounty(id, value)
  ElMessage.success('已关闭')
  await load()
}

async function reviewBounty(id: number, result: string) {
  let reason = ''
  if (result === 'REJECT') {
    const { value } = await ElMessageBox.prompt('驳回原因', '发令审核')
    reason = value
  }
  await adminReviewBounty(id, { result, reason })
  ElMessage.success('已处理')
  await load()
}

async function reviewSubmissionPrompt() {
  const { value } = await ElMessageBox.prompt('成果ID,APPROVE|REJECT,原因', '成果审核')
  const [id, result, reason] = value.split(',')
  await adminReviewSubmission(id.trim(), { result: result.trim(), reason: reason?.trim() })
  ElMessage.success('已处理')
}

onMounted(load)
</script>

<template>
  <div>
    <h2>悬赏与双审核</h2>
    <div style="margin-bottom: 12px; display: flex; gap: 8px">
      <el-input v-model="query.status" placeholder="状态" style="width: 160px" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button @click="reviewSubmissionPrompt">审核成果…</el-button>
    </div>
    <el-table :data="list">
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="status" label="状态" width="140" />
      <el-table-column prop="rewardAmount" label="赏银" width="100" />
      <el-table-column label="操作" width="280">
        <template #default="{ row }">
          <el-button size="small" type="success" @click="reviewBounty(Number(row.id), 'APPROVE')">通过</el-button>
          <el-button size="small" type="warning" @click="reviewBounty(Number(row.id), 'REJECT')">驳回</el-button>
          <el-button size="small" type="danger" @click="forceClose(Number(row.id))">强制关闭</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
